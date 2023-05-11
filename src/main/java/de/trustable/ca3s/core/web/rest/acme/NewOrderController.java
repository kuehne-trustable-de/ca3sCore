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

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.dto.AcmeConfigItems;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.acme.IdentifierResponse;
import de.trustable.ca3s.core.service.dto.acme.NewOrderRequest;
import de.trustable.ca3s.core.service.dto.acme.NewOrderResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.net.IDN;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.trustable.ca3s.core.service.util.PipelineUtil.ACME_ORDER_VALIDITY_SECONDS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/*
 * 7.4.  Applying for Certificate Issuance

 The client begins the certificate issuance process by sending a POST
 request to the server's new-order resource.  The body of the POST is
 a JWS object whose JSON payload is a subset of the order object
 defined in Section 7.1.3, containing the fields that describe the
 certificate to be issued:

 identifiers (required, array of object):  An array of identifier
    objects that the client wishes to submit an order for.

    type (required, string):  The type of identifier.

    value (required, string):  The identifier itself.

 notBefore (optional, string):  The requested value of the notBefore
    field in the certificate, in the date format defined in [RFC3339].

 notAfter (optional, string):  The requested value of the notAfter
    field in the certificate, in the date format defined in [RFC3339].

 POST /acme/new-order HTTP/1.1
 Host: example.com
 Content-Type: application/jose+json

 {
   "protected": base64url({
     "alg": "ES256",
     "kid": "https://example.com/acme/acct/evOfKhNU60wg",
     "nonce": "5XJ1L3lEkMG7tR6pA00clA",
     "url": "https://example.com/acme/new-order"
   }),
   "payload": base64url({
     "identifiers": [
       { "type": "dns", "value": "example.com" }
     ],
     "notBefore": "2016-01-01T00:04:00+04:00",
     "notAfter": "2016-01-08T00:04:00+04:00"
   }),
   "signature": "H6ZXtGjTZyUnPeKn...wEA4TklBdh3e454g"
 }

 The server MUST return an error if it cannot fulfill the request as
 specified, and MUST NOT issue a certificate with contents other than
 those requested.  If the server requires the request to be modified
 in a certain way, it should indicate the required changes using an
 appropriate error type and description.

If the server is willing to issue the requested certificate, it
 responds with a 201 (Created) response.  The body of this response is
 an order object reflecting the client's request and any
 authorizations the client must complete before the certificate will
 be issued.

 HTTP/1.1 201 Created
 Replay-Nonce: MYAuvOpaoIiywTezizk5vw
 Location: https://example.com/acme/order/TOlocE8rfgo

 {
   "status": "pending",
   "expires": "2016-01-01T00:00:00Z",

   "notBefore": "2016-01-01T00:00:00Z",
   "notAfter": "2016-01-08T00:00:00Z",

   "identifiers": [
     { "type": "dns", "value": "example.com" },
   ],

   "authorizations": [
     "https://example.com/acme/authz/PAniVnsZcis",
   ],

   "finalize": "https://example.com/acme/order/TOlocE8rfgo/finalize"
 }

 The order object returned by the server represents a promise that if
 the client fulfills the server's requirements before the "expires"
 time, then the server will be willing to finalize the order upon
 request and issue the requested certificate.  In the order object,
 any authorization referenced in the "authorizations" array whose
 status is "pending" represents an authorization transaction that the
 client must complete before the server will issue the certificate
 (see Section 7.5).  If the client fails to complete the required
 actions before the "expires" time, then the server SHOULD change the
 status of the order to "invalid" and MAY delete the order resource.
 Clients MUST NOT make any assumptions about the sort order of
 "identifiers" or "authorizations" elements in the returned order
 object.

 Once the client believes it has fulfilled the server's requirements,
 it should send a POST request to the order resource's finalize URL.
 The POST body MUST include a CSR:

 csr (required, string):  A CSR encoding the parameters for the
    certificate being requested [RFC2986].  The CSR is sent in the
    base64url-encoded version of the DER format.  (Note: Because this
    field uses base64url, and does not include headers, it is
    different from PEM.).

 POST /acme/order/TOlocE8rfgo/finalize HTTP/1.1
 Host: example.com
 Content-Type: application/jose+json

 {
   "protected": base64url({
     "alg": "ES256",
     "kid": "https://example.com/acme/acct/evOfKhNU60wg",
     "nonce": "MSF2j2nawWHPxxkE3ZJtKQ",
     "url": "https://example.com/acme/order/TOlocE8rfgo/finalize"
   }),
   "payload": base64url({
     "csr": "MIIBPTCBxAIBADBFMQ...FS6aKdZeGsysoCo4H9P",
   }),
   "signature": "uOrUfIIk5RyQ...nw62Ay1cl6AB"
 }

 The CSR encodes the client's requests with regard to the content of
 the certificate to be issued.  The CSR MUST indicate the exact same
 set of requested identifiers as the initial new-order request.
 Identifiers of type "dns" MUST appear either in the commonName
 portion of the requested subject name, or in an extensionRequest
 attribute [RFC2985] requesting a subjectAltName extension, or both.
 (These identifiers may appear in any sort order.)  Specifications
 that define new identifier types must specify where in the
 certificate signing request these identifiers can appear.

 A request to finalize an order will result in error if the CA is
 unwilling to issue a certificate corresponding to the submitted CSR.
 For example:

 o  If the order indicated does not have status "ready"

 o  If the CSR and order identifiers differ

 o  If the account is not authorized for the identifiers indicated in
    the CSR

 o  If the CSR requests extensions that the CA is not willing to
    include

 In such cases, the problem document returned by the server SHOULD use
 error code "badCSR", and describe specific reasons the CSR was
 rejected in its "details" field.  After returning such an error, the
 server SHOULD leave the order in the "ready" state, to allow the
 client to submit a new finalize request with an amended CSR.

 A request to finalize an order will return the order to be finalized.
 The client should begin polling the order by sending a POST-as-GET
 request to the order resource to obtain its current state.  The
 status of the order will indicate what action the client should take:

 o  "invalid": The certificate will not be issued.  Consider this
    order process abandoned.

 o  "pending": The server does not believe that the client has
    fulfilled the requirements.  Check the "authorizations" array for
    entries that are still pending.

 o  "ready": The server agrees that the requirements have been
    fulfilled, and is awaiting finalization.  Submit a finalization
    request.

 o  "processing": The certificate is being issued.  Send a POST-as-GET
    request after the time given in the "Retry-After" header field of
    the response, if any.

 o  "valid": The server has issued the certificate and provisioned its
    URL to the "certificate" field of the order.  Download the
    certificate.


 HTTP/1.1 200 OK
 Replay-Nonce: CGf81JWBsq8QyIgPCi9Q9X
 Location: https://example.com/acme/order/TOlocE8rfgo

 {
   "status": "valid",
   "expires": "2015-12-31T00:17:00.00-09:00",

   "notBefore": "2015-12-31T00:17:00.00-09:00",
   "notAfter": "2015-12-31T00:17:00.00-09:00",

   "identifiers": [
     { "type": "dns", "value": "example.com" },
     { "type": "dns", "value": "www.example.com" }
   ],

   "authorizations": [
     "https://example.com/acme/authz/PAniVnsZcis",
     "https://example.com/acme/authz/r4HqLzrSrpI"
   ],

   "finalize": "https://example.com/acme/order/TOlocE8rfgo/finalize",

   "certificate": "https://example.com/acme/cert/mAt3xBGaobw"
 }

 */

@Transactional
@RestController
@RequestMapping("/acme/{realm}/newOrder")
public class NewOrderController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(NewOrderController.class);

    private final AcmeOrderRepository orderRepository;

    private final AcmeOrderAttributeRepository orderAttributeRepository;

    private final AcmeAuthorizationRepository authorizationRepository;

    private final AcmeChallengeRepository challengeRepository;

    private final AcmeIdentifierRepository identRepository;

    private final PipelineUtil pipelineUtil;

    private final String resolverHost;

    private final int orderValiditySec;


    public NewOrderController(AcmeOrderRepository orderRepository,
                              AcmeOrderAttributeRepository orderAttributeRepository, AcmeAuthorizationRepository authorizationRepository,
                              AcmeChallengeRepository challengeRepository,
                              AcmeIdentifierRepository identRepository,
                              PipelineUtil pipelineUtil,
                              @Value("${ca3s.dns.server:}") String resolverHost,
                              @Value("${ca3s.acme.order.validity.seconds:600}") int orderValiditySec) {

        this.orderRepository = orderRepository;
        this.orderAttributeRepository = orderAttributeRepository;
        this.authorizationRepository = authorizationRepository;
        this.challengeRepository = challengeRepository;
        this.identRepository = identRepository;
        this.pipelineUtil = pipelineUtil;
        this.resolverHost = resolverHost;
        this.orderValiditySec = orderValiditySec;
    }


    @RequestMapping(method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> consumingPostedJoseJson(@RequestBody final String requestBody,
                                                     @PathVariable final String realm,
                                                     @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost,
                                                     @RequestHeader(value=HEADER_X_CA3S_PROXY_ID, required=false) String proxyId) {
        LOG.info("Received consumingPostedJoseJson request ");
        return consumeWithConverter(requestBody, realm, forwardedHost, proxyId);
    }


  @RequestMapping(method = POST, consumes = APPLICATION_JWS_VALUE)
  public ResponseEntity<?> consumingPostedJws(@RequestBody final String requestBody,
                                              @PathVariable final String realm,
                                              @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost,
                                              @RequestHeader(value=HEADER_X_CA3S_PROXY_ID, required=false) String proxyId) {
		LOG.info("Received consumingPostedJws request ");
    return consumeWithConverter(requestBody, realm, forwardedHost, proxyId);
  }

  public ResponseEntity<?> consumeWithConverter(@RequestBody final String requestBody, final String realm, final String forwardedHost, final String proxyIdString) {
	LOG.info("Received NewOrder request for realm {}", realm);

	try {

		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
        NewOrderRequest newOrderRequest = jwtUtil.getNewOrderRequest(context.getJwtClaims());
	    LOG.debug("New Order reads newOrderRequest: " + newOrderRequest);

        Pipeline pipeline = getPipelineForRealm(realm);
        LOG.debug("ACME pipeline '{}' found for request realm '{}'", pipeline.getName(), realm);

        AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

        RequestProxyConfig requestProxyConfig = null;
        if( proxyIdString != null){
            long proxyId = Long.parseLong(proxyIdString);
            Optional<RequestProxyConfig> requestProxyConfigOptional = pipeline.getRequestProxies().stream()
                .filter(p -> p.getId() == proxyId)
                .findFirst();

            if( requestProxyConfigOptional.isPresent()){
                requestProxyConfig = requestProxyConfigOptional.get();
                LOG.debug("new order requested by proxy '{}'", requestProxyConfig.getId());
            }else{
                LOG.info("Proxy Id '{}' provided, but not expected for pipeline '{}'!", proxyIdString, pipeline.getName());
                final ProblemDetail problemDetail = new ProblemDetail(AcmeUtil.MALFORMED, "Invalid Proxy ID",
                    BAD_REQUEST, "Invalid Proxy ID.", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problemDetail);
            }
        }else{
            LOG.debug("new order requested by proxy");
        }

        AcmeOrder orderDao = new AcmeOrder();
		orderDao.setOrderId(generateId());

		orderDao.setAccount(acctDao);
        orderDao.setRealm(realm);
        orderDao.setPipeline(pipeline);
		orderDao.setStatus(AcmeOrderStatus.PENDING);

		Instant now = Instant.now();

        int orderValiditySeconds = pipelineUtil.getPipelineAttribute(pipeline,
            ACME_ORDER_VALIDITY_SECONDS,
            orderValiditySec);

        orderDao.setExpires(now.plus(orderValiditySeconds, ChronoUnit.SECONDS));

        if( newOrderRequest.getNotBefore() != null ) {
            orderDao.setNotBefore(newOrderRequest.getNotBefore().toInstant());
        }
        if( newOrderRequest.getNotAfter() != null ) {
            orderDao.setNotAfter(newOrderRequest.getNotAfter().toInstant());
        }

		orderRepository.save(orderDao);

		Set<AcmeIdentifier> identifiers = new HashSet<>();
		for( IdentifierResponse ident: newOrderRequest.getIdentifiers()) {
			AcmeIdentifier identDao = new AcmeIdentifier();
			identDao.setAcmeIdentifierId(generateId());
			identDao.setOrder(orderDao);
			identDao.setType(ident.getType());
			identDao.setValue( ident.getValue());
			identifiers.add(identDao);
		}
		identRepository.saveAll(identifiers);
		orderDao.setAcmeIdentifiers(identifiers);

		orderRepository.save(orderDao);

		Set<AcmeAuthorization> authorizations = new HashSet<>();
		Set<String> authorizationsResp = new HashSet<>();

        PipelineView pipelineView = pipelineUtil.from(pipeline);
        AcmeConfigItems acmeConfigItems = pipelineView.getAcmeConfigItems();

        Set<AcmeOrderAttribute> acmeOrderAttributeSet = new HashSet<>();
        Set<String> challengeTypeSet = new HashSet<>();

        boolean hasWildcardRequest = false;
		for( AcmeIdentifier identDao: identifiers) {
			AcmeAuthorization authorizationDao = new AcmeAuthorization();
			authorizationDao.setAcmeAuthorizationId(generateId());
			authorizationDao.setOrder(orderDao);

            boolean isWildcardRequest = isWildcardRequest(identDao.getValue());
            hasWildcardRequest |= isWildcardRequest;
            if( isWildcardRequest && !acmeConfigItems.isAllowWildcards() ) {
                LOG.info("Wildcard requested, but no allowed for pipeline '{}'!", pipeline.getName());
                final ProblemDetail problemDetail = new ProblemDetail(AcmeUtil.MALFORMED, "Wildcard request not supported",
                    BAD_REQUEST, "Wildcard requested, but no allowed.", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problemDetail);
            }

			// set the type once it's validated
			authorizationDao.setType(identDao.getType());
			authorizationDao.setValue( identDao.getValue());
			authorizationRepository.save(authorizationDao);

			Set<AcmeChallenge> challenges = new HashSet<>();

            if( isWildcardRequest ){
                LOG.debug("Wildcard requested, HTTP-01 and ALPN disabled!");
            }else {
                if( acmeConfigItems.isAllowChallengeHTTP01() ) {
                    LOG.debug("Offering HTTP-01 challenge");
                    challenges.add(createChallenge(AcmeChallenge.CHALLENGE_TYPE_HTTP_01, identDao.getValue(), authorizationDao, requestProxyConfig));
                    challengeTypeSet.add(AcmeChallenge.CHALLENGE_TYPE_HTTP_01);
                }

                if( acmeConfigItems.isAllowChallengeAlpn() ) {
                    LOG.debug("Offering ALPN-01 challenge");
                    challenges.add(createChallenge(AcmeChallenge.CHALLENGE_TYPE_ALPN_01, identDao.getValue(), authorizationDao, requestProxyConfig));
                    challengeTypeSet.add(AcmeChallenge.CHALLENGE_TYPE_ALPN_01);
                }

            }

            if( resolverHost != null && !resolverHost.isEmpty()) {
                if( acmeConfigItems.isAllowChallengeDNS() ) {
                    LOG.debug("Offering DNS-01 challenge");
                    challenges.add(createChallenge(AcmeChallenge.CHALLENGE_TYPE_DNS_01, identDao.getValue(), authorizationDao, requestProxyConfig));
                    challengeTypeSet.add(AcmeChallenge.CHALLENGE_TYPE_DNS_01);
                }
            }else{
                LOG.debug("DNS-01 challenge skipped, no dns resolver configured.");
                if( isWildcardRequest ) {
                    LOG.info("Wildcard requested, but no dns resolver configured!");
                    final ProblemDetail problemDetail = new ProblemDetail(AcmeUtil.MALFORMED, "DNS auth not supported",
                        BAD_REQUEST, "DNS-01 challenge skipped, no dns resolver configured.", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problemDetail);
                }
            }

            if( challenges.isEmpty()){
                LOG.info("No challenge available / supported for the given configuration of pipeline '{}'", pipeline.getName());
                final ProblemDetail problemDetail = new ProblemDetail(AcmeUtil.MALFORMED, "No challenge available",
                    BAD_REQUEST, "No challenge available / supported for the given configuration.", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problemDetail);
            }

            authorizationDao.setChallenges(challenges);
			authorizationRepository.save(authorizationDao);

			authorizations.add(authorizationDao);

            addOrderAttribute(orderDao, AcmeOrderAttribute.AUTHORIZATION, identDao.getValue(), acmeOrderAttributeSet);

            authorizationsResp.add(locationUriOfAuth(authorizationDao.getAcmeAuthorizationId(), getEffectiveUriComponentsBuilder(realm, forwardedHost)).toString());
		}

        addOrderAttribute(orderDao, AcmeOrderAttribute.WILDCARD_REQUEST, String.valueOf(hasWildcardRequest), acmeOrderAttributeSet);

        for( String type: challengeTypeSet){
            addOrderAttribute(orderDao, AcmeOrderAttribute.CHALLENGE_TYPE, type, acmeOrderAttributeSet);
        }

        if( requestProxyConfig != null){
            addOrderAttribute(orderDao, AcmeOrderAttribute.REQUEST_PROXY_ID_USED, requestProxyConfig.getId().toString(), acmeOrderAttributeSet);
        }

        orderDao.setAttributes(acmeOrderAttributeSet);
        orderAttributeRepository.saveAll(acmeOrderAttributeSet);

		orderDao.setAcmeAuthorizations(authorizations);
		orderRepository.save(orderDao);

        String finalizeUrl = locationUriOfOrderFinalize(orderDao.getOrderId(), getEffectiveUriComponentsBuilder(realm, forwardedHost)).toString();
		NewOrderResponse newOrderResp = new NewOrderResponse(orderDao, authorizationsResp, finalizeUrl);

//		newOrderResp.setStatus(orderDao.getStatus());
//		newOrderResp.setExpires(orderDao.getExpires());
//
//		Set<Identifier> identifiersResp = new HashSet<Identifier>();
//		for( Identifier ident: newIdentifiers.getIdentifiers()) {
//			identifiersResp.add(ident);
//		}
//		newOrderResp.setIdentifiers(identifiersResp );
//
//		newOrderResp.setAuthorizations(authorizationsResp);
//		newOrderResp.setFinalize("http://finalize.foo.com");

		URI locationUri = locationUriOfOrder(orderDao.getOrderId(), getEffectiveUriComponentsBuilder(realm, forwardedHost));

		final HttpHeaders additionalHeaders = buildNonceHeader();
		additionalHeaders.set("Link", "<" + directoryResourceUriBuilderFrom(getEffectiveUriComponentsBuilder(realm, forwardedHost)).build().normalize() + ">;rel=\"index\"");

	    LOG.debug("returning new order response " + jwtUtil.getOrderResponseAsJSON(newOrderResp));

	    return ResponseEntity.created(locationUri).headers(additionalHeaders).body(newOrderResp);

	} catch (AcmeProblemException e) {
	    return buildProblemResponseEntity(e);
	}
}

    private void addOrderAttribute(AcmeOrder orderDao, String challengeType, String type, Set<AcmeOrderAttribute> acmeOrderAttributeSet) {
        AcmeOrderAttribute acmeOrderAttribute  = new AcmeOrderAttribute();
        acmeOrderAttribute.setOrder(orderDao);
        acmeOrderAttribute.setName(challengeType);
        acmeOrderAttribute.setValue(type);
        acmeOrderAttributeSet.add(acmeOrderAttribute);
    }



    private boolean isWildcardRequest(String ident) {
        boolean isWildcardRequest;
        Name name;
        try {
            Name tempName = Name.fromString(ident, Name.root);
            name = Name.fromString(IDN.toASCII(tempName.toString(), IDN.USE_STD3_ASCII_RULES));
        } catch (TextParseException e) {
            throw new IllegalArgumentException("DNS identifier value '" + ident + "'", e);
        }
        isWildcardRequest = name.isWild();
        return isWildcardRequest;
    }

    private AcmeChallenge createChallenge(String type, String value, AcmeAuthorization authorizationDao, final RequestProxyConfig requestProxyConfig) {
        AcmeChallenge challengeDao = new AcmeChallenge();
        challengeDao.setChallengeId(generateId());
        challengeDao.setAcmeAuthorization(authorizationDao);

        challengeDao.setType( type );
        challengeDao.setValue(value);
        challengeDao.setToken( getRandomChallenge());
        challengeDao.setStatus(ChallengeStatus.PENDING);
        if( requestProxyConfig != null) {
            challengeDao.setRequestProxy(requestProxyConfig);
        }
        challengeRepository.save(challengeDao);

        return challengeDao;
    }

/*
  @RequestMapping(method = POST, consumes = APPLICATION_JWS_VALUE)
  public ResponseEntity<Authorization> consumingPostedJws(@RequestBody final String requestBody) {
		LOG.info("Received consumingPostedJws request ");
    return consumeWithConverter(requestBody, compactJwsNewAuthorizationRequest);
  }
*/

/*
  private ResponseEntity<Authorization> consumeWithConverter(final String requestBody, final NewAuthorizationRequest
          newAuthorizationRequest) {

    LOG.info("New AUTHORIZATION requested");
    final Context<Payload> context = newAuthorizationRequest.convert(requestBody);

    final PublicKey publicKey = accountDAO.getPublicKeyWith(context.mustHave(JOSEkid.INSTANCE)).orElseThrow
            (() -> new RuntimeException("Missing required key ID"));

    final Builder<Dns01Challenge> challengeBuilder = Dns01Challenge.builder(pending).randomToken();
    final UriComponentsBuilder authorizationBaseUriBuilder = newOrderResourceUriBuilderFrom(fromCurrentRequestUri());
    final Payload payload = context.getPayload();
    final Authorization authorization = createAndStoreAuthorizationApplying(payload.getIdentifier(), publicKey,
            challengeBuilder, authorizationBaseUriBuilder);
    return ResponseEntity.created(authorization.getUri()).body(authorization);
  }

  private Authorization createAndStoreAuthorizationApplying(final Identifier identifier, final PublicKey publicKey,
                                                            final Builder challengeBuilder, final UriComponentsBuilder authorizationBaseUriBuilder) {
    for (int i = 0; i < MAX_TRIES_TO_GENERATE_AUTHORIZATION; i++) {
      try {
        final Authorization authorization = new Authorization(identifier, Status.pending,
                authorizationBaseUriBuilder, challengeBuilder);
        authorizationDAO.insert(authorization, publicKey);
        return authorization;
      } catch (AlreadyStoredException e) {
        // Ignore, next try
      }
    }

    throw new RuntimeException("Internal error");
  }
*/

}
