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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
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
  	
  	@Autowired
  	private ProtectedContentUtil protContentUtil;

    /**
     * Public certificate download endpoint providing DER format
     *  
     * @param certId the internal certificate id
     * @return the binary certificate
     */
    @RequestMapping(value = "/certPKIX/{certId}/{filename}/{alias}", 
    		method = GET,
    		produces = ACMEController.APPLICATION_PKIX_CERT_VALUE)
    public @ResponseBody byte[] getCertificatePKIX(@PathVariable final long certId, @PathVariable final String filename, @PathVariable final String alias) throws NotFoundException {

		LOG.info("Received certificate download request (PKIX) for id {} as file '{}' and alias '{}'", certId, filename, alias);

    	if( SecurityContextHolder.getContext() == null ) {
			throw new NotFoundException("Authentication required");
    	}

		try {
			return buildByteArrayResponseForId(certId, ACMEController.APPLICATION_PKIX_CERT_VALUE, alias);
		} catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
			throw new NotFoundException(e.getMessage());
		}

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

    
    /**
     * Keystore download endpoint
     * 
     * @param certId the internal certificate id
     * @param accept the description of the requested format
     * @return the certificate in the requested encoded form 
     * @throws NotFoundException 
     */
    @RequestMapping(value = "/keystore/{certId}", 
    		method = GET,
    		produces = ACMEController.APPLICATION_PKCS12_VALUE)
    public @ResponseBody byte[] getKeystore(@PathVariable final long certId, 
    		@RequestHeader(name="Accept", defaultValue=ACMEController.APPLICATION_PKCS12_VALUE) final String accept) throws NotFoundException {

		LOG.info("Received keystore request for id {}", certId);
		
    	try {
			return buildByteArrayResponseForId(certId, accept, "");
		} catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
			throw new NotFoundException(e.getMessage());
		}  			
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

	public @ResponseBody byte[] buildByteArrayResponseForId(final long certId, final String accept, final String alias)
			throws HttpClientErrorException, AcmeProblemException, GeneralSecurityException {
		
		Optional<Certificate> certOpt = certificateRepository.findById(certId);
    	
  		if(!certOpt.isPresent()) {
  		  throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
  		}else {
  			Certificate certDao = certOpt.get();

  			final HttpHeaders headers = new HttpHeaders();

			byte[] resp = null;

			if(ACMEController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)){
				resp = buildPkixCertResponse(certDao, headers);
			}else if(ACMEController.APPLICATION_PKCS12_VALUE.equalsIgnoreCase(accept)){
				resp = buildPKCS12Response(certDao, alias, headers);
			}

			if( resp == null) {
				String msg = "problem returning certificate with accepting type " + accept;
				LOG.info(msg);
				
				throw new GeneralSecurityException(msg);
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
//		}else if(ACMEController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)){
//			return buildPkixCertResponse(certDao, headers);
		}else if(ACMEController.APPLICATION_PEM_CERT_CHAIN_VALUE.equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, true);
		}else if(ACMEController.APPLICATION_PEM_CERT_VALUE.equalsIgnoreCase(accept)){
			return buildPEMResponse(certDao, headers, false);
//		}else if(ACMEController.APPLICATION_PKCS12_VALUE.equalsIgnoreCase(accept)){
//			return buildPKCS12Response(certDao, headers);
		}
		
		LOG.info("unexpected accept type {}", accept);

		return null;
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


	private @ResponseBody byte[]  buildPkixCertResponse(Certificate certDao, final HttpHeaders headers) throws GeneralSecurityException {
		LOG.info("building PKIX certificate response");

		try {
			X509Certificate x509Cert = CryptoService.convertPemToCertificate(certDao.getContent());
			return x509Cert.getEncoded();
		}catch(GeneralSecurityException gse) {
			LOG.info("problem downloading certificate content for cert id " + certDao.getId(), gse);
			throw gse;
		}
	}


	private @ResponseBody byte[] buildPKCS12Response(Certificate certDao, final String alias, final HttpHeaders headers) throws GeneralSecurityException {
		LOG.info("building PKCS12 container response");

		CSR csr = certDao.getCsr();
		if (csr == null) {
			throw new GeneralSecurityException("problem downloading keystore content for cert id "+certDao.getId()+": no csr object available ");
		}

    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();
    	if( userName == null ) {
			throw new GeneralSecurityException("problem downloading keystore content for csr id "+csr.getId()+":  user name not available");
    	}if(userName.equals(csr.getRequestedBy())) {
			throw new GeneralSecurityException("problem downloading keystore content for csr id "+csr.getId()+": user does not match initial requestor");
    	}

		if (!csr.isServersideKeyGeneration()) {
			throw new GeneralSecurityException("problem downloading keystore content for csr id "+csr.getId()+": key not generated serverside");
		}

		List<ProtectedContent> protContentList = protContentUtil.retrieveProtectedContent(ProtectedContentType.PASSWORD,
				ContentRelationType.CSR, csr.getId());
		if (protContentList.size() == 0) {
			throw new GeneralSecurityException("problem downloading keystore content for csr id "+csr.getId()+": no keystore passphrase available ");
		}

		PrivateKey key = certUtil.getPrivateKey(ProtectedContentType.KEY, ContentRelationType.CSR, csr.getId());

		char[] passphraseChars = protContentUtil.unprotectString(protContentList.get(0).getContentBase64())
				.toCharArray();
		try {

			KeyStore p12 = KeyStore.getInstance("pkcs12");
			p12.load(null, passphraseChars);

			X509Certificate[] chain = certUtil.getX509CertificateChain(certDao);

			p12.setKeyEntry("entry", key, passphraseChars, chain);

			try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
				p12.store(baos, passphraseChars);

				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				KeyStore store = KeyStore.getInstance("pkcs12");
				store.load(bais, passphraseChars);
				
				java.security.cert.Certificate cert = store.getCertificate(alias);
				LOG.debug("retrieved cert " + cert);

				return baos.toByteArray();
			}
			
		} catch (IOException gse) {
			throw new GeneralSecurityException("problem downloading keystore content for cert id " + certDao.getId());
		}
	}

}

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Not Found")
class NotFoundException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7873233893252750875L;

	public NotFoundException() {
    }

	public NotFoundException(String message) {
		super(message);
	}
}