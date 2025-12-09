package de.trustable.ca3s.core.est;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.KeyUtil;
import de.trustable.ca3s.est.ESTClientWrapper;
import de.trustable.ca3s.est.OutcomeInfo;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.TestSocketUtils;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static de.trustable.ca3s.core.PipelineTestConfiguration.EST_PASSWORD;

/**
 * Access the EST endpoint with the libEST client
 *
 * Available EST client options
 *   -v                Verbose operation
 *   -g                Get CA certificate from EST server
 *   -e                Enroll with EST server and request a cert
 *   -q                Enroll with EST server and request a cert and a server-side generated private key
 *   -a                Get CSR attributes from EST server
 *   -z                Force binding the PoP by including the challengePassword in the CSR
 *   -r                Re-enroll with EST server and request a cert, must use -c option
 *   -c <certfile>     Identity certificate to use for the TLS session
 *   -k <keyfile>      Use with -c option to specify private key for the identity cert
 *   -x <keyfile>      Use existing private key in the given file for signing the CSR
 *   -y <csrfile>      Use existing CSR in the given file
 *   -s <server>       Enrollment server IP address
 *   -p <port>         TCP port number for enrollment server
 *   -o <dir>          Directory where pkcs7 certs will be written
 *   -i <count>        Number of enrollments to perform per thread (default=1)
 *   -w <count>        Timeout in seconds to wait for server response (default=10)
 *   -f                Runs EST Client in FIPS MODE = ON
 *   -u <string>       Specify user name for HTTP authentication.
 *   -h <string>       Specify password for HTTP authentication.
 *   -?                Print this help message and exit.
 *   --keypass_stdin   Specify en-/decryption of private key, password read from STDIN
 *   --keypass_arg     Specify en-/decryption of private key, password read from argument
 *   --common-name  <string>     Specify the common name to use in the Suject Name field of the new certificate.
 *                               127.0.0.1 will be used if this option is not specified
 *   --pem-output                Convert the new certificate to PEM format
 *   --srp                       Enable TLS-SRP cipher suites.  Use with --srp-user and --srp-password options.
 *   --srp-user     <string>     Specify the SRP user name.
 *   --srp-password <string>     Specify the SRP password.
 *   --auth-token   <string>     Specify the token to be used with HTTP token authentication.
 *   --path-seg     <string>     Specify the optional path segment to use in the URI.
 *   --proxy-server <string>     Proxy server to enable SOCK/HTTP proxy mode.
 *   --proxy-port   <port>       Proxy port number.  Must include proxy-server.
 *   --proxy-proto  <EST_CLIENT_PROXY_PROTO>  Proxy protocol.
 *   --proxy-auth   <BASIC|NTLM>  Proxy authentication method.
 *   --proxy-username <string>   username to pass to proxy server.
 *   --proxy-password <string>   password to pass to proxy server.
 */
@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class LibESTClientIT {

    private static final Logger LOG = LoggerFactory.getLogger(LibESTClientIT.class);

    static String hostFQDN; // the name of this server
    static int serverPort; // random port for EST endpoint

    static File outDirFile;

    List<String> defaultParamsList = new ArrayList<String>();

    static ESTClientWrapper estClientWrapper;

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    KeyUtil keyUtil;

    @BeforeEach
    void init() throws IOException {
        LOG.info("ptc: {}", ptc);
        try {
            ptc.getInternalESTTestPipelineDefaultLaxRestrictions();
            prefTC.getTestUserPreference();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File caCertFile = File.createTempFile("cacert_tmp", ".crt");
        caCertFile.deleteOnExit();

        Files.writeString(caCertFile.toPath(), estClientWrapper.buildCaCertFromTruststore());
        Files.writeString(caCertFile.toPath(), estClientWrapper.buildCaCertForServer(hostFQDN, serverPort));

        estClientWrapper.setCacert(caCertFile.getAbsolutePath());
        if (outDirFile.list() != null && outDirFile.list().length > 0) {
            Arrays.stream(outDirFile.listFiles()).anyMatch(f -> f.delete());
        }

        defaultParamsList = new ArrayList<String>();

        defaultParamsList.add("-v");

        defaultParamsList.add("-s");
        defaultParamsList.add(hostFQDN);

        defaultParamsList.add("-p");
        defaultParamsList.add("" + serverPort);

        defaultParamsList.add("-o");
        defaultParamsList.add(outDirFile.getAbsolutePath());


    }

    @BeforeAll
    public static void setUpBeforeClass() throws IOException {
        JCAManager.getInstance();

        serverPort = TestSocketUtils.findAvailableTcpPort();
        System.setProperty(Ca3SApp.SERVER_EST_PREFIX + "port", "" + serverPort);

        hostFQDN = InetAddress.getLocalHost().getHostName();

        estClientWrapper = new ESTClientWrapper();
        estClientWrapper.setVerbose(true);

        outDirFile = Files.createTempDirectory("outdir_tmp").toFile();
        outDirFile.deleteOnExit();

    }

    @Test
    public void testGetCaCerts() throws Exception {

        List<String> argList = new ArrayList<String>(defaultParamsList);

        argList.add("-g");

        estClientWrapper.buildCaCertForServer(hostFQDN, serverPort);

        OutcomeInfo outcomeInfo = estClientWrapper.execute(argList);

        LOG.info("out: {}", outcomeInfo.getOut());
        LOG.info("err: {}", outcomeInfo.getErr());

        Assertions.assertEquals(0, outcomeInfo.getExitCode());

        Assertions.assertFalse(outcomeInfo.getOut().toLowerCase().contains("error"));
        Assertions.assertFalse(outcomeInfo.getErr().toLowerCase().contains("error"));

        Assertions.assertEquals(1, outDirFile.list().length);

        List<X509Certificate> certList = certificateConverter( outDirFile.listFiles()[0]);

        Assertions.assertEquals(2, certList.size());

    }

    @Test
    public void testGetCsrAttributes() throws Exception {

        List<String> argList = new ArrayList<String>(defaultParamsList);

        argList.add("-a");

        estClientWrapper.buildCaCertForServer(hostFQDN, serverPort);

        OutcomeInfo outcomeInfo = estClientWrapper.execute(argList);

        LOG.info("out: {}", outcomeInfo.getOut());
        LOG.info("err: {}", outcomeInfo.getErr());

        Assertions.assertEquals(0, outcomeInfo.getExitCode());

        Assertions.assertFalse(outcomeInfo.getOut().toLowerCase().contains("error"));
        Assertions.assertFalse(outcomeInfo.getErr().toLowerCase().contains("error"));

        Assertions.assertEquals(1, outDirFile.list().length);

        DLSequence attrSeq = certificateAttributes(outDirFile.listFiles()[0]);
        Assertions.assertNotNull(attrSeq);

    }

    @Test
    public void testEnrollWithBasicAuth() throws Exception {

        X500Principal enrollingPrincipal = new X500Principal("CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable solutions,C=DE");

        KeyPair keyPair = keyUtil.createKeyPair();
        KeyPair keyPairRenew = keyUtil.createKeyPair();

        String csrAsPEM = CryptoUtil.getCsrAsPEM(enrollingPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            null);

        File csrFile = File.createTempFile("csr_tmp", ".pem");
        csrFile.deleteOnExit();

        Files.writeString(csrFile.toPath(), csrAsPEM);


        // request a new certificate
        List<String> argList = new ArrayList<String>(defaultParamsList);

        argList.add("-e"); // enroll

        argList.add("-u"); // basic auth user
        argList.add("EST"); // enroll

        argList.add("-h"); // basic auth user
        argList.add(EST_PASSWORD); // enroll

        argList.add("-y"); // basic auth user
        argList.add(csrFile.getAbsolutePath()); // basic auth user

        argList.add("--pem-output");

        estClientWrapper.buildCaCertForServer(hostFQDN, serverPort);

        OutcomeInfo outcomeInfo = estClientWrapper.execute(argList);

        LOG.info("out: {}", outcomeInfo.getOut());
        LOG.info("err: {}", outcomeInfo.getErr());

        Assertions.assertEquals(0, outcomeInfo.getExitCode());

//        Assertions.assertFalse(outcomeInfo.getOut().toLowerCase().contains("error"));
        Assertions.assertFalse(outcomeInfo.getErr().toLowerCase().contains("error"));

        Assertions.assertEquals(1, outDirFile.list().length);

        LOG.info("created certificate in file {}", outDirFile.listFiles()[0].getAbsolutePath());

        X509Certificate x509Cert = CryptoService.convertPemToCertificate(Files.readString(outDirFile.listFiles()[0].toPath()));
        Assertions.assertNotNull(x509Cert);

        LOG.info("created certificate CN: {}, serial {}", x509Cert.getSubjectX500Principal().getName(), x509Cert.getSerialNumber().toString());

        // reenroll the certificate

        File privateKeyFile = File.createTempFile("privKey_tmp", ".pem");
        csrFile.deleteOnExit();

        privateKeyToPem(keyPair.getPrivate(), privateKeyFile);

        List<String> argReenrollList = new ArrayList<String>(defaultParamsList);

        argReenrollList.add("-r"); // reenroll

        argReenrollList.add("-c"); // authentication certificate
        argReenrollList.add(outDirFile.listFiles()[0].getAbsolutePath());

        argReenrollList.add("-k"); // authentication key file
        argReenrollList.add(privateKeyFile.getAbsolutePath());

        argReenrollList.add("--pem-output");

        OutcomeInfo outcomeInfoReenroll = estClientWrapper.execute(argReenrollList);

        LOG.info("out: {}", outcomeInfoReenroll.getOut());
        LOG.info("err: {}", outcomeInfoReenroll.getErr());

        Assertions.assertEquals(0, outcomeInfoReenroll.getExitCode());

//        Assertions.assertFalse(outcomeInfoReenroll.getOut().toLowerCase().contains("error"));
        Assertions.assertFalse(outcomeInfoReenroll.getErr().toLowerCase().contains("error"));

        Assertions.assertEquals(1, outDirFile.list().length);

        LOG.info("renewed certificate in file {}", outDirFile.listFiles()[0].getAbsolutePath());

        X509Certificate x509CertRenewed = CryptoService.convertPemToCertificate(Files.readString(outDirFile.listFiles()[0].toPath()));
        Assertions.assertNotNull(x509CertRenewed);

        LOG.info("renewed certificate CN: {}, serial {}", x509CertRenewed.getSubjectX500Principal().getName(), x509CertRenewed.getSerialNumber().toString());

    }

    public void privateKeyToPem(PrivateKey priv, File privateKeyFile) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(priv);
        pemWriter.close();
        Files.writeString(privateKeyFile.toPath(), stringWriter.toString());
    }

    static DLSequence certificateAttributes(File binaryFile) throws IOException {

        byte[] csrAttrBytes = Base64.getMimeDecoder().decode(Files.readAllBytes(binaryFile.toPath()));

        return (DLSequence ) ASN1Sequence.fromByteArray(csrAttrBytes);
    }

    static List<X509Certificate> certificateConverter(File binaryFile) throws IOException, CMSException {

        CMSSignedData data = new CMSSignedData(Base64.getMimeDecoder().decode(
            Files.readAllBytes(binaryFile.toPath())
        ));
        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
        Store<X509CertificateHolder> certStore = data.getCertificates();

        return certStore.getMatches(null).stream().map((X509CertificateHolder holder)-> {
            try {
                return certificateConverter.getCertificate(holder);
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
