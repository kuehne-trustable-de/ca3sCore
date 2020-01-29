package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.util.Pkcs10RequestHolder;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class PkcsXXData {

	private static final Logger LOGGER = LoggerFactory.getLogger(PkcsXXData.class);

	@JsonProperty("dataType")
	private PKCSDataType dataType;
	
	@JsonProperty("p10Holder")
	private Pkcs10RequestHolder p10Holder;

	@JsonProperty("certificate")
	private X509CertificateHolderShallow certHolder;
	
	@JsonProperty("certificatePresentInDB")
	private boolean certificatePresentInDB = false;
	
	@JsonProperty("publicKeyPresentInDB")
	private boolean publicKeyPresentInDB = false;
	
	public PkcsXXData() {
	}

	public PkcsXXData(final X509CertificateHolder certHolder, Certificate cert) {

		setDataType(PKCSDataType.X509_CERTIFICATE);
		
		
		setCertHolder( new X509CertificateHolderShallow(certHolder));
		
		this.setCertificatePresentInDB( cert!= null);
	}
	
	public PkcsXXData(final Pkcs10RequestHolder p10Holder) {

		setDataType(PKCSDataType.CSR);

		setP10Holder(p10Holder);

	}

	
	public PKCSDataType getDataType() {
		return dataType;
	}

	public void setDataType(PKCSDataType dataType) {
		this.dataType = dataType;
	}

	public boolean isCertificatePresentInDB() {
		return certificatePresentInDB;
	}

	public void setCertificatePresentInDB(boolean presentInDB) {
		this.certificatePresentInDB = presentInDB;
	}

	
	public boolean isPublicKeyPresentInDB() {
		return publicKeyPresentInDB;
	}

	public void setPublicKeyPresentInDB(boolean publicKeyPresentInDB) {
		this.publicKeyPresentInDB = publicKeyPresentInDB;
	}

	public X509CertificateHolderShallow getCertHolder() {
		return certHolder;
	}

	public void setCertHolder(X509CertificateHolderShallow certHolder) {
		this.certHolder = certHolder;
	}

	public Pkcs10RequestHolder getP10Holder() {
		return p10Holder;
	}

	public void setP10Holder(Pkcs10RequestHolder p10Holder) {
		this.p10Holder = p10Holder;
	}

	
}
