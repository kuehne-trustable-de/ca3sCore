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

package de.trustable.ca3s.core.web.rest.support;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.web.rest.acme.ACMEController;

@Controller
@RequestMapping("/publicapi")
public class CertificateDownloadController  {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateDownloadController.class);

    private boolean chainIncludeRoot = true;
    
  	@Autowired
  	private CertificateRepository certificateRepository;

  	@Autowired
  	private CertificateUtil certUtil;

    /**
     * Public certificate download endpoint providing DER format
     *  
     * @param certId the internal certificate id
     * @return the binary certificate
     */
    @RequestMapping(value = "/certPKIX/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePKIX(@PathVariable final long certId, @PathVariable final String filename) {

		LOG.info("Received certificate download request (PKIX) for id {} as file '{}'", certId, filename);
    	return buildCertResponseForId(certId, ACMEController.APPLICATION_PKIX_CERT_VALUE);  			
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *  
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMChain/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMChain(@PathVariable final long certId, @PathVariable final String filename) {

		LOG.info("Received certificate download request (PEM with chain) for id {} as file '{}'", certId, filename);
    	return buildCertResponseForId(certId, ACMEController.APPLICATION_PEM_CERT_CHAIN_VALUE);  			
    }

    /**
     * Public certificate download endpoint providing PEM format
     *  
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate 
     */
    @RequestMapping(value = "/certPEM/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEM(@PathVariable final long certId, @PathVariable final String filename) {

		LOG.info("Received certificate download request (PEM) for id {} as file '{}'", certId, filename);
    	return buildCertResponseForId(certId, ACMEController.APPLICATION_PKIX_CERT_VALUE);  			
    }

    /**
     * Public certificate download endpoint
     * 
     * @param certId the internal certificate id
     * @param accept the description of the requested format
     * @return the certificate in the requested encoded form 
     */
    @RequestMapping(value = "/cert/{certId}", method = GET)
    public ResponseEntity<?> getCertificate(@PathVariable final long certId, 
    		@RequestHeader(name="Accept", defaultValue=ACMEController.APPLICATION_PEM_CERT_CHAIN_VALUE) final String accept) {

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

  			final HttpHeaders headers = new HttpHeaders();

			ResponseEntity<?> resp = buildCertifcateResponse(accept, certDao, headers);
  			
			if( resp == null) {
				String msg = "problem returning certificate with accepting type " + accept;
				LOG.info(msg);
				
				return ResponseEntity.badRequest().build();
			}
			
			return resp;
  		}
	}

	/**
	 * @param accept
	 * @param certDao
	 * @param headers
	 */
	public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao, final HttpHeaders headers) {
		
		if("*/*".equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, true);
		}else if(ACMEController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)){
			return buildPkixCertResponse(certDao, headers);
		}else if(ACMEController.APPLICATION_PEM_CERT_CHAIN_VALUE.equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, true);
		}else if(ACMEController.APPLICATION_PEM_CERT_VALUE.equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, false);
		}
		
		LOG.info("unexpected accept type {}", accept);

		return null;
	}



	private ResponseEntity<byte[]> buildPkixCertResponse(Certificate certDao, final HttpHeaders headers) {
		LOG.info("building PKIX certificate response");

		try {
			X509Certificate x509Cert = CryptoService.convertPemToCertificate(certDao.getContent());
			return ResponseEntity.ok().contentType(ACMEController.APPLICATION_PKIX_CERT).headers(headers).body(x509Cert.getEncoded());
		}catch(GeneralSecurityException gse) {
			LOG.info("problem downloading certificate content for cert id " + certDao.getId(), gse);
			return ResponseEntity.badRequest().build();
		}
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
			return ResponseEntity.ok().contentType(ACMEController.APPLICATION_PEM_CERT_CHAIN).headers(headers).body(resultPem.getBytes());
			
		} catch (GeneralSecurityException ge) {
			String msg = "problem building certificate chain";
			LOG.info(msg, ge);
			return ResponseEntity.badRequest().build();
		}
			
	}

}
