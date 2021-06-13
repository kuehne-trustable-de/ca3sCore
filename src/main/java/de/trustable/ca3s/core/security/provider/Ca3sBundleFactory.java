package de.trustable.ca3s.core.security.provider;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.CryptoUtil;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ca3sBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sBundleFactory.class);

	private CAConnectorConfig caConfigDao;

	private CaConnectorAdapter cacAdapt;

	private CertificateUtil certUtil;

	public Ca3sBundleFactory(CAConnectorConfig caConfigDao, CaConnectorAdapter cacAdapt, CertificateUtil certUtil) {
		this.caConfigDao = caConfigDao;
		this.cacAdapt = cacAdapt;
		this.certUtil = certUtil;
	}


	@Override
	public KeyCertBundle newKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {


		KeyPair localKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getCanonicalHostName();
			X500Principal subject = new X500Principal("CN=" + hostname + ", OU=ca3s " + System.currentTimeMillis() + ", O=trustable solutions, C=DE");
			LOG.debug("requesting certificate for subject : " + subject.getName() );

            GeneralName[] sanArray = new GeneralName[1];
            sanArray[0] = new GeneralName(GeneralName.dNSName, hostname);

            List<Map<String, Object>> extensions = new ArrayList<>();
            Map<String, Object> serverAuthMap = new HashMap<>();
            serverAuthMap.put("oid", Extension.extendedKeyUsage.getId());
            serverAuthMap.put("critical", Boolean.FALSE);
            List<String> valList = new ArrayList<>();
            valList.add(KeyPurposeId.id_kp_serverAuth.getId());
            serverAuthMap.put("value", valList );
            extensions.add(serverAuthMap);

            PKCS10CertificationRequest req = CryptoUtil.getCsr(subject,
                localKeyPair.getPublic(), localKeyPair.getPrivate(),
                null,
                extensions,
                sanArray);

            String csrBase64 = CryptoUtil.pkcs10RequestToPem(req);

			Certificate cert = cacAdapt.signCertificateRequest(csrBase64, caConfigDao);

			// build the chain
			X509Certificate[] certificateChain = certUtil.getX509CertificateChain(cert);

			LOG.debug("returning new certificate : " + certificateChain[0] );

			return new KeyCertBundle(bundleName, certificateChain, certificateChain[0], localKeyPair.getPrivate());

		} catch (IOException e) {
			// certificate creation failed with an exception not inheriting from 'GeneralSecurityException'
			throw new GeneralSecurityException(e);
		}

	}

}
