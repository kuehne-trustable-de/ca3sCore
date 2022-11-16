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

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.acme.AccountResponse;
import de.trustable.ca3s.core.service.dto.acme.ChangeKeyRequest;
import de.trustable.ca3s.core.service.dto.acme.OrderSetResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.JwtUtil;


/*
 * 7.1.2.  Account Objects

   An ACME account resource represents a set of metadata associated with
   an account.  Account resources have the following structure:

   status (required, string):  The status of this account.  Possible
      values are: "valid", "deactivated", and "revoked".  The value
      "deactivated" should be used to indicate client-initiated
      deactivation whereas "revoked" should be used to indicate server-
      initiated deactivation.  (See Section 7.1.6)

   contact (optional, array of string):  An array of URLs that the
      server can use to contact the client for issues related to this
      account.  For example, the server may wish to notify the client
      about server-initiated revocation or certificate expiration.  For
      information on supported URL schemes, see Section 7.3

   termsOfServiceAgreed (optional, boolean):  Including this field in a
      new-account request, with a value of true, indicates the client's
      agreement with the terms of service.  This field is not updateable
      by the client.

   orders (required, string):  A URL from which a list of orders
      submitted by this account can be fetched via a POST-as-GET
      request, as described in Section 7.1.2.1.

   {
     "status": "valid",
     "contact": [
       "mailto:cert-admin@example.com",
       "mailto:admin@example.com"
     ],
     "termsOfServiceAgreed": true,
     "orders": "https://example.com/acme/acct/evOfKhNU60wg/orders"
   }

 */
@Transactional
@Controller
@RequestMapping("/acme/{realm}/acct")
public class AccountController extends AcmeController {

  private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);
  private static final int CURSOR_CHUNK = 10;

  @Autowired
  JwtUtil jwtUtil;

  public ResponseEntity<AccountResponse> getAccount(@PathVariable final long accountId) {
    LOG.info("Received GET request for '{}'", accountId);
    final HttpHeaders additionalHeaders = new HttpHeaders();

    additionalHeaders.set("Link", "<" + directoryResourceUriBuilderFrom(fromCurrentRequestUri().path("/..")).build()
            .normalize() + ">;rel=\"index\"");

    Optional<AcmeAccount> acct = acctRepository.findById(accountId);
    if( acct.isPresent()) {
        AccountResponse accResp = new AccountResponse(acct.get(), fromCurrentRequestUri());
        return ok().headers(additionalHeaders).body(accResp);
    }else {
    	throw new AccountDoesNotExistException(fromCurrentRequestUri().build().toUri());
    }
  }




	@RequestMapping(value = "/changeKey", method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
	public ResponseEntity<?> changeKey( @RequestBody final String requestBody, @PathVariable final String realm) {

		LOG.info("Received change key request for ");

		try {
			JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

			AcmeAccount acctByKidDao = checkJWTSignatureForAccount(context, realm);

			JwtClaims claims = context.getJwtClaims();
			/*
			LOG.debug( "change key payload : " + claims.toString());
			for( String claimName: claims.getClaimNames()) {
				try {
					LOG.debug( "claim {} : {}", claimName,  claims.getClaimValue(claimName, String.class));
				} catch (MalformedClaimException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/

			String compactInnerJWT;
			try {
				compactInnerJWT = claims.getClaimValue("protected", String.class) + "." +
						claims.getClaimValue("payload", String.class) + "." +
						claims.getClaimValue("signature", String.class);
				LOG.debug( "change key compactInnerJWT : " + compactInnerJWT);
			} catch (MalformedClaimException e) {
				final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "change key: error reading compactInnerJWT " + e.getMessage(),
						BAD_REQUEST, "", AcmeController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}

			JwtContext innerContext = jwtUtil.processCompactJWT(compactInnerJWT);

			JsonWebStructure innerWebStruct = jwtUtil.getJsonWebStructure(innerContext);

			String accountURL;
			try {
				accountURL = innerContext.getJwtClaims().getClaimValue("account", String.class);
			} catch (MalformedClaimException e) {
				final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "change key: error reading claim value for 'account' " + e.getMessage(),
						BAD_REQUEST, "", AcmeController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}

			ChangeKeyRequest changeKeyReq = jwtUtil.getChangeKeyRequest(innerContext.getJwtClaims());

			PublicKey newPK = jwtUtil.getPublicKey(innerWebStruct);

			JsonWebKey oldWebKey = changeKeyReq.getOldKey();
			if( !(oldWebKey instanceof PublicJsonWebKey)) {
			    String msg = "change Key request: old key is NOT a PublicJsonWebKey (but of class " + oldWebKey.getClass().getName();
			    LOG.warn(msg);
		        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "msg",
		                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
		    	throw new AcmeProblemException(problem);
			}
			PublicKey oldPK = ((PublicJsonWebKey)oldWebKey).getPublicKey();

			LOG.debug( "change key, new key : thumb {} : {}", jwtUtil.getJWKThumbPrint(newPK), newPK);
			LOG.debug( "change key, old key : thumb {} : {}", jwtUtil.getJWKThumbPrint(oldPK), oldPK );

			List<AcmeAccount> accListByOldPK = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(oldPK));
			if( accListByOldPK.isEmpty() ) {
			    LOG.warn("change Key request: old key does NOT identify given account");
		        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "old key does NOT identify given account",
		                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
		    	throw new AcmeProblemException(problem);
			}

			AcmeAccount accountDao = accListByOldPK.get(0);

			String[] urlParts = accountURL.split("/");
			long accountId = Long.parseLong(urlParts[urlParts.length -1]);

			if(!accountDao.getAccountId().equals(acctByKidDao.getAccountId()) ) {
			    LOG.warn("change Key request: account identified by old key {} does not match account identified by URL : {}", accountDao.getAccountId(), acctByKidDao.getAccountId());
		        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "old key does NOT identify kid-identified account",
		                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
		    	throw new AcmeProblemException(problem);
			}

			if(accountDao.getAccountId() != accountId ) {
			    LOG.warn("change Key request: account identified by old key {} does not match account isetified by URL : {}", accountDao.getAccountId(), accountId);
		        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "old key does NOT identify payload URL-defined account",
		                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
		    	throw new AcmeProblemException(problem);
			}

			jwtUtil.verifyJWT(innerContext, newPK);
		    LOG.debug("succesful verification of outer JWT");

			String pkAsString = Base64.encodeBase64String(newPK.getEncoded()).trim();
			accountDao.setPublicKey(pkAsString);

			String thumbPrint = jwtUtil.getJWKThumbPrint(newPK);
			accountDao.setPublicKeyHash(thumbPrint);

			acctRepository.save(accountDao);

			LOG.debug("account {} has thumbprint {}", accountDao.getAccountId(), thumbPrint);

		    final HttpHeaders additionalHeaders = buildNonceHeader();
		    return ResponseEntity.ok().headers(additionalHeaders).build();

		} catch (AcmeProblemException e) {
			return buildProblemResponseEntity(e);
		} catch (JoseException e) {
		    LOG.error("Problem verifying JWT", e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Internal crypto problem",
	                 HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), AcmeController.NO_INSTANCE);
			return buildProblemResponseEntity(new AcmeProblemException(problem));
		}

	}

	/*
  private ResponseEntity<?> updateAccount(final Account.Id accountId, final String requestBody, final AccountRequest
          accountRequest) {
    LOG.info("Updating ACCOUNT '{}': {}", accountId, requestBody);
    final AccountRequest.Payload payload = accountRequest.convert(requestBody).getPayload();
    final Optional<Account> optUpdatedAccount = accountDAO.updateWith(accountId, payload.getContacts());

    if (optUpdatedAccount.isPresent()) {
      return ok(optUpdatedAccount.get());
    }

    final HttpHeaders additionalHeaders = new HttpHeaders();
    additionalHeaders.setContentType(APPLICATION_PROBLEM_JSON);
    return status(NOT_FOUND).headers(additionalHeaders).body(new ProblemDetail(ACCOUNT_DOES_NOT_EXIST, "Account is " +
            "unknown", NOT_FOUND,
            "Account ID '" + accountId + "'", NO_INSTANCE));
  }
*/

/*
  7.1.2.1.  Orders List

  Each account object includes an "orders" URL from which a list of
  orders created by the account can be fetched via POST-as-GET request.
  The result of the request MUST be a JSON object whose "orders" field
  is an array of URLs, each identifying an order belonging to the
  account.  The server SHOULD include pending orders, and SHOULD NOT
  include orders that are invalid in the array of URLs.  The server MAY
  return an incomplete list, along with a Link header field with a
  "next" link relation indicating where further entries can be
  acquired.

HTTP/1.1 200 OK
Content-Type: application/json
Link: <https://example.com/acme/acct/evOfKhNU60wg/orders?cursor=2>;rel="next"

{
 "orders": [
   "https://example.com/acme/order/TOlocE8rfgo",
   "https://example.com/acme/order/4E16bbL5iSw",

   "https://example.com/acme/order/neBHYLfw0mg"
 ]
}
*/
  @RequestMapping(value = "/{accountId}/orders", method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
  public ResponseEntity<?> getAccountOrders (@PathVariable final long accountId, @PathVariable final String realm, @RequestParam(name="cursor", defaultValue = "0") String cursorParam,  @RequestBody final String requestBody) {

		LOG.info("Received getAccountOrders request for '{}', cursor '{}'", accountId, cursorParam);
		int cursor = Integer.parseInt(cursorParam);
		int maxCursor = CURSOR_CHUNK + cursor;

		try {
			JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

			AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm, accountId);

		    final HttpHeaders additionalHeaders = buildNonceHeader();

		    OrderSetResponse orderSetResp = new OrderSetResponse();

		    String orderUrl = accountResourceUriBuilderFrom(fromCurrentRequestUri().path("../..")).path("/").path(Long.toString(accountId)).path("/orders/").build().normalize().toUri().toString();

		    int nThisChunk = acctDao.getOrders().size();
		    if( nThisChunk > CURSOR_CHUNK) {
		    	nThisChunk = CURSOR_CHUNK;
		    }
			LOG.info("GetAccountOrders cursor '{}', maxCursor '{}', nThisChunk  '{}'", cursor, maxCursor, nThisChunk );
		    String[] orderUrlArr = new String[nThisChunk];
		    int i = 0;
		    int n = 0;
			for(AcmeOrder orderdDao: acctDao.getOrders()) {
				LOG.info("GetAccountOrders i '{}', n '{}'", i, n );
				if( i >= cursor) {
					orderUrlArr[n++] = orderUrl + orderdDao.getId();
				}
				i++;
				if( i >= maxCursor) {
				    String nextLink= "<" + fromCurrentRequestUri().queryParam("cursor", maxCursor).build().normalize() + ">;rel=\"next\"";
					LOG.info("Next Chunk Link '{}'", nextLink);
				    additionalHeaders.set("Link", nextLink);
					break;
				}
			}

            orderSetResp.setOrderUrls(orderUrlArr);
		    return ok().headers(additionalHeaders).body(orderSetResp);

		} catch (AcmeProblemException e) {
			return buildProblemResponseEntity(e);
		}

  }


    @RequestMapping(value = "/{accountId}", method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> updateAccount(@PathVariable final long accountId, @PathVariable final String realm, @RequestBody final String requestBody) {

        LOG.info("Received updateAccount request for '{}'", accountId);

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            AccountRequest updateAccountReq = jwtUtil.getAccountRequest(context.getJwtClaims());

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm, accountId);

            contactsFromRequest(acctDao, updateAccountReq);

            acctRepository.save(acctDao);

            AccountResponse accResp = new AccountResponse(acctDao, fromCurrentRequestUri());

            final HttpHeaders additionalHeaders = buildNonceHeader();
            return ok().headers(additionalHeaders).body(accResp);

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }

    }


}
