/*^
  ===========================================================================
  ACME server
  ===========================================================================
  Copyright (C) 2017-2018 DENIC eG, 60329 Frankfurt am Main, Germany
  ===========================================================================
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  ===========================================================================
*/

package de.trustable.ca3s.core.web.rest.acme;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeAuthorizationRepository;
import de.trustable.ca3s.core.service.dto.acme.AuthorizationResponse;
import de.trustable.ca3s.core.service.dto.acme.ChallengeResponse;
import de.trustable.ca3s.core.service.dto.acme.IdentifierResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeOrderUtil;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.DateUtil;
import de.trustable.ca3s.core.service.util.RateLimiterService;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Transactional(dontRollbackOn = AcmeProblemException.class)
@RestController
@RequestMapping("/acme/{realm}/authorization")
public class AuthorizationController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);


    final private boolean rejectGet;
    final private boolean iterateChallengesOnGet;

    final private ChallengeController challengeController;

    final private AcmeAuthorizationRepository authorizationRepository;

    private final AcmeOrderUtil acmeOrderUtil;

    private final RateLimiterService rateLimiterService;

    final private HttpServletRequest request;

    public AuthorizationController(@Value("${ca3s.acme.reject.get:true}") boolean rejectGet,
                                   @Value("${ca3s.acme.iterate.challenges:true}") boolean iterateChallengesOnGet,
                                   ChallengeController challengeController,
                                   AcmeAuthorizationRepository authorizationRepository,
                                   AcmeOrderUtil acmeOrderUtil, HttpServletRequest request,
                                   @Value("${ca3s.acme.ratelimit.second:0}") int rateSec,
                                   @Value("${ca3s.acme.ratelimit.minute:20}") int rateMin,
                                   @Value("${ca3s.acme.ratelimit.hour:0}") int rateHour) {
        this.rejectGet = rejectGet;
        this.iterateChallengesOnGet = iterateChallengesOnGet;
        this.challengeController = challengeController;
        this.authorizationRepository = authorizationRepository;
        this.acmeOrderUtil = acmeOrderUtil;
        this.request = request;

        this.rateLimiterService = new RateLimiterService("Authorization", rateSec, rateMin, rateHour);
    }

    @RequestMapping(value = "/{authorizationId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAuthorization(@PathVariable final long authorizationId,
                                              @PathVariable final String realm,
                                              @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

        LOG.info("Received Authorization request 'get' ");

        checkACMERateLimit(rateLimiterService, authorizationId, realm);

        if( LOG.isDebugEnabled()) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = request.getHeader(key);
                LOG.debug("header {} : {} ", key, value);
            }
        }

        final HttpHeaders additionalHeaders = buildNonceHeader();

        if (rejectGet) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(additionalHeaders).build();
        }

        List<AcmeAuthorization> authList = authorizationRepository.findByAcmeAuthorizationId(authorizationId);
        if (authList.isEmpty()) {
            return ResponseEntity.notFound().headers(additionalHeaders).build();
        } else {

            AcmeAuthorization authDao = authList.get(0);

            // No authentication and check against an account!!!
            AuthorizationResponse authResp = buildAuthResponse(authDao, realm, forwardedHost);

            return ResponseEntity.ok().headers(additionalHeaders).body(authResp);
        }

    }

    @RequestMapping(value = "/{authorizationId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> postAuthorization(@RequestBody final String requestBody,
                                               @PathVariable final long authorizationId,
                                               @PathVariable final String realm,
                                               @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

        LOG.debug("Received Authorization request ");

        checkACMERateLimit(rateLimiterService, authorizationId, realm);

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

            final HttpHeaders additionalHeaders = buildNonceHeader();

            LOG.debug("Looking for Authorization id '{}'", authorizationId);
            List<AcmeAuthorization> authList = authorizationRepository.findByAcmeAuthorizationId(authorizationId);
            if (authList.isEmpty()) {
                LOG.debug("Authorization id '{}' unknown", authorizationId);
                return ResponseEntity.notFound().headers(additionalHeaders).build();

            } else {

                AcmeAuthorization authDao = authList.get(0);

                LOG.debug("Authorization id '{}' found", authorizationId);

                if (!Objects.equals(authDao.getOrder().getAccount().getAccountId(), acctDao.getAccountId())) {
                    LOG.warn("Account of signing key {} does not match account id {} associated to given auth{}", acctDao.getAccountId(), authDao.getOrder().getAccount().getAccountId(), authorizationId);
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Account / Auth mismatch",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);

                }
                acmeOrderUtil.alignOrderState(authDao.getOrder());

                AuthorizationResponse authResp = buildAuthResponse(authDao, realm, forwardedHost);

                return ResponseEntity.ok().headers(additionalHeaders).body(authResp);
            }

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        } catch (Exception e) {
            LOG.warn("unexpected problem", e);
            throw e;
        }

    }

    private AuthorizationResponse buildAuthResponse(final AcmeAuthorization authDao, final String realm, final String forwardedHost) throws AcmeProblemException {

        AuthorizationResponse authResp = new AuthorizationResponse();

        AcmeOrder order = authDao.getOrder();
        authResp.setExpires(DateUtil.asDate(order.getExpires()));

        AcmeOrderStatus authStatus = AcmeOrderStatus.PENDING;
        for (AcmeChallenge challDao : authDao.getChallenges()) {

            if (iterateChallengesOnGet) {
                challengeController.isChallengeSolved(challDao);
            }
            if (challDao.getStatus() == ChallengeStatus.VALID) {
                authStatus = AcmeOrderStatus.VALID;
            }
        }
        authResp.setStatus(authStatus);

        Set<ChallengeResponse> challResp = new HashSet<>();
        for (AcmeChallenge challengeDao : authDao.getChallenges()) {
            ChallengeResponse challenge = new ChallengeResponse(challengeDao, locationUriOfChallenge(challengeDao.getId(), getEffectiveUriComponentsBuilder(realm, forwardedHost)).toString());
            challResp.add(challenge);
        }
        authResp.setChallenges(challResp);

        IdentifierResponse identResp = new IdentifierResponse();
        identResp.setType(authDao.getType());
        identResp.setValue(authDao.getValue());

        authResp.setIdentifier(identResp);
        return authResp;
    }

    private URI locationUriOfChallenge(final long challengeId, final UriComponentsBuilder uriBuilder) {
        return challengeResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path(Long.toString(challengeId)).build().normalize().toUri();
    }

}
