package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.ExternalProcessITBase;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.dto.AccountCredentialsType;
import de.trustable.ca3s.core.service.dto.CredentialUpdateType;
import de.trustable.ca3s.core.service.dto.PasswordChangeDTO;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.test.util.AccessPortTestManager;
import de.trustable.ca3s.core.web.rest.ApiTokenController;
import de.trustable.ca3s.core.web.rest.vm.TokenRequest;
import de.trustable.ca3s.core.web.rest.vm.TokenResponse;
import de.trustable.util.CryptoUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.UUID;


@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientLegoIT extends ExternalProcessITBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLegoIT.class);

    public final String LEGO_EXE = "/home/akuehne/Downloads/lego_v5.2.2_linux_amd64/lego";
    String hostname;

    //    @TempDir
    Path directory = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());

    final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
    final String ACME_EAB_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_EAB_REALM + "/directory";
    final String ACME_ALPN_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM_ALPN_DOMAIN_REUSE + "/directory";

    String dirUrl;
    String dirUrlEAB;
    String dirUrlAlpn;

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    ApiTokenController apiTokenController;

    @Autowired
    private CertificateUtil certificateUtil;

    @Autowired
    UserUtil userUtil;

    @Autowired
    UserService userService;

    static CertificateFactory factory;

    static AccessPortTestManager accessPortTestManager = new AccessPortTestManager();

    static int alpnPort;

    @BeforeAll
    static void setUp() throws CertificateException {
        factory = CertificateFactory.getInstance("X.509");
        accessPortTestManager.setUpEnvironmentTLS();

        alpnPort = SocketUtils.findAvailableTcpPort(45000);
        System.setProperty("ca3s.acme.alpn.ports", "" + alpnPort);

    }

    @AfterAll
    static void tearDown() {

        accessPortTestManager.tearDownEnvironment();
        System.clearProperty("ca3s.acme.alpn.ports");

    }


    @BeforeEach
    void init() throws IOException {

        ptc.getInternalACMETestPipelineLaxRestrictions();

        hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
        dirUrl = "https://localhost:" + accessPortTestManager.getAcmeAccessPort() + ACME_PATH_PART;
        dirUrlEAB = "https://localhost:" + accessPortTestManager.getAcmeAccessPort() + ACME_EAB_PATH_PART;
        dirUrlAlpn = "https://localhost:" + accessPortTestManager.getAcmeAccessPort() + ACME_ALPN_PATH_PART;

        ptc.getInternalACMETestPipelineLaxRestrictions();
        ptc.getInternalACMETestPipelineEabRestrictions();
        ptc.getInternalACMETestPipelineALPNLaxDomainReuseRestrictions();

        prefTC.getTestUserPreference();
    }


    @Test
    public void legoCreateAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        if (!isInstalled(LEGO_EXE)) {
            LOG.info("'lego' missing, please install and rerun.");
            return;
        }


        Path webrootFolder = directory.resolve("webroot");
        Path configFolder = directory.resolve("config");
        Path workFolder = directory.resolve("work");
        Path logFolder = directory.resolve("log");

        Assertions.assertTrue(webrootFolder.toFile().mkdirs(), "expecting successful creation of webroot");
        Assertions.assertTrue(configFolder.toFile().mkdirs(), "expecting successful creation of config folder");
        Assertions.assertTrue(workFolder.toFile().mkdirs(), "expecting successful creation of work folder");
        Assertions.assertTrue(logFolder.toFile().mkdirs(), "expecting successful creation of log folder");

        ProcessBuilder builderCreate = new ProcessBuilder();

        builderCreate.command(LEGO_EXE,
            "run",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrl,
            "--tls-skip-verify",
            "--domains", hostname,
            "--accept-tos",
            "--email", "foo@foo.de",
            "--http",
            "--http.address", ":" + prefTC.getHttpChallengePort(),
            "--key-type", "rsa4096");

        int exitCodeCreate = executeExternalProcess(builderCreate);
        Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");

        File crtFile = new File(configFolder.toFile(), "certificates" + File.separator + hostname + ".crt");
        LOG.info("crtFile : {}", crtFile.getAbsolutePath());
        Assertions.assertTrue(crtFile.exists(), "expecting certificate file exists");

        X509Certificate x509Certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(crtFile));
        Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
        Assertions.assertEquals(hostname, cert.getSans());
        Assertions.assertTrue(cert.isActive(), "freshly created certificate expected to be active");

        ProcessBuilder builderRevoke = new ProcessBuilder();

        builderRevoke.command(LEGO_EXE,
            "certificates", "revoke",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrl,
            "--tls-skip-verify",
            "--cert.name", hostname,
            "--reason", "4",
            "--email", "foo@foo.de");


        int exitCodeRevoke = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(0, exitCodeRevoke, "expects an exit code == 0");

        Optional<Certificate> certRevokedOpt = certificateUtil.findCertificateById(cert.getId());
        Assertions.assertTrue(certRevokedOpt.isPresent(), "Expecting to find certificate");
        Assertions.assertTrue(certRevokedOpt.get().isRevoked(), "Expected status change to revoked");

        int exitCodeRevoke2 = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(1, exitCodeRevoke2, "trying to revoke an already revoked cert. Expecting exit code == 1");

    }

    @Test
    public void legoCreateAccountAndOrderCertificatebyALPN() throws IOException, GeneralSecurityException {

        if (!isInstalled(LEGO_EXE)) {
            LOG.info("'lego' missing, please install and rerun.");
            return;
        }


        Path webrootFolder = directory.resolve("webroot");
        Path configFolder = directory.resolve("config");
        Path workFolder = directory.resolve("work");
        Path logFolder = directory.resolve("log");

        Assertions.assertTrue(webrootFolder.toFile().mkdirs(), "expecting successful creation of webroot");
        Assertions.assertTrue(configFolder.toFile().mkdirs(), "expecting successful creation of config folder");
        Assertions.assertTrue(workFolder.toFile().mkdirs(), "expecting successful creation of work folder");
        Assertions.assertTrue(logFolder.toFile().mkdirs(), "expecting successful creation of log folder");

        ProcessBuilder builderCreate = new ProcessBuilder();

        builderCreate.command(LEGO_EXE,
            "run",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrlAlpn,
            "--tls-skip-verify",
            "--domains", hostname,
            "--accept-tos",
            "--email", "foo@foo.de",
            "--tls",
            "--tls.address", ":" + alpnPort,
            "--key-type", "rsa4096");

        int exitCodeCreate = executeExternalProcess(builderCreate);
        Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");

        File crtFile = new File(configFolder.toFile(), "certificates" + File.separator + hostname + ".crt");
        LOG.info("crtFile : {}", crtFile.getAbsolutePath());
        Assertions.assertTrue(crtFile.exists(), "expecting certificate file exists");

        X509Certificate x509Certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(crtFile));
        Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
        Assertions.assertEquals(hostname, cert.getSans());
        Assertions.assertTrue(cert.isActive(), "freshly created certificate expected to be active");

        ProcessBuilder builderRevoke = new ProcessBuilder();

        builderRevoke.command(LEGO_EXE,
            "certificates", "revoke",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrlAlpn,
            "--tls-skip-verify",
            "--cert.name", hostname,
            "--reason", "4",
            "--email", "foo@foo.de");


        int exitCodeRevoke = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(0, exitCodeRevoke, "expects an exit code == 0");

        Optional<Certificate> certRevokedOpt = certificateUtil.findCertificateById(cert.getId());
        Assertions.assertTrue(certRevokedOpt.isPresent(), "Expecting to find certificate");
        Assertions.assertTrue(certRevokedOpt.get().isRevoked(), "Expected status change to revoked");

        int exitCodeRevoke2 = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(1, exitCodeRevoke2, "trying to revoke an already revoked cert. Expecting exit code == 1");

    }


    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    public void legoCreateEabAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        if (!isInstalled(LEGO_EXE)) {
            LOG.info("'lego' missing, please install and rerun.");
            return;
        }

        User user = userUtil.getUserByLogin("user1");
        Assertions.assertNotNull(user, "user should not be null");

        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setValiditySeconds(3600);
        tokenRequest.setCredentialType(AccountCredentialsType.EAB_PASSWORD);
        ResponseEntity<?> responseEntity = apiTokenController.getToken(tokenRequest);
        TokenResponse tokenResponse = (TokenResponse) responseEntity.getBody();

        PasswordChangeDTO passwordChangeDto = new PasswordChangeDTO();
        passwordChangeDto.setCurrentPassword("user");
        passwordChangeDto.setCredentialUpdateType(CredentialUpdateType.EAB_PASSWORD);
        passwordChangeDto.setApiTokenValiditySeconds(3600);
        passwordChangeDto.setApiTokenValue(tokenResponse.getTokenValue());
        passwordChangeDto.setEabKid(tokenResponse.getEabKid());
        userService.changePassword(passwordChangeDto);

        LOG.info("eab kid {}, macKey {}", tokenResponse.getEabKid(), tokenResponse.getTokenValue());


        ProcessBuilder builderExecutabelExixts = new ProcessBuilder();
        builderExecutabelExixts.command("which", "certbot");
        if (executeExternalProcess(builderExecutabelExixts) != 0) {
            LOG.info("'certbot' missing, please install and rerun.");
            return;
        }

        Path webrootFolder = directory.resolve("webroot");
        Path configFolder = directory.resolve("config");
        Path workFolder = directory.resolve("work");
        Path logFolder = directory.resolve("log");

        Path configInvalidEABFolder = directory.resolve("configInvalidEAB");

        webrootFolder.toFile().mkdirs();
        configFolder.toFile().mkdirs();
        workFolder.toFile().mkdirs();
        logFolder.toFile().mkdirs();

        configInvalidEABFolder.toFile().mkdirs();

        ProcessBuilder builderCreateInvalidEAB = new ProcessBuilder();
        builderCreateInvalidEAB.command(LEGO_EXE,
            "run",
            "--path", configInvalidEABFolder.toFile().getAbsolutePath(),
            "--server", dirUrlEAB,
            "--tls-skip-verify",
            "--domains", hostname,
            "--accept-tos",
            "--email", "foo@foo.de",
            "--eab",
            "--eab.kid", "unknown-kid.99",
            "--eab.hmac", tokenResponse.getTokenValue(),
            "--http",
            "--http.address", ":" + prefTC.getHttpChallengePort(),
            "--key-type", "rsa4096");

        int exitCodeCreateInvalidEAB = executeExternalProcess(builderCreateInvalidEAB);
        Assertions.assertEquals( 1, exitCodeCreateInvalidEAB, "expects an exit code == 1");

        ProcessBuilder builderCreate = new ProcessBuilder();
        builderCreate.command(LEGO_EXE,
            "run",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrlEAB,
            "--tls-skip-verify",
            "--domains", hostname,
            "--accept-tos",
            "--email", "foo@foo.de",
            "--eab",
            "--eab.kid", tokenResponse.getEabKid(),
            "--eab.hmac", tokenResponse.getTokenValue(),
            "--http",
            "--http.address", ":" + prefTC.getHttpChallengePort(),
            "--key-type", "rsa4096");

        int exitCodeCreate = executeExternalProcess(builderCreate);
        Assertions.assertEquals( 0, exitCodeCreate,"expects an exit code == 0");

        File crtFile = new File(configFolder.toFile(), "certificates" + File.separator + hostname + ".crt");
        LOG.info("crtFile : {}", crtFile.getAbsolutePath());
        Assertions.assertTrue(crtFile.exists(), "expecting certificate file exists");

        X509Certificate x509Certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(crtFile));
        Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
        Assertions.assertEquals(hostname, cert.getSans());
        Assertions.assertTrue( cert.isActive(),"freshly created certificate expected to be active");


        ProcessBuilder builderRevoke = new ProcessBuilder();

        builderRevoke.command(LEGO_EXE,
            "certificates", "revoke",
            "--path", configFolder.toFile().getAbsolutePath(),
            "--server", dirUrlEAB,
            "--tls-skip-verify",
            "--cert.name", hostname,
            "--reason", "5",
            "--email", "foo@foo.de");


        int exitCodeRevoke = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(0, exitCodeRevoke, "expects an exit code == 0");

        Optional<Certificate> certRevokedOpt = certificateUtil.findCertificateById(cert.getId());
        Assertions.assertTrue(certRevokedOpt.isPresent(), "Expecting to find certificate");
        Assertions.assertTrue(certRevokedOpt.get().isRevoked(), "Expected status change to revoked");
    }
}

