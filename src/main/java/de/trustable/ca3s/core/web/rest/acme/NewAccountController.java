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


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.acme.AccountResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.dto.bpmn.AcmeAccountAuthorizationInput;
import de.trustable.ca3s.core.service.util.*;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Transactional(dontRollbackOn = AcmeProblemException.class)
@RestController
@RequestMapping("/acme/{realm}/newAccount")
public class NewAccountController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(NewAccountController.class);

    final private AcmeAccountRepository acctRepository;
    final private BPMNUtil bpmnUtil;
    final private UserUtil userUtil;
    final private PipelineUtil pipelineUtil;
    final private AlgorithmRestrictionUtil algorithmRestrictionUtil;
    final private JWSService jwsService;
    final boolean checkKeyRestrictions;
    final String eabKidPrefix;

    public NewAccountController(AcmeAccountRepository acctRepository,
                                BPMNUtil bpmnUtil,
                                UserUtil userUtil,
                                PipelineUtil pipelineUtil,
                                AlgorithmRestrictionUtil algorithmRestrictionUtil,
                                JWSService jwsService,
                                @Value("${ca3s.acme.account.checkKeyRestrictions:false}") boolean checkKeyRestrictions,
                                @Value("${ca3s.acme.account.eabKidPrefix:ca3s}") String eabKidPrefix) {
        this.acctRepository = acctRepository;
        this.bpmnUtil = bpmnUtil;
        this.userUtil = userUtil;
        this.pipelineUtil = pipelineUtil;
        this.algorithmRestrictionUtil = algorithmRestrictionUtil;
        this.jwsService = jwsService;
        this.checkKeyRestrictions = checkKeyRestrictions;
        this.eabKidPrefix = eabKidPrefix;
    }

    @Transactional
    @RequestMapping(method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> consumingPostedJoseJson(@RequestBody final String requestBody, @PathVariable final String realm,
                                                     @RequestHeader(value = HEADER_X_CA3S_FORWARDED_HOST, required = false) String forwardedHost) {

        return consumeWithConverter(requestBody, realm, forwardedHost);

    }


    @Transactional
    @RequestMapping(method = POST, consumes = APPLICATION_JWS_VALUE)
    public ResponseEntity<?> consumingPostedJws(@RequestBody final String requestBody, @PathVariable final String realm,
                                                @RequestHeader(value = HEADER_X_CA3S_FORWARDED_HOST, required = false) String forwardedHost) {
        return consumeWithConverter(requestBody, realm, forwardedHost);
    }


    ResponseEntity<?> consumeWithConverter(final String requestBody, final String realm, final String forwardedHost) {

        LOG.info("New ACCOUNT requested for realm {} using requestbody \n {}", realm, requestBody);

        AcmeAccount acctDaoReturn;

        final HttpHeaders additionalHeaders = buildNonceHeader();
//    additionalHeaders.set("Link", "<" + directoryResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize() + ">;rel=\"index\"");

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
            AccountRequest newAcct = jwtUtil.getAccountRequest(context.getJwtClaims());
            LOG.debug("New ACCOUNT reads NewAccountRequest: " + newAcct);

            List<AcmeAccount> accListExisting;
            PublicKey pk;
            JsonWebStructure webStruct = jwtUtil.getJsonWebStructure(context);
            pk = jwtUtil.getPublicKey(webStruct);

            if (pk == null) {
                accListExisting = new ArrayList<>();
                accListExisting.add(checkJWTSignatureForAccount(context, realm));
            } else {
                jwtUtil.verifyJWT(context, pk);
                LOG.debug("provided public key verifies given JWT: " + pk);

                List<String> messageList = new ArrayList<>();
                if (checkKeyRestrictions &&
                    (!algorithmRestrictionUtil.isAlgorithmRestrictionsResolved(pk, messageList))) {
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED,
                        "Public key of new account not accepted.",
                        BAD_REQUEST,
                        messageList.isEmpty() ? NO_DETAIL : messageList.get(0),
                        NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                LOG.debug("JWK with public key found {}, checking with database", pk);
                accListExisting = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(pk));
            }

            if (Boolean.TRUE.equals(newAcct.isOnlyReturnExisting())) {
                if (accListExisting.isEmpty()) {
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.ACCOUNT_DOES_NOT_EXIST, "Account does not exist.",
                        BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                } else {
                    acctDaoReturn = accListExisting.get(0);
                }
            } else {

                if (accListExisting.isEmpty()) {

                    boolean eabPresent = false;
                    boolean eabvalidated = true;
                    boolean eabJwsNeedsValidation = false;
                    JWSObject jwsObject = null;

                    Pipeline pipeline = getPipelineForRealm(realm);

                    if( !pipelineUtil.checkAcceptNetwork(pipeline) ){
                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED,
                            "Request not from expected IP range",
                            BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                    /*
                    JwtClaims claims = context.getJwtClaims();
                    Map<String, Object> claimsMap = claims.getClaimsMap();
                    for( String claimName: claimsMap.keySet()) {
                        LOG.info("Claim '{}' of type {}", claimName, claimsMap.get(claimName).getClass().getName());
                    }
                    */

                    AcmeAccount newAcctDao = new AcmeAccount();

                    //	    LOG.info("New ACCOUNT key: " + webStruct.getKey());
                    //	    LOG.info("New ACCOUNT jwk: " + webStruct.getHeader("jwk"));
                    //	    LOG.info("New ACCOUNT kid: " + webStruct.getKeyIdHeaderValue());
                    LOG.debug( "eab: {}", newAcct.getExternalAccountBinding());
                    Map<String, String> partMap = (Map<String, String>) newAcct.getExternalAccountBinding();
                    try {
                        if(partMap != null) {
                            eabPresent = true;
                            eabvalidated = false;
                            jwsObject = jwsService.getJWSObject(partMap);
                            String kid = jwsObject.getHeader().getKeyID();
                            String ca3sPrefix = eabKidPrefix + ":";
                            if( ca3sPrefix.equals(kid.substring(0, ca3sPrefix.length())) ){
                                User eabUser = jwsService.verifyEABGetUser(jwsObject,userUtil.getLoginFromCa3sKeyId(kid));
                                newAcctDao.setEabUser(eabUser);
                                AcmeContact contact = buildAcmeContact(newAcctDao, "mailto:" + eabUser.getEmail()) ;
                                newAcctDao.getContacts().add(contact);
                                eabvalidated = true;
                            }else{
                                eabJwsNeedsValidation =true;
                            }
                            newAcctDao.setEabKid( kid);
                        }else {
                            if( pipelineUtil.getPipelineAttribute(pipeline,
                                PipelineUtil.ACME_EAB_REQUIRED,
                                false)){
                                final ProblemDetail problem = new ProblemDetail(AcmeUtil.EXTERNAL_ACCOUNT_REQUIRED,
                                    "External account binding attributes missing.",
                                    BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
                                throw new AcmeProblemException(problem);
                            }
                        }
                    } catch (ParseException | JOSEException e) {
                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.EXTERNAL_ACCOUNT_REQUIRED,
                            "External account binding attributes not parseable: " + e.getMessage(),
                            BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                    newAcctDao.setAccountId(generateId());
                    newAcctDao.setRealm(realm);
                    newAcctDao.setCreatedOn(Instant.now());

                    String pkAsString = Base64.encodeBase64String(pk.getEncoded()).trim();
                    newAcctDao.setPublicKey(pkAsString);

                    String thumbPrint = jwtUtil.getJWKThumbPrint(pk);
                    newAcctDao.setPublicKeyHash(thumbPrint);

                    if (Boolean.TRUE.equals(newAcct.isTermsAgreed())) {
                        newAcctDao.setTermsOfServiceAgreed(newAcct.isTermsAgreed());
                    } else {
                        newAcctDao.setTermsOfServiceAgreed(false);
                        if( Boolean.TRUE.equals(pipelineUtil.getPipelineAttribute(pipeline, PipelineUtil.TOS_AGREEMENT_REQUIRED, false))) {

                            URI tosUri = null;
                            try {
                                tosUri = new URI(pipelineUtil.getPipelineAttribute(pipeline, PipelineUtil.TOS_AGREEMENT_LINK, ""));
                            }catch( URISyntaxException uriSyntaxException){
                                LOG.warn("ToS agreement link is not a valid URI", uriSyntaxException);
                            }

                            final ProblemDetail problem = new ProblemDetail(AcmeUtil.USER_ACTION_REQUIRED, "Agreement to terms of service required",
                                BAD_REQUEST, NO_DETAIL,
                                tosUri);
                            throw new AcmeProblemException(problem);
                        }
                    }

                    if (pipeline.getProcessInfoAccountAuthorization() != null) {
                        AcmeAccountAuthorizationInput acmeAccountValidationInput = new AcmeAccountAuthorizationInput(newAcct,jwsObject, eabPresent, eabJwsNeedsValidation);
                        bpmnUtil.startACMEAccountAuthorizationProcess(pipeline,acmeAccountValidationInput);
                    }

                    if(!eabvalidated){
                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.EXTERNAL_ACCOUNT_REQUIRED,
                            "External account data did not validate successfully",
                            BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                    updateAccountFromRequest(newAcctDao, newAcct, pipeline);
                    newAcctDao.setStatus(AccountStatus.VALID);
                    acctRepository.save(newAcctDao);
                    LOG.debug("New Account {} created", newAcctDao.getAccountId());
                    acctDaoReturn = newAcctDao;
                } else {
                    acctDaoReturn = accListExisting.get(0);
                }
            }

            URI locationUri = locationUriOf(acctDaoReturn.getAccountId(), getEffectiveUriComponentsBuilder(realm, forwardedHost));
            String locationHeader = locationUri.toASCIIString();
            LOG.debug("location header set to " + locationHeader);
            additionalHeaders.set("Location", locationHeader);

            AccountResponse accResp = new AccountResponse(acctDaoReturn, getEffectiveUriComponentsBuilder(realm, forwardedHost));
            accResp.setOrders(locationUriOfOrders(acctDaoReturn.getAccountId(), getEffectiveUriComponentsBuilder(realm, forwardedHost)).toString());
            if (accListExisting.isEmpty()) {
                LOG.debug("returning new account response " + jwtUtil.getAccountResponseAsJSON(accResp));
                LOG.debug("created for locationUri '{}' ", locationUri);
                return ResponseEntity.created(locationUri).headers(additionalHeaders).body(accResp);
            } else {
                LOG.debug("returning existing account response " + jwtUtil.getAccountResponseAsJSON(accResp));
                return ok().headers(additionalHeaders).body(accResp);
            }


        } catch (GeneralSecurityException e) {
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "Account creation problem.",
                BAD_REQUEST, e.getMessage(), NO_INSTANCE);
            return buildProblemResponseEntity(new AcmeProblemException(problem));
        } catch (JoseException e) {
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.SERVER_INTERNAL, "Algorithm mismatch.",
                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
            return buildProblemResponseEntity(new AcmeProblemException(problem));
        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }

    }


    private URI locationUriOf(final long accountId, final UriComponentsBuilder uriBuilder) {
        return accountResourceUriBuilderFrom(uriBuilder.path("..")).path("/").path(Long.toString(accountId)).build().normalize().toUri();
    }

    private URI locationUriOfOrders(final long accountId, final UriComponentsBuilder uriBuilder) {
        return accountResourceUriBuilderFrom(uriBuilder.path("..")).path("/").path(Long.toString(accountId)).path("/orders").build().normalize().toUri();
    }

}
