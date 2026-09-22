package de.trustable.ca3s.core.service.dir;


import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ImportedURLRepository;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.schedule.spider.Crawler;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.dto.CaConnectorConfigView;
import de.trustable.ca3s.core.service.exception.CertificateAlreadyExistsException;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.util.CryptoUtil;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DirectoryConnector {


	private static final String FILE_PREFIX = "file://";
	private static final String IMPORT_SELECTOR_REGEX = ".*\\.(cer|cert|crt|pem|der)";

	private static final long MAX_IMPORTS_MILLISECONDS = 300L * 1000L;

	Logger LOGGER = LoggerFactory.getLogger(DirectoryConnector.class);

    private final CertificateUtil certUtil;
    private final CryptoUtil cryptoUtil;
    private final ProtectedContentUtil protUtil;
    private final CaConnectorConfigUtil caConnectorConfigUtil;
    private final CAConnectorConfigRepository caConnectorConfigRepository;
    private final CertificateRepository certificateRepository;
    private final ImportedURLRepository importedURLRepository;
    private final TransactionHandler transactionHandler;
    private final PasswordMasker passwordMasker;
    private final AuditService auditService;

    /**
	 *
	 */
	public DirectoryConnector(CertificateUtil certUtil, CryptoUtil cryptoUtil, ProtectedContentUtil protUtil, CaConnectorConfigUtil caConnectorConfigUtil, CAConnectorConfigRepository caConnectorConfigRepository, CertificateRepository certificateRepository, ImportedURLRepository importedURLRepository, TransactionHandler transactionHandler, PasswordMasker passwordMasker, AuditService auditService) {

        this.certUtil = certUtil;
        this.cryptoUtil = cryptoUtil;
        this.protUtil = protUtil;
        this.caConnectorConfigUtil = caConnectorConfigUtil;
        this.caConnectorConfigRepository = caConnectorConfigRepository;
        this.certificateRepository = certificateRepository;
        this.importedURLRepository = importedURLRepository;
        this.transactionHandler = transactionHandler;
        this.passwordMasker = passwordMasker;
        this.auditService = auditService;
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
                if (status >= 200 && status < 400) {
                    return CAStatus.Active;
                } else {
                    LOGGER.info("getStatus for url '{}' returns status  {}", url, status);
                }
            } catch (Exception e) {
                LOGGER.warn("in getStatus for url '{}' failed with message {}", url, e.getMessage());
            }
        }else if( url.startsWith("jdbc:") ) {

            Connection connection = null;
            try {
                connection = connectWithDatabase(caConfig);
                return CAStatus.Active;
            } catch (SQLException e) {
                LOGGER.warn("in getStatus for url '{}' failed with message {}", url, e.getMessage());
            }finally{
                if( connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.error("Error closing database connection", e);
                    }
                }
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

    private Connection connectWithDatabase(final CAConnectorConfig caConfig) throws SQLException {

        final Properties props = new Properties();

        String userName = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConfig, CaConnectorConfigUtil.ATT_ISSUER_NAME, null);
        String plainSecret = protUtil.unprotectString(caConfig.getSecret().getContentBase64());
        LOGGER.debug("Database user name '{}' with password '{]'", userName, passwordMasker.maskPassword(plainSecret));

        props.setProperty("user", userName);
        props.setProperty("password", plainSecret);

        Connection conn = DriverManager.getConnection(caConfig.getCaUrl(), props);
        LOGGER.debug("in connectWithDatabase: database connection url '{}' for user '{}' using password '*****' succeeded",
            caConfig.getCaUrl(), props.getProperty("user"));
        return conn;
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
                    importCertifiateFromURL(certUrl, importInfo, caConfig);
                }
            }

        }else if( url.startsWith("jdbc:") ) {
            Connection connection = null;
            try {
                connection = connectWithDatabase(caConfig);
                importCertificateFromDB(connection, importInfo, caConfig);
            } catch (SQLException e) {
                LOGGER.warn("in retrieveCertificates for url '{}' failed with message {}", url, e.getMessage());
            }finally{
                if( connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.error("Error closing database connection", e);
                    }
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

    private void importCertificateFromDB(Connection connection, ImportInfo importInfo, CAConnectorConfig caConfig) {

        int pollingOffset = 0;
        if (caConfig.getPollingOffset() != null) {
            pollingOffset = caConfig.getPollingOffset();
        }

        Instant lastUpdate = caConfig.getLastUpdate();

        CaConnectorConfigView caConfigView = caConnectorConfigUtil.from(caConfig);

        String query = "select ";
        String where = "where 1=1 ";

        // chack arguments, again ...
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*$");
        Matcher m = pattern.matcher(caConfigView.getCertificateTable());
        if (!m.find()){
            LOGGER.warn("importCertificateFromDB: table name '{}' contains unexpected characters", caConfigView.getCertificateTable());
            return;
        }

        if (!pattern.matcher(caConfigView.getCertificateColumn()).find()){
            LOGGER.warn("importCertificateFromDB: certificate column name '{}' contains unexpected characters", caConfigView.getCertificateColumn());
            return;
        }
        query += caConfigView.getCertificateColumn();

        boolean hasSequenceColumn = false;
        if( caConfigView.getSequenceColumn() != null && !caConfigView.getSequenceColumn().trim().isEmpty()) {
            if (!pattern.matcher(caConfigView.getSequenceColumn()).find()){
                LOGGER.warn("importCertificateFromDB: sequence column name '{}' contains unexpected characters", caConfigView.getSequenceColumn());
                return;
            }
            query += ", " + caConfigView.getSequenceColumn();
            where += " and " + caConfigView.getSequenceColumn() + " > " + pollingOffset;
            hasSequenceColumn = true;
        }

        boolean hasLastUpdateColumn = false;
        if( caConfigView.getLastUpdateColumn() != null && !caConfigView.getLastUpdateColumn().trim().isEmpty()) {
            if (!pattern.matcher(caConfigView.getLastUpdateColumn()).find()){
                LOGGER.warn("importCertificateFromDB: last Update column name '{}' contains unexpected characters", caConfigView.getLastUpdateColumn());
                return;
            }
            query += ", " + caConfigView.getLastUpdateColumn();

            where += " and " + caConfigView.getLastUpdateColumn() + " > '" + lastUpdate.toString() + "'";
            hasLastUpdateColumn = true;
        }

        query += " from " + caConfigView.getCertificateTable();

        LOGGER.warn("importCertificateFromDB: execute query '{}'", query);

        try (PreparedStatement prpStatement = connection.prepareStatement(query)) {

            int maxSequence = 0;
            long maxLastUpdateMilliSec = 0L;
            ResultSet resultSet = prpStatement.executeQuery();
            while (resultSet.next()) {
                // process resultSet
                String certificateString = resultSet.getString(caConfigView.getCertificateColumn());
                if( hasSequenceColumn ){
                    int sequence = resultSet.getInt(caConfigView.getSequenceColumn());
                    if( sequence > maxSequence ) {
                        maxSequence = sequence;
                    }
                }
                if( hasLastUpdateColumn ){
                    long lastUpdateMilliSec = resultSet.getDate(caConfigView.getLastUpdateColumn()).getTime();
                    if( lastUpdateMilliSec > maxLastUpdateMilliSec ) {
                        maxLastUpdateMilliSec = lastUpdateMilliSec;
                    }
                }

                importCertifiateFromDBColumn(certificateString, importInfo, caConfig);
            }

            if( hasSequenceColumn){
                caConfig.setPollingOffset(maxSequence);
            }
            if( hasLastUpdateColumn){
                caConfig.setLastUpdate(Instant.ofEpochMilli(maxLastUpdateMilliSec));
            }
            caConnectorConfigRepository.save(caConfig);

        } catch (SQLException sqle) {
            LOGGER.warn("importCertificateFromDB: query '{}' execution failed with exception", query, sqle);
        }
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
                    auditService.saveAuditTrace(auditService.createAuditTraceCertificateImported(filename, certificate, caConfig));
                    if( caConfig.getTrustSelfsignedCertificates()){
                        if(certificate.isSelfsigned()){
                            if(certificate.isActive()) {
                                certificate.setTrusted(caConfig.getTrustSelfsignedCertificates());
                                certificateRepository.save(certificate);
                                auditService.saveAuditTrace(auditService.createAuditTraceCertificateTrusted(filename, certificate, caConfig));
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

    /**
     *
     */
    public ImportInfo importCertifiateFromDBColumn(String content, ImportInfo importInfo, final CAConnectorConfig caConfig) {

        try {
            LOGGER.debug("importing certificate from DB ...");

            X509Certificate x509Cert = certUtil.tryParsingCertificateContent(content);

            Certificate certificate = certUtil.createCertificate(cryptoUtil.x509CertToPem(x509Cert), null, null,
                false,
                "database column",
                true);

            auditService.saveAuditTrace(auditService.createAuditTraceCertificateImported("database column", certificate, caConfig));

            if (caConfig.getTrustSelfsignedCertificates()) {
                if (certificate.isSelfsigned()) {
                    if (certificate.isActive()) {
                        certificate.setTrusted(caConfig.getTrustSelfsignedCertificates());
                        certificateRepository.save(certificate);
                        auditService.saveAuditTrace(auditService.createAuditTraceCertificateTrusted("database column", certificate, caConfig));
                    } else {
                        LOGGER.info("selfsigned certificate from database column, not active, not set as 'trusted'");
                    }
                } else {
                    LOGGER.info("'not selfsigned' certificate from database column, not active, not set as 'trusted'");
                }
            }

        }catch (CertificateAlreadyExistsException e) {
            LOGGER.info("certificate already exists in database, skipping import from database column");
        } catch (GeneralSecurityException | IOException e) {
            LOGGER.info("reading and importing certificate from database column causes {}",
                e.getLocalizedMessage());

            importInfo.incRejected();
        }

         /*
            // the import does not necessarily succeed, but we should mark the file as imported
            ImportedURL impUrl = new ImportedURL();
            impUrl.setName(certFile.toURI().toString());
            impUrl.setImportDate(lastChangeDate);
            importedURLRepository.save(impUrl);
         */

        importInfo.incImported();
        return importInfo;

    }

    public ImportInfo importCertifiateFromURL(String url, ImportInfo importInfo, CAConnectorConfig caConfig) {

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
                    auditService.saveAuditTrace(auditService.createAuditTraceCertificateImported( url, certificate, caConfig));

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
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
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

    static class DownloadedContent{
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
