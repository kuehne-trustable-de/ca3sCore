package de.trustable.ca3s.core.service.scep;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.*;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.jscep.client.verification.CertificateVerifier;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.TransactionException;
import org.jscep.transport.response.Capabilities;
import org.jscep.transport.response.Capability;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.test.util.AcceptAllCertSelector;
import de.trustable.ca3s.core.test.util.AcceptAllVerifier;
import de.trustable.ca3s.core.test.util.X509Certificates;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

//@Disabled("Integration test fails for unknown reason, running it as a separate client succeeds. Maybe a classloader issue? ")
@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ScepServiceIT {

    public static final Logger LOG = LogManager.getLogger(ScepServiceIT.class);

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    static KeyPair keyPair;
    static X509Certificate ephemeralCert;
    static X500Principal enrollingPrincipal;
    static char[] password = PipelineTestConfiguration.SCEP_PASSWORD.toCharArray();

    @Autowired
    PipelineTestConfiguration ptc;

    CertificateVerifier acceptAllVerifier = new AcceptAllVerifier();

    Client client;
    Client client1CN;


    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        JCAManager.getInstance();

        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        enrollingPrincipal = new X500Principal("CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE");
        ephemeralCert = X509Certificates.createEphemeral(enrollingPrincipal, keyPair);

    }


    @BeforeEach
    public void setUp() throws MalformedURLException {

        try {
            ptc.getInternalSCEPTestPipelineLaxRestrictions();
            ptc.getInternalSCEPTestPipelineCN1Restrictions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        URL serverUrl = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP_REALM);
        LOG.debug("scep serverUrl : " + serverUrl.toString());

        client = new Client(serverUrl, acceptAllVerifier);


        URL serverUrl1CN = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP1CN_REALM);
        LOG.debug("scep serverUrl1CN : " + serverUrl1CN.toString());

        client1CN = new Client(serverUrl1CN, acceptAllVerifier);

        System.out.println("########## url: " + serverUrl1CN);
    }

    @Test
    public void testScepEnrolAndRenew() throws GeneralSecurityException, IOException, ClientException, TransactionException {


        LOG.info("ephemeralCert : " + ephemeralCert);

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);

        EnrollmentResponse resp = client.enrol(ephemeralCert, keyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
//        LOG.info("FailInfo : " + resp.getFailInfo() );
        Assertions.assertFalse(resp.isFailure());
        if (resp.isSuccess()) {

            CertStore certStore = resp.getCertStore();
            Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());

            for (Certificate cert : collCerts) {
                LOG.debug("returned certificate : " + cert.toString());
            }

            X509CertSelector eeSelector = new X509CertSelector();
            eeSelector.setBasicConstraints(-2);
            Collection<? extends Certificate> collEECerts = certStore.getCertificates(eeSelector);
            X509Certificate issuedCert = (X509Certificate) collEECerts.iterator().next();

            KeyPair keyPairRenew = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PKCS10CertificationRequest csrRenew = CryptoUtil.getCsr(enrollingPrincipal,
                keyPairRenew.getPublic(),
                keyPairRenew.getPrivate(),
                null); // No password, we want to renew !

            LOG.debug("trying to renew certificate : " + issuedCert.toString());

            //renewal, sign with the old key
            EnrollmentResponse respRenew = client.enrol(issuedCert, keyPair.getPrivate(), csrRenew);
            Assertions.assertNotNull(respRenew);
            if (respRenew.isSuccess()) {

                CertStore certStoreRenew = respRenew.getCertStore();
                Collection<? extends Certificate> collCertsRenew = certStoreRenew.getCertificates(eeSelector);

                for (Certificate cert : collCertsRenew) {
                    LOG.debug("returned renewed certificate : " + cert.toString());
                }

            } else {
                Assertions.fail("Renewal failed");
            }
        }


    }

    @Test
    public void testScepEnrolFailedRestrictionCPresent() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, keyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isFailure());

        Assertions.assertEquals(FailInfo.badRequest, resp.getFailInfo(), "Expecting FailInfo.badRequest");

    }

    @Test
    public void testScepEnrolFailedRestriction2CN() throws GeneralSecurityException, IOException, ClientException, TransactionException {


        X500Principal doubleCNPrincipal = new X500Principal("CN=FirstCN, CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE");
        LOG.info("doubleCNPrincipal : " + doubleCNPrincipal);

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(doubleCNPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, keyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isFailure());

        Assertions.assertEquals(FailInfo.badRequest, resp.getFailInfo(), "Expecting FailInfo.badRequest");


    }

    @Test
    public void testScepEnrolInvalidPassword() throws GeneralSecurityException, IOException, ClientException, TransactionException {


        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            "wrong_password".toCharArray());

        EnrollmentResponse resp = client.enrol(ephemeralCert, keyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isFailure());

        Assertions.assertEquals(FailInfo.badRequest, resp.getFailInfo(), "Expecting FailInfo.badRequest");

    }

    @Test
    public void testScepEnrolRejected() throws GeneralSecurityException, IOException, ClientException, TransactionException {


        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, keyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isFailure());

        Assertions.assertEquals(FailInfo.badRequest, resp.getFailInfo(), "Expecting FailInfo.badRequest");

    }


    @Test
    public void testScepCapabilities() throws MalformedURLException {

        // Invoke operations on the client.
        Capabilities caps = client.getCaCapabilities();
        Assertions.assertNotNull(caps);
        LOG.debug("caps : " + caps);
        Assertions.assertTrue(caps.contains(Capability.SHA_256), "Expecting support for SHA-256");
    }

    @Test
    public void testGetCaCertificate() throws MalformedURLException, ClientException, CertStoreException {

        CertStore certStore = client.getCaCertificate();

        Assertions.assertNotNull(certStore);
        Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());

        Assertions.assertTrue(!collCerts.isEmpty(), "Expecting getCaCertificate to return a certificate");
        Certificate caCert = collCerts.iterator().next();
        LOG.debug("caCert : " + caCert.toString());
        LOG.debug("collCerts.size() : " + collCerts.size());
    }

    @Test
    public void testEnrollThenGetCertificate() throws ClientException, GeneralSecurityException, IOException, TransactionException {

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);

        EnrollmentResponse response = client.enrol(
            ephemeralCert,
            keyPair.getPrivate(),
            csr);

        Assertions.assertTrue(response.isSuccess());

        Iterator<? extends Certificate> certIt = response.getCertStore().getCertificates(null).iterator();
        X509Certificate issued = (X509Certificate) certIt.next();
        X509Certificate issuer = (X509Certificate) certIt.next();

        LOG.debug("issued Cert has serial {} and issuer {}", issued.getSerialNumber(),
            issuer.getSubjectX500Principal().getName());

        Certificate retrieved = client.getCertificate(issuer,
            keyPair.getPrivate(),
            issued.getSerialNumber()
        ).getCertificates(null).iterator().next();

        Assertions.assertEquals(issued, retrieved);
    }

    @Test
    public void testGetRolloverCertificate() throws MalformedURLException, ClientException, CertStoreException {

        Capabilities caps = client.getCaCapabilities();
        boolean hasNextCA = caps.contains(Capability.GET_NEXT_CA_CERT);

        try {
            CertStore certStore = client.getRolloverCertificate();

            Assertions.assertTrue(hasNextCA, "only when the capabilities contain the GET_NEXT_CA_CERT bit ...");

            Assertions.assertNotNull(certStore);
            Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());

            Assertions.assertTrue(!collCerts.isEmpty(), "Expecting getRolloverCertificate to return a certificate");
            Certificate caCert = collCerts.iterator().next();
            LOG.debug("caCert : " + caCert.toString());
            LOG.debug("collCerts.size() : " + collCerts.size());

        } catch (UnsupportedOperationException uoe) {
            Assertions.assertFalse(hasNextCA, "expected when the capabilities do NOT contain the GET_NEXT_CA_CERT bit ...");
        }
    }


}
