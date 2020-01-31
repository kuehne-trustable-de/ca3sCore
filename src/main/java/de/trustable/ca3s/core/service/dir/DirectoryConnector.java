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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ImportedURLRepository;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CertificateUtil;

@Service
public class DirectoryConnector {


	private static final String FILE_PREFIX = "file://";
	private static final String IMPORT_SELECTOR_REGEX = ".*\\.(cer|cert|crt|pem)";

	private static final  int MAX_IMPORTS_PER_CALL = 100;
	
	
	Logger LOGGER = LoggerFactory.getLogger(DirectoryConnector.class);

	@Autowired
	CertificateUtil certUtil;
	
	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private ImportedURLRepository importedURLRepository;

	
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
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException {

		File dir = new File(getFilename(caConfig));

	    String regEx = IMPORT_SELECTOR_REGEX;
	    if( (caConfig.getSelector() != null) && (caConfig.getSelector().trim().length() > 0 ) ) {
	    	regEx = caConfig.getSelector().trim();
	    }

		LOGGER.debug("in retrieveCertificates for directory '{}' using regex '{}'", dir, regEx);


		Set<String> certSet = listFilesUsingFileWalkAndVisitor(dir.getAbsolutePath(), regEx);
		
		int n = 0;
		for( String filename: certSet) {

			boolean imported = importCertifiateFromFile(filename);
			if( imported) { n++; }
			
			if( n >= MAX_IMPORTS_PER_CALL ) {
				LOGGER.debug("retrieveCertificates: {} imported, delaying ...", MAX_IMPORTS_PER_CALL);
				break;
			}
		}

		return n;
	}


	/**
	 * 
	 * @param filename
	 */
	public boolean importCertifiateFromFile(String filename) {
		
		File certFile = new File(filename);
		LocalDate lastChangeDate = Instant.ofEpochMilli(certFile.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();;

		List<ImportedURL> impUrlList = importedURLRepository.findEntityByUrl(certFile.toURI().toString());
		if( impUrlList.isEmpty()) {
			// new item found
			try {
				byte[] content = Files.readAllBytes(Paths.get(filename));
				Certificate certDao = certUtil.createCertificate(content, null, null, false);

				// save the source of the certificate
				certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_FILE_SOURCE, filename);

				certificateRepository.save(certDao);

			} catch (GeneralSecurityException | IOException e) {
				LOGGER.info("reading and importing certificate from '{}' causes {}",
						filename, e.getLocalizedMessage());
			}

			// the import does not necessarily succeed, but we should mark the file as imported 
			ImportedURL impUrl = new ImportedURL();
			impUrl.setName(certFile.toURI().toString());
			impUrl.setImportDate(lastChangeDate);
			importedURLRepository.save(impUrl);
			
			LOGGER.debug("certificate imported from '{}'", filename);

			return true;

		}else {
			ImportedURL impUrl = impUrlList.get(0);
			if( impUrl.getImportDate().equals(lastChangeDate)) {
				LOGGER.debug("ImportedURL for '{}' has a different import date {} compared to the files lastChangeDate {}", impUrl.getName(), impUrl.getImportDate(), lastChangeDate);

				try {
					byte[] content = Files.readAllBytes(Paths.get(filename));
					Certificate certDao = certUtil.createCertificate(content, null, null, true);

					// save the source of the certificate
					certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_FILE_SOURCE, filename);
					certificateRepository.save(certDao);

					impUrl.setImportDate(lastChangeDate);
					importedURLRepository.save(impUrl);

					LOGGER.debug("certificate updated from '{}'", filename);

					return true;

				} catch (GeneralSecurityException | IOException e) {
					LOGGER.info("reading and re-importing certificate from '{}' causes {}",
							filename, e.getLocalizedMessage());
				}

			}else {
//				LOGGER.debug("certificate unchanged at '{}'", filename);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	Set<String> listFilesUsingFileWalkAndVisitor(String dir, String regEx) throws IOException {
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

        Pattern pattern = Pattern.compile(regEx);

	    Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	          throws IOException {
	            if (!Files.isDirectory(file) && Files.isReadable(file)) {
	            	String filename = file.getFileName().toString().toLowerCase().trim();
	            	
	                if( pattern.matcher(filename).matches()) {
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


