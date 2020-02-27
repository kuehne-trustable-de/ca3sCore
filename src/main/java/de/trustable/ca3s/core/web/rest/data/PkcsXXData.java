package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class PkcsXXData {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PkcsXXData.class);

	@JsonProperty("dataType")
	private PKCSDataType dataType;
	
	@JsonProperty("p10Holder")
	private Pkcs10RequestHolderShallow p10Holder;

	@JsonProperty("certificates")
	private X509CertificateHolderShallow[] certsHolder;
	
	@JsonProperty("certificatePresentInDB")
	private boolean certificatePresentInDB = false;
	
	@JsonProperty("publicKeyPresentInDB")
	private boolean publicKeyPresentInDB = false;
	
	@JsonProperty("certificateId")
	private long certificateId;
	
	public PkcsXXData() {
	}

	public PkcsXXData(final X509CertificateHolder certHolder, boolean isCertificatePresentInDB) {

		setDataType(PKCSDataType.X509_CERTIFICATE);
		
		X509CertificateHolderShallow[] x509HolderArr = new X509CertificateHolderShallow[1];
		x509HolderArr[0] =	new X509CertificateHolderShallow(certHolder);
		setCertsHolder( x509HolderArr);
		
		this.setCertificatePresentInDB( isCertificatePresentInDB);
	}
	

	public PkcsXXData(final Pkcs10RequestHolderShallow p10Holder) {

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

	public X509CertificateHolderShallow[] getCertsHolder() {
		return certsHolder;
	}

	public void setCertsHolder(X509CertificateHolderShallow[] certsHolder) {
		this.certsHolder = certsHolder;
	}

	public Pkcs10RequestHolderShallow getP10Holder() {
		return p10Holder;
	}

	public void setP10Holder(Pkcs10RequestHolderShallow p10Holder) {
		this.p10Holder = p10Holder;
	}

	public long getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(long certificateId) {
		this.certificateId = certificateId;
	}

	
}
