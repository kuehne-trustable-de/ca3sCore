package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private String requestorComment;

    @CsvBindByName
    private String administrationComment;

    @CsvRecurse
    private NamedValue[] arArr;

    @CsvIgnore
    private String csrBase64;

    @CsvIgnore
    private AuditView[] auditViewArr;

    @CsvIgnore
    private boolean isAdministrable;

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

        Set<CsrAttribute> attributes = csr.getCsrAttributes();
        List<String> sanList = new ArrayList<>();
        for( CsrAttribute csrAttribute: attributes){
            if( csrAttribute.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN)){
                String value = csrAttribute.getValue();
                if( doDNSLookup) {
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
                        if( LOG.isDebugEnabled()) {
                            LOG.info("DNS lookup of '" + value + "' failed.", e);
                        }else {
                            LOG.info("DNS lookup of '" + value + "' failed: {}", e.getMessage());
                        }
                    }
                }
                sanList.add(value);
            }
        }
        this.sanArr = sanList.toArray(new String[0]);

        this.hashAlgorithm = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_HASH_ALGO);

        this.processingCA = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_PROCESSING_CA);

        if( csr.getPipeline() != null) {
            this.pipelineName = csr.getPipeline().getName();
            this.pipelineType = csr.getPipeline().getType();
        }else{
            this.pipelineName = null;
            this.pipelineType = null;
        }
        this.isAdministrable = false;


        this.isCSRValid = csr.isIsCSRValid();
        this.serversideKeyGeneration = csr.isServersideKeyGeneration();

        this.processInstanceId = csr.getProcessInstanceId();

        this.publicKeyHash = csr.getPublicKeyHash();

        this.administeredBy = csr.getAdministeredBy();
        this.approvedOn = csr.getApprovedOn();
        this.acceptedBy = csr.getAcceptedBy();
        this.requestorComment = csr.getRequestorComment();
        this.administrationComment = csr.getAdministrationComment();

        this.arArr = copyArAttributes(csr);

    }

    private NamedValue[] copyArAttributes(final CSR csr) {

        List<NamedValue> nvList = new ArrayList<>();
        for(CsrAttribute csrAttribute: csr.getCsrAttributes()){
            if(csrAttribute.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                NamedValue nv = new NamedValue();
                nv.setName(csrAttribute.getName().substring(CsrAttribute.ARA_PREFIX.length()));
                nv.setValue(csrAttribute.getValue());
                nvList.add(nv);
            }
        }
        return nvList.toArray(new NamedValue[0]);
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

    public String getRequestorComment() {
        return requestorComment;
    }

    public void setRequestorComment(String requestorComment) {
        this.requestorComment = requestorComment;
    }

    public String getAdministrationComment() {
        return administrationComment;
    }

    public void setAdministrationComment(String administrationComment) {
        this.administrationComment = administrationComment;
    }

    public boolean getIsAdministrable() {
        return isAdministrable;
    }

    public void setAdministrable(boolean administrable) {
        isAdministrable = administrable;
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
}
