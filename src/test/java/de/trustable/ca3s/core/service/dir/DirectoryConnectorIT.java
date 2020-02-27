package de.trustable.ca3s.core.service.dir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.CaConfigTestConfiguration;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.schedule.ImportInfo;

@SpringBootTest(classes = Ca3SApp.class)
@Import(CaConfigTestConfiguration.class)
class DirectoryConnectorIT {

	@Autowired
	DirectoryConnector dc;
	 
	@Autowired
	CertificateRepository cr;
	 
	@Test
	void testImportCertifiateFromFile() throws IOException {

		File tmpCrt = File.createTempFile("testCertificate", ".crt");
		tmpCrt.delete();
		
		Files.copy(getClass().getClassLoader().getResourceAsStream("certificates/cert_11844.crt"), tmpCrt.toPath());
		
		ImportInfo importInfo = new ImportInfo();
		
		dc.importCertifiateFromFile(tmpCrt.getAbsolutePath(), importInfo );
		
		assertEquals(1, importInfo.getImported());
		
		List<Certificate> certList = cr.findAll();
		for(Certificate cert: certList ) {
			System.out.println("cert #" + cert.getSerial());
		}
		
//		List<Certificate> certList = cr.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SERIAL, "10000000012751161201");
		
		assertEquals(1, certList.size());
		
	}

}
