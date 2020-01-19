package de.trustable.ca3s.core.web.rest.support;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.validation.Valid;

import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.PkcsXXData;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/publicapi")
public class CSRContentProcessor {

    private final Logger LOG = LoggerFactory.getLogger(CSRContentProcessor.class);

	  @Autowired
	  CryptoUtil cryptoUtil;

	  @Autowired
	  CertificateUtil certUtil;
	  
	  
    /**
     * {@code POST  /csrContent} : Process a PKCSXX-object encoded as PEM.
     *
     * @param cSR the cSR to process.
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/csrContent")
    public ResponseEntity<PkcsXXData> describeCSR(@Valid @RequestBody String csrBase64) {
    	
    	LOG.debug("REST request to describe a PEM clob : {}", csrBase64);
        
		PkcsXXData p10ReqData = new PkcsXXData();
    	
		try {
			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(csrBase64);
			Certificate cert = certUtil.getCertificateByPEM(csrBase64);
			p10ReqData = new PkcsXXData(certHolder, cert );
			
		} catch (GeneralSecurityException | IOException e) {
			
			LOG.debug("not a certificate, trying to parse it as CSR ");
			p10ReqData.setCSRValid(false);
			
			try {
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrBase64);
				p10ReqData = new PkcsXXData(p10ReqHolder);
			} catch (IOException |  GeneralSecurityException e2) {
				LOG.debug("describeCSR ", e2);
				return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
			}
		}


		return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.OK);
	}

}
