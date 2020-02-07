package de.trustable.ca3s.core.service.cmp;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.cmp.CMPException;
import org.bouncycastle.cert.crmf.CRMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.util.CryptoUtil;
import de.trustable.util.PKILevel;

@RestController
public class CMPTestEndpoint {

	@Autowired
	private CryptoUtil cryptoUtil;

	private String hmacSecret = "s3cr3t";

	private KeyPair keyPair;
	private X500Name issuer;
	private X509Certificate issuingCertificate = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(CMPTestEndpoint.class);

	public CMPTestEndpoint() {
		LOGGER.info("in Ca3sCMPEndpointTest()");
	}

	void initializeKey() throws NoSuchAlgorithmException, CertificateException, IOException {

		keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		issuer = new X500Name("CN=test root " + System.currentTimeMillis() + ", O=trustable Ltd, C=DE");
		issuingCertificate = cryptoUtil.issueCertificate(issuer, keyPair, issuer, keyPair.getPublic().getEncoded(), Calendar.MONTH, 1, PKILevel.ROOT);

	}

	@RequestMapping(value = "/cmpTest/{alias}", method = RequestMethod.POST)
	public byte[] handleCMPRequest(@PathVariable String alias, @RequestBody byte[] requestBytes)
			throws IOException, GeneralSecurityException, CRMFException, CMPException {
		if (issuingCertificate == null) {
			initializeKey();
		}

		return cryptoUtil.handleCMPRequest(alias, hmacSecret, requestBytes, issuingCertificate, issuer, keyPair);

	}

}
