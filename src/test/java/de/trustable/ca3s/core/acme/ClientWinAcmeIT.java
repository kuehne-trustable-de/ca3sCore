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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import static de.trustable.ca3s.core.config.EndpointConfigs.*;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_ACME_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_ADMIN_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_RA_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_TLS_PREFIX;
import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// for info : test string for win-acme:
// wacs.exe --source manual --host www.domain.com --webroot C:\sites\wwwroot

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientWinAcmeIT extends ExternalProcessITBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientWinAcmeIT.class);

    public final String CLIENT_EXE = "C:\\Users\\kuehn\\win-acme\\wacs.exe";
    String hostname;

    Path directory = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());

    final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
    final String ACME_EAB_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_EAB_REALM + "/directory";

    String dirUrl;
    String dirUrlEAB;

    @LocalServerPort
    int serverPort; // random port chosen by spring test

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

    @BeforeAll
    static void setUp() throws CertificateException {
        factory = CertificateFactory.getInstance("X.509");
        accessPortTestManager.setUpEnvironmentTLS();

        accessPortTestManager.setUpEnvironmentSinglePort();

    }

    @AfterAll
    static void tearDown() {
        accessPortTestManager.tearDownEnvironment();
    }


    @BeforeEach
    void init() throws IOException {

        ptc.getInternalACMETestPipelineLaxRestrictions();

        hostname = InetAddress.getLocalHost().getHostName().toLowerCase();

        dirUrl = "http://localhost:" + accessPortTestManager.getTlsAccessPort() + ACME_PATH_PART;
        dirUrlEAB = "http://localhost:" + accessPortTestManager.getTlsAccessPort() + ACME_EAB_PATH_PART;

        ptc.getInternalACMETestPipelineLaxRestrictions();
        ptc.getInternalACMETestPipelineEabRestrictions();

        prefTC.getTestUserPreference();
    }


    @Test
    public void winACMECreateAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        if (!isWindows) {
            LOG.info("win-acme test available on Windows, only");
            return;
        }

        if( !isInstalled(CLIENT_EXE, "--version")) {
            fail("'win-acme' missing, please install and rerun.");
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

        builderCreate.command(CLIENT_EXE,
            "--source", "manual",
//            "--config-dir", configFolder.toFile().getAbsolutePath(),
            "--webroot", webrootFolder.toFile().getAbsolutePath(),
            "--baseuri", dirUrl,
            "--host", hostname,
            "--accepttos",
            "--notaskscheduler",
            "--emailaddress", "foo@foo.de",
            "--store", "pemfiles",
            "--pemfilespath", workFolder.toFile().getAbsolutePath(),
            "--validation", "selfhosting",
            "--validationport", "5544",
            "--validationmode", "http-01",
            "--csr", "rsa");

        int exitCodeCreate = executeExternalProcess(builderCreate);
        Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");


        File fullchainPemFile = new File(workFolder.toFile(), hostname + "-chain.pem");
        LOG.info("fullchainPemFile : {}", fullchainPemFile.getAbsolutePath());
        assertTrue("expecting pem chain exists", fullchainPemFile.exists());


        X509Certificate x509Certificate = CryptoUtil.convertPemToCertificate(Files.readString(fullchainPemFile.toPath()));
        Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
        assertEquals(hostname, cert.getSans());
        assertTrue("freshly created certificate expected to be active", cert.isActive());


        ProcessBuilder builderRevoke = new ProcessBuilder();

        builderRevoke.command(CLIENT_EXE,
            "--verbose",
            "--revoke",
            "--force",
            "--baseuri", dirUrl,
            "--store", "pemfiles",
            "--pemfilespath", workFolder.toFile().getAbsolutePath(),
            "--friendlyname", hostname
//            "--reason", "4",
//            "--email", "foo@foo.de"
        );


        int exitCodeRevoke = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(0, exitCodeRevoke, "expects an exit code == 0");

        Optional<Certificate> certRevokedOpt = certificateUtil.findCertificateById(cert.getId());
        Assertions.assertTrue(certRevokedOpt.get().isRevoked(), "Expected status change to revoked");


        int exitCodeRevoke2 = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(1, exitCodeRevoke2, "trying to revoke an already revoked cert. Expecting exit code == 1");

    }


    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    public void winACMECreateEabAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (isWindows) {
            LOG.info("certbot test no available on Windows");
        } else {

            User user = userUtil.getUserByLogin("user1");

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


            if (!isInstalled(CLIENT_EXE, "--version")) {
                fail("'win-acme' missing, please install and rerun.");
            }

            Path webrootFolder = directory.resolve("webroot");
            Path workFolder = directory.resolve("work");

            Assertions.assertTrue(webrootFolder.toFile().mkdirs(), "expecting successful creation of webroot");
            Assertions.assertTrue(workFolder.toFile().mkdirs(), "expecting successful creation of work folder");

            ProcessBuilder builderCreate = new ProcessBuilder();

            builderCreate.command(CLIENT_EXE,
                "--source", "manual",
                "--webroot", webrootFolder.toFile().getAbsolutePath(),
                "--baseuri", dirUrlEAB,
                "--host", hostname,
                "--accepttos",
                "--eab-key-identifier", tokenResponse.getEabKid(),
                "--eab-key", tokenResponse.getTokenValue(),
                "--notaskscheduler",
                "--emailaddress", "foo@foo.de",
                "--store", "pemfiles",
                "--pemfilespath", workFolder.toFile().getAbsolutePath(),
                "--validation", "selfhosting",
                "--validationport", "5544",
                "--validationmode", "http-01",
                "--csr", "rsa");

            int exitCodeCreate = executeExternalProcess(builderCreate);
            Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");


            File fullchainPemFile = new File(workFolder.toFile(), hostname + "-chain.pem");
            LOG.info("fullchainPemFile : {}", fullchainPemFile.getAbsolutePath());
            assertTrue("expecting pem chain exists", fullchainPemFile.exists());


            X509Certificate x509Certificate = CryptoUtil.convertPemToCertificate(Files.readString(fullchainPemFile.toPath()));
            Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
            assertEquals(hostname, cert.getSans());
            assertTrue("freshly created certificate expected to be active", cert.isActive());


        }
    }
}
