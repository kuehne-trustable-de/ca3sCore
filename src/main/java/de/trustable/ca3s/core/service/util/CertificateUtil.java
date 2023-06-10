package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.repository.CertificateCommentRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CRLUpdateInfo;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.PBEParameterSpec;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.time.Instant;
import java.util.*;


@Service
public class CertificateUtil {

    private static final String SERIAL_PADDING_PATTERN = "000000000000000000000000000000000000000000000000000000000000000";

    private static final String TIMESTAMP_PADDING_PATTERN = "000000000000000000";
    public static final int CURRENT_ATTRIBUTES_VERSION = 4;

    static HashSet<Integer> lenSet = new HashSet<>();

    static {
        lenSet.add(256);
        lenSet.add(512);
        lenSet.add(1024);
        lenSet.add(2048);
        lenSet.add(3072);
        lenSet.add(4096);
        lenSet.add(6144);
        lenSet.add(8192);
    }

    private static final Map<ASN1ObjectIdentifier, Integer> dnOrderMap = createDnOrderMap();

    private static final Logger LOG = LoggerFactory.getLogger(CertificateUtil.class);

    final private CertificateRepository certificateRepository;

    final private CertificateAttributeRepository certificateAttributeRepository;

    final private CertificateCommentRepository certificateCommentRepository;

    final private ProtectedContentRepository protContentRepository;

    final private ProtectedContentUtil protUtil;

    private final PreferenceUtil preferenceUtil;

    final private CryptoService cryptoUtil;

    final private AuditService auditService;

    @Autowired
    public CertificateUtil(CertificateRepository certificateRepository, CertificateAttributeRepository certificateAttributeRepository, CertificateCommentRepository certificateCommentRepository, ProtectedContentRepository protContentRepository, ProtectedContentUtil protUtil, PreferenceUtil preferenceUtil, CryptoService cryptoUtil, AuditService auditService) {
        this.certificateRepository = certificateRepository;
        this.certificateAttributeRepository = certificateAttributeRepository;
        this.certificateCommentRepository = certificateCommentRepository;
        this.protContentRepository = protContentRepository;
        this.protUtil = protUtil;
        this.preferenceUtil = preferenceUtil;
        this.cryptoUtil = cryptoUtil;
        this.auditService = auditService;
    }

    private static Map<ASN1ObjectIdentifier, Integer> createDnOrderMap() {
        Map<ASN1ObjectIdentifier, Integer> orderMap = new HashMap<>();
        int count = 0;
        orderMap.put(BCStyle.C, count++);
        orderMap.put(BCStyle.O, count++);
        orderMap.put(BCStyle.OU, count++);
        orderMap.put(BCStyle.CN, count++);
        orderMap.put(BCStyle.L, count++);
        orderMap.put(BCStyle.ST, count++);
        orderMap.put(BCStyle.STREET, count++);
        orderMap.put(BCStyle.DC, count++);
        orderMap.put(BCStyle.UID, count++);
        orderMap.put(BCStyle.E, count++);
        orderMap.put(BCStyle.BUSINESS_CATEGORY, count++);
        orderMap.put(BCStyle.COUNTRY_OF_CITIZENSHIP, count++);
        orderMap.put(BCStyle.COUNTRY_OF_RESIDENCE, count++);
        orderMap.put(BCStyle.DATE_OF_BIRTH, count++);
        orderMap.put(BCStyle.DESCRIPTION, count++);
        orderMap.put(BCStyle.DMD_NAME, count++);
        orderMap.put(BCStyle.DN_QUALIFIER, count++);
        orderMap.put(BCStyle.EmailAddress, count++);
        orderMap.put(BCStyle.GENDER, count++);
        orderMap.put(BCStyle.GENERATION, count++);
        orderMap.put(BCStyle.GIVENNAME, count++);
        orderMap.put(BCStyle.INITIALS, count++);
        orderMap.put(BCStyle.NAME, count++);
        orderMap.put(BCStyle.NAME_AT_BIRTH, count++);
        orderMap.put(BCStyle.ORGANIZATION_IDENTIFIER, count++);
        orderMap.put(BCStyle.PLACE_OF_BIRTH, count++);
        orderMap.put(BCStyle.POSTAL_ADDRESS, count++);
        orderMap.put(BCStyle.POSTAL_CODE, count++);
        orderMap.put(BCStyle.PSEUDONYM, count++);
        orderMap.put(BCStyle.ROLE, count++);
        orderMap.put(BCStyle.SERIALNUMBER, count++);
        orderMap.put(BCStyle.SURNAME, count++);
        orderMap.put(BCStyle.T, count++);
        orderMap.put(BCStyle.TELEPHONE_NUMBER, count++);
        orderMap.put(BCStyle.UNIQUE_IDENTIFIER, count++);
        orderMap.put(BCStyle.UnstructuredAddress, count);
        return Collections.unmodifiableMap(orderMap);
    }

    public static String getNormalizedName(final String inputName) throws InvalidNameException {

        if (inputName.trim().isEmpty()) {
            return "";
        }

        try {
            X500Name x500Name = new X500Name(BCStrictStyle.INSTANCE, inputName);

            RDN[] rdNs = x500Name.getRDNs();
            Arrays.sort(rdNs, new Comparator<>() {
                @Override
                public int compare(RDN o1, RDN o2) {
                    AttributeTypeAndValue o1First = o1.getFirst();
                    AttributeTypeAndValue o2First = o2.getFirst();

                    ASN1ObjectIdentifier o1Type = o1First.getType();
                    ASN1ObjectIdentifier o2Type = o2First.getType();

                    Integer o1Rank = dnOrderMap.get(o1Type);
                    Integer o2Rank = dnOrderMap.get(o2Type);
                    if (o1Rank == null) {
                        if (o2Rank == null) {
                            int idComparison = o1Type.getId().compareTo(o2Type.getId());
                            if (idComparison != 0) {
                                return idComparison;
                            }
                            return String.valueOf(o1Type).compareTo(String.valueOf(o2Type));
                        }
                        return 1;
                    } else if (o2Rank == null) {
                        return -1;
                    }
                    return o1Rank - o2Rank;
                }
            });

            return new LdapName(new X500Name(rdNs).toString()).toString();
        } catch (Exception ex) {
            LOG.error("problem normalizing name : '" + inputName + "'", ex);
        }
        return inputName;
    }

    public void setCertificateComment(Certificate cert, String comment) {

        CertificateComment oldCcomment = (cert.getComment() == null) ? new CertificateComment() : cert.getComment();
        String oldCommentText = (oldCcomment.getComment() == null) ? "" : oldCcomment.getComment();
        if (!oldCommentText.trim().equals(comment.trim())) {
            oldCcomment.setCertificate(cert);
            oldCcomment.setComment(comment);
            certificateCommentRepository.save(oldCcomment);

            auditService.saveAuditTrace(auditService.createAuditTraceCertificateAttribute(CertificateAttribute.ATTRIBUTE_COMMENT,
                oldCommentText, comment, cert));
        }
    }

    X509Certificate getCertifcateFromBase64(String base64Cert) throws CertificateException {
        return getCertifcateFromBytes(Base64.decodeBase64(base64Cert));
    }

    X509Certificate getCertifcateFromBytes(byte[] encodedCert) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(encodedCert));
    }

    public Certificate createCertificate(final byte[] encodedCert, final CSR csr, final String executionId, final boolean reimport) throws GeneralSecurityException {
        return createCertificate(encodedCert, csr, executionId, reimport, null);

    }

    public Certificate createCertificate(final byte[] encodedCert, final CSR csr, final String executionId, final boolean reimport, final String importUrl) throws GeneralSecurityException {

        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(encodedCert));

            String pemCert = cryptoUtil.x509CertToPem(cert);

            return createCertificate(pemCert, csr, executionId, reimport, importUrl);
        } catch (GeneralSecurityException e ) {
            LOG.debug("problem importing certificate: " + e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            LOG.debug("problem importing certificate: " + e.getMessage(), e);
            throw new GeneralSecurityException(e);
        } catch (Throwable th) {
            LOG.debug("problem importing certificate: " + th.getMessage(), th);
            throw new GeneralSecurityException("problem importing certificate: " + th.getMessage());
        }
    }

    /**
     * @param pemCert
     * @param csr
     * @param executionId
     * @return certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Certificate createCertificate(final String pemCert, final CSR csr,
                                         final String executionId) throws GeneralSecurityException, IOException {

        return createCertificate(pemCert, csr, executionId, false, null);
    }

    /**
     * @param pemCert
     * @param csr
     * @param executionId
     * @param reimport
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Certificate createCertificate(final String pemCert, final CSR csr,
                                         final String executionId, final boolean reimport) throws GeneralSecurityException, IOException {

        return createCertificate(pemCert, csr, executionId, reimport, null);
    }

    /**
     * @param b64Cert
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Certificate getCertificateByBase64(final String b64Cert) throws GeneralSecurityException, IOException {
        X509Certificate x509Cert = getCertifcateFromBase64(b64Cert);
        return getCertificateByX509(x509Cert);
    }

    /**
     * @param pemCert
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Certificate getCertificateByPEM(final String pemCert) throws GeneralSecurityException, IOException {
        X509Certificate x509Cert = CryptoService.convertPemToCertificate(pemCert);
        return getCertificateByX509(x509Cert);

    }

    /**
     * @param x509Cert
     * @return
     * @throws GeneralSecurityException
     */
    public Certificate getCertificateByX509(final X509Certificate x509Cert) throws GeneralSecurityException {

        String tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
        LOG.debug("looking for TBS hash '" + tbsDigestBase64 + "' in certificate store");

        List<Certificate> certList = certificateRepository.findByTBSDigest(tbsDigestBase64);

        if (certList.isEmpty()) {
            return null;
        } else if (certList.size() > 1) {
            LOG.debug("#{} certificates found in certificate database for TBS hash '{}'", certList.size(), tbsDigestBase64);
            return certList.get(0);
        } else {
            return certList.get(0);
        }
    }

    public Certificate getCurrentSCEPRecipient(final Pipeline pipeline) {

        LOG.debug("start: getCurrentSCEPRecipient ");
        List<Certificate> certList =
            certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT, "" + pipeline.getId());

        LOG.debug("getCurrentSCEPRecipient #{} found as recepient certificate", certList.size());

        Instant now = Instant.now();
        Certificate currentRecipientCert = null;
        for (Certificate recCert : certList) {

            if (!recCert.isRevoked() && now.isAfter(recCert.getValidFrom())) {
                if (currentRecipientCert == null) {
                    currentRecipientCert = recCert;
                } else {
                    if (recCert.getValidTo().isAfter(currentRecipientCert.getValidTo())) {
                        currentRecipientCert = recCert;
                    }
                }
            }
        }
//        LOG.debug("getCurrentSCEPRecipient " + currentRecipientCert);
        return currentRecipientCert;
    }

    /**
     * @param pemCert
     * @param csr
     * @param executionId
     * @param reimport
     * @return certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Certificate createCertificate(final String pemCert, final CSR csr,
                                         final String executionId,
                                         final boolean reimport, final String importUrl) throws GeneralSecurityException, IOException {


        X509Certificate x509Cert = CryptoService.convertPemToCertificate(pemCert);
        Certificate cert = getCertificateByX509(x509Cert);

        if (cert == null) {
            String tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
            cert = createCertificate(pemCert, csr, executionId, x509Cert, tbsDigestBase64);

            // save the source of the certificate
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SOURCE, importUrl);

        } else {
            LOG.info("certificate '" + cert.getSubject() + "' already exists");

            if (reimport) {
                LOG.debug("existing certificate '" + cert.getSubject() + "' overwriting some attributes, only");
                addAdditionalCertificateAttributes(x509Cert, cert);
            }
        }
        return cert;
    }

    /**
     * @param pemCert
     * @param csr
     * @param executionId
     * @param x509Cert
     * @param tbsDigestBase64
     * @return
     * @throws CertificateEncodingException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateParsingException
     * @throws CertificateException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    private Certificate createCertificate(final String pemCert, final CSR csr, final String executionId,
                                          X509Certificate x509Cert, String tbsDigestBase64)
        throws CertificateEncodingException, IOException, NoSuchAlgorithmException, CertificateParsingException,
        CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {

        Certificate cert;
        LOG.debug("creating new certificate '" + x509Cert.getSubjectX500Principal().toString() + "'");

        byte[] certBytes = x509Cert.getEncoded();
        X509CertificateHolder x509CertHolder = new X509CertificateHolder(certBytes);

        cert = new Certificate();
        cert.setCertificateAttributes(new HashSet<>());

        String type = "X509V" + x509Cert.getVersion();
        cert.setType(type);

        String serial = x509Cert.getSerialNumber().toString();
        cert.setSerial(serial);

        cert.setContent(pemCert);

        if (csr != null) {
            // do not overwrite an existing CSR
            cert.setCsr(csr);
        }

        // indexed key for searching
        cert.setTbsDigest(tbsDigestBase64);

        // derive a readable description
        String desc = cryptoUtil.getDescription(x509Cert);
        cert.setDescription(CryptoService.limitLength(desc, 250));


        // good old SHA1 fingerprint
        String fingerprint = Base64.encodeBase64String(generateSHA1Fingerprint(certBytes));
        cert.setFingerprint(fingerprint);

        cert.setValidFrom(DateUtil.asInstant(x509Cert.getNotBefore()));
        cert.setValidTo(DateUtil.asInstant(x509Cert.getNotAfter()));

        cert.setActive(true);

        Date now = new Date();
        if (x509Cert.getNotBefore().after(now)) {
            cert.setActive(false);
        }
        if (x509Cert.getNotAfter().before(now)) {
            cert.setActive(false);
        }

        //initialize revocation details
        cert.setRevokedSince(null);
        cert.setRevocationReason(null);
        cert.setRevoked(false);

        if (executionId != null) {
            cert.setCreationExecutionId(executionId);
        }

        cert.setContentAddedAt(Instant.now());

        String issuer = CryptoService.limitLength(x509Cert.getIssuerX500Principal().toString(), 250);
        cert.setIssuer(issuer);

        String subject = CryptoService.limitLength(x509Cert.getSubjectX500Principal().toString(), 250);
        cert.setSubject(subject);

        cert.setSelfsigned(false);

        certificateRepository.save(cert);

        interpretBasicConstraint(x509Cert, cert);


        // add the basic key usages a attributes
        usageAsCertAttributes(x509Cert.getKeyUsage(), cert);

        // add the extended key usages a attributes
        List<String> extKeyUsageList = x509Cert.getExtendedKeyUsage();
        if (extKeyUsageList != null) {
            for (String extUsage : extKeyUsageList) {
                setCertMultiValueAttribute(cert, CertificateAttribute.ATTRIBUTE_EXTENDED_USAGE_OID, extUsage);
                setCertMultiValueAttribute(cert, CertificateAttribute.ATTRIBUTE_EXTENDED_USAGE, OidNameMapper.lookupOid(extUsage));
            }
        }

        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ISSUER, issuer.toLowerCase());

        X500Name x500NameIssuer = x509CertHolder.getIssuer();
        insertNameAttributes(cert, CertificateAttribute.ATTRIBUTE_ISSUER, x500NameIssuer);

        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SUBJECT, subject.toLowerCase());

        X500Name x500NameSubject = x509CertHolder.getSubject();
        insertNameAttributes(cert, CertificateAttribute.ATTRIBUTE_SUBJECT, x500NameSubject);

        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_TYPE, type);


        JcaX509ExtensionUtils util = new JcaX509ExtensionUtils();

        // build two SKI variants for cert identification
        SubjectKeyIdentifier ski = util.createSubjectKeyIdentifier(x509Cert.getPublicKey());
        String b46Ski = Base64.encodeBase64String(ski.getKeyIdentifier());

        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SKI, b46Ski);

        SubjectKeyIdentifier skiTruncated = util.createTruncatedSubjectKeyIdentifier(x509Cert.getPublicKey());
        if (!ski.equals(skiTruncated)) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SKI,
                Base64.encodeBase64String(skiTruncated.getKeyIdentifier()));
        }

        // add two serial variants
        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SERIAL, serial);
        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SERIAL_PADDED, getPaddedSerial(serial));

        // add validity period
        setCertAttribute(cert,
            CertificateAttribute.ATTRIBUTE_VALID_FROM_TIMESTAMP, ""
                + x509Cert.getNotBefore().getTime());

        setCertAttribute(cert,
            CertificateAttribute.ATTRIBUTE_VALID_TO_TIMESTAMP, ""
                + x509Cert.getNotAfter().getTime());

        long validityPeriod = (x509Cert.getNotAfter().getTime() - x509Cert.getNotBefore().getTime()) / 1000L;
        setCertAttribute(cert,
            CertificateAttribute.ATTRIBUTE_VALIDITY_PERIOD, "" + validityPeriod);

        addAdditionalCertificateAttributes(x509Cert, cert);

        copyCsrAttributesToCertificate(csr, cert);

        certificateRepository.save(cert);
        certificateAttributeRepository.saveAll(cert.getCertificateAttributes());

        if (x500NameIssuer.equals(x500NameSubject)) {

            // check whether is really selfsigned
            x509Cert.verify(x509Cert.getPublicKey());

            // don't insert the self-reference. This leads to no good when JSON-serializing the object
            // The selfsigned-attribute will mark the fact!
            // cert.setIssuingCertificate(cert);

            // mark it as self signed
            cert.setSelfsigned(true);
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SELFSIGNED, "true");

            cert.setIssuingCertificate(null); // don't build a self reference here
            cert.setRootCertificate(null);

            cert.setRoot(cert.getSubject());
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ROOT, cert.getSubject().toLowerCase());

            LOG.debug("certificate '" + x509Cert.getSubjectX500Principal().toString() + "' is selfsigned");

        } else {
            // try to build cert chain
            try {
                Certificate issuingCert = findIssuingCertificate(x509CertHolder);

                if (issuingCert == null) {
                    LOG.info("unable to find issuer for non-self-signed certificate '" + x509Cert.getSubjectX500Principal().toString() + "' right now ...");
                } else {
                    cert.setIssuingCertificate(issuingCert);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("certificate '" + x509Cert.getSubjectX500Principal().toString() + "' issued by " + issuingCert.getSubject());
                    }
                }

                Certificate rootCert = findRootCertificate(issuingCert);
                if (rootCert != null) {
                    cert.setRootCertificate(rootCert);
                    cert.setRoot(rootCert.getSubject());
                    setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ROOT, rootCert.getSubject().toLowerCase());
                }
            } catch (GeneralSecurityException gse) {
//				LOG.debug("exception while retrieving issuer", gse);
                LOG.info("problem retrieving issuer for certificate '" + x509Cert.getSubjectX500Principal().toString() + "' right now ...");
            }
        }


        certificateRepository.save(cert);
//		LOG.debug("certificate id '" + cert.getId() +"' post-save");
        certificateAttributeRepository.saveAll(cert.getCertificateAttributes());
        LOG.debug("certificate id '{}' saved containing #{} attributes", cert.getId(), cert.getCertificateAttributes().size());
        for (CertificateAttribute cad : cert.getCertificateAttributes()) {
            LOG.debug("Name '" + cad.getName() + "' got value '" + cad.getValue() + "'");
        }

        final X509Principal principal = PrincipalUtil.getSubjectX509Principal(x509Cert);
        final Vector<?> values = principal.getValues(X509Name.CN);

        String cn = values.size() > 0 ? (String) values.get(0) : null;

        List<String> sanList = getCertAttributes(cert, CertificateAttribute.ATTRIBUTE_SAN);
        sanList.addAll(getCertAttributes(cert, CsrAttribute.ATTRIBUTE_TYPED_SAN));
        sanList.addAll(getCertAttributes(cert, CsrAttribute.ATTRIBUTE_TYPED_VSAN));

        List<Certificate> replacedCerts = findReplaceCandidates(Instant.now(), cn, sanList);

        if (replacedCerts.isEmpty()) {
            LOG.debug("certificate id {} does not replace any certificate", cert.getId());
        } else {
            for (Certificate replacedCert : replacedCerts) {
                if (!cert.equals(replacedCert)) {
                    LOG.debug("certificate id {} replaces certificate id {}", cert.getId(), replacedCert.getId());
                    setCertMultiValueAttribute(replacedCert, CertificateAttribute.ATTRIBUTE_REPLACED_BY, cert.getId().toString());
                    certificateAttributeRepository.saveAll(replacedCert.getCertificateAttributes());
                }
            }
        }

        return cert;
    }

    public void interpretBasicConstraint(X509Certificate x509Cert, Certificate cert) {

        //
        // write certificate attributes
        //

        // guess some details from basic constraint
        int basicConstraint = x509Cert.getBasicConstraints();
        if (Integer.MAX_VALUE == basicConstraint) {
            cert.setEndEntity(false);
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CA, "true");
        } else if (-1 == basicConstraint) {
            cert.setEndEntity(true);
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_END_ENTITY, "true");
        } else {
            cert.setEndEntity(false);
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CA, "true");
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CHAIN_LENGTH, "" + basicConstraint);
        }
    }

    private void copyCsrAttributesToCertificate(final CSR csr, final Certificate cert) {

        if (csr == null || cert == null) {
            return;
        }

        if (!StringUtils.isBlank(csr.getRequestedBy())) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_REQUESTED_BY, csr.getRequestedBy());
        }

        if (csr.getComment() != null && !StringUtils.isBlank(csr.getComment().getComment())) {
            setCertificateComment(cert, csr.getComment().getComment());
        }

        for (CsrAttribute csrAttr : csr.getCsrAttributes()) {
            if (csrAttr.getName().startsWith(CsrAttribute.ARA_PREFIX)) {
                setCertAttribute(cert, csrAttr.getName(), csrAttr.getValue());
            }
        }
    }


    /**
     * @param x509Cert
     * @param cert
     * @throws CertificateParsingException
     * @throws IOException
     */
    public void addAdditionalCertificateAttributes(X509Certificate x509Cert, Certificate cert)
        throws CertificateParsingException, IOException {

        int version = Integer.parseInt(getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION, "0"));

        if (version == 0) {

            // extract signature algo
            String keyAlgName = x509Cert.getPublicKey().getAlgorithm();
            cert.setKeyAlgorithm(keyAlgName.toLowerCase());

            AlgorithmInfo algorithmInfo = new AlgorithmInfo(x509Cert.getSigAlgName());

            cert.setHashingAlgorithm(algorithmInfo.getHashAlgName());
            cert.setPaddingAlgorithm(algorithmInfo.getPaddingAlgName());
            cert.setSigningAlgorithm(algorithmInfo.getSigAlgName());

            try {
                String curveName = deriveCurveName(x509Cert.getPublicKey());
                LOG.info("found curve name " + curveName + " for certificate '" + x509Cert.getSubjectX500Principal().toString() + "' with key algo " + keyAlgName);

                cert.setCurveName(curveName.toLowerCase());

            } catch (GeneralSecurityException e) {
                if (keyAlgName.contains("ec")) {
                    LOG.info("unable to derive curve name for certificate '" + x509Cert.getSubjectX500Principal().toString() + "' with key algo " + keyAlgName);
                }
            }

            String subject = x509Cert.getSubjectX500Principal().toString();
            if (subject != null && subject.trim().length() > 0) {

                try {
                    InetAddressValidator inv = InetAddressValidator.getInstance();

                    List<Rdn> rdnList = new LdapName(subject).getRdns();
                    for (Rdn rdn : rdnList) {
                        if ("CN".equalsIgnoreCase(rdn.getType())) {
                            String cn = rdn.getValue().toString();
                            if (inv.isValid(cn)) {
                                LOG.debug("CN found IP in subject: '{}'", cn);
                                setCertMultiValueAttribute(cert, CsrAttribute.ATTRIBUTE_TYPED_VSAN, "IP:" + cn);
                            } else {
                                LOG.debug("CN found DNS name in subject: '{}'", cn);
                                setCertMultiValueAttribute(cert, CsrAttribute.ATTRIBUTE_TYPED_VSAN, "DNS:" + cn);
                            }
                        }
                    }
                } catch (InvalidNameException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            String allSans = "";

            // list all SANs
            if (x509Cert.getSubjectAlternativeNames() != null) {
                Collection<List<?>> altNames = x509Cert.getSubjectAlternativeNames();

                if (altNames != null) {
                    for (List<?> altName : altNames) {
                        int altNameType = (Integer) altName.get(0);

                        String sanValue = "";
                        if (altName.get(1) instanceof String) {
                            sanValue = ((String) altName.get(1)).toLowerCase();
                        } else if (GeneralName.otherName == altNameType) {
                            //	    				sanValue = "--other value--";
                        } else if (altName.get(1) instanceof byte[]) {
                            sanValue = new String((byte[]) (altName.get(1))).toLowerCase();
                        } else {
                            LOG.info("unexpected content type in SANS : {}", altName.get(1).toString());
                        }

                        if (allSans.length() > 0) {
                            allSans += ";";
                        }
                        allSans += sanValue;

                        setCertMultiValueAttribute(cert, CertificateAttribute.ATTRIBUTE_SAN, sanValue);
                        setCertMultiValueAttribute(cert, CsrAttribute.ATTRIBUTE_TYPED_SAN, getTypedSAN(altNameType, sanValue));

                    }
                }
            }

            cert.setSans(CryptoUtil.limitLength(allSans, 250));

            int keyLength = getAlignedKeyLength(x509Cert.getPublicKey());
            cert.setKeyLength(keyLength);

            List<String> crlUrls = getCrlDistributionPoints(x509Cert);
            for (String crlUrl : crlUrls) {
                setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_URL, crlUrl);
            }

            String ocspUrl = getOCSPUrl(x509Cert);
            if (ocspUrl != null) {
                setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_OCSP_URL, ocspUrl);
            }

            List<String> certificatePolicyIds = getCertificatePolicies(x509Cert);
            for (String polId : certificatePolicyIds) {
                setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_POLICY_ID, polId);
            }
        }

        if (version < 2) {

            try {
                setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA1,
                    DigestUtils.sha1Hex(x509Cert.getEncoded()).toLowerCase());
                setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA256,
                    DigestUtils.sha256Hex(x509Cert.getEncoded()).toLowerCase());
            } catch (CertificateEncodingException e) {
                LOG.error("Problem getting encoded certificate '" + x509Cert.getSubjectX500Principal().toString() + "'", e);
            }

            try {
                if (!cert.getSubject().trim().isEmpty()) {
                    X500Name x500Name = new X500Name(cert.getSubject());
                    for (RDN rdn : x500Name.getRDNs()) {

                        AttributeTypeAndValue[] attrTVArr = rdn.getTypesAndValues();
                        for (AttributeTypeAndValue attrTV : attrTVArr) {

                            String rdnReadableName = OidNameMapper.lookupOid(attrTV.getType().toString());

                            setCertAttribute(cert,
                                CertificateAttribute.ATTRIBUTE_RDN_PREFIX + rdnReadableName.toUpperCase(),
                                attrTV.getValue().toString());
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                LOG.error("Problem building X500Name for subject for certificate '" + x509Cert.getSubjectX500Principal().toString() + "'", iae);
            }
        }

        if (version < CURRENT_ATTRIBUTES_VERSION) {

            try {
                String subjectRfc2253 = getNormalizedName(cert.getSubject());

                setCertAttribute(cert,
                    CertificateAttribute.ATTRIBUTE_SUBJECT_RFC_2253,
                    subjectRfc2253,
                    false);
            } catch (InvalidNameException e) {
                LOG.error("Problem building RFC 2253-styled subject for  certificate '" + x509Cert.getSubjectX500Principal().toString() + "'", e);
            }

            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION, "" + CURRENT_ATTRIBUTES_VERSION, false);

        }

    }

    public List<String> getCertificatePolicies(X509Certificate x509Cert) {
        ArrayList<String> certificatePolicyIds = new ArrayList<>();
        byte[] extVal = x509Cert.getExtensionValue(Extension.certificatePolicies.getId());
        if (extVal == null) {
            return certificatePolicyIds;
        }
        try {
            org.bouncycastle.asn1.x509.CertificatePolicies cf = org.bouncycastle.asn1.x509.CertificatePolicies
                .getInstance(X509ExtensionUtil.fromExtensionValue(extVal));
            PolicyInformation[] information = cf.getPolicyInformation();
            for (PolicyInformation p : information) {
                ASN1ObjectIdentifier aIdentifier = p.getPolicyIdentifier();
                certificatePolicyIds.add(aIdentifier.getId());
            }
        } catch (IOException ex) {
            LOG.error("Failed to get OCSP URL for certificate '" + x509Cert.getSubjectX500Principal().toString() + "'", ex);
        }

        return certificatePolicyIds;
    }


    private String getOCSPUrl(X509Certificate x509Cert) {
        ASN1Primitive obj;
        try {
            obj = getExtensionValue(x509Cert, Extension.authorityInfoAccess.getId());
        } catch (IOException ex) {
            LOG.error("Failed to get OCSP URL for certificate '" + x509Cert.getSubjectX500Principal().toString() + "'", ex);
            return null;
        }

        if (obj == null) {
            return null;
        }

        AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(obj);

        AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
        for (AccessDescription accessDescription : accessDescriptions) {
            boolean correctAccessMethod = accessDescription.getAccessMethod().equals(X509ObjectIdentifiers.ocspAccessMethod);
            if (!correctAccessMethod) {
                continue;
            }

            GeneralName name = accessDescription.getAccessLocation();
            if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
                continue;
            }

            DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
            return derStr.getString();
        }

        return null;

    }

    /**
     * @param x509Cert the certificate from which we need the ExtensionValue
     * @param oid      the Object Identifier value for the extension.
     * @return the extension value as an ASN1Primitive object
     * @throws IOException
     */
    private static ASN1Primitive getExtensionValue(X509Certificate x509Cert, String oid) throws IOException {
        byte[] bytes = x509Cert.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    public static String getSAN(final GeneralName gn) {

        if (GeneralName.iPAddress == gn.getTagNo()) {

            DEROctetString derOctetString = (DEROctetString)gn.getName();
            try {
                InetAddress addr = InetAddress.getByAddress(derOctetString.getOctets());
                return addr.getHostAddress();
            } catch (UnknownHostException e) {
                LOG.debug("Problem parsing ip address '" + gn.getName().toString() + "'!", e.getLocalizedMessage());
                return getTypedSAN(gn.getTagNo(), gn.getName().toString());
            }
        }else{
            return getTypedSAN(gn.getTagNo(), gn.getName().toString());
        }

    }

    public static String getTypedSAN(final GeneralName gn) {

        if (GeneralName.iPAddress == gn.getTagNo()) {

            DEROctetString derOctetString = (DEROctetString)gn.getName();
            try {
                InetAddress addr = InetAddress.getByAddress(derOctetString.getOctets());
                return getTypedSAN(gn.getTagNo(), addr.getHostAddress());
            } catch (UnknownHostException e) {
                LOG.debug("Problem parsing ip address '" + gn.getName().toString() + "'!", e.getLocalizedMessage());
                return getTypedSAN(gn.getTagNo(), gn.getName().toString());
            }
        }else{
            return getTypedSAN(gn.getTagNo(), gn.getName().toString());
        }

    }

    public static String getTypedSAN(int altNameType, String sanValue) {

        if (GeneralName.dNSName == altNameType) {
            return ("DNS:" + sanValue);
        } else if (GeneralName.iPAddress == altNameType) {
            return ("IP:" + sanValue);
        } else if (GeneralName.ediPartyName == altNameType) {
            return ("EDI:" + sanValue);
        } else if (GeneralName.otherName == altNameType) {
            return ("other:" + sanValue);
        } else if (GeneralName.registeredID == altNameType) {
            return ("regID:" + sanValue);
        } else if (GeneralName.rfc822Name == altNameType) {
            return ("rfc822:" + sanValue);
        } else if (GeneralName.uniformResourceIdentifier == altNameType) {
            return ("URI:" + sanValue);
        } else if (GeneralName.x400Address == altNameType) {
            return ("X400:" + sanValue);
        } else if (GeneralName.directoryName == altNameType) {
            return ("DirName:" + sanValue);
        } else {
            LOG.warn("unexpected name / tag '{}' in SANs for san {}", altNameType, sanValue);
            return "Unknown:" + sanValue;
        }
    }

    /**
     * @param p10ReqHolder
     * @return
     */
    public static String getAlgoName(final Pkcs10RequestHolder p10ReqHolder) {

        String algoName = p10ReqHolder.getSigningAlgorithmName().toLowerCase(Locale.ROOT);
        if( algoName.contains("withrsaencryption")){
            return "rsa";
        }
        if( algoName.contains("with")){
            algoName = algoName.split("with")[0];
        }
        return algoName;
    }

    /**
     * @param pk
     * @return
     */
    public static int getAlignedKeyLength(final PublicKey pk) {
        int keyLength = getKeyLength(pk);
        if (lenSet.contains(keyLength + 1)) {
            return keyLength + 1;
        }
        if (lenSet.contains(keyLength + 2)) {
            return keyLength + 2;
        }
        return keyLength;
    }

    /**
     * Gets the key length of supported keys
     *
     * @param pk PublicKey used to derive the keysize
     * @return -1 if key is unsupported, otherwise a number &gt;= 0. 0 usually means the length can not be calculated,
     * for example if the key is an EC key and the "implicitlyCA" encoding is used.
     */
    public static int getKeyLength(final PublicKey pk) {
        int len = -1;
        if (pk instanceof RSAPublicKey) {
            final RSAPublicKey rsapub = (RSAPublicKey) pk;
            len = rsapub.getModulus().bitLength();
        } else if (pk instanceof JCEECPublicKey) {
            final JCEECPublicKey ecpriv = (JCEECPublicKey) pk;
            final org.bouncycastle.jce.spec.ECParameterSpec spec = ecpriv.getParameters();
            if (spec != null) {
                len = spec.getN().bitLength();
            } else {
                // We support the key, but we don't know the key length
                len = 0;
            }
        } else if (pk instanceof ECPublicKey) {
            final ECPublicKey ecpriv = (ECPublicKey) pk;
            final java.security.spec.ECParameterSpec spec = ecpriv.getParams();
            if (spec != null) {
                len = spec.getOrder().bitLength(); // does this really return something we expect?
            } else {
                // We support the key, but we don't know the key length
                len = 0;
            }
        } else if (pk instanceof DSAPublicKey) {
            final DSAPublicKey dsapub = (DSAPublicKey) pk;
            if (dsapub.getParams() != null) {
                len = dsapub.getParams().getP().bitLength();
            } else {
                len = dsapub.getY().bitLength();
            }
        } else if (pk instanceof EdDSAPublicKey) {
            len = 256;
        }

        return len;
    }

    /**
     * derive the curve name
     *
     * @param ecParameterSpec
     * @return
     * @throws GeneralSecurityException
     */
    public static String deriveCurveName(org.bouncycastle.jce.spec.ECParameterSpec ecParameterSpec)
        throws GeneralSecurityException {
        for (@SuppressWarnings("rawtypes")
             Enumeration names = ECNamedCurveTable.getNames(); names.hasMoreElements(); ) {
            final String name = (String) names.nextElement();

            final X9ECParameters params = ECNamedCurveTable.getByName(name);

            if (params.getN().equals(ecParameterSpec.getN()) && params.getH().equals(ecParameterSpec.getH())
                && params.getCurve().equals(ecParameterSpec.getCurve())
                && params.getG().equals(ecParameterSpec.getG())) {
                return name;
            }
        }

        throw new GeneralSecurityException("Could not find name for curve");
    }

    public static String deriveCurveName(PublicKey publicKey) throws GeneralSecurityException {
        if (publicKey instanceof java.security.interfaces.ECPublicKey) {
            final java.security.interfaces.ECPublicKey pk = (java.security.interfaces.ECPublicKey) publicKey;
            final ECParameterSpec params = pk.getParams();
            return deriveCurveName(EC5Util.convertSpec(params));
        } else if (publicKey instanceof org.bouncycastle.jce.interfaces.ECPublicKey) {
            final org.bouncycastle.jce.interfaces.ECPublicKey pk = (org.bouncycastle.jce.interfaces.ECPublicKey) publicKey;
            return deriveCurveName(pk.getParameters());
        } else
            throw new GeneralSecurityException("Can only be used with instances of ECPublicKey (either jce or bc implementation)");
    }

    public static String deriveCurveName(PrivateKey privateKey) throws GeneralSecurityException {
        if (privateKey instanceof java.security.interfaces.ECPrivateKey) {
            final java.security.interfaces.ECPrivateKey pk = (java.security.interfaces.ECPrivateKey) privateKey;
            final ECParameterSpec params = pk.getParams();
            return deriveCurveName(EC5Util.convertSpec(params));
        } else if (privateKey instanceof org.bouncycastle.jce.interfaces.ECPrivateKey) {
            final org.bouncycastle.jce.interfaces.ECPrivateKey pk = (org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey;
            return deriveCurveName(pk.getParameters());
        } else
            throw new GeneralSecurityException("Can only be used with instances of ECPrivateKey (either jce or bc implementation)");
    }


    /**
     * @param cert
     * @param attributeName
     * @param x500NameSubject
     */
    public void insertNameAttributes(Certificate cert, String attributeName, X500Name x500NameSubject) {


        try {
            List<Rdn> rdnList = new LdapName(x500NameSubject.toString()).getRdns();
            for (Rdn rdn : rdnList) {
                String rdnExpression = rdn.getType().toLowerCase() + "=" + rdn.getValue().toString().toLowerCase().trim();
                setCertMultiValueAttribute(cert, attributeName, rdnExpression);
            }
        } catch (InvalidNameException e) {
            LOG.info("problem parsing RDN for {}", x500NameSubject);
        }

        for (RDN rdn : x500NameSubject.getRDNs()) {
            for (org.bouncycastle.asn1.x500.AttributeTypeAndValue atv : rdn.getTypesAndValues()) {
                String value = atv.getValue().toString().toLowerCase().trim();
                setCertMultiValueAttribute(cert, attributeName, value);
                String oid = atv.getType().getId().toLowerCase();
                setCertMultiValueAttribute(cert, attributeName, oid + "=" + value);

                if (!oid.equals(atv.getType().toString().toLowerCase())) {
                    setCertMultiValueAttribute(cert, attributeName, atv.getType().toString().toLowerCase() + "=" + value);
                }
            }
        }
    }

    public String getCertAttribute(Certificate certDao, String name, String defaultValue) {
        String value = getCertAttribute(certDao, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String getCertAttribute(Certificate certDao, String name) {
        for (CertificateAttribute certAttr : certDao.getCertificateAttributes()) {
            if (certAttr.getName().equals(name)) {
                return certAttr.getValue();
            }
        }
        return null;
    }

    public List<String> getCertAttributes(Certificate certDao, String name) {
        List<String> stringList = new ArrayList<>();

        for (CertificateAttribute certAttr : certDao.getCertificateAttributes()) {
            if (certAttr.getName().equals(name)) {
                stringList.add(certAttr.getValue());
            }
        }
        return stringList;
    }

    /**
     * @param certDao
     * @param name
     * @param value
     */
    public void setCertAttribute(Certificate certDao, String name, long value) {
        setCertAttribute(certDao, name, Long.toString(value));
    }

    /**
     * @param cert
     * @param name
     * @param value
     */
    public void setCertMultiValueAttribute(Certificate cert, String name, String value) {
        setCertAttribute(cert, name, value, true);
    }

    /**
     * @param cert
     * @param name
     * @param value
     */
    public void setCertAttribute(Certificate cert, String name, String value) {
        setCertAttribute(cert, name, value, true);
    }

    /**
     * @param cert
     * @param name
     * @param value
     * @param multiValue
     */
    public void setCertAttribute(Certificate cert, String name, String value, boolean multiValue) {

        if (name == null) {
            LOG.warn("no use to insert attribute with name 'null'", new Exception());
            return;
        }
        if (value == null) {
            value = "";
        }

        value = CryptoUtil.limitLength(value, 250);

        Collection<CertificateAttribute> certAttrList = cert.getCertificateAttributes();
        for (CertificateAttribute certAttr : certAttrList) {

//	        LOG.debug("checking certificate attribute '{}' containing value '{}'", certAttr.getName(), certAttr.getValue());

            if (name.equals(certAttr.getName())) {
                if (value.equalsIgnoreCase(certAttr.getValue())) {
                    // attribute already present, no use in duplication here
                    return;
                } else {
                    if (!multiValue) {
                        certAttr.setValue(value);
                        return;
                    }
                }
            }
        }

        CertificateAttribute cAtt = new CertificateAttribute();
        cAtt.setCertificate(cert);
        cAtt.setName(name);
        cAtt.setValue(value);

        cert.getCertificateAttributes().add(cAtt);

        certificateAttributeRepository.save(cAtt);

    }


    /**
     * @param startCertDao
     * @return
     * @throws GeneralSecurityException
     */
    public List<Certificate> getCertificateChain(final Certificate startCertDao) throws GeneralSecurityException {

        int MAX_CHAIN_LENGTH = 10;
        ArrayList<Certificate> certChain = new ArrayList<>();

        Certificate certDao = startCertDao;
        LOG.debug("added end entity cert id {} to the chain", certDao.getId());
        certChain.add(certDao);

        for (int i = 0; i <= MAX_CHAIN_LENGTH; i++) {

            if (i == MAX_CHAIN_LENGTH) {
                String msg = "maximum chain length ecxeeded for  cert id : " + startCertDao.getId();
                LOG.info(msg);
                throw new GeneralSecurityException(msg);
            }

            // walk up the certificate chain
            Certificate issuingCertDao;
            try {
                issuingCertDao = findIssuingCertificate(certDao);

                if (issuingCertDao == null) {
                    String msg = "no issuing certificate available / retrievable for cert id : " + certDao.getId();
                    LOG.info(msg);
                    throw new GeneralSecurityException(msg);
                } else {
                    LOG.debug("added issuing cert id {} to the chain", issuingCertDao.getId());
                    certChain.add(issuingCertDao);
                }
            } catch (GeneralSecurityException e) {
                String msg = "Error retrieving issuing certificate for cert id : " + certDao.getId();
                LOG.info(msg);
                throw new GeneralSecurityException(msg);
            }

            if (issuingCertDao.getIssuingCertificate() == null) {
                String msg = "no issuing certificate available / retrievable for cert id : " + issuingCertDao.getId();
                LOG.info(msg);
                break;
//				throw new GeneralSecurityException(msg);
            } else {

                // root reached? No need to move further ..
                if (issuingCertDao.getId().equals(issuingCertDao.getIssuingCertificate().getId())) {
                    LOG.debug("certificate chain complete, cert id '{}' is selfsigned", issuingCertDao.getId());
                    break;
                }
            }

            certDao = issuingCertDao;
        }

        return certChain;
    }

    /**
     * @param startCert end entity certificate for chain search
     * @return X509Certificate Array
     * @throws GeneralSecurityException
     */
    public X509Certificate[] getX509CertificateChain(final Certificate startCert) throws GeneralSecurityException {

        List<Certificate> certList = getCertificateChain(startCert);

        X509Certificate[] chainArr = new X509Certificate[certList.size()];
        for (int i = 0; i < certList.size(); i++) {

            X509Certificate x509Cert = CryptoService.convertPemToCertificate(certList.get(i).getContent());
            chainArr[i] = x509Cert;
        }

        return chainArr;
    }

    /**
     * @param startCert end entity certificate for chain search
     * @return X509Certificate List
     * @throws GeneralSecurityException
     */
    public List<X509Certificate> getX509CertificateChainAsList(final Certificate startCert) throws GeneralSecurityException {

        List<Certificate> certList = getCertificateChain(startCert);

        List<X509Certificate> x509chainList = new ArrayList<>();
        for (int i = 0; i < certList.size(); i++) {

            X509Certificate x509Cert = CryptoService.convertPemToCertificate(certList.get(i).getContent());
            x509chainList.add(x509Cert);
        }

        return x509chainList;
    }

    /**
     * bloat the string-typed serial to a defined length to ensure ordering works out fine. The length of serials has a wide range (1 .. 50 cahrs)
     *
     * @param serial a serial (e.g.'1' or '2586886443079766545651298663063516315029340169') encoded as a string.
     * @return the padded serial string. If serial is null, return max number of zeroes
     */
    public static String getPaddedSerial(final String serial) {

        if (serial == null) {
            return SERIAL_PADDING_PATTERN;
        }
        int len = serial.length();
        if (len >= SERIAL_PADDING_PATTERN.length()) {
            return serial;
        }

        return SERIAL_PADDING_PATTERN.substring(serial.length()) + serial;

    }

    /**
     * bloat the string-typed timestamp to a defined length to ensure ordering works out fine
     *
     * @param timestamp a timestamp (e.g.'1593080183000') encoded as a string
     * @return the padded timestamp string. If timestamp is null, return max number of zeroes
     */
    public static String getPaddedTimestamp(final String timestamp) {

        if (timestamp == null) {
            return TIMESTAMP_PADDING_PATTERN;
        }

        int len = timestamp.length();
        if (len >= TIMESTAMP_PADDING_PATTERN.length()) {
            return timestamp;
        }
        return TIMESTAMP_PADDING_PATTERN.substring(timestamp.length()) + timestamp;
    }

    /**
     * Generate a SHA1 fingerprint from a byte array containing a X.509 certificate
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     * @return Byte array containing SHA1 hash of DER encoded certificate.
     */
    public static byte[] generateSHA1Fingerprint(byte[] ba) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(ba);
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("SHA1 algorithm not supported", nsae);
        }
        return null;
    } // generateSHA1Fingerprint


    /**
     * convert the usage-bits to a readable string
     *
     * @param usage
     * @return descriptive text representing the key usage
     */
    public static String usageAsString(boolean[] usage) {

        if ((usage == null) || (usage.length == 0)) {
            return ("unspecified usage");
        }

        String desc = "valid for ";
        if (usage[0]) desc += "digitalSignature ";
        if ((usage.length > 1) && usage[1]) desc += "nonRepudiation ";
        if ((usage.length > 2) && usage[2]) desc += "keyEncipherment ";
        if ((usage.length > 3) && usage[3]) desc += "dataEncipherment ";
        if ((usage.length > 4) && usage[4]) desc += "keyAgreement ";
        if ((usage.length > 5) && usage[5]) desc += "keyCertSign ";
        if ((usage.length > 6) && usage[6]) desc += "cRLSign ";
        if ((usage.length > 7) && usage[7]) desc += "encipherOnly ";
        if ((usage.length > 8) && usage[8]) desc += "decipherOnly ";

        return (desc);
    }

    /**
     * convert the usage-bits to a readable string
     *
     * @param usage boolean array of usage
     * @param cert  certificate to set attributes
     */
    public void usageAsCertAttributes(boolean[] usage, Certificate cert) {

        if ((usage == null) || (usage.length == 0)) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "unspecified");
            return;
        }

        if (usage[0]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "digitalSignature");
        }
        if ((usage.length > 1) && usage[1]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "nonRepudiation");
        }
        if ((usage.length > 2) && usage[2]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "keyEncipherment");
        }
        if ((usage.length > 3) && usage[3]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "dataEncipherment");
        }
        if ((usage.length > 4) && usage[4]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "keyAgreement");
        }
        if ((usage.length > 5) && usage[5]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "keyCertSign");
        }
        if ((usage.length > 6) && usage[6]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "cRLSign");
        }
        if ((usage.length > 7) && usage[7]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "encipherOnly");
        }
        if ((usage.length > 8) && usage[8]) {
            setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE, "decipherOnly");
        }

    }


    public Certificate findIssuingCertificate(Certificate cert) throws GeneralSecurityException {

        if (cert.isSelfsigned()) {
            // no need for lengthy calculations, we do know the issuer, yet
            return cert;
        }

        Certificate issuingCert = cert.getIssuingCertificate();
        if (issuingCert == null) {
            issuingCert = findIssuingCertificate(convertPemToCertificateHolder(cert.getContent()));
            if (issuingCert != null) {
                if (issuingCert.equals(cert)) {
                    LOG.warn("found untagged self-signed certificate id '{}', '{}'", cert.getId(), cert.getDescription());
                    return cert;
                }
                cert.setIssuingCertificate(issuingCert);
                certificateRepository.save(cert);
            } else {
                LOG.debug("not able to find and store issuing certificate for '" + cert.getDescription() + "'");
            }
        }
        return issuingCert;
    }

    /**
     * @param pem string that will be converted to X509Certificate
     * @return X509CertificateHolder converted from PEM String
     * @throws GeneralSecurityException
     */
    public static X509CertificateHolder convertPemToCertificateHolder(final String pem) throws GeneralSecurityException {

        X509Certificate x509Cert = convertPemToCertificate(pem);
        try {
            return new X509CertificateHolder(x509Cert.getEncoded());
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }

    }

    /**
     * @param pem string that will be converted to X509Certificate
     * @return X509Certificate converted from PEM String
     * @throws GeneralSecurityException
     */
    public static X509Certificate convertPemToCertificate(final String pem)
        throws GeneralSecurityException {

        X509Certificate cert;
        ByteArrayInputStream pemStream;
        pemStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));

        Reader pemReader = new InputStreamReader(pemStream);
        PEMParser pemParser = new PEMParser(pemReader);

        try {
            Object parsedObj = pemParser.readObject();

            if (parsedObj == null) {
                throw new GeneralSecurityException(
                    "Parsing of certificate failed! Not PEM encoded?");
            }

//				LOG.debug("PemParser returned: " + parsedObj);

            if (parsedObj instanceof X509CertificateHolder) {
                cert = new JcaX509CertificateConverter().setProvider("BC")
                    .getCertificate((X509CertificateHolder) parsedObj);

            } else {
                throw new GeneralSecurityException(
                    "Unexpected parsing result: "
                        + parsedObj.getClass().getName());
            }
        } catch (IOException ex) {
            LOG.error("IOException, convertPemToCertificate", ex);
            throw new GeneralSecurityException(
                "Parsing of certificate failed! Not PEM encoded?");
        } finally {
            try {
                pemParser.close();
            } catch (IOException e) {
                // just ignore
                LOG.debug("IOException on close()", e);
            }
        }

        return cert;
    }

    /**
     * @param pem string that will be converted to PrivateKey
     * @return PrivateKey converted from PEM String
     * @throws GeneralSecurityException
     */
    public PrivateKey convertPemToPrivateKey(final String pem)
        throws GeneralSecurityException {

        PrivateKey privKey;
        ByteArrayInputStream pemStream;
        pemStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));

        Reader pemReader = new InputStreamReader(pemStream);
        PEMParser pemParser = new PEMParser(pemReader);

        try {
            Object parsedObj = pemParser.readObject();

            if (parsedObj == null) {
                throw new GeneralSecurityException(
                    "Parsing of certificate failed! Not PEM encoded?");
            }

//				LOG.debug("PemParser returned: " + parsedObj);

            if (parsedObj instanceof PrivateKeyInfo) {
                privKey = new JcaPEMKeyConverter().setProvider("BC")
                    .getPrivateKey((PrivateKeyInfo) parsedObj);
            } else {
                throw new GeneralSecurityException(
                    "Unexpected parsing result: "
                        + parsedObj.getClass().getName());
            }

        } catch (IOException ex) {
            LOG.error("IOException, convertPemToCertificate", ex);
            throw new GeneralSecurityException(
                "Parsing of certificate failed! Not PEM encoded?");
        } finally {
            try {
                pemParser.close();
            } catch (IOException e) {
                // just ignore
                LOG.debug("IOException on close()", e);
            }
        }

        return privKey;
    }

    /**
     * @param x509CertHolder certificate to search issuning certificate
     * @return issuing certificate from input certificate
     * @throws GeneralSecurityException
     */
    public Certificate findIssuingCertificate(X509CertificateHolder x509CertHolder) throws GeneralSecurityException {

        Objects.requireNonNull(x509CertHolder, "x509CertHolder can't be null");

        List<Certificate> rawIssuingCertList = new ArrayList<>();

        // look for the AKI extension in the given certificate
        if (x509CertHolder.getExtensions() != null) {
            AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.fromExtensions(x509CertHolder.getExtensions());
            if (aki != null) {
                rawIssuingCertList = findCertsByAKI(x509CertHolder, aki);
            }
        }

        if (rawIssuingCertList.isEmpty()) {
            LOG.debug("AKI from crt extension failed, trying to find issuer name");
            rawIssuingCertList = certificateRepository.findCACertByIssuer(x509CertHolder.getIssuer().toString());
            if (rawIssuingCertList.isEmpty()) {

                try {
                    X509Certificate x509Cert = getCertifcateFromBytes(x509CertHolder.getEncoded());
                    rawIssuingCertList = certificateRepository.findCACertByIssuer(x509Cert.getIssuerX500Principal().getName());
                } catch (IOException e) {
                    LOG.info("problem parsing certificate '{}' from holder", x509CertHolder.getSubject().toString());
                }
            }
            if (rawIssuingCertList.size() > 1) {
                LOG.debug("more than one issuer found by matching issuer name '{}'", x509CertHolder.getIssuer().toString());
            }
        }
/*
		if( rawIssuingCertList.isEmpty()){
			LOG.debug("AKI from issuer name, trying RDN matching");
			// @todo
		}
*/
        // no issuing certificate found
        //  @todo
        // may not be a reason for a GeneralSecurityException
        if (rawIssuingCertList.isEmpty()) {
            throw new GeneralSecurityException("no issuing certificate for '" + x509CertHolder.getSubject().toString() + "' in certificate store.");
        }

        List<Certificate> issuingCertList = rawIssuingCertList;

        if (issuingCertList.size() > 1) {

            Date notBefore = x509CertHolder.getNotBefore();

            List<Certificate> issuingCertListChecked = new ArrayList<>();
            for (Certificate issuer : issuingCertList) {
                if(issuer.isRevoked() && notBefore.after(DateUtil.asDate(issuer.getRevokedSince()))){
                    LOG.debug("issuer {} was revoked on {}", issuer.getId(), notBefore);
                    continue;
                }
                if( notBefore.after(DateUtil.asDate(issuer.getValidTo()))){
                    LOG.debug("issuer {} was already expired on {}", issuer.getId(), notBefore);
                    continue;
                }
                if( notBefore.before(DateUtil.asDate(issuer.getValidFrom()))){
                    LOG.debug("issuer {} was not yet valid on {}", issuer.getId(), notBefore);
                    continue;
                }
                if( issuer.isEndEntity() ){
                    LOG.debug("probable issuer {} is an end entity, ignoring as issuer", issuer.getId());
                    continue;
                }
                X509CertificateHolder x509CertHolderIssuer = cryptoUtil.convertPemToCertificateHolder(issuer.getContent());
                try {
                    ContentVerifierProvider contentVerifierProvider = new JcaContentVerifierProviderBuilder().build(x509CertHolderIssuer);
                    if( x509CertHolder.isSignatureValid(contentVerifierProvider)) {
                        issuingCertListChecked.add(issuer);
                    }else{
                        LOG.warn("probable issuer {} does not verify certificate {}, ignoring as issuer",
                            issuer.getId(),
                            x509CertHolder.getSubject().toString());
                    }
                } catch (OperatorCreationException | CertException e) {
                    LOG.warn("probable issuer {} not a valid certificate: {}", issuer.getId(), e.getMessage());
                }
            }
            issuingCertList = issuingCertListChecked;

            // that's wierd!!
            if (issuingCertList.size() > 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("more than one issuer found ");
                    for (Certificate issuer : issuingCertList) {
                        LOG.warn("possible issuer id '{}' subject '{}'", issuer.getId(), issuer.getSubject());
                    }
                }
                throw new GeneralSecurityException("more than one (" + issuingCertList.size() + ") issuing certificate for '" + x509CertHolder.getSubject().toString() + "' in certificate store.");
            }
        }

        Certificate issuerDao = issuingCertList.iterator().next();

        if (LOG.isDebugEnabled()) {
			/*
            LOG.debug("issuerDao has attributes: ");
			for( CertificateAttribute cad: issuerDao.getCertificateAttributes()){
				LOG.debug("Name '" + cad.getName() +"' got value '" + cad.getValue() + "'");
			}
			*/
        }

        return issuerDao;
    }

    /**
     * @param cert certificate to search root certificate
     * @return root certificate from input certificate
     * @throws GeneralSecurityException
     */
    private Certificate findRootCertificate(Certificate cert) throws GeneralSecurityException {

        for (int i = 0; i < 10; i++) {

            // end of chain?
            if (cert.isSelfsigned()) {

                // hurrah, terminate ...
                return cert;
            }

            // step up one level
            Certificate issuingCert = cert.getIssuingCertificate();

            // is the issuer already known?
            if (issuingCert == null) {

                // no, try to find it
                issuingCert = findIssuingCertificate(cert);
                if (issuingCert != null) {
                    cert.setIssuingCertificate(issuingCert);
                    certificateRepository.save(cert);
                    LOG.debug("determined issuing certificate {} for {}", issuingCert.getId(), cert.getId());
                } else {
                    break;
                }
            }

            cert = issuingCert;
        }

        LOG.info("unable to determined issuing certificate for {}", cert.getId());
        return null;
    }


    /**
     * @param x509CertHolder certificate
     * @param aki            Authority Key Information
     * @return list of certificates
     */
    private List<Certificate> findCertsByAKI(X509CertificateHolder x509CertHolder, AuthorityKeyIdentifier aki) {

        String aKIBase64 = Base64.encodeBase64String(aki.getKeyIdentifier());
        LOG.debug("looking for issuer of certificate '" + x509CertHolder.getSubject().toString() + "', issuer selected by its SKI '" + aKIBase64 + "'");
        List<Certificate> issuingCertList = certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SKI, aKIBase64);
        if (issuingCertList.isEmpty()) {
            LOG.debug("no certificate found for AKI {}", aKIBase64);
        }
        return issuingCertList;
    }

    public List<Certificate> findCertsBySubjectRFC2253(final String subject) {

        LOG.debug("looking for certificate by subject (by RFC 2253) '" + subject + "'");
        List<Certificate> issuingCertList = certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SUBJECT_RFC_2253, subject);
        if (issuingCertList.isEmpty()) {
            LOG.debug("no certificate found for subject '{}'", subject);
        }
        return issuingCertList;
    }


    public Set<GeneralName> getSANList(X509CertificateHolder x509CertHolder) {

        Set<GeneralName> generalNameSet = new HashSet<>();

        Extensions exts = x509CertHolder.getExtensions();
        for (ASN1ObjectIdentifier objId : exts.getExtensionOIDs()) {
            if (Extension.subjectAlternativeName.equals(objId)) {

                ASN1OctetString octString = exts.getExtension(objId).getExtnValue();
                GeneralNames names = GeneralNames.getInstance(octString);
                LOG.debug("Attribute value SAN" + names);
                LOG.debug("SAN values #" + names.getNames().length);

                for (GeneralName gnSAN : names.getNames()) {
                    LOG.debug("GN " + gnSAN.toString());
                    generalNameSet.add(gnSAN);

                }
            }
        }
        return generalNameSet;
    }

    public Set<GeneralName> getSANList(Pkcs10RequestHolder p10ReqHolder) {

        Set<GeneralName> generalNameSet = new HashSet<>();

        for (Attribute attr : p10ReqHolder.getReqAttributes()) {
            if (PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(attr.getAttrType())) {

                ASN1Set valueSet = attr.getAttrValues();
                LOG.debug("ExtensionRequest / AttrValues has {} elements", valueSet.size());
                for (ASN1Encodable asn1Enc : valueSet) {
                    DERSequence derSeq = (DERSequence) asn1Enc;

                    LOG.debug("ExtensionRequest / DERSequence has {} elements", derSeq.size());
                    LOG.debug("ExtensionRequest / DERSequence[0] is a  {}", derSeq.getObjectAt(0).getClass().getName());

                    DERSequence derSeq2 = (DERSequence) derSeq.getObjectAt(0);
                    LOG.debug("ExtensionRequest / DERSequence2 has {} elements", derSeq2.size());
                    LOG.debug("ExtensionRequest / DERSequence2[0] is a  {}", derSeq2.getObjectAt(0).getClass().getName());


                    ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier) (derSeq2.getObjectAt(0));
                    if (Extension.subjectAlternativeName.equals(objId)) {
                        DEROctetString derStr = (DEROctetString) derSeq2.getObjectAt(1);
                        GeneralNames names = GeneralNames.getInstance(derStr.getOctets());
                        LOG.debug("Attribute value SAN" + names);
                        LOG.debug("SAN values #" + names.getNames().length);

                        for (GeneralName gnSAN : names.getNames()) {
                            LOG.debug("GN " + gnSAN.toString());
                            generalNameSet.add(gnSAN);

                        }
                    } else {
                        LOG.info("Unexpected Extensions Attribute value " + objId.getId());
                    }
                }

            }
        }
        return generalNameSet;
    }

    public void storePrivateKey(CSR csr, KeyPair keyPair, Instant validTo) throws IOException {

        StringWriter sw = keyToPEM(keyPair);

        ProtectedContent pt = protUtil.createProtectedContent(sw.toString(),
            ProtectedContentType.KEY,
            ContentRelationType.CSR,
            csr.getId(),
            -1,
            validTo);

        protContentRepository.save(pt);
    }

    /**
     * @param cert    certificate that needs to be stored in PEM format
     * @param keyPair keypair that needs to be stored in PEM format
     * @throws IOException
     */
    public void storePrivateKey(Certificate cert, KeyPair keyPair) throws IOException {

        StringWriter sw = keyToPEM(keyPair);

        ProtectedContent pt = protUtil.createProtectedContent(sw.toString(), ProtectedContentType.KEY, ContentRelationType.CERTIFICATE, cert.getId());
        protContentRepository.save(pt);
    }

    public void storePrivateKey(Certificate cert, KeyPair keyPair, Instant validTo) throws IOException {

        StringWriter sw = keyToPEM(keyPair);

        ProtectedContent pt = protUtil.createProtectedContent(sw.toString(),
            ProtectedContentType.KEY,
            ContentRelationType.CERTIFICATE,
            cert.getId(),
            -1,
            validTo);
        protContentRepository.save(pt);
    }


    private StringWriter keyToPEM(KeyPair keyPair) throws IOException {
        StringWriter sw = new StringWriter();
        PemObject pemObject = new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded());
        try (PemWriter pemWriter = new PemWriter(sw)) {
            pemWriter.writeObject(pemObject);
        }
        return sw;
    }


    /**
     * @param csr
     * @return
     */
    public PrivateKey getPrivateKey(CSR csr) {

        PrivateKey priKey = null;

        try {
            List<ProtectedContent> pcList = protContentRepository.findByCertificateId(csr.getId());

            if (pcList.isEmpty()) {
                LOG.error("retrieval of private key for csr '{}' returns no key!", csr.getId());
            } else {
                if (pcList.size() > 1) {
                    LOG.warn("retrieval of private key for certificate '{}' returns more than one key ({}) !", csr.getId(), pcList.size());
                }

                String content = protUtil.unprotectString(pcList.get(0).getContentBase64());
                priKey = cryptoUtil.convertPemToPrivateKey(content);
                LOG.debug("getPrivateKey() returns key for csr #" + csr.getId());
            }

        } catch (GeneralSecurityException e) {
            LOG.warn("getPrivateKey", e);
        }

        return priKey;
    }

    /**
     * @param cert
     * @return
     */
    public PrivateKey getPrivateKey(Certificate cert) {

        PrivateKey priKey = null;

        try {
            List<ProtectedContent> pcList = protContentRepository.findByCertificateId(cert.getId());

            if (pcList.isEmpty()) {
                LOG.error("retrieval of private key for certificate '{}' returns not key!", cert.getId());
            } else {
                if (pcList.size() > 1) {
                    LOG.warn("retrieval of private key for certificate '{}' returns more than one key ({}) !", cert.getId(), pcList.size());
                }

                String content = protUtil.unprotectString(pcList.get(0).getContentBase64());
                priKey = cryptoUtil.convertPemToPrivateKey(content);
                LOG.debug("getPrivateKey() returns key for csr #" + cert.getId());
            }

        } catch (GeneralSecurityException e) {
            LOG.warn("getPrivateKey", e);
        }

        return priKey;
    }

    /**
     * @param type
     * @param relationType
     * @param id
     * @return
     */
    public PrivateKey getPrivateKey(ProtectedContentType type, ContentRelationType relationType, Long id) {

        PrivateKey priKey = null;

        try {
            List<ProtectedContent> pcList = protContentRepository.findByTypeRelationId(type, relationType, id);

            if (pcList.isEmpty()) {
                LOG.error("retrieval of private key for element with id '{}' returns not key!", id);
            } else {
                if (pcList.size() > 1) {
                    LOG.warn("retrieval of private key for element with id '{}' returns more than one key ({}) !", id, pcList.size());
                }

                String content = protUtil.unprotectString(pcList.get(0).getContentBase64());
                priKey = cryptoUtil.convertPemToPrivateKey(content);
                LOG.debug("getPrivateKey() returns key for ProtectedContent #" + id);

            }

        } catch (GeneralSecurityException e) {
            LOG.warn("getPrivateKey", e);
        }

        return priKey;
    }

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point"
     * extension in a X.509 certificate. If CRL distribution point extension is
     * unavailable, returns an empty list.
     */
    public List<String> getCrlDistributionPoints(X509Certificate cert) throws IOException {

        List<String> crlUrls = new ArrayList<>();

        byte[] crldpExt = cert.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
        if (crldpExt != null && crldpExt.length > 0) {

            ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));

            ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
            DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
            byte[] crldpExtOctets = dosCrlDP.getOctets();

            ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));

            ASN1Primitive derObj2 = oAsnInStream2.readObject();
            CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                DistributionPointName dpn = dp.getDistributionPoint();
                // Look for URIs in fullName
                if (dpn != null) {
                    if (dpn.getType() == DistributionPointName.FULL_NAME) {
                        GeneralName[] genNames = GeneralNames.getInstance(
                            dpn.getName()).getNames();
                        // Look for an URI
                        for (int j = 0; j < genNames.length; j++) {
                            if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                                String url = DERIA5String.getInstance(genNames[j].getName()).getString();
                                crlUrls.add(url);
                            }
                        }
                    }
                }
            }

            oAsnInStream.close();
            oAsnInStream2.close();
        }

        return crlUrls;
    }

    public void setRevocationStatus(final Certificate cert, final String revocationReason, final Date revocationDate) {
        setRevocationStatus(cert, revocationReason, DateUtil.asInstant(revocationDate));
    }

    public void setRevocationStatus(final Certificate cert, final String revocationReason, final Instant revocationDate) {

        cert.setActive(false);
        cert.setRevoked(true);
        if (revocationReason == null || revocationReason.trim().isEmpty()) {
            cert.setRevocationReason("unspecified");
        } else {
            cert.setRevocationReason(revocationReason);
        }

        cert.setRevokedSince(revocationDate);
    }


    /**
     * @param sanArr SAN array
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(String[] sanArr) {

        return findReplaceCandidates(null, sanArr);
    }

    public List<Certificate> findReplaceCandidates(String cn, String[] sanArr) {
        return findReplaceCandidates(Instant.now(), cn, sanArr);
    }

    /**
     * @param sanArr SAN array
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(Instant validOn, String cn, String[] sanArr) {

        List<String> sans = new ArrayList<>();
        for (String san : sanArr) {
            LOG.debug("SAN present: {} ", san);
            sans.add(san.toLowerCase(Locale.ROOT));
        }

        return findReplaceCandidates(validOn, cn, sans);

    }

    /**
     * @param sanList SAN list
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(Instant validOn, String cn, List<String> sanList) {

        if (cn != null) {
            if (!sanList.contains(cn.toLowerCase(Locale.ROOT))) {
                sanList.add(cn.toLowerCase(Locale.ROOT));
            }
        }
        return findReplaceCandidates(validOn, sanList);

    }

    /**
     * @param sans SANs as List
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(Instant validOn, List<String> sans) {

        LOG.debug("sans list contains {} elements", sans.size());

        List<Certificate> candidateList = new ArrayList<>();

        if (sans.size() == 0) {
            return candidateList;
        }

        List<Certificate> matchingCertList = certificateRepository.findActiveCertificatesBySANs(validOn, sans);
        LOG.debug("objArrList contains {} elements", matchingCertList.size());


        for (Certificate cert : matchingCertList) {
            LOG.debug("replacement candidate {}: {} ", cert.getId(), cert.getSubject());

            boolean matches = true;
            for (CertificateAttribute certAttr : cert.getCertificateAttributes()) {

                if (certAttr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN) || certAttr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_VSAN)) {
                    String san = certAttr.getValue();
                    if (!sans.contains(san)) {
                        matches = false;
                        LOG.debug("candidate san {} NOT in provided san list", san);
                        break;
                    }
                }
            }
            if (matches) {
                candidateList.add(cert);
                LOG.debug("replacement candidate {}: contains all SANs", cert.getId());
            }
        }

        return candidateList;
    }

    public static String getDownloadFilename(final Certificate cert) {

        String cn = null;
        String e = null;
        String firstSAN = null;
        for( CertificateAttribute certificateAttribute: cert.getCertificateAttributes()){

            if( CertificateAttribute.ATTRIBUTE_RDN_CN.equals(certificateAttribute.getName())) {
                if(cn == null) {
                    cn = certificateAttribute.getValue();
                    LOG.debug("getDownloadFilename: cn = {}", cn);
                }
            }
            if( CertificateAttribute.ATTRIBUTE_SUBJECT.equals(certificateAttribute.getName())){
                if( certificateAttribute.getValue().startsWith("e=") &&
                    e == null ){
                    e = certificateAttribute.getValue().substring(2);
                    LOG.debug("getDownloadFilename: e = {}", e);
                }
            }
            if( CertificateAttribute.ATTRIBUTE_SAN.equals(certificateAttribute.getName()) &&
                firstSAN == null){
                firstSAN = certificateAttribute.getValue();
                LOG.debug("getDownloadFilename: firstSAN = {}", firstSAN);
            }
        }

        String downloadFilename = cert.getSubject();
        if( cn != null){
            downloadFilename = cn;
        }else if( e != null){
            downloadFilename = e;
        }else if( firstSAN != null){
            downloadFilename = firstSAN;
        }

        downloadFilename = downloadFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        if (downloadFilename.trim().isEmpty()) {
            downloadFilename = "cert" + cert.getSerial();
        }
        return downloadFilename;
    }

    public static GeneralName[] splitSANString(final String sans, final String hostname) {

        String[] sanArr = sans.split(",");
        List<GeneralName> generalNameList = new ArrayList<>();
        if( hostname == null || hostname.trim().isEmpty()){
            //
        }else{
            generalNameList.add(buildGeneralNameFromName(hostname));
        }

        for(int i = 0;i < sanArr.length;i++){
            GeneralName generalName = buildGeneralNameFromName(sanArr[i]);

            if( !generalNameList.contains(generalName)) {
                generalNameList.add(generalName);
            }
        }
        return generalNameList.toArray(new GeneralName[0]);
    }

    public static GeneralName buildGeneralNameFromName(final String rawName){

        String name = rawName.trim();
        InetAddressValidator inv = InetAddressValidator.getInstance();
        if( inv.isValidInet4Address(name) || inv.isValidInet6Address(name)) {
            return new GeneralName(GeneralName.iPAddress, name);
        }
        return new GeneralName(GeneralName.dNSName, name);
    }

    public CRLUpdateInfo checkAllCRLsForCertificate(final Certificate cert,
                                                    final X509Certificate x509Cert,
                                                    final CRLUtil crlUtil,
                                                    final HashSet<String> brokenCrlUrlList){

        long now = System.currentTimeMillis();
        CRLUpdateInfo info = new CRLUpdateInfo();
        long maxNextUpdate = System.currentTimeMillis() + 1000L * preferenceUtil.getMaxNextUpdatePeriodCRLSec();

        for( CertificateAttribute certAtt: cert.getCertificateAttributes()) {


            // iterate all CRL URLs
            if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
                String crlUrl = certAtt.getValue();

                if(brokenCrlUrlList.contains(crlUrl)){
                    LOG.debug("CRL URL'{}' already marked as broken / inaccessible", crlUrl);
                    continue;
                }

                info.incUrlCount();
                try {
                    LOG.debug("downloading CRL '{}'", crlUrl);
                    X509CRL crl = crlUtil.downloadCRL(crlUrl);
                    if( crl == null) {
                        LOG.debug("downloaded CRL == null ");
                        continue;
                    }

                    if( crl.getNextUpdate() == null){
                        LOG.warn("nextUpdate missing in CRL '{}' of certificate #{}", crlUrl, cert.getId());
                    }else {
                        long nextUpdate = crl.getNextUpdate().getTime();
                        if (nextUpdate > maxNextUpdate) {
                            LOG.debug("nextUpdate {} from CRL limited to {}", crl.getNextUpdate(), new Date(maxNextUpdate));
                            nextUpdate = maxNextUpdate;
                        }

                        if (nextUpdate < now) {
                            LOG.warn("nextUpdate {} of CRL '{}' of certificate #{} already expired", crl.getNextUpdate(), crlUrl, cert.getId());
                            nextUpdate = maxNextUpdate;
                        }

                        // set the crl's 'next update' timestamp to the certificate
                        setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE, Long.toString(nextUpdate), false);
                    }

                    X509CRLEntry crlItem = crl.getRevokedCertificate(new BigInteger(cert.getSerial()));

                    if( (crlItem != null) && (crl.isRevoked(x509Cert) ) ) {

                        String revocationReason = "unspecified";
                        if( crlItem.getRevocationReason() != null ) {
                            if( cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal())) != null ) {
                                revocationReason = cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal()));
                            }
                        }

                        Date revocationDate = new Date();
                        if( crlItem.getRevocationDate() != null) {
                            revocationDate = crlItem.getRevocationDate();
                        }else {
                            LOG.debug("Checking certificate {}: no RevocationDate present for reason {}!", cert.getId(), revocationReason);
                        }

                        setRevocationStatus(cert, revocationReason, revocationDate);

                        auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_REVOKED_BY_CRL, cert));
                    }
                    info.setSuccess();
                    break;
                } catch (CertificateException | CRLException | IOException | NamingException e2) {
                    LOG.info("Problem retrieving CRL for certificate "+ cert.getId());
                    LOG.debug("CRL retrieval for certificate "+ cert.getId() + " failed", e2);
                    brokenCrlUrlList.add(crlUrl);
                }
            }
        }

        return info;
    }

    public byte[] getContainerBytes(Certificate certDao, String entryAlias, CSR csr, String passwordProtectionAlgo) throws IOException, GeneralSecurityException {

        KeyStoreAndPassphrase keyStoreAndPassphrase = getContainer(certDao, entryAlias, csr, passwordProtectionAlgo);

        byte[] contentBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            keyStoreAndPassphrase.keyStore.store(baos, keyStoreAndPassphrase.getPassphraseChars());
            contentBytes = baos.toByteArray();

            if (LOG.isDebugEnabled()) {
                ByteArrayInputStream bais = new ByteArrayInputStream(contentBytes);
                KeyStore store = KeyStore.getInstance("pkcs12");
                store.load(bais, keyStoreAndPassphrase.getPassphraseChars());

                java.security.cert.Certificate cert = store.getCertificate(entryAlias);
                LOG.debug("retrieved cert " + cert);
            }
        }
        return contentBytes;
    }

    @NotNull
    public KeyStoreAndPassphrase getContainer(Certificate certDao, String entryAlias, CSR csr, String passwordProtectionAlgo) throws IOException, GeneralSecurityException {

        if (!csr.isServersideKeyGeneration()) {
            throw new GeneralSecurityException("problem downloading keystore content for csr id " + csr.getId() + ": key not generated serverside");
        }

        List<ProtectedContent> protContentList = protUtil.retrieveProtectedContent(ProtectedContentType.PASSWORD,
            ContentRelationType.CSR, csr.getId());
        if (protContentList.size() == 0) {
            throw new GeneralSecurityException("problem downloading keystore content for csr id " + csr.getId() + ": no keystore passphrase available ");
        }

        PrivateKey key = getPrivateKey(ProtectedContentType.KEY, ContentRelationType.CSR, csr.getId());

        byte[] salt = new byte[20];
        new SecureRandom().nextBytes(salt);

        char[] passphraseChars = protUtil.unprotectString(protContentList.get(0).getContentBase64()).toCharArray();
        KeyStore p12 = KeyStore.getInstance("pkcs12");
        p12.load(null, passphraseChars);

        X509Certificate[] chain = getX509CertificateChain(certDao);

        Set<KeyStore.Entry.Attribute> privateKeyAttributes = new HashSet<>();
        p12.setEntry(entryAlias,
            new KeyStore.PrivateKeyEntry(key, chain, privateKeyAttributes),
            new KeyStore.PasswordProtection(passphraseChars,
                passwordProtectionAlgo,
                new PBEParameterSpec(salt, 100000)));

        return new KeyStoreAndPassphrase(p12, passphraseChars);
    }

    public class KeyStoreAndPassphrase{

        private KeyStore keyStore;
        private char[] passphraseChars;

        public KeyStoreAndPassphrase(KeyStore keyStore, char[] passphraseChars){
            this.keyStore = keyStore;
            this.passphraseChars = passphraseChars;
        }

        public KeyStore getKeyStore() {
            return keyStore;
        }

        public char[] getPassphraseChars() {
            return passphraseChars;
        }
    }
}


