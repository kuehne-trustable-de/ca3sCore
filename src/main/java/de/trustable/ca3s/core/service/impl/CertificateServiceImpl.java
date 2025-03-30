package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.CsrUsage;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.exception.KeyApplicableException;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.AsyncNotificationService;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.CertificateService;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.TypedValue;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.ca3s.core.service.dto.Pkcs10RequestHolderShallow;
import de.trustable.ca3s.core.service.dto.PkcsXXData;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pqc.jcajce.provider.dilithium.BCDilithiumPrivateKey;
import org.bouncycastle.pqc.jcajce.provider.falcon.BCFalconPrivateKey;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.FalconParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.ECKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.trustable.ca3s.core.web.rest.support.ContentUploadProcessor.*;

/**
 * Service Implementation for managing {@link Certificate}.
 */
@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private final Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

    private final CertificateRepository certificateRepository;
    final private CryptoService cryptoUtil;
    final private ProtectedContentUtil protUtil;
    private final PreferenceUtil preferenceUtil;
    private final CertificateUtil certificateUtil;
    private final CSRUtil csrUtil;
    private final CSRRepository csrRepository;
    private final CertificateProcessingUtil cpUtil;
    private final AsyncNotificationService asyncNotificationService;
    final private AuditService auditService;

    public CertificateServiceImpl(CertificateRepository certificateRepository, CryptoService cryptoUtil, ProtectedContentUtil protUtil, PreferenceUtil preferenceUtil, CertificateUtil certificateUtil, CSRUtil csrUtil, CSRRepository csrRepository, CertificateProcessingUtil cpUtil, AsyncNotificationService asyncNotificationService, AuditService auditService) {
        this.certificateRepository = certificateRepository;
        this.cryptoUtil = cryptoUtil;
        this.protUtil = protUtil;
        this.preferenceUtil = preferenceUtil;
        this.certificateUtil = certificateUtil;

        this.csrUtil = csrUtil;
        this.csrRepository = csrRepository;
        this.cpUtil = cpUtil;
        this.asyncNotificationService = asyncNotificationService;
        this.auditService = auditService;
    }

    /**
     * Save a certificate.
     *
     * @param certificate the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Certificate save(Certificate certificate) {
        log.debug("Request to save Certificate : {}", certificate);
        return certificateRepository.save(certificate);
    }

    /**
     * Get all the certificates.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Certificate> findAll(Pageable pageable) {
        log.debug("Request to get all Certificates");
        return certificateRepository.findAll(pageable);
    }


    /**
     * Get one certificate by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Certificate> findOne(Long id) {
        log.debug("Request to get Certificate : {}", id);
        return certificateRepository.findById(id);
    }

    /**
     * Delete the certificate by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Certificate : {}", id);
        certificateRepository.deleteById(id);
    }

    public CSR createServersideKeyAndCertificate(Optional<Pipeline> optPipeline,
                                                 KeyAlgoLengthOrSpec keyAlgoLength,
                                                 NamedValues[] certAttributeArr,
                                                 NamedValues[] arAttributeArr,
                                                 CsrUsage csrUsage,
                                                 final User user,
                                                 final String secret,
                                                 final String requestorComment,
                                                 boolean isTosAgreed,
                                                 boolean tosAgreementRequired,
                                                 String tosAgreementLink,
                                                 boolean notifyRAOfficer)
        throws GeneralSecurityException, IOException, OperatorCreationException {

        KeyPair keypair = keyAlgoLength.generateKeyPair();


        X500NameBuilder namebuilder = new X500NameBuilder(X500Name.getDefaultStyle());
        List<GeneralName> gnList = new ArrayList<>();

        for(NamedValues nv: certAttributeArr) {

            String name = nv.getName();
            if( CertificateUtil.nameOIDMap.containsKey(name)) {
                ASN1ObjectIdentifier oid = CertificateUtil.nameOIDMap.get(name);
                for( TypedValue typedValue: nv.getValues()) {
                    if( typedValue.getValue() != null && !typedValue.getValue().isEmpty()) {
                        namebuilder.addRDN(oid, typedValue.getValue());
                    }
                }
            }else if( "SAN".equalsIgnoreCase(name)){

                for( TypedValue typedValue: nv.getValues()) {
                    String content = typedValue.getValue().trim();
                    if( content.isEmpty()) {
                        continue;
                    }

                    Integer sanType = GeneralName.dNSName;
                    if(CertificateUtil.nameGeneralNameMap.containsKey(typedValue.getType().toUpperCase() )) {
                        sanType = CertificateUtil.nameGeneralNameMap.get(typedValue.getType().toUpperCase());
                    }else {
                        log.warn("SAN certificate attribute has unknown type '{}'", typedValue.getType());
                    }
                    gnList.add(new GeneralName(sanType, content));
                }

            }else {
                log.warn("certificate attribute '{}' unknown ", name);
            }
        }

        PKCS10CertificationRequestBuilder p10Builder =
            new JcaPKCS10CertificationRequestBuilder(namebuilder.build(), keypair.getPublic());

        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        if( !gnList.isEmpty()) {
            GeneralName[] gns = new GeneralName[gnList.size()];
            gnList.toArray(gns);
            GeneralNames subjectAltName = new GeneralNames(gns);
            extensionsGenerator.addExtension(Extension.subjectAlternativeName, false, subjectAltName);
        }

        if(CsrUsage.TLS_SERVER.equals(csrUsage)) {
            extensionsGenerator.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            extensionsGenerator.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        } else if(CsrUsage.TLS_CLIENT.equals(csrUsage)){
            extensionsGenerator.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
            extensionsGenerator.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        } else if(CsrUsage.DOC_SIGNING.equals(csrUsage)){
            extensionsGenerator.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation));
        } else if(CsrUsage.CODE_SIGNING.equals(csrUsage)){
            extensionsGenerator.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
            extensionsGenerator.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_codeSigning));
        }else{
            log.warn("unexpected CsrUsage requested: '{}'", csrUsage);
        }
        p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());

        PrivateKey pk = keypair.getPrivate();
        JcaContentSignerBuilder csBuilder;
        String algo = SIGNATURE_ALG;
        if( pk instanceof ECKey){
            algo = EC_SIGNATURE_ALG;
            csBuilder = new JcaContentSignerBuilder(algo);
        }else if ( pk instanceof EdDSAPrivateKey) {
            algo = ED25519_SIGNATURE_ALG;
            csBuilder = new JcaContentSignerBuilder(algo);
        }else if ( pk instanceof BCDilithiumPrivateKey) {
            DilithiumParameterSpec parameterSpec = ((BCDilithiumPrivateKey) pk).getParameterSpec();
            KeyAlgoLengthOrSpec keyAlgoLengthOrSpec = KeyAlgoLengthOrSpec.from(parameterSpec);
            csBuilder = keyAlgoLengthOrSpec.buildJcaContentSignerBuilder();
        }else if ( pk instanceof BCFalconPrivateKey) {
            FalconParameterSpec parameterSpec = ((BCFalconPrivateKey) pk).getParameterSpec();
            KeyAlgoLengthOrSpec keyAlgoLengthOrSpec = KeyAlgoLengthOrSpec.from(parameterSpec);
            csBuilder = keyAlgoLengthOrSpec.buildJcaContentSignerBuilder();
        }else{
            csBuilder = new JcaContentSignerBuilder(algo);
        }



//            JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(algo);
        ContentSigner signer = csBuilder.build(pk);

        PKCS10CertificationRequest p10CR = p10Builder.build(signer);
        String csrAsPem = CryptoUtil.pkcs10RequestToPem(p10CR);
        log.debug("created csr on behalf of user '{}':\n{}", user.getLogin(), csrAsPem);

        Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10CR);

        Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow( p10ReqHolder);
        PkcsXXData p10ReqData = new PkcsXXData(p10ReqHolderShallow);

        CSR csr = startCertificateCreationProcess(csrAsPem,
            p10ReqData,
            user,
            requestorComment,
            arAttributeArr,
            optPipeline,
            isTosAgreed,
            tosAgreementRequired,
            tosAgreementLink,
            notifyRAOfficer);

        if( csr != null ){
            csr.setServersideKeyGeneration(true);
            csrRepository.save(csr);


            Instant validTo = Instant.now().plus(preferenceUtil.getServerSideKeyDeleteAfterDays(), ChronoUnit.DAYS);
            int leftUsages = preferenceUtil.getServerSideKeyDeleteAfterUses();

            certificateUtil.storePrivateKey(csr, keypair, leftUsages, validTo);

            protUtil.createProtectedContent(secret,
                ProtectedContentType.PASSWORD,
                ContentRelationType.CSR,
                csr.getId(),
                leftUsages,
                validTo);
        }

        return csr;

    }

    private CSR startCertificateCreationProcess(final String csrAsPem,
                                                PkcsXXData p10ReqData,
                                                final User user,
                                                final String requestorComment,
                                                NamedValues[] nvArr,
                                                Optional<Pipeline> optPipeline,
                                                boolean isTosAgreed,
                                                boolean tosAgreementRequired,
                                                String tosAgreementLink,
                                                boolean notifyRAOfficer)  {

        if( optPipeline.isPresent()) {

            Pipeline pipeline = optPipeline.get();
            if( pipeline.isActive()) {
                List<String> messageList = new ArrayList<>();

                CSR csr = null;
                try {
                    csr = cpUtil.buildCSR(csrAsPem,
                        user.getLogin(),
                        AuditService.AUDIT_WEB_CERTIFICATE_REQUESTED,
                        requestorComment,
                        pipeline,
                        null, // no ACMEOrder available
                        nvArr, messageList);
                } catch (KeyApplicableException e) {
                    // no extra handling, just go on ...
                }

                p10ReqData.setWarnings(messageList.toArray(new String[0]));

                if (csr != null) {
                    p10ReqData.setCreatedCSRId(csr.getId().toString());
                    csr.setTenant(user.getTenant());

                    if(isTosAgreed) {
                        csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TOS_AGREED, "true", false);
                        csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TOS_AGREEMENT_LINK,
                            tosAgreementLink,
                            false);
                    }else{
                        if(tosAgreementRequired){
                            log.warn("startCertificateCreationProcess: ToS agreement required, but not set!");
                            return null;
                        }
                    }

                    if (pipeline.isApprovalRequired()) {
                        log.debug("deferring certificate creation for csr #{}", csr.getId());
                        p10ReqData.setCsrPending(true);

                        if( notifyRAOfficer) {
                            asyncNotificationService.notifyRAOfficerOnRequestAsync(csr);
                        }

                    } else {
                        auditService.saveAuditTrace(auditService.createAuditTraceWebAutoAccepted(csr));
                        try {
                            cpUtil.processCertificateRequest(csr, user.getLogin(), AuditService.AUDIT_WEB_CERTIFICATE_CREATED, pipeline);
                        }catch (CAFailureException caFailureException){
                            log.info("certificate creation failed", caFailureException);
                            String msg = "certificate creation failed '"+caFailureException.getMessage()+"'!";
                            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr, msg));
                            log.info(msg);
                        }

                    }
                    return csr;
                } else {
                    log.warn("startCertificateCreationProcess: creation of CSR failed");
                }
            } else {
                log.warn("startCertificateCreationProcess: pipeline {} not active", pipeline.getName());
            }
        }else {
            log.warn("startCertificateCreationProcess: no processing pipeline defined");
        }
        return null;
    }

}
