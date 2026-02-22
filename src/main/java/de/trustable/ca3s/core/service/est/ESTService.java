package de.trustable.ca3s.core.service.est;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.exception.*;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.exception.InvalidCredentialException;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class ESTService {
    public static final String CREATION_OF_CERTIFICATE_BY_EST_REQUEST_FAILED = "creation of certificate by EST request failed ";
    private final Logger LOG = LoggerFactory.getLogger(ESTService.class);

    private final CertificateRepository certificateRepository;

    private final PipelineRepository pipelineRepository;
    private final CryptoUtil cryptoUtil;
    private final CSRUtil csrUtil;
    private final PipelineUtil pipelineUtil;
    private final CertificateProcessingUtil cpUtil;
    private final AuditService auditService;
    private final CertificateUtil certificateUtil;
    private final ProtectedContentRepository protectedContentRepository;
    private final ProtectedContentUtil protectedContentUtil;


    public ESTService(CertificateRepository certificateRepository,
                      PipelineRepository pipelineRepository,
                      CryptoUtil cryptoUtil, CSRUtil csrUtil,
                      PipelineUtil pipelineUtil,
                      CertificateProcessingUtil cpUtil,
                      AuditService auditService,
                      CertificateUtil certificateUtil, ProtectedContentRepository protectedContentRepository, ProtectedContentUtil protectedContentUtil) {
        this.certificateRepository = certificateRepository;
        this.pipelineRepository = pipelineRepository;
        this.cryptoUtil = cryptoUtil;
        this.csrUtil = csrUtil;
        this.pipelineUtil = pipelineUtil;
        this.cpUtil = cpUtil;
        this.auditService = auditService;
        this.certificateUtil = certificateUtil;
        this.protectedContentRepository = protectedContentRepository;
        this.protectedContentUtil = protectedContentUtil;
    }


    public List<X509Certificate> getESTRootCertificates(){
        List<X509Certificate> x509CertificateList = new ArrayList<>();

        List<Certificate> certList = certificateRepository.findByAttributeValue( CertificateAttribute.ATTRIBUTE_CA, "true");
        for(Certificate certificate: certList) {
            try {
                X509Certificate x509Cert = CryptoService.convertPemToCertificate(certificate.getContent());
                x509CertificateList.add(x509Cert);
            } catch (GeneralSecurityException e) {
                LOG.info("problem handling internal root certificate: {}", e.getMessage());
            }
        }
        return x509CertificateList;
    }

    public ResponseEntity<?> enroll(HttpServletRequest request, Pipeline pipeline, byte[] csr) {

        final String authorization = request.getHeader("Authorization");
        if( authorization == null || authorization.isEmpty()){
            LOG.info("No 'Authorization' header provided");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=" + pipeline.getUrlPart());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .headers(httpHeaders)
                .build();
        }else {
            String[] authParts = parseBasicAuth(authorization);
            if (authParts == null) {
                throw new UserCredentialsMissingException("no authentication provided");
            }
            LOG.debug("basic auth: {}:{}", authParts[0], "*****");
            if( !isPasswordValid(pipeline, authParts[1])){
                throw new InvalidCredentialException("no valid authentication provided");
            }
        }
        return buildCertificateResponse(pipeline, csr);
    }


    public ResponseEntity<?> reenroll(HttpServletRequest request, Pipeline pipeline, byte[] csr) {

        Certificate authenticatingCertificate = findClientCertificate(request);
        if( authenticatingCertificate == null ){
            throw new UserCredentialsMissingException("client cert problem");
        }

        try {
            Pkcs10RequestHolder p10Holder = csrUtil.parseCSR(csr);

            Set<GeneralName> generalNameSetCSR = csrUtil.getSANList(p10Holder);

            for (RDN rdn : p10Holder.getSubjectRDNs()) {
                for (AttributeTypeAndValue atv : rdn.getTypesAndValues()) {
                    if (BCStyle.CN.equals(atv.getType())) {
                        String cnValue = atv.getValue().toString();
                        LOG.debug("cn found in CSR: " + cnValue);
                        generalNameSetCSR.add(new GeneralName(GeneralName.dNSName, cnValue));
                    }
                }
            }

            boolean found = true;
            List<String> vsanList = certificateUtil.getCertAttributes(authenticatingCertificate,CsrAttribute.ATTRIBUTE_TYPED_VSAN);
            for(GeneralName generalName: generalNameSetCSR){
                String typedSan = CertificateUtil.getTypedSAN(generalName);
                if( vsanList.contains(typedSan)){
                    LOG.debug("typedSan '{}' found in CSR: ", typedSan);
                }else{
                    LOG.info("typedSan '{}' not found in CSR: ", typedSan);
                    found = false;
                }
            }

            if( !found ){
                throw new CsrCertificateAuthorizationMismatch("provided client certificate does not authorize CSR");
            }

        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidCsrException(e.getMessage());
        }

        return buildCertificateResponse(pipeline, csr);
    }

    public ResponseEntity<?> buildPKCS7CertsResponse(List<X509Certificate> x509CertificateList, boolean bCertsOnly) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Transfer-Encoding", "base64");
        if(bCertsOnly) {
            httpHeaders.add("Content-Type", "application/pkcs7-mime; smime-type=certs-only");
        }else {
            httpHeaders.add("Content-Type", "application/pkcs7-mime");
        }

        try {
            ASN1EncodableVector certsVector = new ASN1EncodableVector();
            for (X509Certificate x509Certificate : x509CertificateList) {
                LOG.debug("adding certificate {} ", x509Certificate.getSubjectX500Principal().getName());
                certsVector.add(org.bouncycastle.asn1.x509.Certificate.getInstance(x509Certificate.getEncoded()));
            }
            ASN1Set certSet = new DERSet(certsVector);

            SignedData sd = new SignedData(new DERSet(),
                new ContentInfo(CMSObjectIdentifiers.data, null),
                certSet,
                new DERSet(),
                new DERSet());

            ContentInfo ci = new ContentInfo(CMSObjectIdentifiers.signedData, sd);
            Base64 base64 = new Base64(78);
            byte[] base64Response = base64.encode(ci.getEncoded("DER"));
            LOG.debug("base64 response: {}", new String(base64Response));
            return ResponseEntity.ok()
                .headers(httpHeaders).body(base64Response);
        } catch (IOException | CertificateEncodingException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * get the pipeline for a given label
     *
     * @param label
     * @return
     */
    public Pipeline getPipelineForLabel(final String label) {

        List<Pipeline> pipelineList = pipelineRepository.findActiveByTypeUrl(PipelineType.EST, label);

        if(pipelineList.isEmpty()) {
            LOG.warn("label {} is not known", label);
            throw new LabelDoesNotExistException("label '"+label+"' not found");
        }

        if(pipelineList.size() > 1) {
            LOG.warn("misconfiguration for label '{}', multiple configurations handling this label", label);
            throw new LabelNotUniqueException("label '"+label+"' ambigous");
        }

        return pipelineList.get(0);
    }

    private Certificate startCertificateCreationProcess(final byte[] csrBase64, Pipeline pipeline){

        Pkcs10RequestHolder p10Holder;
        String csrAsPEM;
        try {
            Base64 base64 = new Base64(78);
            p10Holder = cryptoUtil.parseCertificateRequest(base64.decode(csrBase64));
            csrAsPEM = CryptoUtil.pkcs10RequestToPem(p10Holder.getP10Req());
        } catch (IOException | GeneralSecurityException e) {
            throw new InvalidCsrException(e.getMessage());
        }

        if( !pipelineUtil.isPipelineRestrictionsResolved(pipeline, p10Holder, new ArrayList<>())){
            throw new PipelineRestrictionViolatedException("request restriction mismatch");
        }

        String pipelineName = ( pipeline == null) ? "NoPipeline":pipeline.getName();
        String requestorName = "EST client";
        LOG.debug("doEnrol: processing request by {} using pipeline {}", requestorName,pipelineName);

        CSR csr = cpUtil.buildCSR(csrAsPEM, requestorName, AuditService.AUDIT_EST_CERTIFICATE_REQUESTED, "", pipeline );

        if( csr == null) {
            String msg = CREATION_OF_CERTIFICATE_BY_EST_REQUEST_FAILED;
            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr, msg));
            LOG.info(msg);
            return null;
        }

        Certificate cert = null;
        try {
            cert = cpUtil.processCertificateRequest(csr, requestorName,  AuditService.AUDIT_ESTP_CERTIFICATE_CREATED, pipeline );
        }catch (CAFailureException caFailureException){
            LOG.info("certificate creation failed", caFailureException);
        }

        if( cert == null) {
            String msg = CREATION_OF_CERTIFICATE_BY_EST_REQUEST_FAILED;
            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr, msg));
            LOG.info(msg);
        }else {
            LOG.debug("new certificate id '{}' for EST request", cert.getId());
        }
        return cert;
    }

    String[] parseBasicAuth(final String authorization) {
        //@ToDo look for functionality in a lib
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = java.util.Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            return credentials.split(":", 2);
        }
        return null;
    }

    private Certificate findClientCertificate(HttpServletRequest request) {

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certs == null || certs.length == 0) {
            // fallback for Spring boot 2.*
            certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        }
        if (certs == null || certs.length == 0) {
            return null;
        }
        try {
            LOG.info("user authenticated by client cert with subject '{}' / swrial {}",
                certs[0].getSubjectX500Principal().getName(),
                certs[0].getSerialNumber());
            JcaX509ExtensionUtils util = new JcaX509ExtensionUtils();
            SubjectKeyIdentifier ski = util.createSubjectKeyIdentifier(certs[0].getPublicKey());
            String b46Ski = Base64.encodeBase64String(ski.getKeyIdentifier());

            List<Certificate> certificateList =  certificateRepository.findActiveBySKI(b46Ski);
            if( certificateList.isEmpty()){
                LOG.info("pre-issued certificate unknown");
                return null;
            }
            return certificateList.get(0);
        } catch( GeneralSecurityException e) {
            LOG.info("problem processing client certificate", e);
        }

        return null;
    }

    private ResponseEntity<?> buildCertificateResponse(Pipeline pipeline, byte[] csr) {
        Certificate certificate = startCertificateCreationProcess(csr, pipeline);
        try {
            return buildPKCS7CertsResponse(
                Collections.singletonList(certificateUtil.getX509CertificateChain(certificate)[0]), true);
        } catch (GeneralSecurityException e) {
            throw new CertificateProcessingException(e.getMessage());
        }
    }

    boolean isPasswordValid(final Pipeline pipeline, final String password) {

        if( password == null || password.isEmpty()) {
            LOG.warn("password not present in EST request / is empty!");
            return false;
        }

        List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(ProtectedContentType.PASSWORD, ContentRelationType.EST_PW,pipeline.getId());
        for(ProtectedContent pc: listPC){
            String expectedPassword = protectedContentUtil.unprotectString(pc.getContentBase64()).trim();
            if( password.trim().equals(expectedPassword)) {
                LOG.debug("Protected Content found matching EST password");
                return true; // the only successful exit !!
            } else {

                LOG.debug("Protected Content password does not match EST password '{}' != '{}'",
                    truncatePassword(expectedPassword),
                    truncatePassword(password));
            }
        }

        LOG.warn("no (active) password present in pipeline '" + pipeline.getName() + "' !");
        return false;
    }

    public String truncatePassword(final String password){
        if( password == null || (password.length() < 5)){
            return "******r3t";
        }
        return password.substring(password.length() - 4);
    }
}
