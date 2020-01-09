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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
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
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.Authorization;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.domain.enumeration.OrderStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.dto.acme.FinalizeRequest;
import de.trustable.ca3s.core.service.dto.acme.OrderResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.JwtUtil;
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
    private CSRRepository csrRepository;

	@Autowired
	private BPMNUtil bpmnUtil;
	
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private CertificateUtil certUtil;

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
      	      		LOG.error("Account idenfied by key (accound {}) does not match account of requested order", acctDao, orderDao.getAccount());
      		        return ResponseEntity.badRequest().build();
      			}
      			
      			UriComponentsBuilder baseUriBuilder = fromCurrentRequestUri().path("/../..");
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
  	      		LOG.error("Account idenfied by key (accound {}) does not match account of requested order", acctDao, orderDao.getAccount());
  		        return ResponseEntity.badRequest().build();
  			}

  			/*
  			 * check the order status:
  			 * only 'pending' and 'processing' status of not-yet-expired orders need to be considered
  			 */
  			if( (orderDao.getStatus() == OrderStatus.PENDING) || (orderDao.getStatus() == OrderStatus.PROCESSING )) {
  				if( orderDao.getExpires().isBefore(LocalDate.now()) ) {
					LOG.debug("pending order {} expired on ", orderDao.getOrderId(), orderDao.getExpires().toString());
  			  	  	orderDao.setStatus(OrderStatus.INVALID);
				} else {
					
					for(String san: snSet) {
						boolean bSanFound = false;
						for (Authorization authDao : orderDao.getAuthorizations()) {
							if( san.equalsIgnoreCase(authDao.getValue())) {
								LOG.debug("san '{}' part of order {} in authorization {}", san, orderDao.getOrderId(), authDao.toString());
								bSanFound = true;
								break;
							}
						}
						if(!bSanFound) {
							LOG.info("failed to find requested hostname '{}' (from CSR) in authorization", san);
						}
					}
					
					/*
					 * check all authorizations having at least one successfully validated challenge
					 */
					boolean orderReady = true;
					
					for (Authorization authDao : orderDao.getAuthorizations()) {

						boolean authReady = false;
						for (AcmeChallenge challDao : authDao.getChallenges()) {
							if (challDao.getStatus() == ChallengeStatus.VALID) {
								LOG.debug("challenge {} of type {} is valid ", challDao.getChallengeId(), challDao.getType());
								authReady = true;
								break;
							}
						}
						if (authReady) {
							LOG.debug("found valid challenge, authorization id {} is valid ", authDao.getAuthorizationId());
						} else {
							LOG.debug("no valid challange, authorization id {} and order {} fails ",
									authDao.getAuthorizationId(), orderDao.getOrderId());
							orderReady = false;
							break;
						}
					}

					if (orderReady) {
						LOG.debug("order status {} changes to ready for order {}", orderDao.getStatus(), orderDao.getOrderId());
						orderDao.setStatus(OrderStatus.READY);
						
					  	LOG.debug("order {} status 'ready', producing certificate", orderDao.getOrderId());
				  	  	startCertificateCreationProcess(orderDao, CryptoUtil.pkcs10RequestToPem( p10Holder.getP10Req()));

						orderRepository.save(orderDao);
					}


				}
			}else {
				LOG.debug("unexpected finalize call at order status {} for order {}", orderDao.getStatus(), orderDao.getOrderId());
			}

			// certificate creation only on status 'Ready' and no certificate created, yet
  	  		if((orderDao.getStatus() == OrderStatus.READY) && (orderDao.getCertificate() == null)){
			  	LOG.debug("order {} status 'ready', producing certificate", orderDao.getOrderId());

/*			  	
				CsrDao csrDao = new CsrDao( Base64.encodeToString(csrByte), p10Holder, "ACMEOrder-" + orderDao.getOrderId() );
				csrRepository.save(csrDao);
		  	  	LOG.debug("csr dao stored");
*/	  	  	  
//		  	  	orderDao.setCsrDao(csrDao);

//		  	  	startCertificateCreationProcess(orderDao, CryptoUtil.pkcs10RequestToPem( p10Holder.getP10Req()));
		  	  	
//				orderRepository.save(orderDao);
  			}
  			
  			boolean valid = true;
  			UriComponentsBuilder baseUriBuilder = fromCurrentRequestUri().path("../../../..");
  			
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

		
		Set<String> authorizationsResp = new HashSet<String>();
		for( Authorization authDao: orderDao.getAuthorizations()) {
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
			authorizationsResp.add(locationUriOfAuth(authDao.getAuthorizationId(), uriBuilder).toString());
		}
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUriBuilder.build().normalize().toUri());
		LOG.debug("uriBuilder: {}", uriBuilder.toUriString());
		
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
			LOG.debug("order response for valid {} request: {}", jwtUtil.getOrderResponseAsJSON(orderResp));
		}
		if( valid ) {
//  				URI authUri = locationUriOfAuthorization(challengeDao.getAuthorization().getAuthorizationId(), baseUriBuilder);
//  			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
		    return ok().headers(additionalHeaders).body(orderResp);
		}else {
		    return ok().headers(additionalHeaders).body(orderResp);
		}
	}

    
    /**
     * 
     * @param orderDao
     * @return
     * @throws IOException
     */
	private Certificate startCertificateCreationProcess(AcmeOrder orderDao, final String csrAsPem)  {
		
		orderDao.setStatus(OrderStatus.PROCESSING);
		
		// BPNM call
		try {
			Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrAsPem);

			CSR csr = certUtil.createCSR(csrAsPem, p10ReqHolder, "1");
			csrRepository.save(csr);

			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
			if(cert != null) {
				LOG.debug("updating order id {} with new certificate id {}", orderDao.getOrderId(), cert.getId());
				orderDao.setCertificate(cert);
				orderDao.setStatus(OrderStatus.READY);
				
				LOG.debug("adding certificate attribute 'ACME_ACCOUNT_ID' {} for certificate id {}", orderDao.getAccount().getAccountId(), cert.getId());
				certUtil.addCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID, orderDao.getAccount().getAccountId());
				certUtil.addCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ACME_ORDER_ID, orderDao.getOrderId());
				
				return cert;
				
			} else {
				orderDao.setStatus(OrderStatus.INVALID);
				LOG.warn("creation of certificate by ACME order {} failed ", orderDao.getOrderId());
			}

			// end of BPMN call

		} catch (GeneralSecurityException | IOException e) {
			LOG.warn("execution of CSRProcessingTask failed ", e);
		}

		return null;
	}

/*
	private Certificate createCertificate(CSR csr ,
			CAConnectorConfig caConfig ) throws GeneralSecurityException  {

		if (caConfig == null) {
			throw new GeneralSecurityException("CA connector not selected !");
		}

		if (CAConnectorType.Adcs.equals(caConfig.getCaConnectorType())) {
			LOG.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			
			Certificate  cert = adcsController.signCertificateRequest(csr, caConfig);
			return cert;

//		} else if (CAConnectorType.Cmp.equals(caConfig.getCaConnectorType())) {
//			LOG.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
//
//			X509Certificate x509Cert = cryptoUtil.convertPemToCertificate(certificateDao.getContent());
//
//			caCmpConnector.revokeCertificate(x509Cert, crlReason, caConfig.getSecret(), caConfig.getCaUrl(),
//					caConfig.getName());
		} else {
			throw new GeneralSecurityException("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}
*/
	
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

    void retrieveSANFromCSRAttribute(Set<String> sanSet, Attribute attrExtension ){
    	
        ASN1Set valueSet = attrExtension.getAttrValues();
        LOG.debug( "ExtensionRequest / AttrValues has " + valueSet.size() + " elements" );
        for (ASN1Encodable asn1Enc : valueSet) {
            DERSequence derSeq = (DERSequence)asn1Enc;

            LOG.debug( "ExtensionRequest / DERSequence has "+derSeq.size()+" elements" );

            for( ASN1Encodable asn1Enc2 : derSeq.toArray()) {

            	LOG.debug( "ExtensionRequest / asn1Enc2 is a " + asn1Enc2.getClass().getName());

                DERSequence derSeq2 = (DERSequence) asn1Enc2;
                LOG.debug( "ExtensionRequest / DERSequence2 has " + derSeq2.size() + " elements");
                LOG.debug( "ExtensionRequest / DERSequence2[0] is a " + derSeq2.getObjectAt(0).getClass().getName());

                ASN1Encodable asn1EncValue = derSeq2.getObjectAt(1);
                LOG.debug("ExtensionRequest / DERSequence2[1] (asn1EncValue)is a " + asn1EncValue.getClass().getName());


                ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier) (derSeq2.getObjectAt(0));
                String attrReadableName = OidNameMapper.lookupOid(objId.getId());


                if (Extension.subjectAlternativeName.equals(objId)) {
                    DEROctetString derStr = (DEROctetString) derSeq2.getObjectAt(1);
                    byte[] valBytes = derStr.getOctets();

                    GeneralNames names = GeneralNames.getInstance(valBytes);
                    LOG.debug("Attribute value SAN" + names);
                    LOG.debug("SAN values #" + names.getNames().length);

                    for (GeneralName gnSAN : names.getNames()) {
                    	LOG.debug( "GN " + gnSAN.getName().toString());
                    	sanSet.add(gnSAN.getName().toString());
                    }
                } else {
                    String stringValue = asn1EncValue.toString();

//                    Log.d(TAG, "asn1EncValue.toASN1Primitive " + asn1EncValue.toASN1Primitive().getClass().getName());
                    Method[] methods = asn1EncValue.getClass().getMethods();

                    for( Method m: methods){
//                        Log.d(TAG, "checking method " + m.getName());
                        try {

                            if( "getString".equals(m.getName())){
                                stringValue = (String)m.invoke(asn1EncValue);
                                break;
                            }else if( "getOctets".equals(m.getName())){
                                stringValue = new String((byte[])m.invoke(asn1EncValue));
                                break;
                            }else if( "getValue".equals(m.getName())){
                                stringValue = (String)m.invoke(asn1EncValue);
                                break;
                            }else if( "getId".equals(m.getName())){
                                stringValue = OidNameMapper.lookupOid((String)m.invoke(asn1EncValue));
                                break;
                            }else if( "getAdjustedDate".equals(m.getName())){
                                stringValue = (String)m.invoke(asn1EncValue);
                                break;
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                        	LOG.debug( "invoking " + m.getName(), e);
                        }
                    }
                    LOG.debug("found attrReadableName '{}' with value '{}'", attrReadableName, stringValue);

                }
            }
        }

    }

}
