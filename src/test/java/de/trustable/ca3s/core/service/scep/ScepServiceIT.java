package de.trustable.ca3s.core.service.scep;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentStatus;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.dto.*;
import de.trustable.ca3s.core.service.util.KeyUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.test.util.AcceptAllCertSelector;
import de.trustable.ca3s.core.test.util.AcceptAllVerifier;
import de.trustable.ca3s.core.test.util.X509Certificates;
import de.trustable.ca3s.core.web.html.NDESPasswordController;
import de.trustable.ca3s.core.web.rest.AccountResource;
import de.trustable.ca3s.core.web.rest.ApiTokenController;
import de.trustable.ca3s.core.web.rest.vm.TokenRequest;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import org.apache.commons.codec.binary.Base64;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ScepServiceIT {

    public static final Logger LOG = LogManager.getLogger(ScepServiceIT.class);

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    static KeyPair keyPair;
    static KeyPair ephemeralKeyPair;
    static X509Certificate ephemeralCert;
    static X500Principal enrollingPrincipal;
    static char[] pipelinePassword = PipelineTestConfiguration.SCEP_PASSWORD.toCharArray();

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    KeyUtil keyUtil;


    @Autowired
    ApiTokenController apiTokenController;

    @Autowired
    UserUtil userUtil;

    @Autowired
    UserService userService;

    @Autowired
    ProtectedContentUtil protectedContentUtil;

    @Autowired
    NDESPasswordController ndesPasswordController;

    @Autowired
    AccountResource accountResource;

    CertificateVerifier acceptAllVerifier = new AcceptAllVerifier();

    Client client;
    Client pkiclienClient;
    Client cgiPkiclienClient;
    Client client1CN;
    Client clientSR;


    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        JCAManager.getInstance();
    }


    @BeforeEach
    public void setUp() throws MalformedURLException, GeneralSecurityException {

        keyPair = keyUtil.createKeyPair();
        ephemeralKeyPair = keyUtil.createKeyPair();

        enrollingPrincipal = new X500Principal("CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE");
        ephemeralCert = X509Certificates.createEphemeral(enrollingPrincipal, ephemeralKeyPair);

        try {
            ptc.getInternalSCEPTestPipelineLaxRestrictions();
            ptc.getInternalSCEPTestPipelineCN1Restrictions();
            ptc.getInternalSCEPTestPipelineLaxRestrictionsShortRenewalPeriod();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        URL serverUrl = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP_REALM);
        LOG.debug("scep serverUrl : " + serverUrl);
        client = new Client(serverUrl, acceptAllVerifier);


        URL serverPkiclientUrl = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP_REALM + "/pkiclient.exe");
        LOG.debug("scep serverPkiclientUrl : " + serverPkiclientUrl);
        pkiclienClient = new Client(serverPkiclientUrl, acceptAllVerifier);

        URL serverCgiPkiclientUrl = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP_REALM + "/cgi-bin/pkiclient.exe");
        LOG.debug("scep serverCgiPkiclientUrl : " + serverCgiPkiclientUrl);
        cgiPkiclienClient = new Client(serverCgiPkiclientUrl, acceptAllVerifier);

        URL serverUrl1CN = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP1CN_REALM);
        LOG.debug("scep serverUrl1CN : " + serverUrl1CN);

        client1CN = new Client(serverUrl1CN, acceptAllVerifier);

        URL serverUrlSR = new URL("http://localhost:" + serverPort + "/scep/" + PipelineTestConfiguration.SCEP_REALM_SHORT_RENEWAL);
        LOG.debug("scep serverUrlSR : " + serverUrlSR);

        clientSR = new Client(serverUrlSR, acceptAllVerifier);

    }

    @Test
    public void testScepEnrolAndRenew() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        LOG.info("ephemeralCert : " + ephemeralCert);

        testClient(client);
        testClient(pkiclienClient);
        testClient(cgiPkiclienClient);

    }

    @Test
    @WithMockUser(username = "user1", authorities = {"USER", "ADMIN"})
    public void testScepEnrolAndRenewUsingSCEPToken() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        User user = userUtil.getUserByLogin("user1");

        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setValiditySeconds(3600);
        tokenRequest.setCredentialType(AccountCredentialsType.SCEP_TOKEN);
        ResponseEntity<?> responseEntity = apiTokenController.getToken(tokenRequest);
        TokenResponse tokenResponse = (TokenResponse) responseEntity.getBody();

        PasswordChangeDTO passwordChangeDto = new PasswordChangeDTO();
        passwordChangeDto.setCurrentPassword("user");
        passwordChangeDto.setCredentialUpdateType(CredentialUpdateType.SCEP_TOKEN);
        passwordChangeDto.setApiTokenValiditySeconds(3600);
        passwordChangeDto.setApiTokenValue(tokenResponse.getTokenValue());
        passwordChangeDto.setPipelineId(ptc.getInternalSCEPTestPipelineLaxRestrictions().getId());

        String jws = tokenResponse.getJws();
        char[] chars = jws.toCharArray();
        chars[55]++;
        passwordChangeDto.setTokenResponseJws(new String(chars));
        try {
            userService.changePassword(passwordChangeDto);
            Assertions.fail("Should have thrown an exception");
        } catch (Exception e) {
            // as expected
        }

        passwordChangeDto.setTokenResponseJws(jws);
        userService.changePassword(passwordChangeDto);

        for(AccountCredentialView credentialView: accountResource.tokenList()){
            if(credentialView.getRelationType().equals(AccountCredentialsType.SCEP_TOKEN)) {
                userService.approveCredential(user.getId(), AccountCredentialsType.SCEP_TOKEN, credentialView.getId());
            }
        }

        LOG.info("scep token {}", tokenResponse.getTokenValue());

        testClient(client, tokenResponse.getTokenValue().toCharArray());


    }



    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    public void testScepEnrolViaNDESPage() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        User user = userUtil.getUserByLogin("user1");

        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setValiditySeconds(3600);
        tokenRequest.setCredentialType(AccountCredentialsType.SCEP_TOKEN);
        ResponseEntity<?> responseEntity = apiTokenController.getToken(tokenRequest);
        TokenResponse tokenResponse = (TokenResponse) responseEntity.getBody();

        PasswordChangeDTO passwordChangeDto = new PasswordChangeDTO();
        passwordChangeDto.setCurrentPassword("user");
        passwordChangeDto.setCredentialUpdateType(CredentialUpdateType.SCEP_TOKEN);
        passwordChangeDto.setApiTokenValiditySeconds(3600);
        passwordChangeDto.setApiTokenValue(tokenResponse.getTokenValue());
        passwordChangeDto.setPipelineId(ptc.getInternalSCEPTestPipelineLaxRestrictions().getId());
        passwordChangeDto.setTokenResponseJws(tokenResponse.getJws());
        userService.changePassword(passwordChangeDto);

        // approve the freshly created token
        for(AccountCredentialView credentialView: accountResource.tokenList()){
            if(credentialView.getRelationType().equals(AccountCredentialsType.SCEP_TOKEN)) {
                userService.approveCredential(user.getId(), AccountCredentialsType.SCEP_TOKEN, credentialView.getId());
            }
        }

        Map<String, String> headers = new HashMap<>();

        ResponseEntity<String> response = ndesPasswordController.ndesPasswordHTML(
            ptc.getInternalSCEPTestPipelineLaxRestrictions().getUrlPart(),
            headers);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());


        final String auth = "user1:" + tokenResponse.getTokenValue();
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        final String authHeader = "Basic " + new String(encodedAuth);
        headers.put(HttpHeaders.AUTHORIZATION, authHeader);

        response = ndesPasswordController.ndesPasswordHTML(
            ptc.getInternalSCEPTestPipelineLaxRestrictions().getUrlPart(),
            headers);

        LOG.info( "NDES response:" + response.getBody());

        Pattern p = Pattern.compile(".*The enrollment challenge password is: <B>\\s*([a-zA-Z0-9]*)\\s*</B>.*");
        Matcher m = p.matcher(response.getBody());

        // if an occurrence if a pattern was found in a given string...
        Assertions.assertTrue(m.find());

        String ndesToken = m.group(1);
        LOG.info( "ndesToken:" + ndesToken);

        testClient(client, ndesToken.toCharArray());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    public void testScepEnrolAndRenewUsingNDESToken() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        User user = userUtil.getUserByLogin("user1");

        String tokenNDES = protectedContentUtil.buildNDESToken(user.getId());

        LOG.info("ndes token {}", tokenNDES);

        Instant validTo = Instant.now().plus(3600, ChronoUnit.SECONDS);
        protectedContentUtil.createProtectedContent(tokenNDES,
            ProtectedContentType.TOKEN,
            ContentRelationType.NDES_TOKEN,
            user.getId(),
            -1,
            validTo,
            ptc.getInternalSCEPTestPipelineLaxRestrictions(),
            ProtectedContentStatus.ACTIVE);

        testClient(client, tokenNDES.toCharArray());

    }

    private void testClient(Client client) throws GeneralSecurityException, IOException, ClientException, TransactionException {
        testClient(client, pipelinePassword);
    }
    private void testClient(Client client, char[] password) throws GeneralSecurityException, IOException, ClientException, TransactionException {

        keyPair = keyUtil.createKeyPair();

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            password);


        EnrollmentResponse resp = client.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
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

            KeyPair keyPairRenewFailure = keyUtil.createKeyPair();
            PKCS10CertificationRequest csrRenewFailure = CryptoUtil.getCsr(enrollingPrincipal,
                keyPairRenewFailure.getPublic(),
                keyPairRenewFailure.getPrivate(),
                null); // No password, we want to renew !

            LOG.debug("trying to renew certificate with improper key: " + issuedCert.toString());
            EnrollmentResponse respRenewFailure = client.enrol(issuedCert, keyPair.getPrivate(), csrRenewFailure);
            Assertions.assertNotNull(respRenewFailure);
            Assertions.assertFalse(respRenewFailure.isSuccess(), "Renewal expected to fail due to wrong key");

            KeyPair keyPairRenew = keyUtil.createKeyPair();
            PKCS10CertificationRequest csrFailingRenewal = CryptoUtil.getCsr(enrollingPrincipal,
                keyPairRenew.getPublic(),
                keyPair.getPrivate(),
                null); // No password, we want to renew !

            LOG.debug("trying to renew certificate : " + issuedCert);

            //renewal, sign with the old key
            EnrollmentResponse respRenew = client.enrol(issuedCert, keyPair.getPrivate(), csrFailingRenewal);
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
    public void testScepEnrolAndFailingRenewPeriod() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            pipelinePassword);

        EnrollmentResponse resp = clientSR.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
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

            KeyPair keyPairRenew = keyUtil.createKeyPair();
            PKCS10CertificationRequest csrRenew = CryptoUtil.getCsr(enrollingPrincipal,
                keyPairRenew.getPublic(),
                keyPairRenew.getPrivate(),
                null); // No password, we want to renew !

            LOG.debug("trying to renew certificate : " + issuedCert.toString());

            //renewal, sign with the old key
            EnrollmentResponse respRenew = clientSR.enrol(issuedCert, keyPair.getPrivate(), csrRenew);
            Assertions.assertNotNull(respRenew);
            if (respRenew.isSuccess()) {
                Assertions.fail("Renewal expected to fail due to shortreauthentication period");
            }
        }


    }

    @Test
    public void testScepEnrolFailedRestrictionCPresent() throws GeneralSecurityException, IOException, ClientException, TransactionException {

        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            pipelinePassword);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
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
            pipelinePassword);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
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

        EnrollmentResponse resp = client.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isFailure());

        Assertions.assertEquals(FailInfo.badRequest, resp.getFailInfo(), "Expecting FailInfo.badRequest");

    }

    @Test
    public void testScepEnrolRejected() throws GeneralSecurityException, IOException, ClientException, TransactionException {


        PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            pipelinePassword);

        EnrollmentResponse resp = client1CN.enrol(ephemeralCert, ephemeralKeyPair.getPrivate(), csr);
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
            pipelinePassword);

        EnrollmentResponse response = client.enrol(
            ephemeralCert,
            ephemeralKeyPair.getPrivate(),
            csr);

        Assertions.assertTrue(response.isSuccess());

        Iterator<? extends Certificate> certIt = response.getCertStore().getCertificates(null).iterator();
        X509Certificate issued = (X509Certificate) certIt.next();
        X509Certificate issuer = (X509Certificate) certIt.next();

        LOG.debug("issued Cert has serial {} and issuer {}", issued.getSerialNumber(),
            issuer.getSubjectX500Principal().getName());

        Certificate retrieved = client.getCertificate(ephemeralCert,
            ephemeralKeyPair.getPrivate(),
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
