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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
@WithMockUser(value = "User")
public class CSRContentProcessorIT {


    @Autowired
    private CSRContentProcessor csrContentProcessor;


    @Test
    public void checkCSRBrainpool() throws Exception {

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_brainpoolP160r1.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(160, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }
        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_brainpoolP256r1.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(256, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_brainpoolP512r1.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            assertEquals(512, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
            checkPkcsxxDataOK(pkcsXXData);
        }

    }

    @Test
    public void checkCSREC() throws Exception {

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_ecdsa_256.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(256, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }
        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_ecdsa_384.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(384, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_ecdsa_521.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(521, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ecdsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }

    }

    @Test
    public void checkCSREd() throws Exception {

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_ed25519.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(256, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("ed25519", pkcsXXData.getP10Holder().getKeyAlgName());
        }
    }

    @Test
    public void checkCSRRSA() throws Exception {

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_rsapkcs15_4096.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(4096, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("rsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }
        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/test_rsapss_4096.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            checkPkcsxxDataOK(pkcsXXData);
            assertEquals(4096, pkcsXXData.getP10Holder().getKeyLength());
            assertEquals("rsa", pkcsXXData.getP10Holder().getKeyAlgName());
        }

    }


    @Test
    public void checkCSRBroken() throws Exception {

        {
            ClassPathResource csrTestResource = new ClassPathResource("csr/signatureBroken.csr");
            PkcsXXData pkcsXXData = checkCSR(csrTestResource.getInputStream());
            assertNotNull(pkcsXXData);

            assertNotNull(pkcsXXData.getDataType());
            assertEquals(PKCSDataType.CSR, pkcsXXData.getDataType());

            assertTrue(pkcsXXData.getWarnings().length > 0);
        }
    }


    PkcsXXData checkCSR(InputStream csrIs) throws Exception {
        return checkCSR(new String(csrIs.readAllBytes(), StandardCharsets.UTF_8));
    }

    PkcsXXData checkCSR(String csr) {

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setCreationMode(CreationMode.CSR_AVAILABLE);

        uploaded.setPipelineId(1L);

        uploaded.setContent(csr);

        ResponseEntity<PkcsXXData> response = csrContentProcessor.describeContent(uploaded);
        assertEquals(200, response.getStatusCodeValue());

        return response.getBody();
    }

    void checkPkcsxxDataOK(PkcsXXData pkcsXXData){
        assertNotNull(pkcsXXData);

        assertNotNull(pkcsXXData.getDataType());
        assertEquals(PKCSDataType.CSR, pkcsXXData.getDataType());

        if( pkcsXXData.getWarnings() != null) {
            assertEquals(0, pkcsXXData.getWarnings().length);
        }
        assertTrue(pkcsXXData.getP10Holder().isCSRValid());
        assertFalse(pkcsXXData.isCsrPublicKeyPresentInDB());

    }
}
