package de.trustable.ca3s.core.web.rest.support;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.web.rest.data.Pkcs10RequestData;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CSR}.
 */
@RestController
@RequestMapping("/api")
public class CSRContentProcessor {

    private final Logger LOG = LoggerFactory.getLogger(CSRContentProcessor.class);

	  @Autowired
	  CryptoUtil cryptoUtil;


    /**
     * {@code POST  /csrContent} : Process a cSR.
     *
     * @param cSR the cSR to process.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cSR, or with status {@code 400 (Bad Request)} if the cSR has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/csrContent")
    public ResponseEntity<Pkcs10RequestData> describeCSR(@Valid @RequestBody String csrBase64) {
    	
    	LOG.debug("REST request to describe a CSR : {}", csrBase64);
        
		Pkcs10RequestData p10ReqData = new Pkcs10RequestData();
		p10ReqData.setCSRValid(false);
		try {
			Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrBase64);
			p10ReqData = new Pkcs10RequestData(p10ReqHolder);
		} catch (IOException |  GeneralSecurityException e) {
			LOG.debug("describeCSR ", e);
			return new ResponseEntity<Pkcs10RequestData>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Pkcs10RequestData>(p10ReqData, HttpStatus.OK);
	}

}
