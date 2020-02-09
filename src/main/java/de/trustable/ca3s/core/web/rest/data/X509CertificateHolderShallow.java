package de.trustable.ca3s.core.web.rest.data;

import java.io.IOException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;

public class X509CertificateHolderShallow {
	
	private static final Logger LOG = LoggerFactory.getLogger(X509CertificateHolderShallow.class);

    private String subject;

    private String issuer;

    private String type;

    private String fingerprint;

    private String serial;

    private String validFrom;

    private String validTo;
    
	@JsonProperty("sans")
	private String[] sans;

    private String[] extensions;

    private boolean keyPresent;

    
    public X509CertificateHolderShallow(X509CertificateHolder holder) {
    	this.keyPresent = false;
    	
    	this.subject = holder.getSubject().toString();
    	this.issuer = holder.getIssuer().toString();
    	this.type = "V" + holder.getVersionNumber();
    	
    	try {
			this.fingerprint = Base64.toBase64String(CryptoUtil.generateSHA1Fingerprint(holder.getEncoded()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	this.serial = holder.getSerialNumber().toString();
    	this.validFrom = holder.getNotBefore().toString();
    	this.validTo = holder.getNotAfter().toString();
    	
    	// holder.getExtensions() does not return an empty list but 'null'
    	int nExtensions = 0;
		Extensions exts = holder.getExtensions();
    	if( exts != null && exts.getExtensionOIDs() != null) {
    		nExtensions = exts.getExtensionOIDs().length;
    	}
    	extensions = new String[nExtensions];
    	
		this.sans = new String[0];
    	if( nExtensions > 0) {
			int i = 0;
			for( ASN1ObjectIdentifier objId : exts.getExtensionOIDs()) {
	    		extensions[i++] = OidNameMapper.lookupOid(objId.getId());
	    	}
			
			GeneralNames namesSAN = GeneralNames.fromExtensions(exts, Extension.subjectAlternativeName);
			
			int j = 0;
			this.sans = new String[namesSAN.getNames().length];
			for( GeneralName gn : namesSAN.getNames()) {
				this.sans[j++] = CSRUtil.getGeneralNameDescription(gn);
			}
    	}
    }

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}

	public String getValidTo() {
		return validTo;
	}

	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}

	
	public String[] getSans() {
		return sans;
	}

	public void setSans(String[] sans) {
		this.sans = sans;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	public boolean isKeyPresent() {
		return keyPresent;
	}

	public void setKeyPresent(boolean keyPresent) {
		this.keyPresent = keyPresent;
	}

    
    
}
