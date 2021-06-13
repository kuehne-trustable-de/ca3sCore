package de.trustable.ca3s.core.security.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.*;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.util.CryptoUtil;
import de.trustable.util.PKILevel;

public class Ca3sFallbackBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sFallbackBundleFactory.class);

	private KeyPair rootKeyPair = null ;
	private X509Certificate issuingCertificate = null;

	private X500Name x500Issuer;

	private CryptoUtil cryptoUtil = new CryptoUtil();


	public Ca3sFallbackBundleFactory() {
		try{
			x500Issuer = new X500Name("CN=RootOn" + InetAddress.getLocalHost().getCanonicalHostName() + ", OU=temporary bootstrap root " + System.currentTimeMillis() + ", O=trustable solutions, C=DE");
		} catch(UnknownHostException uhe) {
			LOG.debug("problem retrieving hostname", uhe);
			x500Issuer = new X500Name("CN=CA3SHost, OU=temporary bootstrap root " + System.currentTimeMillis() + ", O=trustable solutions, C=DE");
		}
	}

	private synchronized KeyPair getRootKeyPair() throws GeneralSecurityException{

		if( rootKeyPair == null) {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		    kpg.initialize(2048);
		    rootKeyPair = kpg.generateKeyPair();
			LOG.debug("created new root keypair : {}", rootKeyPair.toString());
		}
	    return rootKeyPair;
	}

	private synchronized X509Certificate getRootCertificate() throws GeneralSecurityException, IOException{

		if( issuingCertificate == null) {

			KeyPair kp = getRootKeyPair();
			issuingCertificate = cryptoUtil.issueCertificate(x500Issuer, kp, x500Issuer, kp.getPublic().getEncoded(), Calendar.MONTH, 1, PKILevel.ROOT);
			LOG.debug("created temp. root certificate with subject : {}", issuingCertificate.getSubjectDN().getName());

			File rootCertFile = File.createTempFile("ca3sTempRoot", ".cer");
			try(FileOutputStream fos = new FileOutputStream(rootCertFile)){
				fos.write(issuingCertificate.getEncoded());
			}
			LOG.debug("written temp. root certificate to file : '{}'", rootCertFile.getAbsolutePath());
		}
		return issuingCertificate;
	}

	@Override
	public KeyCertBundle newKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {


		KeyPair localKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			LOG.debug("requesting certificate for host : " + hostname );
			X500Name subject = new X500Name("CN=" + hostname);

            GeneralName[] sanArray = new GeneralName[1];
            sanArray[0] = new GeneralName(GeneralName.dNSName, hostname);
            GeneralNames gns = new GeneralNames(sanArray);

            List<Map<String, Object>> extensions = new ArrayList<>();
            Map<String, Object> serverAuthMap = new HashMap<>();
            serverAuthMap.put("oid", Extension.extendedKeyUsage.getId());
            serverAuthMap.put("critical", Boolean.FALSE);
            List<String> valList = new ArrayList<>();
            valList.add(KeyPurposeId.id_kp_serverAuth.getId());
            serverAuthMap.put("value", valList );
            extensions.add(serverAuthMap);
            LOG.debug("building certificate for SAN '{}' and EKU {}", hostname, Extension.extendedKeyUsage.getId() );

            X509Certificate issuedCertificate = cryptoUtil.issueCertificate(
                x500Issuer,
                getRootKeyPair(),
                subject,
                SubjectPublicKeyInfo.getInstance(localKeyPair.getPublic().getEncoded()),
                Calendar.HOUR, 1,
                gns,
                extensions,
                PKILevel.END_ENTITY);


			// build the (short) chain
			X509Certificate[] certificateChain = {issuedCertificate, getRootCertificate()};

			LOG.debug("returning temp. certificate : " + issuedCertificate );

			return new KeyCertBundle(bundleName, certificateChain, issuedCertificate, localKeyPair.getPrivate());

		} catch (IOException e) {
			// certificate creation failed with an exception not inheriting from 'GeneralSecurityException'
			throw new GeneralSecurityException(e);
		}

	}

}
