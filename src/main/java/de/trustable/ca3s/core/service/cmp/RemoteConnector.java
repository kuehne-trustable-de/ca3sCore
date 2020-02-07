package de.trustable.ca3s.core.service.cmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RemoteConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnector.class);

	@Autowired
	Environment environment;


	/**
	 * 
	 * @param requestUrl
	 * @param requestBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] sendHttpReq(final String requestUrlParam, final byte[] requestBytes) throws IOException {

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

		// we are going to do a POST
		con.setDoOutput(true);
		con.setRequestMethod("POST");

		con.setRequestProperty("Content-Type", "application/octet-stream;charset=UTF-8");

		java.io.OutputStream os = con.getOutputStream();
		os.write(requestBytes);
		os.close();

		// Read the response
		InputStream in = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			in = con.getInputStream();

			byte[] tmpBA = new byte[4096];
			int nBytes = 0;
			while ((nBytes = in.read(tmpBA)) > 0) {
				baos.write(tmpBA, 0, nBytes);
			}
			LOGGER.debug("# " + baos.size() + " response bytes recieved");
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
