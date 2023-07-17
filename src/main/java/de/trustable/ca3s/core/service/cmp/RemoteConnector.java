package de.trustable.ca3s.core.service.cmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

import de.trustable.ca3s.core.security.provider.Ca3sTrustManager;
import de.trustable.cmp.client.RemoteTargetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;

@Service
public class RemoteConnector implements RemoteTargetHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnector.class);

    private final Environment environment;
    private final Ca3sTrustManager ca3sTrustManager;

    public RemoteConnector(Environment environment, Ca3sTrustManager ca3sTrustManager) {
        this.environment = environment;
        this.ca3sTrustManager = ca3sTrustManager;
    }


    /**
	 *
	 * @param requestUrlParam
	 * @param requestBytes
	 * @return byte array
	 * @throws IOException
	 *
	public byte[] sendHttpReq(final String requestUrlParam, final byte[] requestBytes) throws IOException {
        return sendHttpReq( requestUrlParam, requestBytes, "application/pkixcmp", null,null, null);
    }
*/
    @Override
    public byte[] sendHttpReq(String requestUrlParam,
                              byte[] requestBytes,
                              final String contentType,
                              final String sni,
                              final boolean disableHostNameVerifier,
                              KeyStore keyStore,
                              String keyPassword) throws IOException {

        if (requestUrlParam == null) {
			throw new IllegalArgumentException("requestUrlParam can not be null.");
		}

		String port = environment.getProperty("local.server.port");

		LOGGER.debug("current port is " + port);

		String requestUrl = requestUrlParam;

		// hack for local CMP test endpoint
		if (requestUrl.contains("${server.port}")) {
			requestUrl = requestUrl.replace("${server.port}", "" + port);
		}

		if (requestUrl.contains(":0/")) {
			requestUrl = requestUrl.replace(":0/", ":" + port + "/");
		}

		LOGGER.debug("Sending request to: " + requestUrl);

		long startTime = System.currentTimeMillis();

		URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if("https".equals(url.getProtocol())) {
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
                    LOGGER.debug("using sni '{}' for CA '{}'", sni, requestUrlParam);
                    SSLParameters sslParameters = new SSLParameters();
                    List sniHostNames = new ArrayList(1);
                    sniHostNames.add(new SNIHostName(sni));
                    sslParameters.setServerNames(sniHostNames);
                    socketFactory = new SSLSocketFactoryWrapper(socketFactory, sslParameters);
                }
                HttpsURLConnection conTLS = (HttpsURLConnection)con;

                if( disableHostNameVerifier) {
                    conTLS.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }

                conTLS.setSSLSocketFactory(socketFactory);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException("problem configuring the SSLContext", e);
            } catch (UnrecoverableKeyException e) {
                throw new IOException("problem reading keystore", e);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }

        }else if("http".equals(url.getProtocol())) {
            // everything's fine, nothing to do ...
        }else{
            throw new IOException("Unexpected protocol '" + url.getProtocol() + "'");
        }

		// we are going to do a POST
		con.setDoOutput(true);
		con.setRequestMethod("POST");

        if( contentType != null) {
            con.setRequestProperty("Content-Type", contentType);
        }

		java.io.OutputStream os = con.getOutputStream();
		os.write(requestBytes);
		os.close();

		// Read the response
		InputStream in = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			in = con.getInputStream();

			byte[] tmpBA = new byte[4096];
			int nBytes;
			while ((nBytes = in.read(tmpBA)) > 0) {
				baos.write(tmpBA, 0, nBytes);
			}
			LOGGER.debug("# " + baos.size() + " response bytes received");
		} finally {
			if (in != null) {
				in.close();
			}
		}

		if (con.getResponseCode() == 200) {
			LOGGER.debug("Received certificate reply.");
		} else {
			throw new IOException("Error sending CMP request. Response codse != 200 : " + con.getResponseCode());
		}

		// We are done, disconnect
		con.disconnect();

		LOGGER.debug("duration of remote CMP call " + (System.currentTimeMillis() - startTime));

		return baos.toByteArray();
	}

}
