package de.trustable.ca3s.core.web.rest.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.validation.Valid;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
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
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.data.Pkcs10RequestHolderShallow;
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
	private CryptoUtil cryptoUtil;

	@Autowired
	private CertificateUtil certUtil;

	@Autowired
	private ProtectedContentUtil protUtil;
	
	@Autowired
	private CSRRepository csrRepository;

	@Autowired
	private ProtectedContentRepository protContentRepository;
	  
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
			
			try {
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(cryptoUtil.convertPemToPKCS10CertificationRequest(csrBase64),
						new Pkcs10RequestHolderShallow());
				
				List<CSR> csrList = csrRepository.findByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
				LOG.debug("public key with hash '{}' used in #{} csrs, yet", p10ReqHolder.getPublicKeyHash(), csrList.size());
				
				p10ReqData.setPublicKeyPresentInDB( !csrList.isEmpty());
				p10ReqData = new PkcsXXData(p10ReqHolder);
			} catch (IOException | GeneralSecurityException e2) {
				LOG.debug("describeCSR ", e2);
				LOG.debug("not a csr, trying to parse it as PKCS12 ");
				try {
					
			        KeyStore pkcs12Store = KeyStore.getInstance("PKCS12", "BC");

			        ByteArrayInputStream bais = new ByteArrayInputStream( Base64.decode(csrBase64));
			        
			        char[] passwd = "s3cr3t".toCharArray();
			        pkcs12Store.load(bais, passwd);

			        for (Enumeration<String> en = pkcs12Store.aliases(); en.hasMoreElements();)
			        {
			            String alias = en.nextElement();

			            if (pkcs12Store.isCertificateEntry(alias)){
			            	
			            	X509Certificate x509cert = (X509Certificate)pkcs12Store.getCertificate(alias);
							LOG.debug("certificate {} found in PKCS12 for alias {}", x509cert.getSubjectDN().getName(), alias);

			    			Certificate cert = certUtil.getCertificateByX509(x509cert);
			    			p10ReqData = new PkcsXXData(new X509CertificateHolder(x509cert.getEncoded()), cert );
			            	
				            if (pkcs12Store.isKeyEntry(alias)){

				            	Key key = pkcs12Store.getKey(alias, passwd);
								LOG.debug("key {} found alongside certificate in PKCS12 for alias {}", key, alias);
				            }
			            }
			        }

				}catch(GeneralSecurityException | IOException e3) {
					return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.OK);
	}

	/**
	 * 
	 * @param keyPair
	 * @return
	 * @throws IOException
*/	 
	private void storePrivateKey(Certificate cert, Key key) throws IOException {
		
		StringWriter sw = new StringWriter();
		PemObject pemObject = new PemObject( "PRIVATE KEY", key.getEncoded());
		PemWriter pemWriter = new PemWriter(sw);
		try {
			pemWriter.writeObject(pemObject);
		} finally {
			pemWriter.close();
		}

		LOG.debug("new private key as PEM : " + sw.toString());

		String protContent = protUtil.protectString(sw.toString());
		ProtectedContent pt = new ProtectedContent();
		pt.setType(ProtectedContentType.KEY);
		pt.setContentBase64(protContent);
		pt.setRelationType(ContentRelationType.CERTIFICATE);
		pt.setRelatedId(cert.getId());
		
		protContentRepository.save(pt);
		
	}


}
