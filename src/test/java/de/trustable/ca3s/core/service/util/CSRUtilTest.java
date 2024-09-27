package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Set;

class CSRUtilTest {

    public static final Logger LOGGER = LogManager.getLogger(CSRUtilTest.class);

    static final String PARSING_PROBLEM_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIIE6DCCAtACAQAwKjELMAkGA1UEBhMCREUxDTALBgNVBAoTBEJ1bmQxDDAKBgNV\n" +
        "BAsTA0JLQTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBALpXRaBA5n79\n" +
        "DqE7KewjXSPyAuTUpAjujO5bMPIbEi71gLy7/fQ562drQAl3HnYMCmyEkOP4TsRH\n" +
        "y/zujFidzE6rGD0KdUbpcnHSaR8qdD3mC0d2cL9CTYk5uew99m40HttvklMFFNsK\n" +
        "0hF4Tymdv3Mk0DQSzSkyC4Ct1D2gM/ufRjoATiu3D8yF0PgDTfi2AL9u79Oofpuk\n" +
        "k0cTOgi3RVSIB/EjJKjw/V6s/0czPxmPPpjmz47eMDL/k72Z/ULkf4bVwqdNf34M\n" +
        "1KzYn2qgfLLNLJ5iE7cXMA69od6DN5vfRKPxf6lJBC6J7pMYMxF7bGsht3btynBt\n" +
        "2zxW5RNq8DkNB6dKuHzfUkWSrYYvvIPRlsEeWUGqKtqRaUy7k5BWI48eJc3Dk9UW\n" +
        "vPsnLM+Onw1jk8rPPrlzMlTFqiLRejcc1l3nuN3bzRXP0VNtFdwC80jiJHFC8Ipn\n" +
        "Uaf0O5bkjqE/W0NlCEw8qStqbjsLWGubhEtK6KLgo6lLtNy4Lt6LXZBH/Ju3Ne1p\n" +
        "kv0XWRsQGywQZYuPkTBVwF5o8lBNA+EzNljY5p/Yb+OiXA84RSbqsNbUS0llZCSA\n" +
        "eAOBdCc77noIwO99rP6MtluE64O+lqX3IYpQw0sElPhgwoW0tYABSckfh6KmLkQI\n" +
        "JJVE0LqZbONUhD2a9n1kZH3mNu4+AsddAgMBAAGgeTB3BgkqhkiG9w0BCQ4xajBo\n" +
        "MFYGA1UdEQRPME2CS3Rlc3QwMS5kZW5nZWwtdGVzdC5rOHMtc2hhcmVkLWV4dC1z\n" +
        "ejFkZXYuYXBwLnN6MWRldi13MC5jYWFzLnBzcC5ia2EuYnVuZC5kZTAOBgNVHQ8B\n" +
        "Af8EBAMCBaAwDQYJKoZIhvcNAQELBQADggIBAIEf15h5kh5ZLKZXiuXtSqq4oV/X\n" +
        "hx5qlL9G2Nj9htk7Nu9046kOo4c4f5/kORlZtOkV1T/uwI1x6+nwNFy6up5rTHaL\n" +
        "8wwFoEh5r/ZZ72OIa3EJxoiXsQRBKEh/HhU8eJfPNFHh3E0fQm3lJzKfjgKmtw+I\n" +
        "0/5mr8o5ahG3GtbnB47gpSNgvXb+DX5mDKok3F4M+Hgtf1kHUWKUedklk0OrA2I+\n" +
        "ReEyaeENcBu0X4fD0WYdo45kCmMpCNWOejY7+LbWNMBIPpkcbQi4ObOXT9p/e16K\n" +
        "74BItD6eqqKNmzfeqmOIIIQvqVSP+6aicFugXm9n0Zbpdge8p/0DuhHOrLziSHgW\n" +
        "TLK6gVW7MFUbpbGnFDaKWeTFgKUOpk8btrLlj1y73vk5drmeH9e8cqu3IrkQjLiX\n" +
        "tyTfY+i2U5J938YkOdetNJgVzLFDAC4jOPC1j6Qj1JrFEHuXF6a/dJkqFWVp15mu\n" +
        "4ht84ZUPgBSJoPz+FGJv3yc9R5eO3sAvMc9H+k+cH0kCBADXRUK9mvRZ+yy5OZRA\n" +
        "V21Yn45oh0zWDT9XtwIbx1UurqqmaQJjZaOmM9MfhFMSyu2rm1wF5DUMLYLVIDLJ\n" +
        "zbYWCE/SGrJx4xyaJQzNHmVb25MHfh+v+5gAw+YFlJ1C/8x2zMztOilppL11bfzx\n" +
        "TeGyG/3RIZ3oWk7A\n" +
        "-----END CERTIFICATE REQUEST-----";

    PKCS10CertificationRequest p10ProblemReq;

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        p10ProblemReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(PARSING_PROBLEM_CSR.getBytes()));

    }

    @Test
    public void testGettingSANList() {

        Set<GeneralName> generalNameSet = CSRUtil.getSANList(p10ProblemReq.getAttributes());
    }

    /**
     * convert a csr stream to the corresponding BC object
     *
     * @param isCSR the csr input stream
     * @return the csr input stream
     * @throws GeneralSecurityException something cryptographic went wrong
     */
    public PKCS10CertificationRequest convertPemToPKCS10CertificationRequest(final InputStream isCSR)
        throws GeneralSecurityException {

        PKCS10CertificationRequest csr = null;

        Reader pemReader = new InputStreamReader(isCSR);
        PEMParser pemParser = new PEMParser(pemReader);

        try {
            Object parsedObj = pemParser.readObject();

            if (parsedObj == null) {
                throw new GeneralSecurityException("Parsing of CSR failed! Not PEM encoded?");
            }

//	            LOGGER.debug("PemParser returned: " + parsedObj);

            if (parsedObj instanceof PKCS10CertificationRequest) {
                csr = (PKCS10CertificationRequest) parsedObj;

            }
        } catch (IOException ex) {
            LOGGER.warn("IOException, convertPemToPublicKey", ex);
            throw new GeneralSecurityException("Parsing of CSR failed! Not PEM encoded?");
        } finally {
            try {
                pemParser.close();
            } catch (IOException e) {
                // just ignore
                LOGGER.warn("IOException on close()", e);
            }
        }

        return csr;
    }


}
