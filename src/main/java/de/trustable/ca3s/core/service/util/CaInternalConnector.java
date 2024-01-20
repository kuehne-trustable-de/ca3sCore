package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.util.CryptoUtil;
import de.trustable.util.PKILevel;

@Service
public class CaInternalConnector {


	private static final Logger LOG = LoggerFactory.getLogger(CaInternalConnector.class);

    @Autowired
    CertificateRepository certRepository;

    @Autowired
    CSRRepository csrRepository;

    @Autowired
    CryptoUtil cryptoUtil;

    @Autowired
    CertificateUtil certUtil;

    @Autowired
    CSRUtil csrUtil;

    @Autowired
    KeyUtil keyUtil;

	Certificate getRoot() throws GeneralSecurityException, IOException {

		List<Certificate> certList = certRepository.findByAttributeValue( CertificateAttribute.ATTRIBUTE_CA3S_ROOT, "true");

		Certificate certRoot = getLongestValidCertificate(certList);
		if( certRoot == null ) {
			certRoot = createNewRoot();
		}

		return certRoot;

	}

	Certificate getIntermediate() throws GeneralSecurityException, IOException {

		List<Certificate> certList = certRepository.findByAttributeValue( CertificateAttribute.ATTRIBUTE_CA3S_INTERMEDIATE, "true");

		Certificate certIntermediate = getLongestValidCertificate(certList);
		if( certIntermediate == null ) {
			certIntermediate = createNewIntermediate( getRoot() );
		}

		return certIntermediate;

	}

	private Certificate createNewIntermediate(Certificate root) throws GeneralSecurityException, IOException {

		KeyPair keyPair = keyUtil.createKeyPair();

		X500Name subject = new X500Name("CN=CA3S-Intermediate"
				+ System.currentTimeMillis()
				+ ", OU=Internal Only, OU=Dev/Test Only, O=trustable solutions, C=DE");

		PrivateKey privKeyRoot = certUtil.getPrivateKey(root);
		KeyPair kpRoot = new KeyPair(CertificateUtil.convertPemToCertificate(root.getContent()).getPublicKey(), privKeyRoot);

		X509Certificate x509Cert = cryptoUtil.issueCertificate(normalizeX500Name(new X500Name(root.getSubject())), kpRoot, normalizeX500Name(subject), keyPair.getPublic().getEncoded(), Calendar.YEAR, 1, PKILevel.INTERMEDIATE);

		Certificate intermediateCert = certUtil.createCertificate(x509Cert.getEncoded(), null, "", false);

		certUtil.storePrivateKey(intermediateCert, keyPair);

		certUtil.setCertAttribute(intermediateCert, CertificateAttribute.ATTRIBUTE_CA3S_INTERMEDIATE, "true");

		certRepository.save(intermediateCert);

        LOG.info("############# createNewIntermediate returns cert #{}, serial {}", intermediateCert.getId(), intermediateCert.getSerial());

		return intermediateCert;
	}

    X500Name normalizeX500Name( X500Name dn){

        X500NameBuilder x500NameBuilder = new X500NameBuilder(X500Name.getDefaultStyle());

        addExistingRDN(BCStyle.C, dn, x500NameBuilder);
        addExistingRDN(BCStyle.OU, dn, x500NameBuilder);
        addExistingRDN(BCStyle.O, dn, x500NameBuilder);
        addExistingRDN(BCStyle.CN, dn, x500NameBuilder);

        return x500NameBuilder.build();

    }

    private void addExistingRDN(ASN1ObjectIdentifier identifier, X500Name dn, X500NameBuilder x500NameBuilder) {
        RDN[] rdnArr = dn.getRDNs(identifier);
        for(RDN rdn: rdnArr) {
            x500NameBuilder.addRDN(identifier, rdn.getFirst().getValue().toString());
        }
    }

    /**
	 *
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private Certificate createNewRoot() throws GeneralSecurityException, IOException {

        KeyPair keyPair = keyUtil.createKeyPair();

		X500Name subject = new X500Name("CN=CA3S-InternalRoot"
				+ System.currentTimeMillis()
				+ ", OU=Internal Only, OU=Dev/Test Only, O=trustable solutions, C=DE");

		X509Certificate x509Cert = cryptoUtil.issueCertificate(
            normalizeX500Name(subject),
            keyPair,
            normalizeX500Name(subject),
            keyPair.getPublic().getEncoded(),
            Calendar.YEAR, 1, PKILevel.ROOT);

		Certificate rootCert = certUtil.createCertificate(x509Cert.getEncoded(), null, "", false);

		certUtil.storePrivateKey(rootCert, keyPair);

        rootCert.setTrusted(true);
        certUtil.setCertAttribute(rootCert, CertificateAttribute.ATTRIBUTE_CA3S_ROOT, "true");

		certRepository.save(rootCert);

        LOG.info("############# createNewRoot returns cert #{}, serial {}", rootCert.getId(), rootCert.getSerial());

        return rootCert;
	}

	/**
	 *
	 * @param certList
	 * @return
	 */
	private Certificate getLongestValidCertificate(List<Certificate> certList) {
		Instant now = Instant.now();
		Certificate certLongestValid = null;

		for( Certificate cert:certList) {
			if( now.isAfter(cert.getValidFrom()) && now.isBefore(cert.getValidTo())){
				if( certLongestValid == null ) {
					certLongestValid = cert;
				}else {
					if( certLongestValid.getValidTo().isBefore(cert.getValidTo())) {
						certLongestValid = cert;
					}
				}
			}
		}
		return certLongestValid;
	}

	public Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConfig) throws GeneralSecurityException {

		try {

			csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_STARTED_TIMESTAMP,"" + System.currentTimeMillis(), false);

			csr.setStatus(CsrStatus.PROCESSING);

			Certificate intermediate = getIntermediate();

			PrivateKey privKeyIntermediate = certUtil.getPrivateKey(intermediate);
			KeyPair kpIntermediate = new KeyPair(CertificateUtil.convertPemToCertificate(intermediate.getContent()).getPublicKey(), privKeyIntermediate);

			PKCS10CertificationRequest p10 = cryptoUtil.convertPemToPKCS10CertificationRequest(csr.getCsrBase64());

            GeneralNames gns = null;
            org.bouncycastle.asn1.pkcs.Attribute[] certAttributes = p10.getAttributes();
            for (org.bouncycastle.asn1.pkcs.Attribute attribute : certAttributes) {
                if (attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    Extensions extensions = Extensions.getInstance(attribute.getAttrValues().getObjectAt(0));
                    gns = GeneralNames.fromExtensions(extensions, Extension.subjectAlternativeName);
                }
            }

            X509Certificate x509Cert = cryptoUtil.issueCertificate(
                normalizeX500Name(new X500Name(intermediate.getSubject())),
                kpIntermediate,
                normalizeX500Name(p10.getSubject()),
                p10.getSubjectPublicKeyInfo(),
                Calendar.YEAR, 1,
                gns,
                null,
                PKILevel.END_ENTITY);

			Certificate cert = certUtil.createCertificate(x509Cert.getEncoded(), csr, "", false);
			cert.setRevocationCA(caConfig);

			certRepository.save(cert);

			csrUtil.setCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP,"" + System.currentTimeMillis(), true);

			csr.setStatus(CsrStatus.ISSUED);
			csrRepository.save(csr);

			return cert;

		} catch (IOException e) {
			LOG.info("Problem signing certificate request", e);
			throw new GeneralSecurityException(e);

		}

		/*
		RDN[] rdnArr = new RDN[csr.getRdns().size()];

		int i = 0;
		for(de.trustable.ca3s.core.domain.RDN rdn:csr.getRdns()) {
			LOG.debug("RDN contains #{}", rdn.getRdnAttributes().size());
			int attLen = rdn.getRdnAttributes().size();
			AttributeTypeAndValue[] atavArr = new AttributeTypeAndValue[attLen];
			int j = 0;
			for(RDNAttribute rdnAtt: rdn.getRdnAttributes()) {
				AttributeTypeAndValue atav = new AttributeTypeAndValue( rdnAtt.getAttributeType(), new DEROctetString(rdnAtt.getAttributeValue().getBytes()));
			}
			rdnArr[i++] = new RDN(atav);
		}
		X500Name subject = new X500Name(csr.getRdns());
*/

	}

	public void revokeCertificate(Certificate cert, CRLReason crlReason, Date revocationDate,
			CAConnectorConfig caConfig) {

		if (cert.isRevoked()) {
			LOG.warn("failureReason: " +
					"certificate with id '" + cert.getId() + "' already revoked.");
		}

		String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
		LOG.debug("crlReason : " + crlReasonStr);

		cert.setActive(false);
		cert.setRevoked(true);
		cert.setRevokedSince(Instant.now());
		cert.setRevocationReason(crlReasonStr);

		certRepository.save(cert);

	}

}
