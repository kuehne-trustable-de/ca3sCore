package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.service.dto.PKCSDataType;
import de.trustable.ca3s.core.service.dto.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.CreationMode;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
@WithMockUser(value = "User")
public class ContentUploadProcessorIT {


    @Autowired
    private ContentUploadProcessor contentUploadProcessor;


    @Test
    public void checkCertificateECDSA() throws Exception {

        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/D-TRUST_Root_CA_4_2021.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("C=DE,O=D-Trust GmbH,CN=D-TRUST Root CA 4 2021", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("384", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-384", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("secp384r1", pkcsXXData.getCertificateView().getCurveName());

        }
        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/D-Trust.SMCB-CA6.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("C=DE,O=D-TRUST GmbH,OU=Institution des Gesundheitswesens-CA der Telematikinfrastruktur,CN=D-Trust.SMCB-CA6", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("256", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-256", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("brainpoolp256r1", pkcsXXData.getCertificateView().getCurveName());

        }
        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/D-Trust_GmbH ec prime.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("C=DE,O=D-Trust GmbH,CN=D-Trust GmbH,L=Berlin,organizationIdentifier=DT:DE-5728476779,PostalCode=10969,STREET=Kommandantenstraße 15,SERIALNUMBER=DTI210003118P0001,ST=Berlin", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("256", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-384", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("prime256v1", pkcsXXData.getCertificateView().getCurveName());

        }
    }



    @Test
    public void checkCertificateECDSA2() throws Exception {

        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/BC_SHA256withECDSA_Test_TA.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("CN=BC SHA256withECDSA Test TA", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("239", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-256", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("prime239v1", pkcsXXData.getCertificateView().getCurveName());

        }
    }
    @Test
    public void checkCertificateECDSA3() throws Exception {

        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/TSE_Root_CA_1.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("CN=TSE Root CA 1,O=T-Systems International GmbH,OU=Telekom Security,C=DE", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("384", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-384", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("brainpoolp384r1", pkcsXXData.getCertificateView().getCurveName());

        }
    }
    @Test
    public void checkCertificateECDSA4() throws Exception {

        {
            ClassPathResource certTestResource = new ClassPathResource("certificates/TeleSec_qualified_Root_CA_1.pem");
            PkcsXXData pkcsXXData = checkCertificate(certTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);

            assertEquals("C=DE,O=Deutsche Telekom AG,CN=TeleSec qualified Root CA 1,organizationIdentifier=USt-IdNr. DE 123475223", pkcsXXData.getCertsHolder()[0].getSubject());
            assertEquals("ec", pkcsXXData.getCertificateView().getKeyAlgorithm());
            assertEquals("521", pkcsXXData.getCertificateView().getKeyLength());
            assertEquals("sha-512", pkcsXXData.getCertificateView().getHashAlgorithm());
            assertEquals("secp521r1", pkcsXXData.getCertificateView().getCurveName());

        }
    }


    PkcsXXData checkCertificate(InputStream csrIs) throws Exception {
        return checkCertificate(new String(csrIs.readAllBytes(), StandardCharsets.UTF_8));
    }

    PkcsXXData checkCertificate(String csr) {

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setCreationMode(CreationMode.CSR_AVAILABLE);

        uploaded.setPipelineId(1L);

        uploaded.setContent(csr);

        ResponseEntity<PkcsXXData> response = contentUploadProcessor.uploadContent(uploaded);
        assertEquals(201, response.getStatusCodeValue());

        return response.getBody();
    }


    void checkPkcsxxDataOK(PkcsXXData pkcsXXData){
        assertNotNull(pkcsXXData);

        assertNotNull(pkcsXXData.getDataType());
        assertEquals(PKCSDataType.X509_CERTIFICATE, pkcsXXData.getDataType());

        if( pkcsXXData.getWarnings() != null) {
            assertEquals(0, pkcsXXData.getWarnings().length);
        }
        assertNull(pkcsXXData.getP10Holder());
        assertFalse(pkcsXXData.isCsrPublicKeyPresentInDB());

    }
}
