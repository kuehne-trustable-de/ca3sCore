package de.trustable.ca3s.core.service.dir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.CaConfigTestConfiguration;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.schedule.ImportInfo;

@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes=CaConfigTestConfiguration.class)
class DirectoryConnectorIT {

	private static final String CERTIFICATE_A_SIGN_STRONG = "certificates/cert_11844.crt";

	private static final String SUBJECT_A_SIGN_STRONG = "a-sign strong";

	@Autowired
    DirectoryConnector dc;

	@Autowired
	CertificateRepository cr;

	@Test
	void testImportCertifiateFromFile() throws IOException {

		List<Certificate> certListPreTest = cr.findAll();

		List<Certificate> certListTestCert = cr.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SUBJECT, SUBJECT_A_SIGN_STRONG);
		assertEquals(0, certListTestCert.size() );

		File tmpCrt = File.createTempFile("testCertificate", ".crt");
		tmpCrt.delete();

		Files.copy(getClass().getClassLoader().getResourceAsStream(CERTIFICATE_A_SIGN_STRONG), tmpCrt.toPath());

		ImportInfo importInfo = new ImportInfo();
        CAConnectorConfig caConnectorConfig = new CAConnectorConfig();
        caConnectorConfig.setTrustSelfsignedCertificates(false);

		dc.importCertifiateFromFile(tmpCrt.getAbsolutePath(), importInfo, caConnectorConfig);

		assertEquals(1, importInfo.getImported());

		List<Certificate> certList = cr.findAll();
		for(Certificate cert: certList ) {
			System.out.println("cert #" + cert.getSerial());
		}


		assertEquals(1, certList.size() - certListPreTest.size());

		certListTestCert = cr.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SUBJECT, SUBJECT_A_SIGN_STRONG);
		assertEquals(1, certListTestCert.size() );

	}

}
