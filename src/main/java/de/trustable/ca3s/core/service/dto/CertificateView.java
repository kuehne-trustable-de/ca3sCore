package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.time.Instant;

import de.trustable.ca3s.core.domain.Certificate;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CertificateView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String tbsDigest;

    private String subject;

    private String issuer;

    private String type;

    private String description;

    private String serial;

    private Instant validFrom;

    private Instant validTo;

    private Instant contentAddedAt;

    private Instant revokedSince;

    private String revocationReason;

    private Boolean revoked;

    public CertificateView() {}
    
    public CertificateView(final Certificate cert) {
    	this.id = cert.getId();
    	this.tbsDigest = cert.getTbsDigest();
    	this.subject = cert.getSubject();
    	this.issuer = cert.getIssuer();
    	this.type = cert.getType();
    	this.description = cert.getDescription();
    	this.serial = cert.getSerial();
    	this.validFrom = cert.getValidFrom();
    	this.validTo = cert.getValidTo();
    	this.contentAddedAt = cert.getContentAddedAt();
    	this.revokedSince = cert.getRevokedSince();
    	this.revocationReason = cert.getRevocationReason();
    	this.revoked = cert.isRevoked();
    	
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

    
}
