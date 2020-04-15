package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.time.Instant;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CertificateView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String tbsDigest;

    private String subject;

    private String sans;

    private String issuer;

    private String type;

    private String keyLength;

    private String signingAlgorithm;
    
    private String paddingAlgorithm;
    
    private String hashAlgorithm;
    
    private String description;

    private String serial;

    private Instant validFrom;

    private Instant validTo;

    private Instant contentAddedAt;

    private Instant revokedSince;

    private String revocationReason;

    private Boolean revoked;

    public CertificateView() {}
    
    public CertificateView(final CertificateUtil certUtil, final Certificate cert) {
    	this.id = cert.getId();
    	this.tbsDigest = cert.getTbsDigest();
    	this.subject = cert.getSubject();
    	this.sans = cert.getSans();
    	this.issuer = cert.getIssuer();
    	this.type = cert.getType();
   		this.keyLength = cert.getKeyLength().toString();
		this.signingAlgorithm = cert.getSigningAlgorithm();
		this.paddingAlgorithm = cert.getPaddingAlgorithm();
		this.hashAlgorithm = cert.getHashingAlgorithm();
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

    
}
