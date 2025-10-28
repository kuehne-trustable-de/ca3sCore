package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.badkeys.BadKeysService;
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
import org.mockito.Mock;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CSRUtilTest {

    public static final Logger LOGGER = LogManager.getLogger(CSRUtilTest.class);

    static final String PARSING_PROBLEM_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "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\n" +
        "TeGyG/3RIZ3oWk7A\n" +
        "-----END CERTIFICATE REQUEST-----";

    static final String NO_CN_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIIEmjCCAoICAQAwADCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANtf\n" +
        "/yshzOx3Y/TaH3BxLA4NzrPYC2kBhcTWBsY6KJt79pZxB0xhWq+9tzXTP+YfABvT\n" +
        "LGlpb2H6XPSfToUeVb0EA+117AtbaVgpADNneMCxggoychc4RFlpFyP55i9/LZbp\n" +
        "jeRT5e5QNGbnL6KfbyQQuE3tel+FHZoun29XrDdvklZhrl8Cnbs09JWoUXjRJYKJ\n" +
        "amQNkbBEBdrR0gHSRY4feQpS2kWVYbjspjnM1+Mazr3YODJyaaxPFLdbJfBzngKw\n" +
        "508dalnlYxG/iBotsiTCm2N+vkiXd7ZNh1E5IbzzIoSip6prSzmY5um78D5r5GNX\n" +
        "3MmEhFVy7UqzaJT/ZHyoVUtk0cUU4O9/YoGMmRO8HgptBX29L9vMALuR4cXgDOAw\n" +
        "2PAomCMmov/tR9gwqNgu21qzW/KQu7wUVIjrzL6nIGTiED3AJ8GnxpR0IcguBg+M\n" +
        "QnFRg6b9a/wz19Pc1857Rpe+YCQYfOPSViJnuuje45n1/1qBS6YoC1GsbOnstzbY\n" +
        "zLZvjJeNnt7prG1FWYSq3fzzYmVsbhCImjzBPnw1h1FHf+oqzJ/dMDfson8k9xUQ\n" +
        "uoH7EoYdmgpJ2AfdbpPvlF5rzQ+23S4eOfCM6O1J1gZiwLRREwE1xPwRD6QFnpPB\n" +
        "3Ht1oCJrhzORaeaOo2gmKeNc9shkpD00RqjYLKyLAgMBAAGgVTBTBgkqhkiG9w0B\n" +
        "CQ4xRjBEMB0GA1UdEQQWMBSCEnRlc3Quc2VydmVyc2lkZS5kZTAOBgNVHQ8BAf8E\n" +
        "BAMCBaAwEwYDVR0lBAwwCgYIKwYBBQUHAwEwDQYJKoZIhvcNAQELBQADggIBAJb9\n" +
        "FWN0oTbQGB+cf4dvtKK/1aslgIotP7J42wnBKAGml5uW9iEvJbk1Njoi7WjEMVQ/\n" +
        "EFSN11w+sGzJ+HuZtILH1rBykTdD21oDuy0NfoJSCGorfLXti/vjCi4/JH/jexCW\n" +
        "+LUZpoLAhmcife/dMIxRn3raHsWGruHJZBcHw6PrjASnzBctZ1RZxRHZImSw0ucW\n" +
        "wZxddCSNUG2OxTyjhAMPuyQlhR7xgGuWB5fcCYpf5uyEtwXfHq9mU2suOcX/d2UF\n" +
        "fGf93MSC5rBvXZIeFfdgA9Ww9zm5gRWOBZBFhGgOEKy53Pc/ClOrO8UvrX/TVrNY\n" +
        "2hVBevHskhrsKJZOFBOEGuFxy+MUGAd685dAzIMHSAKp095a3JDo2yCPquPKjmTS\n" +
        "ppg/BcJ8PIWWVgO+e3fkwntFDtX1Tw1jlidNW8MDScBitaLD2FLEucH+hFe8MOE3\n" +
        "2dK/fVuyLr1TdBhDGp1dtP1Q7MdAOKFf0ALrzENwZODvUcbxJ8ZDKeETWNf1RKMg\n" +
        "jdnHVMcgHal7C1r9dA0qcakObdM7V2mUC5t5RwzaIfPPsjEpyDnbXIPNf+UMBcU8\n" +
        "9Dq8x3A/WE+iKgMngavwzPlYL0XP3jzFRsCrEV2JSY4c7cu0lxR6dXP7PgIEixkU\n" +
        "Nhx2OzfyrqZnqLNoLVgjxUNDmCzX9DPvu2r/k2WK\n" +
        "-----END CERTIFICATE REQUEST-----\n";


    static final String CN_NOT_EQUALS_SAN_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIIEpzCCAo8CAQAwHTEbMBkGA1UEAwwSdGVzdC5zZXJ2ZXJzaWRlLmRlMIICIjAN\n" +
        "BgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAwZ3s7RXDZfBml2nh/cEDGpQzKIIA\n" +
        "Ic5Y4wHgCEQxieBkxrHAh4WQUFShj2O6JjNvQzkn2XgcH3wOTRmAV/VSsOxU7hHr\n" +
        "2FZqqL6EjUQVPxcuma8RXdmGHG/64luXL8XOjauOawvZKcs6TtXmnDwKQLK4cbUE\n" +
        "tawZh+8yuo2HEWBkpMuz9fdbb8Tw8/J3CaA0DMS/jtGv8YCwYOAz1zoBmTJEi6Q8\n" +
        "pgixPnSJdaLu7uEMJlC3fUuJCUvh6a+i9YPUpjetwPHUFcdFd5E30VQWI1m3JcQv\n" +
        "SUUcdWNE4DQv+ialZUhHli8ytxI9pnCyWUPKYBD3tGs/oki/8p7I9hoyKQtDqzfN\n" +
        "Q2RSVsII8jJLj1sNDegOLe0poFBi6B7R//piXazvPM2C9l+jOxrTXzVDi6qPQlHa\n" +
        "2LIehgKZhJSD/4SDmIMz+W7q/5JNcbB3sNgPdm/rupJTX+rPaUBNDOeCeNzaH2zH\n" +
        "WHi6bkG0q+KjGu8Movj+sYshH93sjwEIZF5SVt3ztQh/7NjUuG7eZkQjw+nrv7+R\n" +
        "v3iJPAZ1/ak+mRzncDkzsTaj5tvVMbt0KafiedOqMlcBH/Qp+4SjhG8EMRdvv0MC\n" +
        "2SSWs0pDeTSxpzNPn6pR+jAclx6/aTiIud2x4FjPps/rX7worm2EeurOX5vK4cvh\n" +
        "FkrvyzM9UkuG6qcCAwEAAaBFMEMGCSqGSIb3DQEJDjE2MDQwDQYDVR0RBAYwBIIC\n" +
        "dGUwDgYDVR0PAQH/BAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMA0GCSqGSIb3\n" +
        "DQEBCwUAA4ICAQCnnEh/UZITSQXehD3x02YsnukqtOXSi49Dg3VdsnpZYHbDQXYo\n" +
        "S7dhS8S62hrQ5/61GGp66B9TICYD1cqSOIyumSmQDBOeDWsiuLyl7VKLfbhs35gJ\n" +
        "DnKZUB1pPrAqZTWlb1LaW+2ReAKhcErhug4LPBNU+wEmGDDmUGptqbC3bf0oOSQ2\n" +
        "kkldK4EqBMj/S5dZyK1EcEYsXaHYvp+V6CvVsEzMMuld5ZI7Bbpd1Z6tPI9WlHhT\n" +
        "wJRMUxjM3Il+JF4TGKLg3X9bU7G3H08YD/Sq7nMUJTRInf28poEHpubSbmoHSZal\n" +
        "Kype5gTiOv5RbMbilSbwNZrOGamUI8zkwxPP63DRAmUI2+nbBdAAkMNWRglDiqOQ\n" +
        "zv6YpHSKVSYM9iXPsRKhIhjjcfn091oHsGITf0kEyhFs1IpXgPkcM3WVG/SeOMOi\n" +
        "WwPC+Xh3w9/+gqIPZufNEX052Ul2pmLLCAmvLqZFcnYRB0OQxNBr9fSYbHuiaW7/\n" +
        "Uutv+2JMSsK/KYHKW4Qrvtm50bhYYLmkIYhlWpYyXTMeURFoaOAQsfu8QKOO9Zvd\n" +
        "gfgY96VSnULzG6JWtTkk9+PIrAL8Rc8/Anq5A3GmPRzchtKCJjP6xP8MSbVnYSO4\n" +
        "uDoQJ2to8l/BezB4FqrrJKP5L3Ek4f0aZl3avVJPBXwDeBIpjTNeXX1F3w==\n" +
        "-----END CERTIFICATE REQUEST-----\n";

    static final String CN_NOT_IN_SANS ="-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIIE4zCCAssCAQAwHTEbMBkGA1UEAwwSdGVzdC5zZXJ2ZXJzaWRlLmRlMIICIjAN\n" +
        "BgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyw3LghD0h44SqK/DsmwKyYs6JtiC\n" +
        "AchcDUKsZNi6qTPR6pgUkvo7fYmSpn+j+OVBefozNAhj1Z9XY/EHSeZXUieWMPyv\n" +
        "dwQHhAuiOgKQdOR58dSvBuQWVdYxQEIlwXNBs1ExPMeIbE0L/KqBysFzu2Rw2vv6\n" +
        "aO0WiDsYC+eRNkjnSthqnGsqYAHj+zzT6EM6FW3I4TBOYfamfQRDh0A4125WP9kD\n" +
        "GPMiZgrkOZu37HWkXR1iW/M+HkoXDSGXjiTQ1xtX8w8x8W9bhzce3vQ3ak+Jpla/\n" +
        "Q2wPdgmikyJjWde4OySsoxph2ezvtHkM7QtWoJBZfazN7Sm4KGAloeW1nml6m6QE\n" +
        "13pVrWLbN4n5Iwtnq5zEcz4FLZtvvkbgYZrOZLF9HXtbzHo8blN63/340KqiozUd\n" +
        "3DWB+6uUue/SN1AhB2KuQnFaiN84wQqgcWtn9uujaqaonLyddTpd04HFfkCCDFW1\n" +
        "Cfa1kfnHsvoMTYBz3q2h6dZf1i91JaTCE7zU4Oif2sX9gqK1AvXreTT7VlAthtCe\n" +
        "biAeOpUexdDLLCTvfemtc+JH/7P/lB78wGm7HTXssDVst25IZaKIAPM/CmxE6geS\n" +
        "m66Ktg0Ud4FCftJ0IwYxDYLWR7EGhirNelpmoVCP3t/vcunYGOeE9tNScsywwXl5\n" +
        "c6mrGJOHjTPpCMkCAwEAAaCBgDB+BgkqhkiG9w0BCQ4xcTBvMEgGA1UdEQRBMD+C\n" +
        "E3Rlc3QxLnNlcnZlcnNpZGUuZGWCE3Rlc3QyLnNlcnZlcnNpZGUuZGWCE3Rlc3Qz\n" +
        "LnNlcnZlcnNpZGUuZGUwDgYDVR0PAQH/BAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUF\n" +
        "BwMBMA0GCSqGSIb3DQEBCwUAA4ICAQC0gVueh0pi7kP7Q9cS7HOtKjBDgeYiuXh8\n" +
        "3geTkpEQ+ucsstWVsatyt9HDUewoYE77n/Moe3ypoB2zUErweue1ewf9F2vc+Mh0\n" +
        "X8w99mEBr64vjDJi3EmMwg9mxl2L8xkyJpVOk68x8o7vMxXwi9IgEQsQsr8TY2e7\n" +
        "VhxGS4tBJd2CxuAxdUwqvggLyEzc1RXnwdDfxcWn3NnQ8/LZJ/pEqTal+bg6qa33\n" +
        "l3JexULhtiSBN4bZFl0DuakF6SaY7NNPuvN2YzgtNIUtCqMnt+q4mRJ5zlNmRmAc\n" +
        "OwKv4NoJeIoiV5YNmN0WkoYqWQYqjt/sZQspe3AzQWCvnnoBn1lZTBSmBVG6SDgr\n" +
        "MUekyWKluvXieO6m3rQaaE0RKbqWyQbfaFY5zXH1Zt9jhUOJNzFyqDwRlfCA/67b\n" +
        "yQcWA4GJEG3h54I8AUXm0yPH2H3fwDuaDGR9H39/QzIcUMdaLfS9yCvfSvOPL0nj\n" +
        "hgIqejGFjesK6L1uPX2uB4q66OHIxO6255e1WANrKLy15snCPI1GseemhfpqqVGY\n" +
        "f2rr3h3IP9hDKpNrqt2crDKu0oW6rpyeEBAzH71SUmE+jkVpMXvsNI1M6e08gi8H\n" +
        "5ogeFlCrL2n8oHqoA44pcorqjHNviivtJGAxdPn28hVRTuLrUnOsZHWkRGPTkex8\n" +
        "zrnz5Wcfdw==\n" +
        "-----END CERTIFICATE REQUEST-----";

    static final String CN_MATCHING_ONE_OF_SANS = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "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\n" +
        "-----END CERTIFICATE REQUEST-----";

    PKCS10CertificationRequest p10ProblemReq;
    PKCS10CertificationRequest p10NoCNReq;
    PKCS10CertificationRequest p10CNNotEqualsSanReq;
    PKCS10CertificationRequest p10CNNotInSansReq;
    PKCS10CertificationRequest p10CNMatchingOneOfManySansReq;

    @Mock
    RDNRepository rdnRepository = mock(RDNRepository.class);

    @Mock
    CSRRepository csrRepository = mock(CSRRepository.class);

    @Mock
    CSRCommentRepository csrCommentRepository = mock(CSRCommentRepository.class);

    @Mock
    RDNAttributeRepository rdnAttRepository = mock(RDNAttributeRepository.class);

    @Mock
    CsrAttributeRepository csrAttRepository = mock(CsrAttributeRepository.class);

    @Mock
    BadKeysService badKeysService = mock(BadKeysService.class);

    CryptoService cryptoUtil = new CryptoService();

    @Mock
    PipelineUtil pipelineUtil = mock(PipelineUtil.class);

    @Mock
    AuditService auditService = mock(AuditService.class);

    CSRUtil csrUtil = new CSRUtil( csrRepository,  rdnRepository,  csrCommentRepository,
         rdnAttRepository,  csrAttRepository,
         badKeysService,
         cryptoUtil,  pipelineUtil,  auditService);

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        JCAManager.getInstance();
        p10ProblemReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(PARSING_PROBLEM_CSR.getBytes()));

        p10NoCNReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(NO_CN_CSR.getBytes()));
        p10CNNotEqualsSanReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(CN_NOT_EQUALS_SAN_CSR.getBytes()));
        p10CNNotInSansReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(CN_NOT_IN_SANS.getBytes()));
        p10CNMatchingOneOfManySansReq = convertPemToPKCS10CertificationRequest(new ByteArrayInputStream(CN_MATCHING_ONE_OF_SANS.getBytes()));

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

    @Test
    public void testCNinSANSet() throws GeneralSecurityException, IOException {

        assertTrue(csrUtil.isCNinSANSet(cryptoUtil.parseCertificateRequest(p10NoCNReq.getEncoded())));
        assertTrue(csrUtil.isCNinSANSet(cryptoUtil.parseCertificateRequest(p10CNMatchingOneOfManySansReq.getEncoded())));
        assertFalse(csrUtil.isCNinSANSet(cryptoUtil.parseCertificateRequest(p10CNNotInSansReq.getEncoded())));
        assertFalse(csrUtil.isCNinSANSet(cryptoUtil.parseCertificateRequest(p10CNNotEqualsSanReq.getEncoded())));

    }
}
