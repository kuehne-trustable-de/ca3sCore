package de.trustable.ca3s.core.web.rest.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.PKCSDataType;
import de.trustable.ca3s.core.web.rest.data.Pkcs10RequestHolderShallow;
import de.trustable.ca3s.core.web.rest.data.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
import de.trustable.ca3s.core.web.rest.data.X509CertificateHolderShallow;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class ContentUploadProcessor {

	private final Logger LOG = LoggerFactory.getLogger(ContentUploadProcessor.class);

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	private CertificateUtil certUtil;

    @Autowired
    private CSRUtil csrUtil;

	@Autowired
	private CSRRepository csrRepository;

	@Autowired
	private CsrAttributeRepository csrAttributeRepository;

    @Autowired
    private CertificateRepository certificateRepository;

	@Autowired
	private BPMNUtil bpmnUtil;


    /**
     * {@code POST  /csrContent} : Process a PKCSXX-object encoded as PEM.
     *
     * @param a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/uploadContent")
	@Transactional
    public ResponseEntity<PkcsXXData> uploadContent(@Valid @RequestBody UploadPrecheckData uploaded) {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String requestorName = auth.getName();
    	
    	String content = uploaded.getContent();
    	LOG.debug("Request to upload a PEM clob : {} by user {}", content, auth.getName());
        
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
			List<Certificate> certList = findCertificateByIssuerSerial(certHolder);
			if(!certList.isEmpty()){
				// certificate already present in db
				LOG.info("certificate already present");
				return new ResponseEntity<PkcsXXData>(HttpStatus.CONFLICT);
			}
			
			Certificate cert = insertCertificate(content, requestorName);
			if( cert == null ) {
				LOG.info("problem importing uploaded certificate content");
				return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
			}
			
			// certificate inserted into the db 
			p10ReqData = new PkcsXXData(certHolder, content, true );
			certUtil.setCertAttribute(cert, CsrAttribute.ATTRIBUTE_REQUSTOR_NAME, requestorName);
			
			return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.CREATED);
			
		} catch (DecoderException de){	
			// not parseable ...
			p10ReqData.setDataType(PKCSDataType.UNKNOWN);
			LOG.debug("certificate parsing problem of uploaded content:", de);
		} catch (GeneralSecurityException | IOException e) {
			
			LOG.debug("not a certificate, trying to parse it as CSR ");
			
			try {
				
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(cryptoUtil.convertPemToPKCS10CertificationRequest(content));
				
				Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow( p10ReqHolder);
				
				List<CSR> csrList = csrRepository.findByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
				LOG.debug("public key with hash '{}' used in #{} csrs, yet", p10ReqHolder.getPublicKeyHash(), csrList.size());

				p10ReqData = new PkcsXXData(p10ReqHolderShallow);

				p10ReqData.setCsrPublicKeyPresentInDB(!csrList.isEmpty());
				if(csrList.isEmpty()) {
					Certificate cert = startCertificateCreationProcess(content, requestorName );
					if( cert != null) {
						X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(cert.getContent());
						p10ReqData = new PkcsXXData(certHolder, cert != null );
						p10ReqData.getCertsHolder()[0].setCertificateId(cert.getId());
						p10ReqData.getCertsHolder()[0].setCertificatePresentInDB(true);
						return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.CREATED);
					}
				}
				
			} catch (IOException | GeneralSecurityException e2) {
				LOG.debug("describeCSR ", e2);
				LOG.debug("not a certificate, not a CSR, trying to parse it as a P12 container");
				try {
					
			        KeyStore pkcs12Store = KeyStore.getInstance("PKCS12", "BC");

			        ByteArrayInputStream bais = new ByteArrayInputStream( Base64.decode(content));
			        
			        char[] passphrase = new char[0];
			        if( ( uploaded.getPassphrase() != null ) && (uploaded.getPassphrase().trim().length() > 0)) {
			        	passphrase = uploaded.getPassphrase().toCharArray();
			        }
			        
			        pkcs12Store.load(bais, passphrase);
					LOG.debug("keystore loaded successfully!");

			        List<X509CertificateHolderShallow> certList = new ArrayList<X509CertificateHolderShallow>();

			        for (Enumeration<String> en = pkcs12Store.aliases(); en.hasMoreElements();)
			        {
			            String alias = en.nextElement();
						LOG.debug("iterating keystore, found alias {}, isCertificateEntry {}, isKeyEntry {}", alias, pkcs12Store.isCertificateEntry(alias), pkcs12Store.isKeyEntry(alias));

			            if (pkcs12Store.isCertificateEntry(alias) || pkcs12Store.isKeyEntry(alias)){
			            	
			            	X509Certificate x509cert = (X509Certificate)pkcs12Store.getCertificate(alias);
			            	if( x509cert == null) {
								LOG.debug("alias {} does NOT refer to a certificate entry", alias);
			            		continue;
			            	}
							LOG.debug("certificate {} found in PKCS12 for alias '{}'", x509cert.getSubjectDN().getName(), alias);
			            	
					    	String b64Content = cryptoUtil.x509CertToPem(x509cert);
			    			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(b64Content);
			    			X509CertificateHolderShallow x509Holder = new X509CertificateHolderShallow(certHolder);
			    			x509Holder.setPemCertificate(b64Content);
			            	
		    				Certificate cert = null;
			    			List<Certificate> certListDB = findCertificateByIssuerSerial(certHolder);
							LOG.debug("certListDB has # {} item", certListDB.size());
			    			if(!certListDB.isEmpty()){
			    				cert = certListDB.get(0);
			    				if( certListDB.size() > 1 ) {
			    					LOG.info("problem: found more than one matching certificate for issuer {}, serial {}", certHolder.getIssuer().toString(), certHolder.getSerialNumber().toString());
			    				}
			    			}else {
			    				// insert certificate
			    				cert = insertCertificate(b64Content, requestorName);
			    				if( cert == null ) {
			    					LOG.info("problem importing uploaded certificate content");
			    					return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
			    				}
			    			}
		    				x509Holder.setCertificateId(cert.getId());
		    				x509Holder.setCertificatePresentInDB(true);

				            if (pkcs12Store.isKeyEntry(alias)){

				            	Key key = pkcs12Store.getKey(alias, passphrase);
								LOG.debug("key {} found alongside certificate in PKCS12 for alias {}", "*****", alias);
								
								KeyPair keyPair = new KeyPair(x509cert.getPublicKey(), (PrivateKey) key);
								certUtil.storePrivateKey(cert, keyPair);
								x509Holder.setKeyPresent(true);
								LOG.debug("key {} stored for certificate {}", "*****", cert.getId());

				            }
			    			certList.add(x509Holder);
			            }
			        }

			        p10ReqData = new PkcsXXData();
			        X509CertificateHolderShallow[] chsArr = new X509CertificateHolderShallow[certList.size()];
			        certList.toArray(chsArr);
			        p10ReqData.setCertsHolder(chsArr);

					p10ReqData.setDataType(PKCSDataType.CONTAINER);

				} catch( IOException ioe) {
					// not able to process, presumable passphrase required ...
					p10ReqData.setPassphraseRequired(true);
					p10ReqData.setDataType(PKCSDataType.CONTAINER_REQUIRING_PASSPHRASE);
					LOG.debug("p12 missing a passphrase:", ioe);
				} catch (org.bouncycastle.util.encoders.DecoderException de){	
					// no parseable ...
					p10ReqData.setDataType(PKCSDataType.UNKNOWN);
					LOG.debug("p12 parsing problem of uploaded content:", de);
				}catch(GeneralSecurityException e3) {
					LOG.debug("general problem with uploaded content:", e3);
					return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.OK);
	}


	private Certificate insertCertificate(String content, String requestorName)
			throws GeneralSecurityException, IOException {
		// insert certificate
		Certificate cert = certUtil.createCertificate(content, null, null, false);
		
		// save the source of the certificate
		certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_UPLOADED_BY, requestorName);

		certificateRepository.save(cert);
		
		LOG.info("created new certificate entry with id {} uploaded by {}", cert.getId(), requestorName);

		return cert;
	}

    
    /**
     * 
     * @param orderDao
     * @return
     * @throws IOException
     */
	private Certificate startCertificateCreationProcess(final String csrAsPem, final String requestorName )  {
		
		
		// BPNM call
		try {
			Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrAsPem);

			CSR csr = csrUtil.buildCSR(csrAsPem, p10ReqHolder);
			
			csrRepository.save(csr);

			CsrAttribute csrAttRequestorName = new CsrAttribute();
			csrAttRequestorName.setCsr(csr);
			csrAttRequestorName.setName(CsrAttribute.ATTRIBUTE_REQUSTOR_NAME);
			csrAttRequestorName.setValue(requestorName);
			csr.getCsrAttributes().add(csrAttRequestorName);
			
			csrAttributeRepository.save(csrAttRequestorName);
			
			LOG.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());
			for(de.trustable.ca3s.core.domain.RDN rdn:csr.getRdns()) {
				LOG.debug("RDN contains #{}", rdn.getRdnAttributes().size());
			}
			
			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
			if(cert != null) {
				
				certificateRepository.save(cert);
				return cert;
				
			} else {
				LOG.warn("creation of certificate requested by {} failed ", requestorName);
			}

			// end of BPMN call

		} catch (GeneralSecurityException | IOException e) {
			LOG.warn("execution of CSRProcessingTask failed ", e);
		}

		return null;
	}

	private List<Certificate> findCertificateByIssuerSerial(X509CertificateHolder certHolder) {
		return certificateRepository.findByIssuerSerial(certHolder.getIssuer().toString(), certHolder.getSerialNumber().toString());
		
	}

}
