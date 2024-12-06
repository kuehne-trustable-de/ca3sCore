package de.trustable.ca3s.core.service.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvRecurse;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CertificateView implements Serializable {

    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(CertificateView.class);

    @CsvBindByName
    private Long id;

    @CsvIgnore
    private Long csrId;

    @CsvIgnore
    private Long issuerId;

    @CsvIgnore
    private Long rootId;

    @CsvIgnore
    private String tbsDigest;

    @CsvBindByName
    private String subject;

    @CsvIgnore
    private String rdn_c;

    @CsvIgnore
    private String rdn_cn;

    @CsvIgnore
    private String issuer_rdn_cn;

    @CsvIgnore
    private String root_rdn_cn;

    @CsvIgnore
    private String rdn_o;

    @CsvIgnore
    private String rdn_ou;

    @CsvIgnore
    private String rdn_s;

    @CsvIgnore
    private String rdn_l;

    @CsvBindByName
    private String sans;

    @CsvBindByName
    private String issuer;

    @CsvBindByName
    private String root;

    @CsvBindByName
    private Boolean trusted;

    @CsvIgnore
    private String fingerprintSha1;

    @CsvIgnore
    private String fingerprintSha256;

    @CsvBindByName
    private String type;

    @CsvBindByName
    private String keyLength;
    @CsvBindByName
    private String keyAlgorithm;

    @CsvIgnore
    private String altKeyAlgorithm;

    @CsvIgnore
    private String signingAlgorithm;
    @CsvIgnore
    private String paddingAlgorithm;

    @CsvIgnore
    private String hashAlgorithm;

    @CsvIgnore
    private String description;

    @CsvBindByName
    private String comment;

    @CsvBindByName
    private String csrComment;

    @CsvBindByName
    private String serial;

    @CsvBindByName
    private String serialHex;

    @CsvBindByName
    private Instant validFrom;

    @CsvBindByName
    private Instant validTo;

    private Instant contentAddedAt;

    @CsvBindByName
    private Instant revokedSince;

    @CsvBindByName
    private String revocationReason;

    @CsvBindByName
    private Boolean revoked;

    @CsvBindByName
    private Boolean selfsigned;

    @CsvBindByName
    private Boolean ca;

    @CsvBindByName
    private Boolean intermediate;

    @CsvBindByName
    private Boolean endEntity;

    @CsvBindByName
    private Long chainLength;

    @CsvIgnore
    private String[] usage;

    @CsvBindByName(column="usage")
    private String usageString;

    @CsvIgnore
    private String[] extUsage;

    @CsvBindByName(column="extUsage")
    private String extUsageString;

    @CsvIgnore
    private String[] sanArr;

    @CsvBindByName(column="sans")
    private String sansString;

    @CsvIgnore
    private String typedSansString;

    @CsvIgnore
    private Long caConnectorId;

    @CsvIgnore
    private Long caProcessingId;

    @CsvBindByName
    private String processingCa;

    @CsvIgnore
    private String acmeAccountId;

    @CsvIgnore
    private String acmeOrderId;

    @CsvIgnore
    private String scepTransId;

    @CsvIgnore
    private String scepRecipient;

    @CsvIgnore
    private String fileSource;

    @CsvBindByName
    private String uploadedBy;

    @CsvBindByName
    private String revokedBy;

    @CsvBindByName
    private String requestedBy;

    @CsvIgnore
    private String firstName;

    @CsvIgnore
    private String lastName;

    @CsvIgnore
    private String email;
    @CsvBindByName
    private String tenantName;

    @CsvIgnore
    private Long tenantId;

    @CsvBindByName
    private String crlUrl;

    @CsvIgnore
    private Long crlExpirationNotificationId;

    @CsvIgnore
    private Instant crlNextUpdate;

    @CsvIgnore
    private String certB64;

    @CsvIgnore
    private String downloadFilename;

    @CsvIgnore
    private Boolean isServersideKeyGeneration = false;

    @CsvIgnore
    private Instant serversideKeyValidTo = null;

    @CsvIgnore
    private int serversideKeyLeftUsages = -1;

    @CsvIgnore
    private Boolean isAuditPresent = false;

    @CsvIgnore
    private Boolean isFullChainAvailable = false;

    @CsvIgnore
    private Boolean isIssuingActiveCertificates = false;

    @CsvIgnore
    private String[] replacedCertArr;

    @CsvRecurse
    private NamedValue[] arArr;

    public CertificateView() {}

    public CertificateView(final Certificate cert) {
        this(cert, null);
    }

    public CertificateView(Certificate cert, ProtectedContent pt) {
    	this.id = cert.getId();
    	this.tbsDigest = cert.getTbsDigest();
    	this.subject = cert.getSubject();
    	this.sans = cert.getSans();
        this.issuer = cert.getIssuer();
        this.type = cert.getType();
   		this.keyLength = cert.getKeyLength().toString();
   		this.keyAlgorithm = cert.getKeyAlgorithm();
		this.signingAlgorithm = cert.getSigningAlgorithm();
		this.paddingAlgorithm = cert.getPaddingAlgorithm();
		this.hashAlgorithm = cert.getHashingAlgorithm();
    	this.description = cert.getDescription();
    	this.serial = cert.getSerial();
    	try {
            BigInteger bi = new BigInteger(serial);
            this.serialHex = bi.toString(16);
        }catch(NumberFormatException nfe){
            this.serialHex ="";
        }
        this.validFrom = cert.getValidFrom();
    	this.validTo = cert.getValidTo();
    	this.contentAddedAt = cert.getContentAddedAt();
    	this.revokedSince = cert.getRevokedSince();
    	this.revocationReason = cert.getRevocationReason();
        this.revoked = cert.isRevoked();
        this.trusted = cert.isTrusted();
    	this.certB64 = cert.getContent();

        this.csrComment = "";

        Map<String, Integer> orderAttributeMap = new HashMap<>();

        CSR csr = cert.getCsr();
    	if( csr != null) {
    		this.requestedBy = csr.getRequestedBy();
    		this.csrId = csr.getId();
    		this.isServersideKeyGeneration = csr.isServersideKeyGeneration();

            if( pt != null){
                this.serversideKeyLeftUsages = pt.getLeftUsages();
                this.serversideKeyValidTo = pt.getValidTo();
            }

    		if(csr.getComment() != null) {
                this.csrComment = csr.getComment().getComment();
            }

            if( csr.getPipeline() != null) {
                orderAttributeMap =
                    csr.getPipeline().getPipelineAttributes().stream()
                        .filter(attr -> (attr.getName().startsWith("RESTR_ARA_") && attr.getName().endsWith("_NAME")))
                        .collect(Collectors.toMap(attr -> (attr.getValue()),
                            attr -> (Integer.parseInt(attr.getName().replace("RESTR_ARA_", "").replace("_NAME", "")))));
            }
        } else {
    		this.requestedBy = "";
    	}

        if( cert.getTenant() != null ){
            this.tenantName = cert.getTenant().getLongname();
            this.tenantId = cert.getTenant().getId();
        }else{
            this.tenantName = "";
            this.tenantId = 0L;
        }

        this.root = "";
        this.intermediate = false;
        this.isFullChainAvailable = false;
    	if( cert.getIssuingCertificate() != null) {
    		this.issuerId = cert.getIssuingCertificate().getId();

            Certificate issuerCheck = cert.getIssuingCertificate();
            for(int i = 0; i < 10 && issuerCheck != null; i++){
                if( issuerCheck.isSelfsigned() ){
                    this.isFullChainAvailable = true;
                    this.rootId = issuerCheck.getId();
                    break;
                }
                issuerCheck = issuerCheck.getIssuingCertificate();
            }
        }

    	List<String> usageList = new ArrayList<>();
        List<String> extUsageList = new ArrayList<>();
        List<String> sanList = new ArrayList<>();
        List<String> replacedCertList = new ArrayList<>();

        this.usageString = "";
        this.extUsageString = "";
        this.sansString = "";
        this.typedSansString = "";

        this.altKeyAlgorithm = null;

        for (CertificateAttribute certAttr : cert.getCertificateAttributes()) {
           try {
                if (CertificateAttribute.ATTRIBUTE_CA_CONNECTOR_ID.equalsIgnoreCase(certAttr.getName())) {
                    this.caConnectorId = Long.parseLong(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID.equalsIgnoreCase(certAttr.getName())) {
                    this.caProcessingId = Long.parseLong(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_PROCESSING_CA.equalsIgnoreCase(certAttr.getName())) {
                    this.processingCa = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_SOURCE.equalsIgnoreCase(certAttr.getName())) {
                    this.fileSource = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_UPLOADED_BY.equalsIgnoreCase(certAttr.getName())) {
                    this.uploadedBy = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_REVOKED_BY.equalsIgnoreCase(certAttr.getName())) {
                    this.revokedBy = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_CRL_URL.equalsIgnoreCase(certAttr.getName())) {
                    this.crlUrl = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA1.equalsIgnoreCase(certAttr.getName())) {
                    this.fingerprintSha1 = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA256.equalsIgnoreCase(certAttr.getName())) {
                    this.fingerprintSha256 = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_CN.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_cn = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_C.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_c = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_O.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_o = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_OU.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_ou = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_L.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_l = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_RDN_S.equalsIgnoreCase(certAttr.getName())) {
                    this.rdn_s = certAttr.getValue();

                } else if (CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE.equalsIgnoreCase(certAttr.getName())) {
                    this.crlNextUpdate = Instant.ofEpochMilli(Long.parseLong(certAttr.getValue()));
                } else if (CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID.equalsIgnoreCase(certAttr.getName())) {
                    this.acmeAccountId = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_ACME_ORDER_ID.equalsIgnoreCase(certAttr.getName())) {
                    this.acmeOrderId = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT.equalsIgnoreCase(certAttr.getName())) {
                    this.scepRecipient = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID.equalsIgnoreCase(certAttr.getName())) {
                    this.scepTransId = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_SELFSIGNED.equalsIgnoreCase(certAttr.getName())) {
                    this.selfsigned = Boolean.valueOf(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_CA.equalsIgnoreCase(certAttr.getName())) {
                    this.ca = Boolean.valueOf(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_CA3S_ROOT.equalsIgnoreCase(certAttr.getName())) {
                } else if (CertificateAttribute.ATTRIBUTE_ROOT.equalsIgnoreCase(certAttr.getName())) {
                    this.root = certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_CA3S_INTERMEDIATE.equalsIgnoreCase(certAttr.getName())) {
                    this.intermediate = true;
                } else if (CertificateAttribute.ATTRIBUTE_END_ENTITY.equalsIgnoreCase(certAttr.getName())) {
                    this.endEntity = Boolean.valueOf(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_CHAIN_LENGTH.equalsIgnoreCase(certAttr.getName())) {
                    this.chainLength = Long.parseLong(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_USAGE.equalsIgnoreCase(certAttr.getName())) {
                    usageList.add(certAttr.getValue());
                    this.usageString = this.usageString.isEmpty() ? certAttr.getValue() : this.usageString + ", " + certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_EXTENDED_USAGE.equalsIgnoreCase(certAttr.getName())) {
                    extUsageList.add(certAttr.getValue());
                    this.extUsageString = this.extUsageString.isEmpty() ? certAttr.getValue() : this.extUsageString + ", " + certAttr.getValue();
                } else if (CsrAttribute.ATTRIBUTE_SAN.equalsIgnoreCase(certAttr.getName())) {
                    sanList.add(certAttr.getValue());
                    this.sansString = this.sansString.isEmpty() ? certAttr.getValue() : this.sansString + ", " + certAttr.getValue();
                } else if (CsrAttribute.ATTRIBUTE_TYPED_SAN.equalsIgnoreCase(certAttr.getName())) {
                    this.typedSansString = this.typedSansString.isEmpty() ? certAttr.getValue() : this.typedSansString + ", " + certAttr.getValue();
                } else if (CertificateAttribute.ATTRIBUTE_REPLACED_BY.equalsIgnoreCase(certAttr.getName())) {
                    replacedCertList.add(certAttr.getValue());
                } else if (CertificateAttribute.ATTRIBUTE_ALT_ALGO.equalsIgnoreCase(certAttr.getName())) {
                    altKeyAlgorithm = certAttr.getValue();
                } else {
 //                   LOG.debug("Irrelevant certificate attribute '{}' with value '{}'", certAttr.getName(), certAttr.getValue());

                }
            }catch(NumberFormatException nfe){
                LOG.warn("Number parsing problem for certificate attribute '{}' with value '{}'", certAttr.getName(), certAttr.getValue());
            }
        }

        this.usage = usageList.toArray(new String[0]);
        this.extUsage = extUsageList.toArray(new String[0]);
        this.sanArr = sanList.toArray(new String[0]);
        this.replacedCertArr = replacedCertList.toArray(new String[0]);

        this.downloadFilename = CertificateUtil.getDownloadFilename(cert);

        List<NamedValue> listArNamedAttributes = copyArAttributes(cert);
        listArNamedAttributes.sort( new NVOrderComparator(orderAttributeMap));

        for( String attName: orderAttributeMap.keySet()){
            if( !listArNamedAttributes.stream().anyMatch(nv ->( nv.getName().equals(attName)))){
                listArNamedAttributes.add(new NamedValue(attName));
            }
        }

        this.arArr = listArNamedAttributes.toArray(new NamedValue[0]);

        this.issuer_rdn_cn = getRdnCn(cert.getIssuingCertificate());
        this.root_rdn_cn = getRdnCn(cert.getRootCertificate());

        CertificateComment comment = (cert.getComment() == null)? new CertificateComment() : cert.getComment();
        this.setComment( (comment.getComment()==null) ? "": comment.getComment());

    }


    private String getRdnCn(Certificate cert){
        if( cert != null ){
            for (CertificateAttribute certAttr : cert.getCertificateAttributes()) {
                if (CertificateAttribute.ATTRIBUTE_RDN_CN.equalsIgnoreCase(certAttr.getName())) {
                    return certAttr.getValue();
                }
            }
        }
        return "";
    }


	public Long getId() {
		return id;
	}

	public String getTbsDigest() {
		return tbsDigest;
	}

	public String getSubject() {
		return subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getSerial() {
		return serial;
	}

	public Instant getValidFrom() {
		return validFrom;
	}

	public Instant getValidTo() {
		return validTo;
	}

	public Instant getContentAddedAt() {
		return contentAddedAt;
	}

	public Instant getRevokedSince() {
		return revokedSince;
	}

	public String getRevocationReason() {
		return revocationReason;
	}

	public Boolean getRevoked() {
		return revoked;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTbsDigest(String tbsDigest) {
		this.tbsDigest = tbsDigest;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSerial(String serial) {
		this.serial = serial;
        try {
            BigInteger bi = new BigInteger(serial);
            this.serialHex = bi.toString(16);
        }catch(NumberFormatException nfe){
            this.serialHex ="";
        }
    }

	public void setValidFrom(Instant validFrom) {
		this.validFrom = validFrom;
	}

	public void setValidTo(Instant validTo) {
		this.validTo = validTo;
	}

	public void setContentAddedAt(Instant contentAddedAt) {
		this.contentAddedAt = contentAddedAt;
	}

	public void setRevokedSince(Instant revokedSince) {
		this.revokedSince = revokedSince;
	}

	public void setRevocationReason(String revocationReason) {
		this.revocationReason = revocationReason;
	}

	public void setRevoked(Boolean revoked) {
		this.revoked = revoked;
	}

	public String getKeyLength() {
		return keyLength;
	}

	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}

	public String getPaddingAlgorithm() {
		return paddingAlgorithm;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setKeyLength(String keyLength) {
		this.keyLength = keyLength;
	}

	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	public void setPaddingAlgorithm(String paddingAlgorithm) {
		this.paddingAlgorithm = paddingAlgorithm;
	}

	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public String getSans() {
		return sans;
	}

	public void setSans(String sans) {
		this.sans = sans;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

    public String getAltKeyAlgorithm() {
        return altKeyAlgorithm;
    }

    public void setAltKeyAlgorithm(String altKeyAlgorithm) {
        this.altKeyAlgorithm = altKeyAlgorithm;
    }

    public Boolean getSelfsigned() {
		return selfsigned;
	}

	public Boolean getCa() {
		return ca;
	}

	public String getRoot() {
		return root;
	}

	public Boolean getIntermediate() {
		return intermediate;
	}

	public Boolean getEndEntity() {
		return endEntity;
	}

	public Long getChainLength() {
		return chainLength;
	}

	public String[] getUsage() {
		return usage;
	}

	public String[] getSanArr() {
		return sanArr;
	}

	public Long getCaConnectorId() {
		return caConnectorId;
	}

	public Long getCaProcessingId() {
		return caProcessingId;
	}

	public String getFileSource() {
		return fileSource;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public String getRevokedBy() {
		return revokedBy;
	}

	public String getCrlUrl() {
		return crlUrl;
	}

	public Instant getCrlNextUpdate() {
		return crlNextUpdate;
	}

	public void setSelfsigned(Boolean selfsigned) {
		this.selfsigned = selfsigned;
	}

	public void setCa(Boolean ca) {
		this.ca = ca;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setIntermediate(Boolean intermediate) {
		this.intermediate = intermediate;
	}

	public void setEndEntity(Boolean endEntity) {
		this.endEntity = endEntity;
	}

	public void setChainLength(Long chainLength) {
		this.chainLength = chainLength;
	}

	public void setUsage(String[] usage) {
		this.usage = usage;
	}

	public void setSanArr(String[] sanArr) {
		this.sanArr = sanArr;
	}

	public void setCaConnectorId(Long caConnectorId) {
		this.caConnectorId = caConnectorId;
	}

	public void setCaProcessingId(Long caProcessingId) {
		this.caProcessingId = caProcessingId;
	}

	public void setFileSource(String fileSource) {
		this.fileSource = fileSource;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public void setRevokedBy(String revokedBy) {
		this.revokedBy = revokedBy;
	}

	public void setCrlUrl(String crlUrl) {
		this.crlUrl = crlUrl;
	}

	public void setCrlNextUpdate(Instant crlNextUpdate) {
		this.crlNextUpdate = crlNextUpdate;
	}

	public String getAcmeAccountId() {
		return acmeAccountId;
	}

	public String getAcmeOrderId() {
		return acmeOrderId;
	}

	public String getScepTransId() {
		return scepTransId;
	}

	public String getScepRecipient() {
		return scepRecipient;
	}

	public void setAcmeAccountId(String acmeAccountId) {
		this.acmeAccountId = acmeAccountId;
	}

	public void setAcmeOrderId(String acmeOrderId) {
		this.acmeOrderId = acmeOrderId;
	}

	public void setScepTransId(String scepTransId) {
		this.scepTransId = scepTransId;
	}

	public void setScepRecipient(String scepRecipient) {
		this.scepRecipient = scepRecipient;
	}

	public String[] getExtUsage() {
		return extUsage;
	}

	public void setExtUsage(String[] extUsage) {
		this.extUsage = extUsage;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public Long getCsrId() {
		return csrId;
	}
    public void setCsrId(Long csrId) {
        this.csrId = csrId;
    }


    public Long getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public String getFingerprintSha1() {
        return fingerprintSha1;
    }

    public void setFingerprintSha1(String fingerprint) {
        this.fingerprintSha1 = fingerprint;
    }

    public String getFingerprintSha256() {
        return fingerprintSha256;
    }

    public void setFingerprintSha256(String fingerprint) {
        this.fingerprintSha256 = fingerprint;
    }

    public String getProcessingCa() {
		return processingCa;
	}

	public void setProcessingCa(String processingCa) {
		this.processingCa = processingCa;
	}

	public String getCertB64() {
		return certB64;
	}

	public void setCertB64(String certB64) {
		this.certB64 = certB64;
	}

	public Boolean getIsServersideKeyGeneration() {
		return isServersideKeyGeneration;
	}

	public void setIsServersideKeyGeneration(Boolean isServersideKeyGeneration) {
		this.isServersideKeyGeneration = isServersideKeyGeneration;
	}

    public String getRdn_c() {
        return rdn_c;
    }

    public void setRdn_c(String rdn_c) {
        this.rdn_c = rdn_c;
    }

    public String getRdn_cn() {
        return rdn_cn;
    }

    public void setRdn_cn(String rdn_cn) {
        this.rdn_cn = rdn_cn;
    }

    public String getIssuer_rdn_cn() {
        return issuer_rdn_cn;
    }

    public void setIssuer_rdn_cn(String issuer_rdn_cn) {
        this.issuer_rdn_cn = issuer_rdn_cn;
    }

    public String getRoot_rdn_cn() {
        return root_rdn_cn;
    }

    public void setRoot_rdn_cn(String root_rdn_cn) {
        this.root_rdn_cn = root_rdn_cn;
    }

    public String getRdn_o() {
        return rdn_o;
    }

    public void setRdn_o(String rdn_o) {
        this.rdn_o = rdn_o;
    }

    public String getRdn_ou() {
        return rdn_ou;
    }

    public void setRdn_ou(String rdn_ou) {
        this.rdn_ou = rdn_ou;
    }

    public String getRdn_s() {
        return rdn_s;
    }

    public void setRdn_s(String rdn_s) {
        this.rdn_s = rdn_s;
    }

    public String getRdn_l() {
        return rdn_l;
    }

    public void setRdn_l(String rdn_l) {
        this.rdn_l = rdn_l;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCsrComment() {
        return csrComment;
    }

    public void setCsrComment(String csrComment) {
        this.csrComment = csrComment;
    }

    public NamedValue[] getArArr() {
        return arArr;
    }

    public void setArArr(NamedValue[] arArr) {
        this.arArr = arArr;
    }

    public String getArValue(final String name) {

        if( arArr != null) {
            for (NamedValue nv : arArr) {
                if (nv.getName().equals(name)) {
                    return nv.getValue();
                }
            }
        }
        return "";
    }

    private List<NamedValue> copyArAttributes(final Certificate cert) {

        List<NamedValue> nvList = new ArrayList<>();
        for(CertificateAttribute certAttr: cert.getCertificateAttributes()){
            if(certAttr.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                NamedValue nv = new NamedValue();
                nv.setName(certAttr.getName().substring(CsrAttribute.ARA_PREFIX.length()));
                nv.setValue(certAttr.getValue());
                nvList.add(nv);
            }
        }
        return nvList;
    }

    public Boolean getAuditPresent() {
        return isAuditPresent;
    }

    public void setAuditPresent(Boolean auditPresent) {
        isAuditPresent = auditPresent;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public String getUsageString() {
        return usageString;
    }

    public void setUsageString(String usageString) {
        this.usageString = usageString;
    }

    public String getExtUsageString() {
        return extUsageString;
    }

    public void setExtUsageString(String extUsageString) {
        this.extUsageString = extUsageString;
    }

    public String getSansString() {
        return sansString;
    }

    public void setSansString(String sansString) {
        this.sansString = sansString;
    }

    public String getTypedSansString() {
        return typedSansString;
    }

    public void setTypedSansString(String typedSansString) {
        this.typedSansString = typedSansString;
    }

    public Boolean getServersideKeyGeneration() {
        return isServersideKeyGeneration;
    }

    public void setServersideKeyGeneration(Boolean serversideKeyGeneration) {
        isServersideKeyGeneration = serversideKeyGeneration;
    }

    public Instant getServersideKeyValidTo() {
        return serversideKeyValidTo;
    }

    public void setServersideKeyValidTo(Instant serversideKeyValidTo) {
        this.serversideKeyValidTo = serversideKeyValidTo;
    }

    public int getServersideKeyLeftUsages() {
        return serversideKeyLeftUsages;
    }

    public void setServersideKeyLeftUsages(int serversideKeyLeftUsages) {
        this.serversideKeyLeftUsages = serversideKeyLeftUsages;
    }

    public String getSerialHex() {
        return serialHex;
    }

    public void setSerialHex(String serialHex) {
        this.serialHex = serialHex;
    }

    public String[] getReplacedCertArr() {
        return replacedCertArr;
    }

    public void setReplacedCertArr(String[] replacedCertArr) {
        this.replacedCertArr = replacedCertArr;
    }

    public Boolean getFullChainAvailable() {
        return isFullChainAvailable;
    }

    public void setFullChainAvailable(Boolean fullChainAvailable) {
        isFullChainAvailable = fullChainAvailable;
    }

    public Long getCrlExpirationNotificationId() {
        return crlExpirationNotificationId;
    }

    public void setCrlExpirationNotificationId(Long crlExpirationNotificationId) {
        this.crlExpirationNotificationId = crlExpirationNotificationId;
    }

    public Boolean getIssuingActiveCertificates() {
        return isIssuingActiveCertificates;
    }

    public void setIssuingActiveCertificates(Boolean issuingActiveCertificates) {
        isIssuingActiveCertificates = issuingActiveCertificates;
    }

    @Override
    public String toString() {
        return "CertificateView{" +
            "id=" + id +
            ", csrId=" + csrId +
            ", issuerId=" + issuerId +
            ", rootId=" + rootId +
            ", tbsDigest='" + tbsDigest + '\'' +
            ", subject='" + subject + '\'' +
            ", rdn_c='" + rdn_c + '\'' +
            ", rdn_cn='" + rdn_cn + '\'' +
            ", issuer_rdn_cn='" + issuer_rdn_cn + '\'' +
            ", root_rdn_cn='" + root_rdn_cn + '\'' +
            ", rdn_o='" + rdn_o + '\'' +
            ", rdn_ou='" + rdn_ou + '\'' +
            ", rdn_s='" + rdn_s + '\'' +
            ", rdn_l='" + rdn_l + '\'' +
            ", sans='" + sans + '\'' +
            ", issuer='" + issuer + '\'' +
            ", root='" + root + '\'' +
            ", trusted=" + trusted +
            ", fingerprintSha1='" + fingerprintSha1 + '\'' +
            ", fingerprintSha256='" + fingerprintSha256 + '\'' +
            ", type='" + type + '\'' +
            ", keyLength='" + keyLength + '\'' +
            ", keyAlgorithm='" + keyAlgorithm + '\'' +
            ", altKeyAlgorithm='" + altKeyAlgorithm + '\'' +
            ", signingAlgorithm='" + signingAlgorithm + '\'' +
            ", paddingAlgorithm='" + paddingAlgorithm + '\'' +
            ", hashAlgorithm='" + hashAlgorithm + '\'' +
            ", description='" + description + '\'' +
            ", comment='" + comment + '\'' +
            ", csrComment='" + csrComment + '\'' +
            ", serial='" + serial + '\'' +
            ", serialHex='" + serialHex + '\'' +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            ", contentAddedAt=" + contentAddedAt +
            ", revokedSince=" + revokedSince +
            ", revocationReason='" + revocationReason + '\'' +
            ", revoked=" + revoked +
            ", selfsigned=" + selfsigned +
            ", ca=" + ca +
            ", intermediate=" + intermediate +
            ", endEntity=" + endEntity +
            ", chainLength=" + chainLength +
            ", usage=" + Arrays.toString(usage) +
            ", usageString='" + usageString + '\'' +
            ", extUsage=" + Arrays.toString(extUsage) +
            ", extUsageString='" + extUsageString + '\'' +
            ", sanArr=" + Arrays.toString(sanArr) +
            ", sansString='" + sansString + '\'' +
            ", typedSansString='" + typedSansString + '\'' +
            ", caConnectorId=" + caConnectorId +
            ", caProcessingId=" + caProcessingId +
            ", processingCa='" + processingCa + '\'' +
            ", acmeAccountId=" + acmeAccountId +
            ", acmeOrderId=" + acmeOrderId +
            ", scepTransId='" + scepTransId + '\'' +
            ", scepRecipient='" + scepRecipient + '\'' +
            ", fileSource='" + fileSource + '\'' +
            ", uploadedBy='" + uploadedBy + '\'' +
            ", revokedBy='" + revokedBy + '\'' +
            ", requestedBy='" + requestedBy + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", tenantName='" + tenantName + '\'' +
            ", tenantÎd=" + tenantId +
            ", crlUrl='" + crlUrl + '\'' +
            ", crlExpirationNotificationId=" + crlExpirationNotificationId +
            ", crlNextUpdate=" + crlNextUpdate +
            ", certB64='" + certB64 + '\'' +
            ", downloadFilename='" + downloadFilename + '\'' +
            ", isServersideKeyGeneration=" + isServersideKeyGeneration +
            ", isAuditPresent=" + isAuditPresent +
            ", isFullChainAvailable=" + isFullChainAvailable +
            ", isIssuingActiveCertificates=" + isIssuingActiveCertificates +
            ", replacedCertArr=" + Arrays.toString(replacedCertArr) +
            ", arArr=" + Arrays.toString(arArr) +
            '}';
    }
}
