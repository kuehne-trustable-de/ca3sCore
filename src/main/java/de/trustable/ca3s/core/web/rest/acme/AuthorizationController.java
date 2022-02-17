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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.repository.AcmeAuthorizationRepository;
import de.trustable.ca3s.core.service.dto.acme.AuthorizationResponse;
import de.trustable.ca3s.core.service.dto.acme.ChallengeResponse;
import de.trustable.ca3s.core.service.dto.acme.IdentifierResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.DateUtil;


@Transactional
@Controller
@RequestMapping("/acme/{realm}/authorization")
public class AuthorizationController extends ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    @Value("${ca3s.acme.reject.get:true}")
    boolean rejectGet;

    @Autowired
    private AcmeAuthorizationRepository authorizationRepository;

   @Autowired
	private HttpServletRequest request;

    @RequestMapping(value = "/{authorizationId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAuthorization(@PathVariable final long authorizationId) {

        LOG.info("Received Authorization request 'get' ");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            LOG.debug("header {} : {} ",key, value);
        }

        final HttpHeaders additionalHeaders = buildNonceHeader();

        if( rejectGet ){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(additionalHeaders).build();
        }

        List<AcmeAuthorization> authList = authorizationRepository.findByAcmeAuthorizationId(authorizationId);
        if(authList.isEmpty()) {
            return ResponseEntity.notFound().headers(additionalHeaders).build();
        }else {

            AcmeAuthorization authDao = authList.get(0);

            // No authentication and check against an account!!!
            AuthorizationResponse authResp = buildAuthResponse(authDao);

            return ResponseEntity.ok().headers(additionalHeaders).body(authResp);
        }

    }

    @RequestMapping(value = "/{authorizationId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
  public ResponseEntity<?> postAuthorization(@RequestBody final String requestBody,
		  @PathVariable final long authorizationId, @PathVariable final String realm) {

	LOG.debug("Received Authorization request ");

	try {
		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

		ACMEAccount acctDao = checkJWTSignatureForAccount(context, realm);

	    final HttpHeaders additionalHeaders = buildNonceHeader();

		LOG.debug("Looking for Authorization id '{}'", authorizationId);
		List<AcmeAuthorization> authList = authorizationRepository.findByAcmeAuthorizationId(authorizationId);
		if(authList.isEmpty()) {
			LOG.debug("Authorization id '{}' unknown", authorizationId);
		    return ResponseEntity.notFound().headers(additionalHeaders).build();

		}else {

			AcmeAuthorization authDao = authList.get(0);

			LOG.debug("Authorization id '{}' found", authorizationId);

			if( authDao.getOrder().getAccount().getAccountId() != acctDao.getAccountId()) {
				LOG.warn("Account of signing key {} does not match account id {} associated to given auth{}", acctDao.getAccountId(), authDao.getOrder().getAccount().getAccountId(), authorizationId );
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "Account / Auth mismatch",
						BAD_REQUEST, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);

			}

			AuthorizationResponse authResp = buildAuthResponse(authDao);

		    return ResponseEntity.ok().headers(additionalHeaders).body(authResp);
		}

	} catch (AcmeProblemException e) {
	    return buildProblemResponseEntity(e);
	} catch (Exception e) {
		LOG.warn("unexpected problem", e);
		throw e;
	}

  }

private AuthorizationResponse buildAuthResponse(final AcmeAuthorization authDao) throws AcmeProblemException {

	AuthorizationResponse authResp = new AuthorizationResponse();

	AcmeOrder order = authDao.getOrder();
	authResp.setExpires(DateUtil.asDate( order.getExpires()));

	AcmeOrderStatus authStatus = AcmeOrderStatus.PENDING;
	for( AcmeChallenge challDao: authDao.getChallenges()) {
		if( challDao.getStatus() == ChallengeStatus.VALID) {
			authStatus = AcmeOrderStatus.VALID;
		}
	}
	authResp.setStatus(authStatus);

	Set<ChallengeResponse> challResp = new HashSet<ChallengeResponse>();
	for( AcmeChallenge challengeDao: authDao.getChallenges()) {
		ChallengeResponse challenge = new ChallengeResponse(challengeDao, locationUriOfChallenge(challengeDao.getId(), fromCurrentRequestUri()).toString());
		challResp.add(challenge );
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
