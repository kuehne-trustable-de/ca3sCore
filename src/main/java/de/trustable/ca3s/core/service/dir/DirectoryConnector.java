package de.trustable.ca3s.core.service.dir;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.schedule.spider.Crawler;
import de.trustable.ca3s.core.service.AuditService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ImportedURLRepository;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.TransactionHandler;

@Service
public class DirectoryConnector {


	private static final String FILE_PREFIX = "file://";
	private static final String IMPORT_SELECTOR_REGEX = ".*\\.(cer|cert|crt|pem|der)";

	private static final long MAX_IMPORTS_MILLISECONDS = 300L * 1000L;

	Logger LOGGER = LoggerFactory.getLogger(DirectoryConnector.class);

	@Autowired
	CertificateUtil certUtil;

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private CertificateAttributeRepository certificateAttributeRepository;

	@Autowired
	private ImportedURLRepository importedURLRepository;

    @Autowired
    private TransactionHandler transactionHandler;

    @Autowired
    private AuditService auditService;

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

        if( caConfig.getCaUrl() == null) {
            LOGGER.warn("in retrieveCertificates: url missing");
            return CAStatus.Deactivated;
        }

        String url = caConfig.getCaUrl().toLowerCase();
        if( url.startsWith("http://") ||
            url.startsWith("https://") ) {

            // check access
            try {
                int status = getHTTPResponseStatusCode(url);
                if( status >= 200 && status < 400 ) {
                    return CAStatus.Active;
                }else{
                    LOGGER.info("getStatus for url '{}' returns status  {}", url, status );
                }
            } catch (Exception e) {
                LOGGER.warn("in getStatus for url '{}' failed with message {}", url, e.getMessage() );
            }
        }else {
            File dir = new File(getFilename(caConfig));

            if (dir.exists() && dir.canRead()) {
                return CAStatus.Active;
            }else{
                LOGGER.warn("in getStatus: filename '{}', exists {}, can read {}", getFilename(caConfig), dir.exists(), dir.canRead());
            }
        }
        return CAStatus.Problem;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException {

		ImportInfo importInfo = new ImportInfo();

//	    String regEx = IMPORT_SELECTOR_REGEX;
	    String regEx = ( (caConfig.getSelector() != null) && (caConfig.getSelector().trim().length() > 0 ) ) ? caConfig.getSelector().trim(): IMPORT_SELECTOR_REGEX;


		if( caConfig.getCaUrl() == null) {
			LOGGER.warn("in retrieveCertificates: url missing");
			return 0;
		}
		String url = caConfig.getCaUrl().toLowerCase();
		if( url.startsWith("http://") ||
				url.startsWith("https://") ) {

            Crawler crawler = new Crawler();

            List<String> crawlDomains = Arrays.asList(caConfig.getCaUrl());
			for( String domain: crawlDomains) {
                Set<String> certificateSet = crawler.search(domain, regEx);
                for( String certUrl: certificateSet) {
                    importCertifiateFromURL(certUrl, importInfo);
                }
            }

        }else {

			File dir = new File(getFilename(caConfig));

			LOGGER.debug("in retrieveCertificates for directory '{}' using regex '{}'", dir, regEx);


			Set<String> certSet = listFilesUsingFileWalkAndVisitor(dir.getAbsolutePath(), regEx);

			long startTime = System.currentTimeMillis();
			for( String filename: certSet) {

				transactionHandler.runInNewTransaction(() -> importCertifiateFromFile(filename, importInfo, caConfig));

				if( (System.currentTimeMillis() - startTime) > MAX_IMPORTS_MILLISECONDS ) {
					LOGGER.debug("retrieveCertificates: imported for more than {} sec., delaying ...", MAX_IMPORTS_MILLISECONDS / 1000L);
					break;
				}
			}
		}
		return importInfo.getImported();
	}


	/**
	 *
     * @param filename
     * @param caConfig
     */
    public ImportInfo importCertifiateFromFile(String filename, ImportInfo importInfo, final CAConnectorConfig caConfig) {

        try {
            File certFile = new File(filename);

            // discard the milliseconds
            Instant lastChangeDate = Instant.ofEpochMilli((certFile.lastModified() / 1000L) * 1000L);

            List<ImportedURL> impUrlList = importedURLRepository.findEntityByUrl(certFile.toURI().toString());
            if( impUrlList.isEmpty()) {
                // new item found
                try {
                    LOGGER.debug("new certificate '{}' found, importing ...", filename);

                    byte[] content = Files.readAllBytes(Paths.get(filename));
                    Certificate certificate = certUtil.createCertificate(content, null, null, false, filename);
                    auditService.saveAuditTrace(auditService.createAuditTraceCertificateImported(filename, certificate));
                    if( caConfig.getTrustSelfsignedCertificates()){
                        if(certificate.isSelfsigned()){
                            if(certificate.isActive()) {
                                certificate.setTrusted(true);
                                certificateRepository.save(certificate);
                                auditService.saveAuditTrace(auditService.createAuditTraceCertificateTrusted(filename, certificate));
                            }else{
                                LOGGER.info("selfsigned certificate from file'{}', not active, not set as 'trusted'", filename);
                            }
                        }else{
                            LOGGER.info("'not selfsigned' certificate from file'{}', not active, not set as 'trusted'", filename);
                        }

                    }

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

                importInfo.incImported();
                return importInfo;

            }else {
                ImportedURL impUrl = impUrlList.get(0);
                if( impUrl.getImportDate().getEpochSecond() != lastChangeDate.getEpochSecond()) {
                    LOGGER.debug("ImportedURL for '{}' has a different import date {} compared to the files lastChangeDate {}",
                        impUrl.getName(), impUrl.getImportDate().getEpochSecond(), lastChangeDate.getEpochSecond());
	/*
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
	*/

                }else {
                    //				LOGGER.debug("certificate unchanged at '{}'", filename);
                }
            }

            importInfo.incRejected();
        } catch (Throwable th) {
            LOGGER.debug("certificate import failed", th);
        }
        return importInfo;
    }

    public ImportInfo importCertifiateFromURL(String url, ImportInfo importInfo) {

        try {

            Instant lastChangeDate = Instant.now();

            List<ImportedURL> impUrlList = importedURLRepository.findEntityByUrl(url);
            if( impUrlList.isEmpty()) {
                // new item found
                try {

                    LOGGER.debug("new certificate '{}' found, importing ...", url);

                    DownloadedContent downloadedContent = downloadFile(url);
                    lastChangeDate = Instant.ofEpochMilli(downloadedContent.getDate());

                    Certificate certificate = certUtil.createCertificate(downloadedContent.getContent(), null, null, false, url);
                    auditService.saveAuditTrace(auditService.createAuditTraceCertificateImported( url, certificate));

                    LOGGER.debug("certificate imported from '{}'", url);

                } catch (GeneralSecurityException | IOException e) {
                    LOGGER.info("reading and importing certificate from '{}' causes {}",
                        url, e.getLocalizedMessage());
                }

                // the import does not necessarily succeed, but we should mark the file as imported
                ImportedURL impUrl = new ImportedURL();
                impUrl.setName(url);
                impUrl.setImportDate(lastChangeDate);
                importedURLRepository.save(impUrl);

                importInfo.incImported();
                return importInfo;

            }else {
                ImportedURL impUrl = impUrlList.get(0);
                if( impUrl.getImportDate().getEpochSecond() != lastChangeDate.getEpochSecond()) {
                    LOGGER.debug("ImportedURL for '{}' has a different import date {} compared to the files lastChangeDate {}",
                        impUrl.getName(), impUrl.getImportDate().getEpochSecond(), lastChangeDate.getEpochSecond());

                }else {
                    //				LOGGER.debug("certificate unchanged at '{}'", filename);
                }
            }

            importInfo.incRejected();
        } catch (Throwable th) {
            LOGGER.debug("certificate import failed", th);
        }
        return importInfo;
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

    private int getHTTPResponseStatusCode(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        return http.getResponseCode();
    }

    private DownloadedContent downloadFile(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(conn.getInputStream(), baos);

        return new DownloadedContent(baos.toByteArray(), conn.getDate());
    }

    class DownloadedContent{
	    private byte[] content;
	    private long date;

        public DownloadedContent(byte[] content, long date){
            this.content = content;
            this.date = date;
        }

        public byte[] getContent() {
            return content;
        }

        public long getDate() {
            return date;
        }
    }
}
