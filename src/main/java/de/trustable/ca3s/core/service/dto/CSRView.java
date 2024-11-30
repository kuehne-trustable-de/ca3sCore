package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvRecurse;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.service.util.CSRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CSRView implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CSRView.class);

    @CsvBindByName
    private Long id;

    @CsvBindByName
    private Long certificateId;

    @CsvBindByName
    private CsrStatus status;

    @CsvBindByName
    private String subject;

    @CsvBindByName
    private String sans;

    @CsvBindByName
    private String[] sanArr;

    @CsvBindByName
  	private PipelineType pipelineType;

    @CsvBindByName
    private Instant rejectedOn;

    @CsvBindByName
    private String rejectionReason;

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

    @CsvBindByName
    private String processingCA;

    @CsvBindByName
    private String pipelineName;

    @CsvIgnore
    private Long pipelineId;

    @CsvBindByName
    private String x509KeySpec;

    @CsvBindByName
    private String hashAlgorithm;

    @CsvBindByName
    private String keyAlgorithm;

    @CsvBindByName
    private String keyLength;

    @CsvBindByName
    private String signingAlgorithm;

    @CsvBindByName
    private String publicKeyAlgorithm;

    @CsvBindByName
    private Instant requestedOn;

    @CsvBindByName
    private Boolean isCSRValid;

    @CsvBindByName
    private Boolean serversideKeyGeneration;

    @CsvBindByName
    private String processInstanceId;

    @CsvBindByName
    private String publicKeyHash;

    @CsvBindByName
    private String administeredBy;

    @CsvBindByName
    private String acceptedBy;

    @CsvBindByName
    private Instant approvedOn;

    @CsvBindByName
    private String comment;

    @CsvRecurse
    private NamedValue[] arArr;

    @CsvIgnore
    private String csrBase64;

    @CsvIgnore
    private AuditView[] auditViewArr;

    @CsvIgnore
    private boolean isAdministrable;

    @CsvBindByName
    private boolean tosAgreed;

    @CsvBindByName
    private String tosAgreementLink;

    public CSRView() {
    }

    public CSRView(final CSRUtil csrUtil, final CSR csr, boolean doDNSLookup) {

    	this.id = csr.getId();
        this.csrBase64 = "";
    	this.certificateId = csr.getCertificate() != null? csr.getCertificate().getId(): null;
    	this.subject = csr.getSubject();
    	this.sans = csr.getSans();
    	this.status = csr.getStatus();
    	this.publicKeyAlgorithm = csr.getPublicKeyAlgorithm();
    	this.signingAlgorithm = csr.getSigningAlgorithm();
    	this.x509KeySpec = csr.getx509KeySpec();

        this.keyLength = csr.getKeyLength().toString();

        this.requestedOn = csr.getRequestedOn();
        this.requestedBy = csr.getRequestedBy();


        if( csr.getTenant() != null ){
            this.tenantName = csr.getTenant().getLongname();
        }else{
            this.tenantName = "";
        }

        Set<CsrAttribute> attributes = csr.getCsrAttributes();
        List<String> sanList = new ArrayList<>();
        for (CsrAttribute csrAttribute : attributes) {
            if (csrAttribute.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN)) {
                String value = csrAttribute.getValue();
                if (doDNSLookup) {
                    try {
                        if (value.startsWith("IP:")) {
                            String ip = value.substring(3);
                            String names = "";
                            InetAddress[] inetAddresses = InetAddress.getAllByName(ip);
                            for (InetAddress inetAddress : inetAddresses) {
                                // return real names, not just the known IP
                                if (!ip.equals(inetAddress.getHostName())) {
                                    if (!names.isEmpty()) {
                                        names += ", ";
                                    }
                                    names += inetAddress.getHostName();
                                }
                            }
                            if (!names.isEmpty()) {
                                value += " (" + names + ")";
                            }
                        } else if (value.startsWith("DNS:")) {
                            if (InetAddress.getAllByName(value.substring(4)).length > 0) {
                                value += " (in DNS vorhanden)";
                            }
                        } else {
                            if (InetAddress.getAllByName(value).length > 0) {
                                value += " (in DNS vorhanden)";
                            }
                        }
                    } catch (UnknownHostException e) {
                        LOG.debug("DNS lookup of '" + value + "' failed: {}",  e.getLocalizedMessage());
                    }
                }
                sanList.add(value);
            }
        }
        this.sanArr = sanList.toArray(new String[0]);

        this.hashAlgorithm = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_HASH_ALGO);
        this.processingCA = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_PROCESSING_CA);

        this.tosAgreed = "true".equalsIgnoreCase(csrUtil.getCSRAttribute(csr,CsrAttribute.ATTRIBUTE_TOS_AGREED));
        this.tosAgreementLink = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_TOS_AGREEMENT_LINK);

        Map<String, Integer> orderAttributeMap = new HashMap<>();

        if( csr.getPipeline() != null) {
            this.pipelineName = csr.getPipeline().getName();
            this.pipelineType = csr.getPipeline().getType();
            orderAttributeMap =
                csr.getPipeline().getPipelineAttributes().stream()
                    .filter(attr -> (attr.getName().startsWith("RESTR_ARA_") && attr.getName().endsWith("_NAME")))
                    .collect(Collectors.toMap( attr -> (attr.getValue()),
                        attr -> (Integer.parseInt(attr.getName().replace("RESTR_ARA_", "").replace("_NAME", "")))));
        }else{
            this.pipelineName = null;
            this.pipelineType = null;
        }
        this.isAdministrable = false;


        this.isCSRValid = csr.getIsCSRValid();
        this.serversideKeyGeneration = csr.isServersideKeyGeneration();

        this.processInstanceId = csr.getProcessInstanceId();

        this.publicKeyHash = csr.getPublicKeyHash();

        this.administeredBy = csr.getAdministeredBy();
        this.approvedOn = csr.getApprovedOn();
        this.acceptedBy = csr.getAcceptedBy();

        if( csr.getComment() != null) {
            this.comment = csr.getComment().getComment();
        }

        List<NamedValue> listArNamedAttributes = copyArAttributes(csr);

        listArNamedAttributes.sort( new NVOrderComparator(orderAttributeMap));

        for( String attName: orderAttributeMap.keySet()){
            if( !listArNamedAttributes.stream().anyMatch(nv ->( nv.getName().equals(attName)))){
                listArNamedAttributes.add(new NamedValue(attName));
            }
        }

        this.arArr = listArNamedAttributes.toArray(new NamedValue[0]);

    }

    private List<NamedValue> copyArAttributes(final CSR csr) {

        List<NamedValue> nvList = new ArrayList<>();
        for(CsrAttribute csrAttribute: csr.getCsrAttributes()){
            if(csrAttribute.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                NamedValue nv = new NamedValue();
                nv.setName(csrAttribute.getName().substring(CsrAttribute.ARA_PREFIX.length()));
                nv.setValue(csrAttribute.getValue());
                nvList.add(nv);
            }
        }
        return nvList;
    }

    public Long getId() {
		return id;
	}

	public Long getCertificateId() {
		return certificateId;
	}

	public CsrStatus getStatus() {
		return status;
	}


	public String getProcessingCA() {
		return processingCA;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	public String getX509KeySpec() {
		return x509KeySpec;
	}

	public String getKeyLength() {
		return keyLength;
	}

	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}

	public String getPublicKeyAlgorithm() {
		return publicKeyAlgorithm;
	}

	public Instant getRequestedOn() {
		return requestedOn;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public void setStatus(CsrStatus status) {
		this.status = status;
	}

	public void setProcessingCA(String processingCA) {
		this.processingCA = processingCA;
	}

	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	public void setX509KeySpec(String x509KeySpec) {
		this.x509KeySpec = x509KeySpec;
	}

	public void setKeyLength(String keyLength) {
		this.keyLength = keyLength;
	}

	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
		this.publicKeyAlgorithm = publicKeyAlgorithm;
	}

	public void setRequestedOn(Instant requestedOn) {
		this.requestedOn = requestedOn;
	}

	public PipelineType getPipelineType() {
		return pipelineType;
	}

	public Instant getRejectedOn() {
		return rejectedOn;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setPipelineType(PipelineType pipelineType) {
		this.pipelineType = pipelineType;
	}

	public void setRejectedOn(Instant rejectedOn) {
		this.rejectedOn = rejectedOn;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

    public String getSans() {
		return sans;
	}

	public void setSans(String sans) {
		this.sans = sans;
	}

    public String[] getSanArr() {
        return sanArr;
    }

    public void setSanArr(String[] sanArr) {
        this.sanArr = sanArr;
    }

    public AuditView[] getAuditViewArr() {
        return auditViewArr;
    }

    public void setAuditViewArr(AuditView[] auditViewArr) {
        this.auditViewArr = auditViewArr;
    }

    public String getCsrBase64() {
        return csrBase64;
    }

    public void setCsrBase64(String csrBase64) {
        this.csrBase64 = csrBase64;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Boolean getCSRValid() {
        return isCSRValid;
    }

    public void setCSRValid(Boolean CSRValid) {
        isCSRValid = CSRValid;
    }

    public Boolean getServersideKeyGeneration() {
        return serversideKeyGeneration;
    }

    public void setServersideKeyGeneration(Boolean serversideKeyGeneration) {
        this.serversideKeyGeneration = serversideKeyGeneration;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public String getAdministeredBy() {
        return administeredBy;
    }

    public void setAdministeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Instant getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(Instant approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean getIsAdministrable() {
        return isAdministrable;
    }

    public void setAdministrable(boolean administrable) {
        isAdministrable = administrable;
    }

    public boolean isTosAgreed() {
        return tosAgreed;
    }

    public void setTosAgreed(boolean tosAgreed) {
        this.tosAgreed = tosAgreed;
    }

    public String getTosAgreementLink() {
        return tosAgreementLink;
    }

    public void setTosAgreementLink(String tosAgreementLink) {
        this.tosAgreementLink = tosAgreementLink;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public boolean isAdministrable() {
        return isAdministrable;
    }

    public NamedValue[] getArArr() {
        return arArr;
    }

    public void setArArr(NamedValue[] arArr) {
        this.arArr = arArr;
    }

    @Override
    public String toString() {
        return "CSRView{" +
            "id=" + id +
            ", certificateId=" + certificateId +
            ", status=" + status +
            ", subject='" + subject + '\'' +
            ", sans='" + sans + '\'' +
            ", sanArr=" + Arrays.toString(sanArr) +
            ", pipelineType=" + pipelineType +
            ", rejectedOn=" + rejectedOn +
            ", rejectionReason='" + rejectionReason + '\'' +
            ", requestedBy='" + requestedBy + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", tenantName='" + tenantName + '\'' +
            ", processingCA='" + processingCA + '\'' +
            ", pipelineName='" + pipelineName + '\'' +
            ", pipelineId=" + pipelineId +
            ", x509KeySpec='" + x509KeySpec + '\'' +
            ", hashAlgorithm='" + hashAlgorithm + '\'' +
            ", keyAlgorithm='" + keyAlgorithm + '\'' +
            ", keyLength='" + keyLength + '\'' +
            ", signingAlgorithm='" + signingAlgorithm + '\'' +
            ", publicKeyAlgorithm='" + publicKeyAlgorithm + '\'' +
            ", requestedOn=" + requestedOn +
            ", isCSRValid=" + isCSRValid +
            ", serversideKeyGeneration=" + serversideKeyGeneration +
            ", processInstanceId='" + processInstanceId + '\'' +
            ", publicKeyHash='" + publicKeyHash + '\'' +
            ", administeredBy='" + administeredBy + '\'' +
            ", acceptedBy='" + acceptedBy + '\'' +
            ", approvedOn=" + approvedOn +
            ", comment='" + comment + '\'' +
            ", arArr=" + Arrays.toString(arArr) +
            ", csrBase64='" + csrBase64 + '\'' +
            ", auditViewArr=" + Arrays.toString(auditViewArr) +
            ", isAdministrable=" + isAdministrable +
            '}';
    }

}
