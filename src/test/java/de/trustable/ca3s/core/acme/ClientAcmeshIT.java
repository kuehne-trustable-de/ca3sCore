package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.ExternalProcessITBase;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.ApiTokenController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;

// for info : test string for acme.sh:
// acme.sh --renew --force -d ubuntu.trustable.eu --standalone --alpn --tlsport 8443 --server http://192.168.56.1:9090/acme/acmeTest/directory

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientAcmeshIT extends ExternalProcessITBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientAcmeshIT.class);

    String hostname;

//    @TempDir(cleanup = CleanupMode.NEVER)
    @TempDir
    Path directory;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

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

    @BeforeEach
	void init() throws IOException {
        ptc.getInternalACMETestPipelineLaxRestrictions();

        hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
        dirUrlEAB  = "http://localhost:" + serverPort + ACME_EAB_PATH_PART;

        ptc.getInternalACMETestPipelineLaxRestrictions();
        ptc.getInternalACMETestPipelineEabRestrictions();

        prefTC.getTestUserPreference(80);
    }

    @Test
    public void acmeshCreateAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (isWindows) {
            LOG.info("acme.sh test no available on Windows");
        } else {
            ProcessBuilder builderExecutabelExixts = new ProcessBuilder();
            builderExecutabelExixts.command("which", "acme.sh");
            if( executeExternalProcess(builderExecutabelExixts) != 0) {
                LOG.info("'acme.sh' missing, please install and rerun.");
                return;
            }

            Path webrootFolder = directory.resolve("webroot");
            Path configFolder= directory.resolve("config");
            Path workFolder= directory.resolve("work");
            Path logFolder= directory.resolve("log");

            webrootFolder.toFile().mkdirs();
            configFolder.toFile().mkdirs();
            workFolder.toFile().mkdirs();
            logFolder.toFile().mkdirs();

            ProcessBuilder builderCreate = new ProcessBuilder();
            builderCreate.command("acme.sh",
                "--issue",
                "--force",
                "--server", dirUrl,
                "--keylength", "4096",
                "--accountkeylength", "4096",
                "--email", "foo@foo.de",
                "--standalone",
                "--httpport", "5544",
//                "--httpport", Integer.toString(prefTC.getHttpChallengePort()),
                "-d", hostname,
                "--config-home",  configFolder.toFile().getAbsolutePath(),
                "--webroot", webrootFolder.toFile().getAbsolutePath()
            );


            int exitCodeCreate = executeExternalProcess(builderCreate);
            Assertions.assertEquals(0, exitCodeCreate, "expects an exit code == 0");
/*
            File fullchainPemFile = new File( configFolder.toFile(), "live" + File.separator + hostname + File.separator + "fullchain.pem" );
            LOG.info("fullchainPemFile : {}", fullchainPemFile.getAbsolutePath());
            assertTrue("expecting pem chain exists", fullchainPemFile.exists());


            X509Certificate x509Certificate = CryptoUtil.convertPemToCertificate(Files.readString(fullchainPemFile.toPath()));
            Certificate cert = certificateUtil.getCertificateByX509(x509Certificate);
            assertEquals( hostname, cert.getSans());
            assertTrue( "freshly created certificate expected to be active", cert.isActive());

            ProcessBuilder builderRevoke = new ProcessBuilder();
            builderRevoke.command("certbot",
                "revoke", "-n", "-v", "--debug",
                "--reason", "superseded",
                "--server", dirUrl,
                "--cert-name", hostname,
                "--webroot-path", webrootFolder.toFile().getAbsolutePath(),
                "--config-dir", configFolder.toFile().getAbsolutePath(),
                "--work-dir", workFolder.toFile().getAbsolutePath(),
                "--logs-dir", logFolder.toFile().getAbsolutePath()
            );

            int exitCodeRevoke = executeExternalProcess(builderRevoke);
            assertEquals("expects an exit code == 0", 0, exitCodeRevoke);

            Optional<Certificate> certRevokedOpt = certificateUtil.findCertificateById(cert.getId());
            assertTrue( "Expected status change to revoked", certRevokedOpt.get().isRevoked());


            int exitCodeRevoke2 = executeExternalProcess(builderRevoke);
            assertEquals("trying to revoke an already revoked cert. Expecting exit code == 1", 1, exitCodeRevoke2);
*/
        }
    }


}
