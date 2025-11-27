package de.trustable.ca3s.core.service.dto;

import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class KeyAlgoLengthTest {

    private static final Logger LOG = LoggerFactory.getLogger(KeyAlgoLengthTest.class);

    CryptoUtil cryptoUtil = new CryptoUtil();

    @BeforeAll
    public static void setUpBeforeClass() {
        JCAManager.getInstance();
    }

    @Test
    void fromRSA() {
        KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa-2048");
        assertEquals(2048, keyAlgoLength.getKeyLength());
        assertEquals("rsa-2048", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa_2048");
        assertEquals(2048, keyAlgoLength.getKeyLength());
        assertEquals("rsa-2048", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "RSA_2048");
        assertEquals(2048, keyAlgoLength.getKeyLength());
        assertEquals("rsa-2048", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "RSA_3072");
        assertEquals(3072, keyAlgoLength.getKeyLength());
        assertEquals("rsa-3072", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "RSA_4096");
        assertEquals(4096, keyAlgoLength.getKeyLength());
        assertEquals("rsa-4096", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "RSA_6144");
        assertEquals(6144, keyAlgoLength.getKeyLength());
        assertEquals("rsa-6144", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "RSA_8192");
        assertEquals(8192, keyAlgoLength.getKeyLength());
        assertEquals("rsa-8192", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

    }

    @Test
    void fromEC() {
        KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from( "brainpoolP256r1");
        assertEquals(256, keyAlgoLength.getKeyLength());
        assertEquals("brainpoolP256r1", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "brainpoolP384r1");
        assertEquals(384, keyAlgoLength.getKeyLength());
        assertEquals("brainpoolP384r1", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "brainpoolP512r1");
        assertEquals(512, keyAlgoLength.getKeyLength());
        assertEquals("brainpoolP512r1", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "Brainpoolp512R1");
        assertEquals(512, keyAlgoLength.getKeyLength());
        assertEquals("brainpoolP512r1", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "Ed25519");
        assertEquals(256, keyAlgoLength.getKeyLength());
        assertEquals("Ed25519", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "ecdsa-224");
        assertEquals(224, keyAlgoLength.getKeyLength());
        assertEquals("ecdsa-224", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "ecdsa-256");
        assertEquals(256, keyAlgoLength.getKeyLength());
        assertEquals("ecdsa-256", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "ecdsa-384");
        assertEquals(384, keyAlgoLength.getKeyLength());
        assertEquals("ecdsa-384", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "ecdsa-512");
        assertEquals(512, keyAlgoLength.getKeyLength());
        assertEquals("ecdsa-512", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "ecDSA-512");
        assertEquals(512, keyAlgoLength.getKeyLength());
        assertEquals("ecdsa-512", keyAlgoLength.getAlgoName());
        assertNotNull(keyAlgoLength.buildJcaContentSignerBuilder());

    }

    @Test
    void testToString() {
        KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa-2048");
        assertEquals("rsa-2048", keyAlgoLength.toString());
    }


    @Test
    void analysePublicKey() throws GeneralSecurityException, IOException {


        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        File folder = new File("src/test/resources/certificates/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                X509Certificate cert;
                if( file.getName().toLowerCase(Locale.ROOT).endsWith("crt")){
                    cert = (X509Certificate) factory.generateCertificate(new FileInputStream(file));
                }else{
                    cert =cryptoUtil.convertPemToCertificate(Files.readString(file.toPath()));
                }

                String algoName = KeyAlgoLengthOrSpec.getAlgorithmName(cert.getPublicKey());
                LOG.debug("certificate parsed from file {} has algo {}, pk.algo {}", file.getName(), algoName, cert.getPublicKey().getAlgorithm());

            }
        }

        /*
        X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(Base64.decode(content)));
        content = cryptoUtil.x509CertToPem(cert);
        LOG.debug("certificate parsed from base64 (non-pem) content");
*/
    }

}
