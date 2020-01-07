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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.CRLReason;
import org.cryptacular.util.CertUtil;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;

import de.trustable.ca3s.core.service.dto.acme.RevokeRequest;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;

@Controller
@RequestMapping("/acme/{realm}/cert")
public class ACMECertificateController extends ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(ACMECertificateController.class);

    private boolean chainIncludeRoot = true;
    
  	@Autowired
  	private CertificateRepository certificateRepository;
  	
	@Autowired
	private CAConnectorConfigRepository caccRepo;

	@Autowired
	private ADCSConnector adcsController;


  	@Autowired
  	private CertificateUtil certUtil;

//    @Autowired
//    ProcessHandler processHandler;
    
    @RequestMapping(value = "/{certId}", method = GET)
    public ResponseEntity<?> getCertificatePKIX(@PathVariable final long certId, 
    		@RequestHeader(name="Accept",  defaultValue=APPLICATION_PEM_CERT_CHAIN_VALUE)  final String accept) {

		LOG.info("Received certificate request for id {}", certId);
		
    	return buildCertResponseForId(certId, accept);  			
    }

	public ResponseEntity<?> buildCertResponseForId(final long certId, final String accept)
			throws HttpClientErrorException, AcmeProblemException {
		Optional<Certificate> certOpt = certificateRepository.findById(certId);
    	
  		if(!certOpt.isPresent()) {
  		  throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
  		}else {
  			Certificate certDao = certOpt.get();

			final HttpHeaders headers = buildNonceHeader();

			ResponseEntity<?> resp = buildCertifcateResponse(accept, certDao, headers);
  			
			if( resp == null) {
				String msg = "problem returning certificate with accepting type " + accept;
				LOG.info(msg);
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, msg,
						UNSUPPORTED_MEDIA_TYPE, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}
			
			return resp;
  		}
	}

	public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao) {
		return buildCertifcateResponse(accept, certDao, new HttpHeaders());
	}
	
	/**
	 * @param accept
	 * @param certDao
	 * @param headers
	 */
	public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao, final HttpHeaders headers) {
		
		if("*/*".equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers);
		}else if("application/pkix-cert".equalsIgnoreCase(accept)){
			return buildPkixCertResponse(certDao, headers);
		}else if("application/pem-certificate-chain".equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers);
		}else if("application/pem-certificate".equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, false);
		}
		
		LOG.info("unexpected accept type {}", accept);

		return null;
	}

	@RequestMapping(value = "/revoke", method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
	public ResponseEntity<?> revokeCertificate(@RequestBody final String requestBody ) {

		LOG.info("Received revoke request for certificate ");

		try {
			JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

			RevokeRequest revokeReq = jwtUtil.getRevokeReq(context.getJwtClaims());
			
			ACMEAccount acctDao = checkJWTSignatureForAccount(context);

			String certB64 = revokeReq.getCertificate();
			X509Certificate x509Cert = CertUtil.decodeCertificate(Base64.decodeBase64(certB64));

			LOG.info("Revoke request for certificate {} ", x509Cert.getSubjectDN().toString() );

			String tbsDigestBase64;
			try {
				tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
			} catch (CertificateEncodingException | NoSuchAlgorithmException e) {
				LOG.info("problem selecting certificate for RevokeRequest", e);
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "problem selecting certificate for RevokeRequest",
						BAD_REQUEST, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}
			
			List<Certificate> certList = certificateRepository.findByTBSDigest(tbsDigestBase64);

			if (certList.isEmpty()) {
				LOG.warn("Certificate {} to be revoked not found in database", x509Cert.getSubjectDN().toString() );
	  		    return ResponseEntity.notFound().build();
			} else {
				Certificate certDao = certList.get(0);

				final HttpHeaders headers = buildNonceHeader();

				// check whether this certificate belongs to the account signing the request
				if( acctDao.getAccountId() == Long.parseLong(certUtil.getCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID))) {

					revokeCertificate(certDao, Integer.toString(revokeReq.getReason()));
					
		  	        return ResponseEntity.ok().headers(headers).build();
				}else {
					LOG.warn("Revoke request for certificate {} identified by account {} does not match cert's associated account {}", 
							x509Cert.getSubjectDN().toString(), 
							acctDao.getAccountId(),
							certUtil.getCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID));
		  	        return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(headers).build();
				}
			}

		} catch (AcmeProblemException e) {
		    return buildProblemResponseEntity(e);
		} catch (Exception e) {
			LOG.info("problem revoking certificate ", e);
			final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "problem revoking certificate ",
					BAD_REQUEST, "", ACMEController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}

	}

    /**
     * Retrieve a certificate as a PEM structure containing the complete chain
     * Bug in certbot: content type set to 'application/pkix-cert' despite containing a JWT in the request body, as usual.
     * 
     * 
     */
	@RequestMapping(value = "/{certId}", method = POST, consumes = {APPLICATION_JOSE_JSON_VALUE, APPLICATION_PKIX_CERT_VALUE})
	public ResponseEntity<?>  retrieveCertificate(@RequestBody final String requestBody,
			@RequestHeader(name="Accept",  defaultValue=APPLICATION_PEM_CERT_CHAIN_VALUE) final String accept, 
			@RequestHeader("Content-Type") final String contentType, 
			@PathVariable final long certId) {

		try {
			JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

			ACMEAccount acctDao = checkJWTSignatureForAccount(context);
			// check order for certificate matches account identified by JWT protecting key
			// ... or not, as certs a public ...

			LOG.info("Received certificate request for certifacte id {} of content-type {}, identified by account id {} ", certId, contentType, acctDao.getAccountId());

	    	return buildCertResponseForId(certId, accept);  			
/*
			List<Certificate> certList = certificateRepository.findByCertificateId(certId);

			if (certList.isEmpty()) {
	  		    return ResponseEntity.notFound().build();
			} else {
				Certificate certDao = certList.get(0);
				
				final HttpHeaders headers = buildNonceHeader();

				if ("application/pkix-cert".equalsIgnoreCase(accept) ) { 
					LOG.debug("application/pkix-cert requested");
					return buildPkixCertResponse(certDao, headers);
				}else {
					LOG.debug("default handling for accept {}: returning PEM", accept);
		  			return buildPEMResponse(certDao, headers);
				}
			}
*/
	    	
		} catch (AcmeProblemException e) {
		    return buildProblemResponseEntity(e);
		}

	}

	private ResponseEntity<byte[]> buildPkixCertResponse(Certificate certDao, final HttpHeaders headers) {
		LOG.info("building PKIX certificate response");

		byte[] certBytes = Base64.decodeBase64(certDao.getContent());
		
//		headers.setContentType(APPLICATION_PKIX_CERT);
//		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(certBytes, headers, HttpStatus.OK);
		
		return ResponseEntity.ok().contentType(APPLICATION_PKIX_CERT).headers(headers).body(certBytes);
	}

	private ResponseEntity<?> buildPEMResponse(Certificate certDao, final HttpHeaders headers) {
		return buildPEMResponse(certDao, headers, true);
	}
	
	private ResponseEntity<?> buildPEMResponse(Certificate certDao, final HttpHeaders headers, boolean includeChain) {
		LOG.info("building PEM certificate response");
		
		try {
			String resultPem = "";
			if( includeChain) {
				List<Certificate> chain = certUtil.getCertificateChain(certDao);
	
				for( Iterator<Certificate> it = chain.iterator(); it.hasNext(); ) {
					Certificate chainCertDao = it.next();
					// skip the last cert, the root
					if( it.hasNext() || chainIncludeRoot) {
						resultPem += chainCertDao.getContent();
					}
				}
			} else {
				resultPem += certDao.getContent();
			}
			
			LOG.debug("returning cert and issuer : \n" + resultPem );
			return ResponseEntity.ok().contentType(APPLICATION_PEM_CERT_CHAIN).headers(headers).body(resultPem.getBytes());
			
		} catch (GeneralSecurityException ge) {
			String msg = "problem building certificate chain";
			LOG.info(msg, ge);
			final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, msg,
					INTERNAL_SERVER_ERROR, msg, ACMEController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}
			
	}

	
    /**
     * 
     * @param orderDao
     * @return
     * @throws Exception 
     * @throws IOException
     */
/*	
	private void startCertificateCreationProcess(Certificate certDao, final String reason)  {
		
		
		ProcessDefinitionData pdd = processHandler.getProcessDefinitionDataByKey("ImmediateCertificateRevocation");
		StartFormDataVO sfdVO = processHandler.getFormData(pdd.getId());
		for (FormPropertyVO formPropertyVO : sfdVO.getListFormProperty()) {
			
			if ("certificateId".equalsIgnoreCase(formPropertyVO.getName())) {
				formPropertyVO.setValue(Long.toString(certDao.getCertificateId()));
			}
		}
		
		ProcessInstance pi = processHandler.startProcessInstance(sfdVO);

		java.util.Map<String, Object> procVars = pi.getProcessVariables();

		String status = (String) procVars.get("status");
		if ("Revoked".equals(status)) {

			LOG.debug("certificate id {} revoked by ACME call", certDao.getCertificateId());
			
		} else {
			LOG.warn("revocation of certificate id {} by ACME call failed with status {}", certDao.getCertificateId(), status);
		}
	}
*/
	
	private void revokeCertificate(Certificate certDao, final String reason) throws Exception {
		

		if (certDao.isRevoked()) {
			LOG.warn("failureReason: " +
					"certificate with id '" + certDao.getId() + "' already revoked.");
		}

		CRLReason crlReason = cryptoUtil.crlReasonFromString(reason);

		String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
		LOG.debug("crlReason : " + crlReasonStr);

		Date revocationDate = new Date();

		
		
		revokeCertificate(certDao, crlReason, revocationDate, caccRepo.findDefaultCA().get(0));

		certDao.setRevoked(true);
		certDao.setRevokedSince(LocalDate.now());
		certDao.setRevocationReason(crlReasonStr);
		certDao.setRevocationExecutionId("39");

	}

	private void revokeCertificate(Certificate certificateDao, CRLReason crlReason, Date revocationDate,
			CAConnectorConfig caConfig ) throws Exception {

		if (caConfig == null) {
			throw new Exception("CA connector not selected !");
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOG.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			adcsController.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

//		} else if (CAConnectorType.Cmp.equals(caConfig.getCaConnectorType())) {
//			LOG.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
//
//			X509Certificate x509Cert = cryptoUtil.convertPemToCertificate(certificateDao.getContent());
//
//			caCmpConnector.revokeCertificate(x509Cert, crlReason, caConfig.getSecret(), caConfig.getCaUrl(),
//					caConfig.getName());
		} else {
			throw new Exception("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}

}
