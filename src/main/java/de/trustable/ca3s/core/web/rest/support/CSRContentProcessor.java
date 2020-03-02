package de.trustable.ca3s.core.web.rest.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.validation.Valid;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.PKCSDataType;
import de.trustable.ca3s.core.web.rest.data.Pkcs10RequestHolderShallow;
import de.trustable.ca3s.core.web.rest.data.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
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
	private CryptoUtil cryptoUtil;

	@Autowired
	private CertificateUtil certUtil;

	@Autowired
	private CSRRepository csrRepository;

	@Autowired
	private CertificateRepository certificateRepository;


    /**
     * {@code POST  /csrContent} : Process a PKCSXX-object encoded as PEM.
     *
     * @param a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/describeContent")
    public ResponseEntity<PkcsXXData> describeContent(@Valid @RequestBody UploadPrecheckData uploaded) {
    	
    	String content = uploaded.getContent();
    	LOG.debug("REST request to describe a PEM clob : {}", content);
        
		PkcsXXData p10ReqData = new PkcsXXData();
    	
		try {
			
	    	try {
		    	CertificateFactory factory = CertificateFactory.getInstance("X.509");
		    	X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(Base64.decode(content)));
		    	content = cryptoUtil.x509CertToPem(cert);
		    	LOG.debug("certificate parsed from base64 (non-pem) content");
	    	} catch (GeneralSecurityException | IOException | DecoderException gse) {
		    	LOG.debug("certificate parsing from base64 (non-pem) content failed", gse);
	    	}

			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(content);
			List<Certificate> certList = certificateRepository.findByIssuerSerial(certHolder.getIssuer().toString(), certHolder.getSerialNumber().toString());
			p10ReqData = new PkcsXXData(certHolder, content, !certList.isEmpty());
		} catch (org.bouncycastle.util.encoders.DecoderException de){	
			// no parseable ...
			p10ReqData.setDataType(PKCSDataType.UNKNOWN);
			LOG.debug("certificate parsing problem of uploaded content:", de);
		} catch (GeneralSecurityException e) {
			
			LOG.debug("not a certificate, trying to parse it as CSR ", e);
			
			try {
				
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(cryptoUtil.convertPemToPKCS10CertificationRequest(content));
				
				Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow( p10ReqHolder);
				
				List<CSR> csrList = csrRepository.findByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
				LOG.debug("public key with hash '{}' used in #{} csrs, yet", p10ReqHolder.getPublicKeyHash(), csrList.size());
				
				p10ReqData.setPublicKeyPresentInDB( !csrList.isEmpty());
				p10ReqData = new PkcsXXData(p10ReqHolderShallow);
			} catch (IOException | GeneralSecurityException e2) {
				LOG.debug("describeCSR ", e2);
				LOG.debug("not a csr, trying to parse it as PKCS12 ");
				try {
					
			        KeyStore pkcs12Store = KeyStore.getInstance("PKCS12", "BC");

			        ByteArrayInputStream bais = new ByteArrayInputStream( Base64.decode(content));
			        
			        char[] passwd = new char[0];
			        if( ( uploaded.getPassphrase() != null ) && (uploaded.getPassphrase().trim().length() > 0)) {
			        	passwd = uploaded.getPassphrase().toCharArray();
			        }
			        
			        pkcs12Store.load(bais, passwd);

			        for (Enumeration<String> en = pkcs12Store.aliases(); en.hasMoreElements();)
			        {
			            String alias = en.nextElement();

			            if (pkcs12Store.isCertificateEntry(alias)){
			            	
			            	X509Certificate x509cert = (X509Certificate)pkcs12Store.getCertificate(alias);
							LOG.debug("certificate {} found in PKCS12 for alias {}", x509cert.getSubjectDN().getName(), alias);

			    			Certificate cert = certUtil.getCertificateByX509(x509cert);
			    			p10ReqData = new PkcsXXData(new X509CertificateHolder(x509cert.getEncoded()), cert != null );
			            	
				            if (pkcs12Store.isKeyEntry(alias)){

				            	Key key = pkcs12Store.getKey(alias, passwd);
								LOG.debug("key {} found alongside certificate in PKCS12 for alias {}", key, alias);
				            }
			            }
			        }

				} catch (org.bouncycastle.util.encoders.DecoderException de){	
					// no parseable ...
					p10ReqData.setDataType(PKCSDataType.UNKNOWN);
					LOG.debug("p12 parsing problem of uploaded content:", de);
				}catch(GeneralSecurityException | IOException e3) {
					LOG.debug("general problem with uploaded content:", e3);
					return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.OK);
	}


}
