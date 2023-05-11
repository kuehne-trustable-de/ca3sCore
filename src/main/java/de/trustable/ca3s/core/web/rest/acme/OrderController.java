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
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.acme.FinalizeRequest;
import de.trustable.ca3s.core.service.dto.acme.OrderResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.ca3s.core.web.rest.util.RateLimiter;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.jetbrains.annotations.NotNull;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;



@Transactional
@RestController
@RequestMapping("/acme/{realm}/order")
public class OrderController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    final private AcmeOrderRepository orderRepository;

    final private JwtUtil jwtUtil;

    final private CryptoUtil cryptoUtil;

    final private CertificateUtil certUtil;

    final private CertificateProcessingUtil cpUtil;

    final private PipelineUtil pipelineUtil;

    private final RateLimiter rateLimiter;

    final private AuditService auditService;

    final private boolean finalizeLocationBackwardCompat;

    final private boolean iterateAuthenticationsOnGet;

    public OrderController(AcmeOrderRepository orderRepository,
                           JwtUtil jwtUtil,
                           CryptoUtil cryptoUtil,
                           CertificateUtil certUtil,
                           CertificateProcessingUtil cpUtil,
                           PipelineUtil pipelineUtil, AuditService auditService,
                           @Value("${ca3s.acme.backward.finalize.location:true}") boolean finalizeLocationBackwardCompat,
                           @Value("${ca3s.acme.iterate.authentications:true}") boolean iterateAuthenticationsOnGet,
                           @Value("${ca3s.acme.ratelimit.second:0}") int rateSec,
                           @Value("${ca3s.acme.ratelimit.minute:20}") int rateMin,
                           @Value("${ca3s.acme.ratelimit.hour:0}") int rateHour) {
        this.orderRepository = orderRepository;
        this.jwtUtil = jwtUtil;
        this.cryptoUtil = cryptoUtil;
        this.certUtil = certUtil;
        this.cpUtil = cpUtil;
        this.pipelineUtil = pipelineUtil;
        this.auditService = auditService;
        this.finalizeLocationBackwardCompat = finalizeLocationBackwardCompat;

        this.iterateAuthenticationsOnGet = iterateAuthenticationsOnGet;

        this.rateLimiter = new RateLimiter("Order", rateSec, rateMin, rateHour);
    }


    @RequestMapping(value = "/{orderId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> postAsGetOrder(@RequestBody final String requestBody,
                                            @PathVariable final long orderId,
                                            @PathVariable final String realm,
                                            @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

        LOG.info("Received read order request for orderId {}", orderId);

        rateLimiter.checkRateLimit(orderId, realm);

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

            final HttpHeaders additionalHeaders = buildNonceHeader();

            List<AcmeOrder> orderList = orderRepository.findByOrderId(orderId);
            if (orderList.isEmpty()) {
                LOG.debug("reading attempt for non-existing orderId {}", orderId);
                return ResponseEntity.notFound().headers(additionalHeaders).build();
            } else {
                AcmeOrder orderDao = orderList.get(0);
                if (!orderDao.getAccount().equals(acctDao)) {
                    LOG.error("Account identified by key (account {}) does not match account {} of requested order", acctDao, orderDao.getAccount());
                    return ResponseEntity.badRequest().build();
                }

                if (iterateAuthenticationsOnGet) {
                    updateAcmeOrderState(orderDao);
                }

                UriComponentsBuilder baseUriBuilder = getEffectiveUriComponentsBuilder(realm, forwardedHost).path("/../..");
                LOG.debug("postAsGetOrder: baseUriBuilder : " + baseUriBuilder.toUriString());

                return buildOrderResponse(additionalHeaders, orderDao, baseUriBuilder, true);
            }
        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }
    }

    private void updateAcmeOrderState(AcmeOrder orderDao) {
        Instant now = Instant.now();
        if (now.isAfter(orderDao.getExpires())) {
            AcmeOrderStatus acmeOrderStatus = orderDao.getStatus();
            if (!AcmeOrderStatus.INVALID.equals(acmeOrderStatus)) {
                LOG.debug("pending order {} expired on {}, setting to state 'INVALID'", orderDao.getOrderId(), orderDao.getExpires().toString());
                auditService.saveAuditTrace(
                    auditService.createAuditTraceAcmeOrderExpired(orderDao.getAccount(), orderDao));
                orderDao.setStatus(AcmeOrderStatus.INVALID);
                orderRepository.save(orderDao);
                // @ToDo
//                auditService.saveAuditTrace( auditService. .createAuditTraceCAConfigCreated(cAConnectorConfig));
            }
        }
    }

    @RequestMapping(value = "/finalize/{orderId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> finalizeOrder(@RequestBody final String requestBody,
                                           @PathVariable final long orderId,
                                           @PathVariable final String realm,
                                           @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

        LOG.info("Received finalize order request ");

        rateLimiter.checkRateLimit(orderId, realm);

        // check for existence of a pipeline for the realm
        Pipeline pipeline = getPipelineForRealm(realm);

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
            FinalizeRequest finalizeReq = jwtUtil.getFinalizeReq(context.getJwtClaims());

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

            /*
             * Prepare the response header, e.g. add a nonce
             */
            final HttpHeaders additionalHeaders = buildNonceHeader();


            /*
             * Order retrieval
             */
            List<AcmeOrder> orderList = orderRepository.findByOrderId(orderId);
            if (orderList.isEmpty()) {
                return ResponseEntity.notFound().headers(additionalHeaders).build();
            } else {
                AcmeOrder orderDao = orderList.get(0);

                /*
                 * does the order correlate to the Account selected by the JWT
                 */
                if (!orderDao.getAccount().equals(acctDao)) {
                    LOG.error("Account identified by key (account {}) does not match account {} of requested order", acctDao, orderDao.getAccount());
                    return ResponseEntity.badRequest().build();
                }
                /**
                 * check validity
                 */
                updateAcmeOrderState(orderDao);

                /*
                 * check the order status:
                 * only 'ready' status of not-yet-expired orders need to be considered
                 */
                if (orderDao.getStatus() == AcmeOrderStatus.READY) {
                    /*
                     * parse the CSR included in the finalize request
                     */
                    String csrAsString = finalizeReq.getCsr();
                    LOG.debug("csr received: " + csrAsString);

                    byte[] csrByte = Base64Url.decode(csrAsString);
                    Pkcs10RequestHolder p10Holder = cryptoUtil.parseCertificateRequest(csrByte);

                    LOG.debug("csr decoded: " + p10Holder);

                    List<AcmeAccount> accListExisting = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(p10Holder.getPublicSigningKey()));
                    if (!accListExisting.isEmpty()) {
                        LOG.debug("public key in csr already used for account #" + accListExisting.get(0).getAccountId());

                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.BAD_CSR, "CSR rejected.",
                            BAD_REQUEST, "Public key of CSR already in use ", NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                    Set<String> snSet = collectAllSANS(p10Holder);

                    for (String san : snSet) {
                        boolean bSanFound = false;
                        for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {
                            if (san.equalsIgnoreCase(authDao.getValue())) {
                                LOG.debug("san '{}' part of order {} in authorization {}", san, orderDao.getOrderId(), authDao);
                                bSanFound = true;
                                break;
                            }
                        }
                        if (!bSanFound) {
                            String msg = "failed to find requested hostname '" + san + "' (from CSR) in authorization for order " + orderDao.getOrderId();
                            LOG.info(msg);
                            orderDao.setStatus(AcmeOrderStatus.INVALID);
                            orderRepository.save(orderDao);

                            throw new AcmeProblemException(new ProblemDetail(AcmeUtil.BAD_CSR, msg,
                                BAD_REQUEST, NO_DETAIL, NO_INSTANCE));
                        }
                    }

                /*
                if (orderValid) {
                    for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {

                        boolean authReady = false;
                        for (AcmeChallenge challDao : authDao.getChallenges()) {
                            if (challDao.getStatus() == ChallengeStatus.VALID) {
                                LOG.debug("challenge {} of type {} is valid ", challDao.getChallengeId(), challDao.getType());
                                authReady = true;
                                break;
                            }
                        }
                        if (authReady) {
                            LOG.debug("found valid challenge, authorization id {} is valid ", authDao.getAcmeAuthorizationId());
                        } else {
                            LOG.debug("no valid challenge, authorization id {} and order {} fails ",
                                    authDao.getAcmeAuthorizationId(), orderDao.getOrderId());
                            orderValid = false;
                            break;
                        }
                    }
                }
*/
                    List<String> messageList = new ArrayList<>();
                    if (!pipelineUtil.isPipelineRestrictionsResolved(pipeline, p10Holder, messageList)) {

                        String detail = NO_DETAIL;
                        if (!messageList.isEmpty()) {
                            detail = messageList.get(0);
                        }
                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.BAD_CSR, "Restriction check failed.",
                            BAD_REQUEST, detail, NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                    LOG.debug("order status {} changes to 'processing' for order {}", orderDao.getStatus(), orderDao.getOrderId());
                    orderDao.setStatus(AcmeOrderStatus.PROCESSING);
                    orderRepository.save(orderDao);


                    LOG.debug("order {} status 'valid', producing certificate", orderDao.getOrderId());
                    startCertificateCreationProcess(orderDao, pipeline, "ACME_ACCOUNT_" + acctDao.getAccountId(), CryptoUtil.pkcs10RequestToPem(p10Holder.getP10Req()));

                    LOG.debug("order status {} changes to valid for order {}", orderDao.getStatus(), orderDao.getOrderId());
                    orderDao.setStatus(AcmeOrderStatus.VALID);

                    orderRepository.save(orderDao);
                } else {
                    String msg = "unexpected finalize call at order status " + orderDao.getStatus() + " for order " + orderDao.getOrderId();
                    LOG.debug(msg);
                    throw new AcmeProblemException(new ProblemDetail(AcmeUtil.ORDER_NOT_READY, msg,
                        HttpStatus.FORBIDDEN, NO_DETAIL, NO_INSTANCE));
                }

                boolean valid = true;
                UriComponentsBuilder baseUriBuilder = getEffectiveUriComponentsBuilder(realm, forwardedHost).path("/../../..");
                LOG.debug("finalize: baseUriBuilder : " + baseUriBuilder.toUriString());

                return buildOrderResponse(additionalHeaders, orderDao, baseUriBuilder, valid);
            }

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        } catch (JoseException | IOException | GeneralSecurityException e) {
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.SERVER_INTERNAL, e.getMessage(),
                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
            return buildProblemResponseEntity(new AcmeProblemException(problem));
        }

    }

    @NotNull
    private Set<String> collectAllSANS(Pkcs10RequestHolder p10Holder) {
        /*
         * retrieve all the requested SANs contained in the CSR
         */
        Set<String> snSet = new HashSet<>();

        // consider subject's CN as a possible source of names to verified
        for (RDN rdn : p10Holder.getSubjectRDNs()) {

            for (AttributeTypeAndValue atv : rdn.getTypesAndValues()) {

                if (BCStyle.CN.equals(atv.getType())) {
                    String cnValue = atv.getValue().toString();
                    LOG.debug("cn found in CSR: " + cnValue);
                    snSet.add(cnValue);
                }
            }
        }

        // add all SANs as source of names to verified
        for (Attribute csrAttr : p10Holder.getReqAttributes()) {

            String attrOid = csrAttr.getAttrType().getId();
            String attrReadableName = OidNameMapper.lookupOid(attrOid);

            if (PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(csrAttr.getAttrType())) {

                LOG.debug("CSR contains extensionRequest");
                retrieveSANFromCSRAttribute(snSet, csrAttr);

            } else if ("certReqExtensions".equals(attrReadableName)) {
                LOG.debug("CSR contains attrReadableName");
                retrieveSANFromCSRAttribute(snSet, csrAttr);
            } else {
                String value = getASN1ValueAsString(csrAttr);
                LOG.debug("found attrReadableName '{}' with value '{}'", attrReadableName, value);
            }

        }
        return snSet;
    }

    private ResponseEntity<OrderResponse> buildOrderResponse(final HttpHeaders additionalHeaders,
                                                             final AcmeOrder orderDao,
                                                             final UriComponentsBuilder baseUriBuilder,
                                                             boolean valid) {


        Set<String> authorizationsResp = new HashSet<>();
        for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
            LOG.debug("uriBuilder: {}", uriBuilder.toUriString());
            UriComponentsBuilder uriBuilderOrder = uriBuilder.path(ORDER_RESOURCE_MAPPING);
            LOG.debug("uriBuilderOrder: {}", uriBuilderOrder.toUriString());

            String authUrl = locationUriOfAuth(authDao.getAcmeAuthorizationId(), uriBuilderOrder).toString();
            authorizationsResp.add(authUrl);
            LOG.debug("authUrl: {}", authUrl);
        }

        if (finalizeLocationBackwardCompat) {
            String orderLocation = baseUriBuilder.build().toUriString();
            additionalHeaders.add("location", orderLocation);
            LOG.debug("added location header '{}' for backward compatibility reasons.", orderLocation);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
//		String finalizeUrl = uriBuilderOrder.path("/finalize/").path(Long.toString(orderDao.getOrderId())).build().toUriString();
        String finalizeUrl = uriBuilder.path(ORDER_RESOURCE_MAPPING).path("/finalize/").path(Long.toString(orderDao.getOrderId())).build().toUriString();
        LOG.debug("order request finalize url: {}", finalizeUrl);


        String certificateUrl = null;
        if (orderDao.getCertificate() != null) {
            long certId = orderDao.getCertificate().getId();
            uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
            certificateUrl = uriBuilder.path(CERTIFICATE_RESOURCE_MAPPING).path("/").path(Long.toString(certId)).build().toUriString();
            LOG.debug("order request cert url: {}", certificateUrl);

        }
        OrderResponse orderResp = new OrderResponse(orderDao, authorizationsResp, finalizeUrl, certificateUrl);

        if (LOG.isDebugEnabled()) {
            LOG.debug("order response : {}", jwtUtil.getOrderResponseAsJSON(orderResp));
        }

        if (valid) {
//  				URI authUri = locationUriOfAuthorization(challengeDao.getAuthorization().getAuthorizationId(), baseUriBuilder);
//  			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
            return ok().headers(additionalHeaders).body(orderResp);
        } else {
            return ok().headers(additionalHeaders).body(orderResp);
        }
    }


    private Certificate startCertificateCreationProcess(AcmeOrder orderDao, Pipeline pipeline, final String requestorName, final String csrAsPem) {

        List<String> messageList = new ArrayList<>();
        NamedValues[] nvArr = new NamedValues[0];
        CSR csr = cpUtil.buildCSR(csrAsPem, requestorName, AuditService.AUDIT_ACME_CERTIFICATE_REQUESTED, "", pipeline, nvArr, messageList);

        if (csr == null) {
            LOG.info("building CSR failed");
            String msg = "";
            if (!messageList.isEmpty()) {
                msg = messageList.get(0);
            }
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.BAD_CSR, msg,
                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }

        orderDao.setCsr(csr);

        Certificate cert = cpUtil.processCertificateRequest(csr, requestorName, AuditService.AUDIT_ACME_CERTIFICATE_CREATED, pipeline);

        if (cert == null) {
            LOG.warn("creation of certificate by ACME order {} failed ", orderDao.getOrderId());
            auditService.saveAuditTrace(
                auditService.createAuditTraceAcmeOrderInvalid(orderDao.getAccount(), orderDao, "certificate creation failed"));
            orderDao.setStatus(AcmeOrderStatus.INVALID);
        } else {
            LOG.debug("updating order id {} with new certificate id {}", orderDao.getOrderId(), cert.getId());
            auditService.saveAuditTrace(
                auditService.createAuditTraceAcmeOrderSucceeded(orderDao.getAccount(), orderDao));
            orderDao.setCertificate(cert);
            orderDao.setStatus(AcmeOrderStatus.VALID);

            LOG.debug("adding certificate attribute 'ACME_ACCOUNT_ID' {} for certificate id {}", orderDao.getAccount().getAccountId(), cert.getId());
            certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID, orderDao.getAccount().getAccountId());
            certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ORDER_ID, orderDao.getOrderId());
        }

        return cert;

    }


    private String getASN1ValueAsString(Attribute attr) {
        return getASN1ValueAsString(attr.getAttrValues().toArray());
    }

    private String getASN1ValueAsString(ASN1Encodable[] asn1EncArr) {
        String value = "";
        for (ASN1Encodable asn1Enc : asn1EncArr) {
            if (value.length() > 0) {
                value += ", ";
            }
            value += asn1Enc.toString();
        }
        return value;
    }

    private void retrieveSANFromCSRAttribute(Set<String> sanSet, Attribute attrExtension) {

        Set<GeneralName> generalNameSet = new HashSet<>();

        CSRUtil.retrieveSANFromCSRAttribute(generalNameSet, attrExtension);

        for (GeneralName gn : generalNameSet) {
            sanSet.add(gn.getName().toString());
        }

    }

}
