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
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.trustable.ca3s.core.domain.dto.NamedValues;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.*;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwt.consumer.JwtContext;
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
import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.dto.acme.FinalizeRequest;
import de.trustable.ca3s.core.service.dto.acme.OrderResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;


@Transactional
@Controller
@RequestMapping("/acme/{realm}/order")
public class OrderController extends ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private AcmeOrderRepository orderRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private CertificateUtil certUtil;

	@Autowired
	private CertificateProcessingUtil cpUtil;

    @Autowired
    private PipelineUtil pipelineUtil;

    @RequestMapping(value = "/{orderId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> postAsGetOrder(@RequestBody final String requestBody,
  		  @PathVariable final long orderId, @PathVariable final String realm) {

    	LOG.info("Received read order request for orderId {}", orderId);
      	try {
      		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

    		ACMEAccount acctDao = checkJWTSignatureForAccount(context, realm);

      	    final HttpHeaders additionalHeaders = buildNonceHeader();

      		List<AcmeOrder> orderList = orderRepository.findByOrderId(orderId);
      		if(orderList.isEmpty()) {
      			LOG.debug("reading attempt for non-existing orderId {}", orderId);
      		    return ResponseEntity.notFound().headers(additionalHeaders).build();
      		}else {
      			AcmeOrder orderDao = orderList.get(0);
      			if( !orderDao.getAccount().equals(acctDao) ) {
      	      		LOG.error("Account idenfied by key (accound {}) does not match account {} of requested order", acctDao, orderDao.getAccount());
      		        return ResponseEntity.badRequest().build();
      			}

      			UriComponentsBuilder baseUriBuilder = fromCurrentRequestUri().path("../../..");
                LOG.debug("postAsGetOrder: baseUriBuilder : " + baseUriBuilder.toUriString());

                return buildOrderResponse(additionalHeaders, orderDao, baseUriBuilder, true);
      		}
    	} catch (AcmeProblemException e) {
    	    return buildProblemResponseEntity(e);
      	}
    }

    @RequestMapping(value = "/finalize/{orderId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> finalizeOrder(@RequestBody final String requestBody,
  		  @PathVariable final long orderId, @PathVariable final String realm) {

  	LOG.info("Received finalize order request ");

	// check for existence of a pipeline for the realm
  	Pipeline pipeline = getPipelineForRealm(realm);

  	try {
  		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
  		FinalizeRequest finalizeReq = jwtUtil.getFinalizeReq(context.getJwtClaims());

		ACMEAccount acctDao = checkJWTSignatureForAccount(context, realm);

		/*
		 * parse the CSR included in the finalize request
		 */
  		String csrAsString = finalizeReq.getCsr();
  	  	LOG.debug("csr received: " + csrAsString);

  	  	byte[] csrByte = Base64Url.decode(csrAsString);
  	  	Pkcs10RequestHolder p10Holder = cryptoUtil.parseCertificateRequest(csrByte);

  	  	LOG.debug("csr decoded: " + p10Holder);

  	  	/*
  	  	 * retrieve all the requested SANs contained in the CSR
  	  	 */
  	  	Set<String> snSet = new HashSet<>();

  	  	// consider subject's CN as a possible source of names to verified
  	  	for( RDN rdn: p10Holder.getSubjectRDNs()) {

  	  	  	for( AttributeTypeAndValue atv: rdn.getTypesAndValues()) {

  	  	  		if( BCStyle.CN.equals(atv.getType())) {
  	  	  			String cnValue = atv.getValue().toString();
  	  	  			LOG.debug("cn found in CSR: " + cnValue);
  	  	  			snSet.add(cnValue);
  	  	  		}
  	  	  	}
  	  	}

  	  	// add all SANs as source of names to verified
  	    for( Attribute csrAttr : p10Holder.getReqAttributes()) {

            String attrOid = csrAttr.getAttrType().getId();
            String attrReadableName = OidNameMapper.lookupOid(attrOid);

            if( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(csrAttr.getAttrType()) ) {

            	LOG.debug("CSR contains extensionRequest");
                retrieveSANFromCSRAttribute(snSet, csrAttr);

            } else if( "certReqExtensions".equals(attrReadableName)){
            	LOG.debug("CSR contains attrReadableName");
                retrieveSANFromCSRAttribute(snSet, csrAttr);
            }else {
                String value = getASN1ValueAsString(csrAttr);
                LOG.debug("found attrReadableName '{}' with value '{}'", attrReadableName, value);
            }

  	    }


  	    /*
  	     * Prepare the response header, e.g. add a nonce
  	     */
  	    final HttpHeaders additionalHeaders = buildNonceHeader();

  	    /*
  	     * Order retrieval
  	     */
  		List<AcmeOrder> orderList = orderRepository.findByOrderId(orderId);
  		if(orderList.isEmpty()) {
  		    return ResponseEntity.notFound().headers(additionalHeaders).build();
  		}else {
  			AcmeOrder orderDao = orderList.get(0);

  			/*
  			 * does the order correlate to the Account selected by the JWT
  			 */
  			if( !orderDao.getAccount().equals(acctDao) ) {
  	      		LOG.error("Account idenfied by key (accound {}) does not match account {} of requested order", acctDao, orderDao.getAccount());
  		        return ResponseEntity.badRequest().build();
  			}

  			/*
  			 * check the order status:
  			 * only 'pending' and 'processing' status of not-yet-expired orders need to be considered
  			 */
  			if( (orderDao.getStatus() == AcmeOrderStatus.PENDING) || (orderDao.getStatus() == AcmeOrderStatus.PROCESSING )) {
  				if( orderDao.getExpires().isBefore(Instant.now()) ) {
					LOG.debug("pending order {} expired on {}", orderDao.getOrderId(), orderDao.getExpires().toString());
  			  	  	orderDao.setStatus(AcmeOrderStatus.INVALID);
				} else {

					boolean orderReady = true;

					for(String san: snSet) {
						boolean bSanFound = false;
						for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {
							if( san.equalsIgnoreCase(authDao.getValue())) {
								LOG.debug("san '{}' part of order {} in authorization {}", san, orderDao.getOrderId(), authDao.toString());
								bSanFound = true;
								break;
							}
						}
						if(!bSanFound) {
							LOG.info("failed to find requested hostname '{}' (from CSR) in authorization", san);
		  			  	  	orderReady = false;
		  			  	  	break;
						}
					}

					if (orderReady) {
						/*
						 * check all authorizations having at least one successfully validated challenge
						 */
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
								LOG.debug("no valid challange, authorization id {} and order {} fails ",
										authDao.getAcmeAuthorizationId(), orderDao.getOrderId());
								orderReady = false;
								break;
							}
						}
					}

					if (orderReady) {

                        List<String> messageList = new ArrayList<>();
                        if( !pipelineUtil.isPipelineRestrictionsResolved(pipeline, p10Holder, messageList)){

                            String detail = NO_DETAIL;
                            if( !messageList.isEmpty()){
                                detail = messageList.get(0);
                            }
                            final ProblemDetail problem = new ProblemDetail(ACMEUtil.BAD_CSR, "Restriction check failed.",
                                BAD_REQUEST, detail, NO_INSTANCE);
                            throw new AcmeProblemException(problem);
                        }

                        LOG.debug("order status {} changes to ready for order {}", orderDao.getStatus(), orderDao.getOrderId());
						orderDao.setStatus(AcmeOrderStatus.READY);

					  	LOG.debug("order {} status 'ready', producing certificate", orderDao.getOrderId());
				  	  	startCertificateCreationProcess(orderDao, pipeline, "ACME_ACCOUNT_" + acctDao.getAccountId(), CryptoUtil.pkcs10RequestToPem( p10Holder.getP10Req()));

						orderRepository.save(orderDao);
					}else {
	  			  	  	orderDao.setStatus(AcmeOrderStatus.INVALID);
					}


				}
			}else {
				LOG.debug("unexpected finalize call at order status {} for order {}", orderDao.getStatus(), orderDao.getOrderId());
			}

			// certificate creation only on status 'Ready' and no certificate created, yet
  	  		if((orderDao.getStatus() == AcmeOrderStatus.READY) && (orderDao.getCertificate() == null)){
			  	LOG.debug("order {} status 'ready', producing certificate", orderDao.getOrderId());
  			}

  			boolean valid = true;
  			UriComponentsBuilder baseUriBuilder = fromCurrentRequestUri().path("../../../..");
            LOG.debug("finalize: baseUriBuilder : " + baseUriBuilder.toUriString());

  			return buildOrderResponse(additionalHeaders, orderDao, baseUriBuilder, valid);
  		}

	} catch (AcmeProblemException e) {
	    return buildProblemResponseEntity(e);
	} catch (IOException | GeneralSecurityException e) {
        final ProblemDetail problem = new ProblemDetail(ACMEUtil.SERVER_INTERNAL, "Algorithm mismatch.",
                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
        return buildProblemResponseEntity(new AcmeProblemException(problem));
	}

  }

	private ResponseEntity<OrderResponse> buildOrderResponse(final HttpHeaders additionalHeaders, AcmeOrder orderDao,
			final UriComponentsBuilder baseUriBuilder,
			boolean valid) {


		Set<String> authorizationsResp = new HashSet<>();
		for( AcmeAuthorization authDao: orderDao.getAcmeAuthorizations()) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
            LOG.debug("uriBuilder: {}", uriBuilder.toUriString());
            UriComponentsBuilder uriBuilderOrder = uriBuilder.path(ORDER_RESOURCE_MAPPING);
            LOG.debug("uriBuilderOrder: {}", uriBuilderOrder.toUriString());

            String authUrl = locationUriOfAuth(authDao.getAcmeAuthorizationId(), uriBuilderOrder).toString();
            authorizationsResp.add(authUrl);
            LOG.debug("authUrl: {}", authUrl);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
//		String finalizeUrl = uriBuilderOrder.path("/finalize/").path(Long.toString(orderDao.getOrderId())).build().toUriString();
        String finalizeUrl = uriBuilder.path(ORDER_RESOURCE_MAPPING).path("/finalize/").path(Long.toString(orderDao.getOrderId())).build().toUriString();
        LOG.debug("order request finalize url: {}", finalizeUrl);

		String certificateUrl = null;
		if( orderDao.getCertificate() != null) {
			long certId = orderDao.getCertificate().getId();
            uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
			certificateUrl = uriBuilder.path(CERTIFICATE_RESOURCE_MAPPING).path("/").path(Long.toString(certId)).build().toUriString();
			LOG.debug("order request cert url: {}", certificateUrl);
		}
		OrderResponse orderResp = new OrderResponse(orderDao, authorizationsResp, finalizeUrl, certificateUrl);

		if( LOG.isDebugEnabled()) {
			LOG.debug("order response for verified request: {}", jwtUtil.getOrderResponseAsJSON(orderResp));
		}

		if( valid ) {
//  				URI authUri = locationUriOfAuthorization(challengeDao.getAuthorization().getAuthorizationId(), baseUriBuilder);
//  			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
		    return ok().headers(additionalHeaders).body(orderResp);
		}else {
		    return ok().headers(additionalHeaders).body(orderResp);
		}
	}


	private Certificate startCertificateCreationProcess(AcmeOrder orderDao, Pipeline pipeline, final String requestorName, final String csrAsPem)  {

	    List<String> messageList = new ArrayList<>();
        NamedValues[] nvArr = new NamedValues[0];
		CSR csr = cpUtil.buildCSR(csrAsPem, requestorName, AuditService.AUDIT_ACME_CERTIFICATE_REQUESTED, "", pipeline, nvArr, messageList );

		if( csr == null) {
			LOG.info("building CSR failed");
			String msg = "";
			if( !messageList.isEmpty()) {
				msg = messageList.get(0);
			}
			final ProblemDetail problem = new ProblemDetail(ACMEUtil.BAD_CSR, msg,
					BAD_REQUEST, "", ACMEController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}

		Certificate cert = cpUtil.processCertificateRequest(csr, requestorName, AuditService.AUDIT_ACME_CERTIFICATE_CREATED, pipeline );

		if( cert == null) {
			orderDao.setStatus(AcmeOrderStatus.INVALID);
			LOG.warn("creation of certificate by ACME order {} failed ", orderDao.getOrderId());
		}else {
			LOG.debug("updating order id {} with new certificate id {}", orderDao.getOrderId(), cert.getId());
			orderDao.setCertificate(cert);
			orderDao.setStatus(AcmeOrderStatus.VALID);

			LOG.debug("adding certificate attribute 'ACME_ACCOUNT_ID' {} for certificate id {}", orderDao.getAccount().getAccountId(), cert.getId());
			certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID, orderDao.getAccount().getAccountId());
			certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ORDER_ID, orderDao.getOrderId());
		}

		return cert;

	}


    private String getASN1ValueAsString(Attribute attr) {
        return  getASN1ValueAsString(attr.getAttrValues().toArray());
    }

    private String getASN1ValueAsString(ASN1Encodable[] asn1EncArr ) {
        String value = "";
        for (ASN1Encodable asn1Enc : asn1EncArr) {
            if (value.length() > 0) {
                value += ", ";
            }
            value += asn1Enc.toString();
        }
        return value;
    }

    private void retrieveSANFromCSRAttribute(Set<String> sanSet, Attribute attrExtension ){

  	  	Set<GeneralName> generalNameSet = new HashSet<>();

	    CSRUtil.retrieveSANFromCSRAttribute(generalNameSet, attrExtension );

        for (GeneralName gn : generalNameSet) {
        	sanSet.add(gn.getName().toString());
        }

    }

}
