package de.trustable.ca3s.core.service.dir;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CertificateUtil;

@Service
public class DirectoryConnector {


	private static final String FILE_PREFIX = "file://";

	Logger LOGGER = LoggerFactory.getLogger(DirectoryConnector.class);

	@Autowired
	CertificateUtil certUtil;
	
	@Autowired
	private CertificateRepository certificateRepository;

	/**
	 * 
	 */
	public DirectoryConnector() {

	}

	
	/**
	 * 
	 * @param caConfig
	 * @return
	 */
	public CAStatus getStatus(final CAConnectorConfig caConfig) {
	
		File dir = new File(getFilename(caConfig));
		if( dir.exists() && dir.canRead()) {
			return CAStatus.Active;
		}
		return CAStatus.Deactivated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.trustable.ca3s.adcs.CertificateSource#retrieveCertificates()
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException {

		File dir = new File(getFilename(caConfig));

		LOGGER.debug("in retrieveCertificates for directory '{}'", dir);

		Set<String> certSet = listFilesUsingFileWalkAndVisitor(dir.getAbsolutePath());
		for( String filename: certSet) {
			
			try {
				byte[] content = Files.readAllBytes(Paths.get(filename));
				Certificate certDao = certUtil.createCertificate(content, null, null, false);

				// the Request ID is specific to ADCS instance
				certUtil.addCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_FILE_SOURCE, filename);

				certificateRepository.save(certDao);

				LOGGER.debug("certificate imported from '{}'", filename);

			} catch (GeneralSecurityException | IOException e) {
				LOGGER.info("reading and importing certificate from '{}' causes {}",
						filename, e.getLocalizedMessage());
			}


		}

		return 1;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	Set<String> listFilesUsingFileWalkAndVisitor(String dir) throws IOException {
	    Set<String> fileList = new HashSet<>();
	    File target = new File(dir);
	    if( !target.exists() ) {
			LOGGER.warn("certificate import from '{}' failed, directory does not exist.", dir);
		    return fileList;
	    }
	    if( !target.canRead()) {
			LOGGER.warn("certificate import from '{}' failed, no read access.", dir);
		    return fileList;
	    }
	    
	    Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	          throws IOException {
	            if (!Files.isDirectory(file) && Files.isReadable(file)) {
	            	String filename = file.getFileName().toString().toLowerCase().trim();
	            	if( filename.endsWith(".cer") || filename.endsWith(".crt") || filename.endsWith(".pem") ) {
	            		fileList.add(file.toString());
	            	}else {
	            		LOGGER.debug("ignoring file {}", file.getFileName().toString());
	            	}
	            }
	            return FileVisitResult.CONTINUE;
	        }
	    });
	    return fileList;
	}


	/**
	 * 
	 * @param caConfig
	 * @return
	 */
	private String getFilename(final CAConnectorConfig caConfig) {
		String url = caConfig.getCaUrl();
		String filename = url;
		if( url.toLowerCase().trim().startsWith(FILE_PREFIX) ) {
			filename  = url.substring(FILE_PREFIX.length());
		}
		return filename;
	}
	


}


