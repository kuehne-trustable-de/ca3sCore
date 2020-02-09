package de.trustable.ca3s.core.web.rest.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.validation.Valid;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
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
    public ResponseEntity<PkcsXXData> uploadContent(@Valid @RequestBody UploadPrecheckData uploaded) {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String requestorName = auth.getName();
    	
    	String content = uploaded.getContent();
    	LOG.debug("Request to upload a PEM clob : {} by user {}", content, auth.getName());
        
		PkcsXXData p10ReqData = new PkcsXXData();
    	
		try {
			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(content);
			Certificate cert = certUtil.getCertificateByPEM(content);
			p10ReqData = new PkcsXXData(certHolder, cert );
			
		} catch (org.bouncycastle.util.encoders.DecoderException de){	
			// no parseable ...
			p10ReqData.setDataType(PKCSDataType.UNKNOWN);
		} catch (GeneralSecurityException | IOException e) {
			
			LOG.debug("not a certificate, trying to parse it as CSR ");
			
			try {
				
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(cryptoUtil.convertPemToPKCS10CertificationRequest(content));
				
				Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow( p10ReqHolder);
				
				List<CSR> csrList = csrRepository.findByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
				LOG.debug("public key with hash '{}' used in #{} csrs, yet", p10ReqHolder.getPublicKeyHash(), csrList.size());

				p10ReqData = new PkcsXXData(p10ReqHolderShallow);

				p10ReqData.setPublicKeyPresentInDB( !csrList.isEmpty());
				if(csrList.isEmpty()) {
					Certificate cert = startCertificateCreationProcess(content, requestorName );
					if( cert != null) {
						p10ReqData.setCertificateId(cert.getId());
						p10ReqData.setCertificatePresentInDB(true);
						
						X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(cert.getContent());
						p10ReqData = new PkcsXXData(certHolder, cert );
					}
				}
				
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
			    			p10ReqData = new PkcsXXData(new X509CertificateHolder(x509cert.getEncoded()), cert );
			            	
				            if (pkcs12Store.isKeyEntry(alias)){

				            	Key key = pkcs12Store.getKey(alias, passwd);
								LOG.debug("key {} found alongside certificate in PKCS12 for alias {}", key, alias);
				            }
			            }
			        }

				} catch (org.bouncycastle.util.encoders.DecoderException de){	
					// no parseable ...
					p10ReqData.setDataType(PKCSDataType.UNKNOWN);
				}catch(GeneralSecurityException | IOException e3) {
					return new ResponseEntity<PkcsXXData>(HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<PkcsXXData>(p10ReqData, HttpStatus.OK);
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


}
