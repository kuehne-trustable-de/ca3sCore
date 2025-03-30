package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

class CSRUtilTest {

    public static final Logger LOGGER = LogManager.getLogger(CSRUtilTest.class);

    static final String PARSING_PROBLEM_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "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\n" +
        "TeGyG/3RIZ3oWk7A\n" +
        "-----END CERTIFICATE REQUEST-----";

    static final String NO_CN_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIIEwjCCAqoCAQAwADCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBALuQ\n" +
        "ytWFhS6nGeFQW6KEv7Luzi0YkqsZL+s0L1xZ+HCeyUkHBS81zGvZpFZ0ZcTvuPKL\n" +
        "ZUvAnYF/wwfmuP/ifxAqz4MvKPVF8jHsqSrIDX6rWYSTkgAdGOveCp6yFapnjlwm\n" +
        "zMZTM9O5BEoMOm+hzAkihTlB0eSDU2iSagKUfgxO7QI9oNfsnJoEAwn6eAiqLee9\n" +
        "/Xeze6DJOEU7fX9VZFHlAyAS/hKOK9gnsfTiO8+3jpvogz6GKxndar23l0rI1EIO\n" +
        "NdPu8hzPiRMmepLbOJ+NDw8y+8o3yhWBMprbnz881qG0eN4dYFTN/jrZrlh016LZ\n" +
        "ZmMi/8zosjasqXlvOx/8beasjR+V7Sdrrd4j9jOujRPzBNNvuQJ6nt/yDRy3f1Lk\n" +
        "JFby/Ddc7AZ9K88zIcv5xs9bXEy2Vjvf9OnwKt5hDnI6vQ23C7Qjx93jDJlabACA\n" +
        "JbtWKOwSB+HVZ3ci4xMBN1f6eedRHJb21iDGBOyHLQ0BMq/qhGETDI+bQ9gRzLEO\n" +
        "ls5DSZMbaEsEuqtimkRtMd/A9f47l3ACv5Q6/pXJptkgv2D14HtTzMx8Lwy2a+y5\n" +
        "3D+e703vROzwri7SZnmX4Y4vdEv6jYoAeZFFHAIXTPOcMWIhyEnqZjhZkDmA1OqY\n" +
        "gsxIZnUgKtBuBYQ+wmBbf4RiZW7rPXN2/rHhwATdAgMBAAGgfTB7BgkqhkiG9w0B\n" +
        "CQ4xbjBsMFoGA1UdEQEB/wRQME6CTHRlc3QuYXJ0aWZhY3RvcnktYXJlcy5rOHMt\n" +
        "aW5mcmEtcm90LWludC1zejRpbnQuYXBwLnN6NGludC13MC5jYWFzLmFyZXMucGxh\n" +
        "Y2UwDgYDVR0PAQH/BAQDAgWgMA0GCSqGSIb3DQEBCwUAA4ICAQChwXBGxmG26SC4\n" +
        "IfClq1Ym+ZqIbqYbsCIdy/vZDyuoRR8bwvcUCR9tj4o5OBBUIcj03qG3t3OrueSt\n" +
        "0WxpuobNexm11aZe1fZTtPlS5lzSXOynGoqDUiUj/Mv9FsrLu9DXjO0vM7MQCUm2\n" +
        "pYjqaxEeY/SdM3KsQNC3jFhy+C/YKle45+SIhDnk8p4UnJK6N8cl389sCpMVUFCj\n" +
        "BAYzJVng6e0mqJGCG927aGUEDR4q+C2jj0PxxGAflddeJaVsHd0HkJ0SgQkiTaT6\n" +
        "vIVNcjTw/4ixogdbSO60mMt9d5nAnpvtLAX9yzC04rZSHz8VCYs8hbI/LuZISyDK\n" +
        "XM8iA0hDo4/Z6lqDTXCVkrQOCaDkyYRk+IK+Ksn+U0E+IvxJMog9FnX6bMWMPjxT\n" +
        "baM3eWvD1pgk10eOcR9ojsWsrGPQ5zSTMIWmWFaHH6takB+8eAN9sfBq34FwiG5U\n" +
        "Z9dUDXDgM3ZtCLyYnmIuCHiwYLFFTXeeFGlJ/7JCfTxKWNe9bIjLdDvK4+pW2lnb\n" +
        "HBjnnM0sUBJVz08c/WmJ5a3ZugBnioUjnSGlxDMIABN+GgyL2vpR9oZFdY+3N0P3\n" +
        "hhMpy6QJ12Rcxrd9FcuQGjb4XO2WYBh5ixJY+qzlSL4KzGWkqFIdHlT/c/pza1f7\n" +
        "mjdMQd54ecE4PKQEFErQlMlpOKykMA==\n" +
        "-----END CERTIFICATE REQUEST-----\n";

    PKCS10CertificationRequest p10ProblemReq;
    PKCS10CertificationRequest p10NoCNReq;

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        JCAManager.getInstance();
        p10ProblemReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(PARSING_PROBLEM_CSR.getBytes()));

        p10NoCNReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(NO_CN_CSR.getBytes()));

    }

    @Test
    public void testGettingSANList() throws IOException, GeneralSecurityException {

        CryptoUtil cryptoUtil = new CryptoUtil();
        Pkcs10RequestHolder p10Holder = cryptoUtil.parseCertificateRequest(p10NoCNReq.getEncoded());

        Set<String> snSet = new HashSet<>();

        // add all SANs as source of names to be verified
        for (Attribute csrAttr : p10Holder.getReqAttributes()) {

            String attrOid = csrAttr.getAttrType().getId();
            String attrReadableName = OidNameMapper.lookupOid(attrOid);

            if (PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(csrAttr.getAttrType())) {
                retrieveSANFromCSRAttribute(snSet, csrAttr);
            } else if ("certReqExtensions".equals(attrReadableName)) {
                retrieveSANFromCSRAttribute(snSet, csrAttr);
            } else {
                String value = getASN1ValueAsString(csrAttr);
            }

        }
        Set<GeneralName> generalNameSet = CSRUtil.getSANList(p10ProblemReq.getAttributes());
    }


    private String getASN1ValueAsString(Attribute attr) {
        return getASN1ValueAsString(attr.getAttrValues().toArray());
    }

    private String getASN1ValueAsString(ASN1Encodable[] asn1EncArr) {
        String value = "";
        for (ASN1Encodable asn1Enc : asn1EncArr) {
            if (value.length() > 0) {
                value += ", ";
            }
            value += asn1Enc.toString();
        }
        return value;
    }

    private void retrieveSANFromCSRAttribute(Set<String> sanSet, Attribute attrExtension) {

        Set<GeneralName> generalNameSet = new HashSet<>();

        CSRUtil.retrieveSANFromCSRAttribute(generalNameSet, attrExtension);

        for (GeneralName gn : generalNameSet) {
            sanSet.add(gn.getName().toString());
        }

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
