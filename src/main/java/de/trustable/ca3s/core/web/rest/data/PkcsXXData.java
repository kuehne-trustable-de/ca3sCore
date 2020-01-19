package de.trustable.ca3s.core.web.rest.data;

import java.util.ArrayList;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;

public class Pkcs10RequestData {

	private static final Logger LOGGER = LoggerFactory.getLogger(Pkcs10RequestData.class);

	private String signingAlgorithm;

	private String signingAlgorithmName;

	private boolean isCSRValid;

	private String x509KeySpec;

	private String subject;

	private String[] subjectRDNs;

	private String[] reqAttributes;

	private String publicKeyBase64;

	private String publicKeyAlgorithm;

	private String publicKeyAlgorithmName;

	private String publicKeyHash;

	private String subjectPublicKeyInfoBase64;

	public Pkcs10RequestData() {
	}

	public Pkcs10RequestData(final Pkcs10RequestHolder p10Holder) {

		setSubject(p10Holder.getSubject());

		p10Holder.getPublicKeyAlgorithmName();

		setSigningAlgorithm(p10Holder.getSigningAlgorithm());
		setSigningAlgorithmName(p10Holder.getSigningAlgorithmName());

		setCSRValid(p10Holder.isCSRValid());

		setX509KeySpec(p10Holder.getX509KeySpec());

		setSubjectRDNs(new String[p10Holder.getSubjectRDNs().length]);
		for (int i = 0; i < p10Holder.getSubjectRDNs().length; i++) {
			RDN rdn = p10Holder.getSubjectRDNs()[i];
			AttributeTypeAndValue atv = rdn.getFirst();
			getSubjectRDNs()[i] = OidNameMapper.lookupOid(atv.getType().toString()) + " : " + atv.getValue().toString();
		}

		ArrayList<String> reqAttributeList = new ArrayList<String>();

		for (int i = 0; i < p10Holder.getReqAttributes().length; i++) {
			Attribute att = p10Holder.getReqAttributes()[i];
			String attrType = att.getAttrType().toString().trim();

			String reqAttr = OidNameMapper.lookupOid(attrType);
			for (ASN1Encodable val : att.getAttributeValues()) {
				if ("1.2.840.113549.1.9.7".equals(attrType)) {
					reqAttr += " '******'";
					reqAttributeList.add(reqAttr);
				} else if (PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.toString().equals(attrType)) {
					DERSequence derSeq = (DERSequence) val;
					for (int j = 0; j < derSeq.size(); j++) {
						ASN1Encodable asn1Enc = derSeq.getObjectAt(j);
						LOGGER.debug("pkcs_9_at_extensionRequest #" + j + ": " + asn1Enc.getClass().getName());
						if (asn1Enc instanceof DERSequence) {
							DERSequence derSeq2 = (DERSequence) asn1Enc;
							for (int k = 0; k < derSeq2.size(); k++) {
								ASN1Encodable asn1Enc2 = derSeq2.getObjectAt(k);
								LOGGER.debug("pkcs_9_at_extensionRequest #" + k + ": " + asn1Enc2.getClass().getName());
							}

							if (derSeq2.size() == 2) {
								reqAttributeList.add(OidNameMapper.lookupOid(derSeq2.getObjectAt(0).toString()) + " : "
										+ derSeq2.getObjectAt(1).toString());
							}
						}
					}
				} else {
					reqAttr += " " + val.toString();
					reqAttributeList.add(reqAttr);
				}
			}
		}

		setReqAttributes(reqAttributeList.toArray(new String[reqAttributeList.size()]));

		setPublicKeyBase64(Base64.getEncoder().encodeToString(p10Holder.getPublicSigningKey().getEncoded()));

		setPublicKeyAlgorithm(p10Holder.getPublicKeyAlgorithm());
		setPublicKeyAlgorithmName(p10Holder.getPublicKeyAlgorithmName());

		setPublicKeyHash(p10Holder.getPublicKeyHash());

		setSubjectPublicKeyInfoBase64(p10Holder.getSubjectPublicKeyInfoBase64());

	}

	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}

	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	public boolean isCSRValid() {
		return isCSRValid;
	}

	public void setCSRValid(boolean isCSRValid) {
		this.isCSRValid = isCSRValid;
	}

	public String getX509KeySpec() {
		return x509KeySpec;
	}

	public void setX509KeySpec(String x509KeySpec) {
		this.x509KeySpec = x509KeySpec;
	}

	public String[] getSubjectRDNs() {
		return subjectRDNs;
	}

	public void setSubjectRDNs(String[] subjectRDNs) {
		this.subjectRDNs = subjectRDNs;
	}

	public String[] getReqAttributes() {
		return reqAttributes;
	}

	public void setReqAttributes(String[] reqAttributes) {
		this.reqAttributes = reqAttributes;
	}

	public String getPublicKeyBase64() {
		return publicKeyBase64;
	}

	public void setPublicKeyBase64(String publicKeyBase64) {
		this.publicKeyBase64 = publicKeyBase64;
	}

	public String getPublicKeyAlgorithm() {
		return publicKeyAlgorithm;
	}

	public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
		this.publicKeyAlgorithm = publicKeyAlgorithm;
	}

	public String getPublicKeyHash() {
		return publicKeyHash;
	}

	public void setPublicKeyHash(String publicKeyHash) {
		this.publicKeyHash = publicKeyHash;
	}

	public String getSubjectPublicKeyInfoBase64() {
		return subjectPublicKeyInfoBase64;
	}

	public void setSubjectPublicKeyInfoBase64(String subjectPublicKeyInfoBase64) {
		this.subjectPublicKeyInfoBase64 = subjectPublicKeyInfoBase64;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPublicKeyAlgorithmName() {
		return publicKeyAlgorithmName;
	}

	public void setPublicKeyAlgorithmName(String publicKeyAlgorithmName) {
		this.publicKeyAlgorithmName = publicKeyAlgorithmName;
	}

	public String getSigningAlgorithmName() {
		return signingAlgorithmName;
	}

	public void setSigningAlgorithmName(String signingAlgorithmName) {
		this.signingAlgorithmName = signingAlgorithmName;
	}

}
