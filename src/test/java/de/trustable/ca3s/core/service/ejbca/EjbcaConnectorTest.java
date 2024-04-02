package de.trustable.ca3s.core.service.ejbca;

import com.unboundid.util.ssl.TrustAllTrustManager;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

class EjbcaConnectorTest {

    CertificateUtil certUtil = mock(CertificateUtil.class);
    Certificate cert = mock(Certificate.class);

    @Test
    void retrieveCertificates() throws IOException, GeneralSecurityException {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        char[] passChars = "Eh/6cU!17DBZ".toCharArray();
        char[] dummyChars = "passphraseChars".toCharArray();

        keyStore.load(
            ClassLoader.getSystemClassLoader().getResourceAsStream("keystore/SuperAdminBitnami.p12"),
            passChars );

        CertificateUtil.KeyStoreAndPassphrase keyStoreAndPassphrase = new CertificateUtil.KeyStoreAndPassphrase(
            keyStore,
            passChars);

        Mockito.when(certUtil.getContainer(any(),
            eq("entryAlias"),
            eq(dummyChars),
            anyString()
        )).thenReturn(keyStoreAndPassphrase);

        Mockito.when(certUtil.createCertificate((byte[])any(),
            (CSR)any(),
            anyString(),
            eq(true),
            anyString()
        )).thenReturn(cert);


        CAConnectorConfig caConfig = new CAConnectorConfig();
        caConfig.setCaUrl("https://192.168.3.31/ejbca/ejbca-rest-api/v2/certificate/search");
        caConfig.setSelector("ManagementCA");
        caConfig.setLastUpdate(Instant.ofEpochSecond(0L));
        caConfig.setTlsAuthentication(cert);

        X509TrustManager trustManager = new TrustAllTrustManager();
        EjbcaConnector ejbcaConnector = new EjbcaConnector(trustManager, certUtil);


        ejbcaConnector.retrieveCertificates(caConfig);

    }

}
