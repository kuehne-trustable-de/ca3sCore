package de.trustable.ca3s.core.service.ejbca;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.service.cmp.SSLSocketFactoryWrapper;
import de.trustable.ca3s.core.service.dto.ejbca.*;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EjbcaConnector {

    Logger LOGGER = LoggerFactory.getLogger(EjbcaConnector.class);

    final int PAGE_SIZE = 10;
    private final X509TrustManager ca3sTrustManager;

    private final CertificateUtil certUtil;

    public EjbcaConnector(X509TrustManager ca3sTrustManager, CertificateUtil certUtil) {
        this.ca3sTrustManager = ca3sTrustManager;
        this.certUtil = certUtil;
    }

    @Transactional
    public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException {

        ImportInfo importInfo = new ImportInfo();

        if (caConfig.getCaUrl() == null) {
            LOGGER.warn("in retrieveCertificates: url missing");
            return 0;
        }
/*
        if (caConfig.getSelector() == null  || caConfig.getSelector().isEmpty()) {
            LOGGER.warn("in retrieveCertificates: selector missing");
            return 0;
        }
*/
        SearchCertificatesRestRequestV2 searchCertificatesRestRequestV2 = new SearchCertificatesRestRequestV2(); // SearchCertificatesRestRequestV2 | Collection of search criterias and pagination information.

        SearchCertificateCriteriaRestRequest certificateCriteriaRestRequest = new SearchCertificateCriteriaRestRequest();
        certificateCriteriaRestRequest.setProperty(SearchCertificateCriteriaRestRequest.PropertyEnum.ISSUED_DATE);
        String lastUpdate = caConfig.getLastUpdate() == null ? "2018-06-27T08:07:52Z": caConfig.getLastUpdate().toString();
        certificateCriteriaRestRequest.setValue(lastUpdate);
        certificateCriteriaRestRequest.setOperation(SearchCertificateCriteriaRestRequest.OperationEnum.AFTER);

        searchCertificatesRestRequestV2.addCriteriaItem(certificateCriteriaRestRequest);

        Pagination pagination = new Pagination();
        pagination.setCurrentPage(1);
        pagination.setPageSize(PAGE_SIZE);
        searchCertificatesRestRequestV2.setPagination(pagination);

        SearchCertificateSortRestRequest searchCertificateSortRestRequest = new SearchCertificateSortRestRequest();
        searchCertificateSortRestRequest.setProperty(SearchCertificateSortRestRequest.PropertyEnum.ISSUED_DATE);
        searchCertificateSortRestRequest.setOperation(SearchCertificateSortRestRequest.OperationEnum.ASC);
        searchCertificatesRestRequestV2.setSort(searchCertificateSortRestRequest);

        try {
            Certificate certificateTlsAuthentication = caConfig.getTlsAuthentication();

            String url = caConfig.getCaUrl().toLowerCase();
            if (url.startsWith("http://") || certificateTlsAuthentication == null ) {
                importInfo = invokeRestEndpoint(caConfig,
                    importInfo,
                    url,
                    marshallRequest(searchCertificatesRestRequestV2 ),
                    null, // SNI
                    true, // disableHostNameVerifier
                    null,
                    null
                );
            } else if (url.startsWith("https://")) {
                CertificateUtil.KeyStoreAndPassphrase keyStoreAndPassphrase =
                    certUtil.getContainer(certificateTlsAuthentication,
                        "entryAlias",
                        "passphraseChars".toCharArray(),
                        "PBEWithHmacSHA256AndAES_256");

                importInfo = invokeRestEndpoint(caConfig,
                    importInfo,
                    url,
                    marshallRequest(searchCertificatesRestRequestV2 ),
                    null, // SNI
                    true, // disableHostNameVerifier
                    keyStoreAndPassphrase.getKeyStore(),
                    new String(keyStoreAndPassphrase.getPassphraseChars()) );
            } else {
                return 0;
            }

        } catch (IOException e) {
            LOGGER.debug("problem retrieving certificates from ejbca", e);
            throw e;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        return importInfo.getImported();
    }


    private ImportInfo invokeRestEndpoint(CAConnectorConfig caConfig,
                                          ImportInfo importInfo,
                                          String requestUrl,
                                          byte[] requestBytes,
                                          final String sni,
                                          final boolean disableHostNameVerifier,
                                          KeyStore keyStore,
                                          String keyPassword) throws IOException {

        LOGGER.debug("Sending request to: " + requestUrl);

        long startTime = System.currentTimeMillis();

        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if ("https".equals(url.getProtocol())) {
            try {
                KeyManager[] keyManagers = null;
                if (keyStore != null) {
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                    keyManagerFactory.init(keyStore, keyPassword.toCharArray());
                    keyManagers = keyManagerFactory.getKeyManagers();
                    LOGGER.debug("using client keystore");
                }

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(keyManagers,
                    new TrustManager[]{ca3sTrustManager},
                    new java.security.SecureRandom());

                SSLSocketFactory socketFactory = sc.getSocketFactory();
                if (sni != null && !sni.trim().isEmpty()) {
                    LOGGER.debug("using sni '{}' for CA '{}'", sni, requestUrl);
                    SSLParameters sslParameters = new SSLParameters();
                    List sniHostNames = new ArrayList(1);
                    sniHostNames.add(new SNIHostName(sni));
                    sslParameters.setServerNames(sniHostNames);
                    socketFactory = new SSLSocketFactoryWrapper(socketFactory, sslParameters);
                }
                HttpsURLConnection conTLS = (HttpsURLConnection) con;

                if (disableHostNameVerifier) {
                    conTLS.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }

                conTLS.setSSLSocketFactory(socketFactory);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException("problem configuring the SSLContext", e);
            } catch (
                UnrecoverableKeyException e) {
                throw new IOException("problem reading keystore", e);
            } catch (
                KeyStoreException e) {
                throw new RuntimeException(e);
            }

        } else if ("http".equals(url.getProtocol())) {
            // everything's fine, nothing to do ...
        } else {
            throw new IOException("Unexpected protocol '" + url.getProtocol() + "'");
        }

        // we are going to do a POST
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        java.io.OutputStream os = con.getOutputStream();
        os.write(requestBytes);
        os.close();

        byte[] responseArr = IOUtils.toByteArray(con.getInputStream());
        LOGGER.debug("Received response bytes: {}", new String(responseArr));
        // Read the response
        parseResponse( responseArr, importInfo, caConfig );

        if (con.getResponseCode() == 200) {
            LOGGER.debug("Received certificate reply.");
        } else {
            throw new IOException("Error sending CMP request. Response code != 200 : " + con.getResponseCode());
        }

        // We are done, disconnect
        con.disconnect();

        LOGGER.debug("duration of remote EJBCA inventory call " + (System.currentTimeMillis() - startTime));

        return importInfo;
    }

    byte[] marshallRequest(final SearchCertificatesRestRequestV2 searchCertificatesRestRequestV2 ) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        objectMapper.writeValue(boas, searchCertificatesRestRequestV2);

        LOGGER.debug("in retrieveCertificates: query: {}", boas.toString());

        return boas.toByteArray();
    }

    void parseResponse( byte[] responseArr, ImportInfo importInfo,final CAConnectorConfig caConfig ){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SearchCertificatesRestResponseV2 certificateSearchResponse =
                objectMapper.readValue(responseArr, SearchCertificatesRestResponseV2.class);

            for(CertificateRestResponseV2 certificateRestResponse : certificateSearchResponse.getCertificates()){

                String desc = ( certificateRestResponse.getSubjectDN() == null ? "" : certificateRestResponse.getSubjectDN() ) + ", #" + certificateRestResponse.getSerialNumber();
                if( certificateRestResponse.getBase64Cert() != null){

                    CSR csr = null;
                    if( certificateRestResponse.getCertificateRequest() != null ) {
                        LOGGER.info("CertificateRestResponseV2 contains csr!");
                    }

                    byte[] certBytes = Base64.decodeBase64(
                        Base64.decodeBase64(certificateRestResponse.getBase64Cert()));
                    try {
                        try {
                            Certificate certificate = certUtil.createCertificate(certBytes, csr, "", true, caConfig.getCaUrl());
                            LOGGER.debug("imported certificate: {}", certificate);
                            importInfo.incImported();
                        } catch (Exception e) {
                            LOGGER.info("CertificateRestResponseV2: processing certificate for '{}' failed: {} ", desc, e.getMessage());
                            importInfo.incRejected();
                        }

                        if(certificateRestResponse.getNotBefore() != null) {
                            LOGGER.info("CertificateRestResponseV2 contains UpdateTime: {}", certificateRestResponse.getUdpateTime());
                            Instant notBeforeInstant = Instant.ofEpochMilli(certificateRestResponse.getNotBefore());
                            Instant notBeforeConfig = caConfig.getLastUpdate() == null ? Instant.MIN: caConfig.getLastUpdate();
                            if (notBeforeInstant.isAfter(notBeforeConfig)) {
                                LOGGER.info("caConfig.setLastUpdate( {} )", certificateRestResponse.getUdpateTime());
                                caConfig.setLastUpdate(notBeforeInstant);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.info("CertificateRestResponseV2: processing last update failed: {} ", e.getMessage());
                    }

                }else{
                    LOGGER.info("CertificateRestResponseV2 does not contain base64 data for {}", desc);
                }
            }

        } catch (IOException e) {
            LOGGER.info("unmarshalling CertificateRestResponseV2", e);
        }

    }
}
