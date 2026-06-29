package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.ExternalProcessITBase;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.test.util.AccessPortTestManager;
import de.trustable.ca3s.core.web.rest.ApiTokenController;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;

import static org.assertj.core.api.Fail.fail;

// for info : test string for win-acme:
// wacs.exe --source manual --host www.domain.com --webroot C:\sites\wwwroot

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientWinAcmeIT extends ExternalProcessITBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientWinAcmeIT.class);

    public final String CLIENT_EXE = "C:\\Users\\kuehn\\win-acme\\wacs.exe";
    String hostname;

//    @TempDir
    Path directory = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
    final String ACME_EAB_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_EAB_REALM + "/directory";

    String dirUrl;
    String dirUrlEAB;

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
    }

    @AfterAll
    static void tearDown(){
        accessPortTestManager.tearDownEnvironment();
    }


    @BeforeEach
	void init() throws IOException {

        ptc.getInternalACMETestPipelineLaxRestrictions();

        hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
		dirUrl = "https://localhost:" + accessPortTestManager.getAcmeAccessPort() + ACME_PATH_PART;
        dirUrlEAB  = "https://localhost:" + accessPortTestManager.getAcmeAccessPort() + ACME_EAB_PATH_PART;

        ptc.getInternalACMETestPipelineLaxRestrictions();
        ptc.getInternalACMETestPipelineEabRestrictions();

        prefTC.getTestUserPreference();
    }


    @Test
    public void certbotCreateAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        if( !isInstalled(CLIENT_EXE, "--version")) {
            fail("'win-acme' missing, please install and rerun.");
        }


        Path webrootFolder = directory.resolve("webroot");
        Path configFolder= directory.resolve("config");
        Path workFolder= directory.resolve( "work");
        Path logFolder= directory.resolve("log");

        Assertions.assertTrue(webrootFolder.toFile().mkdirs(), "expecting successful creation of webroot");
        Assertions.assertTrue(configFolder.toFile().mkdirs(), "expecting successful creation of config folder");
        Assertions.assertTrue(workFolder.toFile().mkdirs(), "expecting successful creation of work folder");
        Assertions.assertTrue(logFolder.toFile().mkdirs(), "expecting successful creation of log folder");

        ProcessBuilder builderCreate = new ProcessBuilder();

        builderCreate.command(CLIENT_EXE,
                "--source", "manual",
                "--webroot", webrootFolder.toFile().getAbsolutePath(),
                "--baseuri", dirUrl,
                "--host", hostname,
                "--accepttos",
                "--notaskscheduler",
                "--emailaddress", "foo@foo.de",
                "--store", "pemfiles",
                "--pemfilespath",  workFolder.toFile().getAbsolutePath(),
                "--validation", "selfhosting",
                "--validationmode", "http-01",
                "--csr", "rsa");

        int exitCodeCreate = executeExternalProcess(builderCreate);
        Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");

        File crtFile = new File( configFolder.toFile(), "certificates" + File.separator + hostname + ".crt" );
        LOG.info("crtFile : {}", crtFile.getAbsolutePath());
        Assertions.assertTrue(crtFile.exists(), "expecting certificate file exists");

        X509Certificate x509Certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(crtFile));
        Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
        Assertions.assertEquals(hostname, cert.getSans());
        Assertions.assertTrue(cert.isActive(), "freshly created certificate expected to be active");
/*
        ProcessBuilder builderRevoke = new ProcessBuilder();

        builderRevoke.command(CLIENT_EXE,
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
        Assertions.assertTrue(certRevokedOpt.get().isRevoked(), "Expected status change to revoked");


        int exitCodeRevoke2 = executeExternalProcess(builderRevoke);
        Assertions.assertEquals(1, exitCodeRevoke2, "trying to revoke an already revoked cert. Expecting exit code == 1");
*/
    }

}
