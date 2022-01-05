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
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.ACMEAccountRepository;
import de.trustable.ca3s.core.repository.AcmeContactRepository;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.acme.AccountResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;

@Transactional
@Controller
@RequestMapping("/acme/{realm}/newAccount")
public class NewAccountController extends ACMEController {

  private static final Logger LOG = LoggerFactory.getLogger(NewAccountController.class);


  @Autowired
  private ACMEAccountRepository acctRepository;

  @Autowired
  AcmeContactRepository contactRepo;

  @RequestMapping(method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
  public ResponseEntity<?> consumingPostedJoseJson(@RequestBody final String requestBody, @PathVariable final String realm) {

    return consumeWithConverter(requestBody, realm);

  }


  @RequestMapping(method = POST, consumes = APPLICATION_JWS_VALUE)
  public ResponseEntity<?> consumingPostedJws(@RequestBody final String requestBody, @PathVariable final String realm) {
    return consumeWithConverter(requestBody, realm);
  }


  @Transactional
  ResponseEntity<?> consumeWithConverter(final String requestBody, final String realm) {

    LOG.info("New ACCOUNT requested for realm {} using requestbody \n {}", realm, requestBody);

	ACMEAccount acctDaoReturn;

    final HttpHeaders additionalHeaders = buildNonceHeader();
//    additionalHeaders.set("Link", "<" + directoryResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize() + ">;rel=\"index\"");

	try {
		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
	    AccountRequest newAcct = jwtUtil.getAccountRequest(context.getJwtClaims());
	    LOG.debug("New ACCOUNT reads NewAccountRequest: " + newAcct);

	    List<ACMEAccount> accListExisting;
		PublicKey pk;
		JsonWebStructure webStruct = jwtUtil.getJsonWebStructure(context);
		pk = jwtUtil.getPublicKey(webStruct);

		if( pk == null) {

			accListExisting = new ArrayList<>();
			accListExisting.add(checkJWTSignatureForAccount(context));
			if( accListExisting.isEmpty()) {
			    LOG.debug("NewAccountRequest does NOT provide key, no matching account found ");
		        final ProblemDetail problem = new ProblemDetail(ACMEUtil.ACCOUNT_DOES_NOT_EXIST, "Account does not exist.",
		                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}

		}else {
		    LOG.debug("JWK with public key found : " + pk);
			accListExisting = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(pk));

			jwtUtil.verifyJWT(context, pk);
		    LOG.debug("provided public key verifies given JWT: " + pk);
		}

		if(Boolean.TRUE.equals( newAcct.isOnlyReturnExisting())) {
			if( accListExisting.isEmpty()) {
		        final ProblemDetail problem = new ProblemDetail(ACMEUtil.ACCOUNT_DOES_NOT_EXIST, "Account does not exist.",
		                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}else {
				acctDaoReturn = accListExisting.get(0);
			}
		} else {

			if( accListExisting.isEmpty()) {
				/*
			    JwtClaims claims = context.getJwtClaims();
			    Map<String, Object> claimsMap = claims.getClaimsMap();
			    for( String claimName: claimsMap.keySet()) {
				    LOG.info("Claim '{}' of type {}", claimName, claimsMap.get(claimName).getClass().getName());
			    }
			    */
		//	    LOG.info("New ACCOUNT key: " + webStruct.getKey());
		//	    LOG.info("New ACCOUNT jwk: " + webStruct.getHeader("jwk"));
		//	    LOG.info("New ACCOUNT kid: " + webStruct.getKeyIdHeaderValue());

			    ACMEAccount newAcctDao = new ACMEAccount();
			    newAcctDao.setAccountId(generateId());
			    newAcctDao.setRealm(realm);

				String pkAsString = Base64.encodeBase64String(pk.getEncoded()).trim();
				newAcctDao.setPublicKey(pkAsString);

				String thumbPrint = jwtUtil.getJWKThumbPrint(pk);
				newAcctDao.setPublicKeyHash(thumbPrint);

				if( newAcct.isTermsAgreed() != null) {
					newAcctDao.setTermsOfServiceAgreed(newAcct.isTermsAgreed());
				}else {
					newAcctDao.setTermsOfServiceAgreed(false);
				}

			    acctRepository.save(newAcctDao);
			    contactsFromRequest(newAcctDao, newAcct);

			    newAcctDao.setStatus(AccountStatus.VALID);

			    acctRepository.save(newAcctDao);
			    LOG.debug("New Account {} created", newAcctDao.getAccountId());
			    acctDaoReturn = newAcctDao;
			}else {
				acctDaoReturn = accListExisting.get(0);
			}
		}


	    URI locationUri = locationUriOf(acctDaoReturn.getAccountId(), fromCurrentRequestUri());
	    String locationHeader = locationUri.toASCIIString();
	    LOG.debug("location header set to " + locationHeader);
	    additionalHeaders.set("Location", locationHeader);

        AccountResponse accResp = new AccountResponse(acctDaoReturn, fromCurrentRequestUri());
	    accResp.setOrders(locationUriOfOrders(acctDaoReturn.getAccountId(), fromCurrentRequestUri()).toString());
		if( accListExisting.isEmpty()) {
		    LOG.debug("returning new account response " + jwtUtil.getAccountResponseAsJSON(accResp));
		    LOG.debug("created for locationUri '{}' ", locationUri);
		    return ResponseEntity.created(locationUri).headers(additionalHeaders).body(accResp);
		}else {
		    LOG.debug("returning existing account response " + jwtUtil.getAccountResponseAsJSON(accResp));
		    return ok().headers(additionalHeaders).body(accResp);
		}


	} catch (JoseException e) {
        final ProblemDetail problem = new ProblemDetail(ACMEUtil.SERVER_INTERNAL, "Algorithm mismatch.",
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
