package de.trustable.ca3s.core.security.provider;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.ca3s.core.service.KeyGenerationService;
import de.trustable.util.CryptoUtil;
import de.trustable.util.PKILevel;
import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.*;

public class Ca3sFallbackBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sFallbackBundleFactory.class);

	private KeyPair rootKeyPair = null ;
	private X509Certificate issuingCertificate = null;

	private final String dnSuffix;

	private X500Name x500Issuer;

	private final CryptoUtil cryptoUtil = new CryptoUtil();
    private final KeyGenerationService keyGenerationService;

    private final int fallbackCertValidity;


	public Ca3sFallbackBundleFactory(String dnSuffix, int fallbackCertValidity, KeyGenerationService keyGenerationService) {
	    this.dnSuffix = dnSuffix;
        this.fallbackCertValidity = fallbackCertValidity;
        this.keyGenerationService = keyGenerationService;
        try{
			x500Issuer = new X500Name("CN=RootOn" + InetAddress.getLocalHost().getCanonicalHostName() + ", OU=temporary bootstrap root " + System.currentTimeMillis() + ", O=trustable solutions, C=DE");
		} catch(UnknownHostException uhe) {
			LOG.debug("problem retrieving hostname", uhe);
			x500Issuer = new X500Name("CN=CA3SHost, OU=temporary bootstrap root " + System.currentTimeMillis() + ", O=trustable solutions, C=DE");
		}
	}

	private synchronized KeyPair getRootKeyPair(){

		if( rootKeyPair == null) {
            rootKeyPair = keyGenerationService.createKeyPair();
			LOG.debug("created new root keypair : {}", rootKeyPair.toString());
		}
	    return rootKeyPair;
	}

	private synchronized X509Certificate getRootCertificate() throws GeneralSecurityException, IOException{

		if( issuingCertificate == null) {

			KeyPair kp = getRootKeyPair();
			issuingCertificate = cryptoUtil.issueCertificate(x500Issuer, kp, x500Issuer, kp.getPublic().getEncoded(), Calendar.MONTH, 1, PKILevel.ROOT);
			LOG.debug("created temp. root certificate with subject : {}", issuingCertificate.getSubjectX500Principal().toString());

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


        KeyAlgoLengthOrSpec keyAlgoLengthOrSpec = new KeyAlgoLengthOrSpec("RSA", 4096);
        KeyPair localKeyPair = keyGenerationService.generateKeyPair(keyAlgoLengthOrSpec);

        try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			LOG.debug("requesting certificate for host : " + hostname );
            String x500Name = "CN=" + hostname;
            if(!dnSuffix.trim().isEmpty()){
                x500Name += ", " + dnSuffix;
            }
            X500Name subject = new X500Name(x500Name);

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
                Calendar.HOUR, fallbackCertValidity,
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
