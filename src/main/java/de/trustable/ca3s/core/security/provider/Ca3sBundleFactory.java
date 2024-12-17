package de.trustable.ca3s.core.security.provider;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.KeyUtil;
import de.trustable.util.CryptoUtil;
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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ca3sBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sBundleFactory.class);

	private final CAConnectorConfig caConfigDao;

	private final CaConnectorAdapter cacAdapt;

	private final CertificateUtil certUtil;

    private final CertificateRepository certificateRepository;

    private final KeyUtil keyUtil;
    private final String dnSuffix;

    private final String sans;

    private final KeyPersistenceType keyPersistenceType;

    public Ca3sBundleFactory(CAConnectorConfig caConfigDao,
                             CaConnectorAdapter cacAdapt,
                             CertificateUtil certUtil,
                             CertificateRepository certificateRepository,
                             KeyUtil keyUtil,
                             String dnSuffix,
                             String sans,
                             String persist) {
		this.caConfigDao = caConfigDao;
		this.cacAdapt = cacAdapt;
		this.certUtil = certUtil;
        this.certificateRepository = certificateRepository;
        this.keyUtil = keyUtil;
        this.dnSuffix = dnSuffix;
        this.sans = sans;
        this.keyPersistenceType = KeyPersistenceType.valueOf(persist);
        LOG.debug("keyPersistenceType : " + keyPersistenceType );
    }


	@Override
	public KeyCertBundle newKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {

        if( KeyPersistenceType.DB.equals(keyPersistenceType)){
            LOG.debug("Storing TLS certificate in database." );
            return newDBKeyBundle(bundleName, minValiditySeconds);
        }else if( KeyPersistenceType.FILE.equals(keyPersistenceType)){
            LOG.warn("Storing of TLS certificate in file not implemented, yet! Falling bak to 'NO' persistence." );
        }

        return createKeyBundle(bundleName, minValiditySeconds).getKeyCertBundle();
    }

    public KeyCertBundle newDBKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {

        Certificate currentCertificate = null;

        List<Certificate> certDaoList = certificateRepository.findActiveTLSCertificate();

        if(!certDaoList.isEmpty()){
            Certificate certificate = certDaoList.get(0);
            LOG.debug("Found TLS certificate {} in database.", certificate.getId() );

            Instant minValidTo = Instant.now().plusSeconds(minValiditySeconds);
            if( certificate.getValidTo().isAfter(minValidTo)){
                LOG.debug("Found TLS certificate {} valid long enough: {}", certificate.getId(), certificate.getValidTo() );
                currentCertificate = certificate;
            }else{
                LOG.info("Current TLS certificate {} not valid long enough: {}", certificate.getId(), certificate.getValidTo() );
            }
        }

        if(currentCertificate == null){
            LOG.debug("Creating new TLS certificate." );
            BundleCertHolder bundleCertHolder = createKeyBundle(bundleName, minValiditySeconds);
            KeyCertBundle keyCertBundle = bundleCertHolder.getKeyCertBundle();
            KeyPair keyPair = new KeyPair(keyCertBundle.getCertificate().getPublicKey(), (PrivateKey)keyCertBundle.getKey());
            try {
                certUtil.storePrivateKey(bundleCertHolder.getCertificate(), keyPair);
                certUtil.setCertAttribute(bundleCertHolder.getCertificate(), CertificateAttribute.ATTRIBUTE_TLS_KEY, "true");
            } catch (IOException e) {
                LOG.warn("problem storing key and certificate ", e );
                throw new GeneralSecurityException(e.getMessage());
            }
            return keyCertBundle;
        }else{
            LOG.debug("Using TLS certificate {} from database.", currentCertificate.getId() );

            X509Certificate[] certificateChain = certUtil.getX509CertificateChain(currentCertificate);
            PrivateKey privateKey = certUtil.getPrivateKey(currentCertificate);
            return new KeyCertBundle(bundleName, certificateChain, certificateChain[0], privateKey);
        }
    }

    public BundleCertHolder createKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {

        KeyPair localKeyPair = keyUtil.createKeyPair();

		try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getCanonicalHostName();
			String x500Name = "CN=" + hostname;
			if(!dnSuffix.trim().isEmpty()){
                x500Name += ", " + dnSuffix;
            }
            X500Principal subject = new X500Principal(x500Name);
			LOG.debug("requesting certificate for subject : " + subject.getName() );

            GeneralName[] sanArray = CertificateUtil.splitSANString(sans, hostname);

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
            certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_TLS_CERTIFICATE, "true");

			// build the chain
			X509Certificate[] certificateChain = certUtil.getX509CertificateChain(cert);

			LOG.debug("returning new certificate : " + certificateChain[0] );


            return new BundleCertHolder(
                new KeyCertBundle(bundleName, certificateChain, certificateChain[0], localKeyPair.getPrivate()),
                cert );
		} catch (IOException e) {
			// certificate creation failed with an exception not inheriting from 'GeneralSecurityException'
			throw new GeneralSecurityException(e);
		}

	}

}

class BundleCertHolder{

    private final KeyCertBundle keyCertBundle;
    private final Certificate certificate;

    public BundleCertHolder(KeyCertBundle keyCertBundle, Certificate certificate){
        this.keyCertBundle = keyCertBundle;
        this.certificate = certificate;
    }

    public KeyCertBundle getKeyCertBundle() {
        return keyCertBundle;
    }

    public Certificate getCertificate() {
        return certificate;
    }
}
