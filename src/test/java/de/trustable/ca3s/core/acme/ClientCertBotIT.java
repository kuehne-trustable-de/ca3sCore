package de.trustable.ca3s.core.acme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;

// for info : test string for acme.sh:
// acme.sh --renew --force -d ubuntu.trustable.eu --standalone --alpn --tlsport 8443 --server http://192.168.56.1:9090/acme/acmeTest/directory

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientCertBotIT {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCertBotIT.class);

    String hostname;

    @TempDir
    Path directory;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
	String dirUrl;

	@Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    private CertificateUtil certificateUtil;

    @BeforeEach
	void init() throws IOException {

        ptc.getInternalACMETestPipelineLaxRestrictions();

        hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
		ptc.getInternalACMETestPipelineLaxRestrictions();
        prefTC.getTestUserPreference();
    }


    @Test
    public void certbotCreateAccountAndOrderCertificate() throws IOException, GeneralSecurityException {

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (isWindows) {
            LOG.info("certbot test no available on Windows");
        } else {
            ProcessBuilder builderExecutabelExixts = new ProcessBuilder();
            builderExecutabelExixts.command("which", "certbot");
            if( executeExternalProcess(builderExecutabelExixts) != 0) {
                LOG.info("'certbot' missing, please install and rerun.");
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
            builderCreate.command("certbot",
                "certonly", "-n", "-v", "--debug", "--agree-tos",
                "--server", dirUrl,
                "--key-type", "rsa",
                "--rsa-key-size", "4096",
                "--standalone",
                "--email", "foo@foo.de",
                "--preferred-challenges", "http",
                "--http-01-port", Integer.toString(prefTC.getHttpChallengePort()),
                "-d", hostname,
                "--webroot-path", webrootFolder.toFile().getAbsolutePath(),
                "--config-dir", configFolder.toFile().getAbsolutePath(),
                "--work-dir", workFolder.toFile().getAbsolutePath(),
                "--logs-dir", logFolder.toFile().getAbsolutePath()
            );

            int exitCodeCreate = executeExternalProcess(builderCreate);
            assertEquals("expects an exit code == 0", 0, exitCodeCreate);

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

        }
    }

//    @Test
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
                "--server", dirUrl,
                "--keylength", "4096",
                "--accountkeylength", "4096",
                "--standalone",
                "--email", "foo@foo.de",
                "--httpport", Integer.toString(prefTC.getHttpChallengePort()),
                "-d", hostname,
                "--config-home",  configFolder.toFile().getAbsolutePath(),
                "--webroot", webrootFolder.toFile().getAbsolutePath()
            );


            int exitCodeCreate = executeExternalProcess(builderCreate);
            assertEquals("expects an exit code == 0", 0, exitCodeCreate);
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

    /**
	 * @param builder
	 */
	private int executeExternalProcess(ProcessBuilder builder) {

		int exitCode = -1;

		String cmd = "";
	    for( String s:builder.command()) {
	    	cmd += s + " ";
	    }
		LOG.debug("certbot command '"+ cmd +"' " );

		try {

//			builder.directory(new File(System.getProperty("user.home")));
			builder.inheritIO();

			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            ExecutorService execSrv = Executors.newSingleThreadExecutor();
            execSrv.submit(streamGobbler);

            exitCode = process.waitFor();
            LOG.debug("certbot exitCode '" + exitCode + "' ");

            execSrv.shutdownNow();

        }catch(InterruptedException | IOException ex) {
			LOG.error("executing external process failed with exception", ex);
		}

		return exitCode;
	}

	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;

	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }

	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	}

}
