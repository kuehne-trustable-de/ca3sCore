package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.PKCSDataType;
import de.trustable.ca3s.core.service.dto.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.CreationMode;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import de.trustable.util.PKILevel;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.security.auth.x500.X500Principal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Optional;

import de.trustable.ca3s.core.helper.AuthenticationHelper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class ContentUploadProcessorRequestMatchIT {

    @Autowired
    CryptoUtil cryptoUtil;

    @Autowired
    CSRRepository csrRepository;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    PipelineTestConfiguration ptc;

    Pipeline pipelineCsrRACheck;
    Pipeline pipelineUpload;

    @BeforeAll
    public static void setUpBeforeClass() {
        JCAManager.getInstance();
    }

    @BeforeEach
    public void init() throws InterruptedException {

        ptc.getInternalWebDirectTestPipeline();
        pipelineCsrRACheck = ptc.getInternalWebRACheckTestPipeline();
        pipelineUpload = ptc.getUploadTestPipeline();
    }

    @Autowired
    private ContentUploadProcessor contentUploadProcessor;

    @Autowired
    private CSRContentProcessor csrContentProcessor;


    /**
     * Check the describe and the upload interface for both CSR and certificate
     * ensure the related CSR and certificate (with the same key pair) get detected
     *
     * @throws Exception
     */
    @Test
    public void checkCsrAndCertificateUploadWithoutAssignement() throws Exception {

        AuthenticationHelper.setAuthenticationUser();

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        String subject ="CN=CsrAndCertRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);
        X500Name x500NameSubject = new X500Name(subject);

        // create a CSR ..
        String req = CryptoUtil.getCsrAsPEM(subjectPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            null);

        // and a certificate with the same keypair
        X509Certificate x509Cert = cryptoUtil.issueCertificate(
            x500NameSubject,
            keyPair,
            x500NameSubject,
            keyPair.getPublic().getEncoded(),
            Calendar.DATE, 10, PKILevel.ROOT);


        PkcsXXData pkcsXXDescribeCSR = describeContent(req, pipelineCsrRACheck);
        checkPkcsxxForCSR(pkcsXXDescribeCSR, false);

        PkcsXXData pkcsXXDescribeCertificate = describeContent(cryptoUtil.x509CertToPem(x509Cert), pipelineCsrRACheck);
        assertNotNull(pkcsXXDescribeCertificate);
        assertNull(pkcsXXDescribeCertificate.getCertificateView());
        checkPkcsxxForCert(pkcsXXDescribeCertificate, false);

        PkcsXXData pkcsXXData = uploadCSR(req, pipelineCsrRACheck);
        checkPkcsxxForCSR(pkcsXXData, true);

        AuthenticationHelper.setAuthenticationAdmin();

        PkcsXXData pkcsXXDataCertificate = uploadCertificate(cryptoUtil.x509CertToPem(x509Cert),pipelineUpload, null);
        assertNotNull(pkcsXXDataCertificate);
        assertNotNull(pkcsXXDataCertificate.getCertificateView());

        checkPkcsxxForCert(pkcsXXDataCertificate, true);

        assertNotNull(pkcsXXDataCertificate.getCreatedCertificateId());
        assertNull(pkcsXXDataCertificate.getCertificateView().getCsrId());
    }

    @Test
    public void checkCsrAndCertificateUploadWithAssignement() throws Exception {

        AuthenticationHelper.setAuthenticationUser();

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        String subject ="CN=CsrAndCertRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);
        X500Name x500NameSubject = new X500Name(subject);

        // create a CSR ..
        String req = CryptoUtil.getCsrAsPEM(subjectPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            null);

        // and a certificate with the same keypair
        X509Certificate x509Cert = cryptoUtil.issueCertificate(
            x500NameSubject,
            keyPair,
            x500NameSubject,
            keyPair.getPublic().getEncoded(),
            Calendar.DATE, 10, PKILevel.ROOT);

        PkcsXXData pkcsXXData = uploadCSR(req, pipelineCsrRACheck);
        checkPkcsxxForCSR(pkcsXXData, true);

        AuthenticationHelper.setAuthenticationAdmin();

        PkcsXXData pkcsXXDataCertificate = uploadCertificate(cryptoUtil.x509CertToPem(x509Cert),
            pipelineUpload,
            Long.parseLong(pkcsXXData.getCreatedCSRId()));

        assertNotNull(pkcsXXDataCertificate);
        assertNotNull(pkcsXXDataCertificate.getCertificateView());

        checkPkcsxxForCert(pkcsXXDataCertificate, true);

        assertNotNull(pkcsXXDataCertificate.getCreatedCertificateId());
        assertNull(pkcsXXDataCertificate.getCertificateView().getCsrId());
    }

    PkcsXXData describeContent(String csr, Pipeline pipeline) {

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setCreationMode(CreationMode.CSR_AVAILABLE);

        uploaded.setPipelineId(pipeline.getId());

        uploaded.setContent(csr);

        ResponseEntity<PkcsXXData> response = csrContentProcessor.describeContent(uploaded);

        assertEquals(200, response.getStatusCodeValue());

        return response.getBody();
    }

    PkcsXXData uploadCSR(String csr, Pipeline pipeline) {

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setCreationMode(CreationMode.CSR_AVAILABLE);

        uploaded.setPipelineId(pipeline.getId());

        uploaded.setContent(csr);

        ResponseEntity<PkcsXXData> response = contentUploadProcessor.uploadContent(uploaded);
        assertEquals(200, response.getStatusCodeValue());

        return response.getBody();
    }

    PkcsXXData uploadCertificate(String certificate, Pipeline pipeline, Long relatedCSRId) {

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setCreationMode(CreationMode.CSR_AVAILABLE);

        uploaded.setPipelineId(pipeline.getId());

        uploaded.setContent(certificate);

        uploaded.setRelatedCSRId(relatedCSRId);

        ResponseEntity<PkcsXXData> response = contentUploadProcessor.uploadContent(uploaded);
        assertEquals(201, response.getStatusCodeValue());

        return response.getBody();
    }


    void checkPkcsxxForCSR(PkcsXXData pkcsXXData, boolean expectCsrCreated ){
        assertNotNull(pkcsXXData);
        assertNotNull(pkcsXXData.getP10Holder());

        assertNotNull(pkcsXXData.getDataType());
        assertEquals(PKCSDataType.CSR, pkcsXXData.getDataType());

        if(expectCsrCreated) {
            Optional<CSR> optCsr = csrRepository.findById(Long.parseLong(pkcsXXData.getCreatedCSRId()));
            assertTrue(optCsr.isPresent(), "expecting the new CSR to be created");
        }

        if( pkcsXXData.getWarnings() != null) {
            assertEquals(0, pkcsXXData.getWarnings().length);
        }
        assertFalse(pkcsXXData.isCsrPublicKeyPresentInDB());
    }

    void checkPkcsxxForCert(PkcsXXData pkcsXXData, boolean expectedRelatedCsr){
        assertNotNull(pkcsXXData);

        assertNotNull(pkcsXXData.getDataType());
        assertEquals(PKCSDataType.X509_CERTIFICATE, pkcsXXData.getDataType());

        if( expectedRelatedCsr) {
            Optional<Certificate> optCert = certificateRepository.findById(pkcsXXData.getCertificateView().getId());
            assertTrue(optCert.isPresent(), "expecting the certificate to be created");
        }

        if( pkcsXXData.getWarnings() != null) {
            assertEquals(0, pkcsXXData.getWarnings().length);
        }
        assertFalse(pkcsXXData.isCsrPublicKeyPresentInDB());
    }

}
