package de.trustable.ca3s.core.service.ejbca;

import com.unboundid.util.ssl.TrustAllTrustManager;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.RandomUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

@Disabled("Disabled, not sure to have an ejbca at hand")
class EjbcaConnectorTest {

    static final String EJBCA_HOST = "192.168.3.31";
    static final int EJBCA_PORT = 443;

    private static final Logger LOGGER = LoggerFactory.getLogger(EjbcaConnectorTest.class);

    CertificateUtil certUtil = mock(CertificateUtil.class);
    Certificate cert = mock(Certificate.class);
    RandomUtil randomUtil = mock(RandomUtil.class);

    @Test
    void retrieveCertificates() throws IOException, GeneralSecurityException {

        if( !serverListening(EJBCA_HOST, EJBCA_PORT)){
            LOGGER.info("ejbca instance at '{}:{}' not available!", EJBCA_HOST, EJBCA_PORT);
            return;
        }

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
        caConfig.setCaUrl("https://" + EJBCA_HOST + ":" + EJBCA_PORT+ "/ejbca/ejbca-rest-api/v2/certificate/search");
        caConfig.setSelector("ManagementCA");
        caConfig.setLastUpdate(Instant.ofEpochSecond(0L));
        caConfig.setTlsAuthentication(cert);

        X509TrustManager trustManager = new TrustAllTrustManager();
        EjbcaConnector ejbcaConnector = new EjbcaConnector(trustManager, certUtil, randomUtil);


        ejbcaConnector.retrieveCertificates(caConfig);

    }


    public static boolean serverListening(String host, int port)
    {
        Socket s = null;
        try{
            s = new Socket(host, port);
            return true;
        }catch (Exception e){
            return false;
        }finally{
            if(s != null)
                try {s.close();}
                catch(Exception e){}
        }
    }
}
