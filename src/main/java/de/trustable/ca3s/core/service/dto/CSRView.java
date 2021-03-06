package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.service.util.CSRUtil;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CSRView implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @CsvBindByName
    private String x509KeySpec;

    @CsvBindByName
    private String keyLength;

    @CsvBindByName
    private String signingAlgorithm;

    @CsvBindByName
    private String publicKeyAlgorithm;

    @CsvBindByName
    private Instant requestedOn;

    @CsvIgnore
    private AuditView[] auditViewArr;

    public CSRView() {}

    public CSRView(final CSRUtil csrUtil, final CSR csr) {

    	this.id = csr.getId();
    	this.certificateId = csr.getCertificate() != null? csr.getCertificate().getId(): null;
    	this.subject = csr.getSubject();
    	this.sans = csr.getSans();
    	this.status = csr.getStatus();
    	this.publicKeyAlgorithm = csr.getPublicKeyAlgorithm();
    	this.signingAlgorithm = csr.getSigningAlgorithm();
    	this.x509KeySpec = csr.getx509KeySpec();

    	Set<CsrAttribute> attributes = csr.getCsrAttributes();

    	this.requestedBy = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_REQUESTED_BY);
    	this.processingCA = csrUtil.getCSRAttribute(csr, CsrAttribute.ATTRIBUTE_PROCESSING_CA);

    	this.pipelineName = csr.getPipeline() != null ? csr.getPipeline().getName(): null;

//   		this.keyLength = cert.getKeyLength().toString();

    	this.requestedOn = csr.getRequestedOn();
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

    public AuditView[] getAuditViewArr() {
        return auditViewArr;
    }

    public void setAuditViewArr(AuditView[] auditViewArr) {
        this.auditViewArr = auditViewArr;
    }
}
