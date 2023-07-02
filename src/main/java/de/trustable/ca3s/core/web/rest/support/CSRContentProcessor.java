package de.trustable.ca3s.core.web.rest.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.badkeys.BadKeysResult;
import de.trustable.ca3s.core.service.badkeys.BadKeysService;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
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
@RequestMapping("/publicapi")
public class CSRContentProcessor {

	private final Logger LOG = LoggerFactory.getLogger(CSRContentProcessor.class);

	private final CryptoUtil cryptoUtil;

	private final CSRRepository csrRepository;

	private final CertificateRepository certificateRepository;

	private final CertificateUtil certUtil;

    private final PipelineRepository pipelineRepository;

    private final PipelineUtil pvUtil;

    private final BadKeysService badKeysService;

    private final boolean findReplacementCandidates;

    public CSRContentProcessor(CryptoUtil cryptoUtil,
                               CSRRepository csrRepository,
                               CertificateRepository certificateRepository,
                               CertificateUtil certUtil,
                               PipelineRepository pipelineRepository,
                               PipelineUtil pvUtil,
                               BadKeysService badKeysService,
                               @Value("${ca3s.issuance.findReplacements:false}") boolean findReplacementCandidates) {
        this.cryptoUtil = cryptoUtil;
        this.csrRepository = csrRepository;
        this.certificateRepository = certificateRepository;
        this.certUtil = certUtil;
        this.pipelineRepository = pipelineRepository;
        this.pvUtil = pvUtil;
        this.badKeysService = badKeysService;
        this.findReplacementCandidates = findReplacementCandidates;
    }


    /**
     * {@code POST  /csrContent} : Process a PKCSXX-object encoded as PEM.
     *
     * @param uploaded a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @Transactional
    @PostMapping("/describeContent")
    public ResponseEntity<PkcsXXData> describeContent(@Valid @RequestBody UploadPrecheckData uploaded) {

    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    	String content = uploaded.getContent();
    	LOG.debug("REST request to describe a PEM clob : {}", content);

		PkcsXXData p10ReqData = new PkcsXXData();
		if( content == null || content.trim().isEmpty()) {
            return new ResponseEntity<>(p10ReqData, HttpStatus.OK);
        }

		try {

	    	try {

                BufferedReader reader = new BufferedReader( new StringReader(content));
                String rawBase64 = "";
                String line = reader.readLine();
                while(line != null){
                    if( line.toUpperCase().contains("-BEGIN CERTIFICATE-")){
                        LOG.debug("PEM certificate start found");
                    } else if( line.toUpperCase().contains("-END CERTIFICATE-")){
                        LOG.debug("PEM certificate end found");
                    }else{
                        rawBase64 += line.replace("\n", "").replace("\r", "");
                    }
                    line = reader.readLine();
                }
                LOG.debug("stripped PEM: {}", rawBase64);

                CertificateFactory factory = CertificateFactory.getInstance("X.509");
		    	X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(Base64.decode(rawBase64)));
		    	content = cryptoUtil.x509CertToPem(cert);
		    	LOG.debug("certificate parsed from base64 (non-pem) content");
	    	} catch (GeneralSecurityException | IOException | DecoderException gse) {
		    	LOG.debug("certificate parsing from base64 (non-pem) content failed: {}", gse.getMessage());
	    	}

			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(content);
			if( auth.isAuthenticated()) {
				List<Certificate> certList = certificateRepository.findByIssuerSerial(certHolder.getIssuer().toString(), certHolder.getSerialNumber().toString());
				p10ReqData = new PkcsXXData(certHolder, content, !certList.isEmpty());
			} else {
				// no information leakage to the outside if not authenticated
				p10ReqData = new PkcsXXData(certHolder, content, false);
			}
            if( badKeysService.isInstalled()){
                List<String> messageList = new ArrayList<>();

                BadKeysResult badKeysResult = badKeysService.checkContent(content);
                p10ReqData.setBadKeysResult(badKeysResult);
                if( badKeysResult.isValid()) {
                    LOG.debug("BadKeys is installed and returns OK");
//                    messageList.add("BadKeys check: no findings");
                }else{
                    if( badKeysResult.getResponse() != null &&
                        badKeysResult.getResponse().getResults() != null &&
                        badKeysResult.getResponse().getResults().getResultType() != null ) {
                        messageList.add("ca3SApp.messages.badkeys." + badKeysResult.getResponse().getResults().getResultType());

                    }
                }
                p10ReqData.setWarnings(messageList.toArray(new String[0]));

            }else{
                LOG.debug("BadKeys not installed");
            }

            LOG.debug("certificate parsed from uploaded PEM content : " + certHolder.getSubject());
		} catch (org.bouncycastle.util.encoders.DecoderException de){
			// no parseable ...
			p10ReqData.setDataType(PKCSDataType.UNKNOWN);
			LOG.debug("certificate parsing problem of uploaded content: " + de.getMessage());
		} catch (GeneralSecurityException e) {
			LOG.debug("not a certificate, trying to parse it as CSR ");

			try {
                PKCS10CertificationRequest pkcs10CertificationRequest = cryptoUtil.convertPemToPKCS10CertificationRequest(content);
				Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(pkcs10CertificationRequest);

				Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow( p10ReqHolder);

				p10ReqData = new PkcsXXData(p10ReqHolderShallow);
				// no information leakage to the outside: check authentication
				if( auth.isAuthenticated()) {
					List<CSR> csrList = csrRepository.findByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
					LOG.debug("public key with hash '{}' used in #{} csrs, yet", p10ReqHolder.getPublicKeyHash(), csrList.size());
					p10ReqData.setCsrPublicKeyPresentInDB(!csrList.isEmpty());

                    List<String> messageList = new ArrayList<>();
                    handleBadKeys(p10ReqData, pkcs10CertificationRequest, messageList);

                    if( uploaded.getPipelineId() != null) {
                        Optional<Pipeline> optPipeline = pipelineRepository.findById(uploaded.getPipelineId());
                        if (optPipeline.isPresent()) {
                            if (pvUtil.isPipelineRestrictionsResolved(optPipeline.get(), p10ReqHolder, uploaded.getArAttributes(), messageList)) {
                                LOG.debug("pipeline restrictions for pipeline '{}' solved", optPipeline.get().getName());
                            } else {
                                p10ReqData.setWarnings(messageList.toArray(new String[0]));
//                            return new ResponseEntity<>(p10ReqData, HttpStatus.BAD_REQUEST);
                            }
                        } else {
                            LOG.info("pipeline id '{}' not found", uploaded.getPipelineId());
                        }
                    }

                    if( findReplacementCandidates) {
                        List<Certificate> candidates = certUtil.findReplaceCandidates(p10ReqData.getP10Holder().getSans());
                        p10ReqData.setReplacementCandidatesFromList(candidates);
                        LOG.debug("#{} replacement candidates found", candidates);
                    }else{
                        LOG.debug("retrieval of replacement candidates disabled");
                    }
				}

			} catch (IOException | GeneralSecurityException e2) {
				LOG.debug("describeCSR : " + e2.getMessage());
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

			        List<X509CertificateHolderShallow> certList = new ArrayList<>();

                    PKCSDataType dataType = PKCSDataType.CONTAINER;
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
							LOG.debug("certificate {} found in PKCS12 for alias {}", x509cert.getSubjectX500Principal().toString(), alias);

					    	String b64Content = cryptoUtil.x509CertToPem(x509cert);
			    			X509CertificateHolder certHolder = cryptoUtil.convertPemToCertificateHolder(b64Content);
			    			X509CertificateHolderShallow x509Holder = new X509CertificateHolderShallow(certHolder);
			    			x509Holder.setPemCertificate(b64Content);

				            if (pkcs12Store.isKeyEntry(alias)){
				            	Key key = pkcs12Store.getKey(alias, passphrase);
				            	x509Holder.setKeyPresent(true);
								LOG.debug("key {} found alongside certificate in PKCS12 for alias {}", key, alias);
                                dataType = PKCSDataType.CONTAINER_WITH_KEY;
				            }

			    			certList.add(x509Holder);
			            }
			        }

			        p10ReqData = new PkcsXXData();
			        X509CertificateHolderShallow[] chsArr = new X509CertificateHolderShallow[certList.size()];
			        certList.toArray(chsArr);
			        p10ReqData.setCertsHolder(chsArr);

					p10ReqData.setDataType(dataType);

				} catch( IOException ioe) {
					// not able to process, presumably passphrase required ...
					p10ReqData.setPassphraseRequired(true);
					p10ReqData.setDataType(PKCSDataType.CONTAINER_REQUIRING_PASSPHRASE);
					LOG.debug("p12 missing a passphrase:", ioe);
				} catch (org.bouncycastle.util.encoders.DecoderException de){
					// not parseable ...
					p10ReqData.setDataType(PKCSDataType.UNKNOWN);
					LOG.debug("p12 parsing problem of uploaded content: " + de.getMessage());
				} catch(GeneralSecurityException e3) {
					LOG.debug("general problem with uploaded content: " + e3.getMessage());
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<>(p10ReqData, HttpStatus.OK);
	}

    private void handleBadKeys(PkcsXXData p10ReqData, PKCS10CertificationRequest pkcs10CertificationRequest, List<String> messageList) throws IOException {
        if( badKeysService.isInstalled()){
            BadKeysResult badKeysResult = badKeysService.checkContent(CryptoUtil.pkcs10RequestToPem(pkcs10CertificationRequest));
            p10ReqData.setBadKeysResult(badKeysResult);
            if( badKeysResult.isValid()) {
                LOG.debug("BadKeys is installed and returns OK");
//                messageList.add("BadKeys check: no findings");
            }else{
                if( badKeysResult.getResponse() != null &&
                    badKeysResult.getResponse().getResults() != null &&
                    badKeysResult.getResponse().getResults().getResultType() != null ) {
                    messageList.add("ca3SApp.messages.badkeys." + badKeysResult.getResponse().getResults().getResultType());

                }
            }
        }else{
            LOG.debug("BadKeys not installed");
        }
    }

}
