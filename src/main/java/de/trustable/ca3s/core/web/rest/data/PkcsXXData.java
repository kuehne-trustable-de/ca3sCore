package de.trustable.ca3s.core.web.rest.data;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.Certificate;

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

	private boolean csrPublicKeyPresentInDB = false;

	@JsonProperty("createdCertificateId")
	private String createdCertificateId;

	@JsonProperty("passphraseRequired")
	private boolean passphraseRequired = false;

	@JsonProperty("csrPending")
	private boolean csrPending = false;

	@JsonProperty("createdCSRId")
	private String createdCSRId;

    @JsonProperty("messages")
    private String[] messages;

    @JsonProperty("warnings")
    private String[] warnings;

    @JsonProperty("replacementCandidates")
	CertificateNameId[] replacementCandidates;


	public PkcsXXData() {
	}

	public PkcsXXData(final X509CertificateHolder certHolder, Certificate cert) {

		setDataType(PKCSDataType.X509_CERTIFICATE_CREATED);
		X509CertificateHolderShallow[] x509HolderArr = new X509CertificateHolderShallow[1];
		x509HolderArr[0] = new X509CertificateHolderShallow(certHolder);
		x509HolderArr[0].setCertificateId(cert.getId());
		x509HolderArr[0].setCertificatePresentInDB(true);
		setCertsHolder( x509HolderArr);
	}

	public PkcsXXData(final X509CertificateHolder certHolder, boolean isCertificatePresentInDB) {

		this(certHolder, null, isCertificatePresentInDB);
	}

	public PkcsXXData(final X509CertificateHolder certHolder, String pemCertificate, boolean isCertificatePresentInDB) {

		setDataType(PKCSDataType.X509_CERTIFICATE);

		X509CertificateHolderShallow[] x509HolderArr = new X509CertificateHolderShallow[1];
		x509HolderArr[0] =	new X509CertificateHolderShallow(certHolder);
		x509HolderArr[0].setPemCertificate(pemCertificate);
		x509HolderArr[0].setCertificatePresentInDB( isCertificatePresentInDB);

		setCertsHolder( x509HolderArr);
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

	public boolean isPassphraseRequired() {
		return passphraseRequired;
	}

	public void setPassphraseRequired(boolean passphraseRequired) {
		this.passphraseRequired = passphraseRequired;
	}

	public boolean isCsrPublicKeyPresentInDB() {
		return csrPublicKeyPresentInDB;
	}

	public void setCsrPublicKeyPresentInDB(boolean csrPublicKeyPresentInDB) {
		this.csrPublicKeyPresentInDB = csrPublicKeyPresentInDB;
	}

	public String getCreatedCertificateId() {
		return createdCertificateId;
	}

	public void setCreatedCertificateId(String createdCertificateId) {
		this.createdCertificateId = createdCertificateId;
	}

	public boolean isCsrPending() {
		return csrPending;
	}

	public void setCsrPending(boolean csrPending) {
		this.csrPending = csrPending;
	}

	public String getCreatedCSRId() {
		return createdCSRId;
	}

	public void setCreatedCSRId(String createdCSRId) {
		this.createdCSRId = createdCSRId;
	}

	public String[] getWarnings() {
		return warnings;
	}

	public void setWarnings(String[] warnings) {
		this.warnings = warnings;
	}

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public CertificateNameId[] getReplacementCandidates() {
		return replacementCandidates;
	}

	public void setReplacementCandidates(CertificateNameId[] replacementCandidates) {
		this.replacementCandidates = replacementCandidates;
	}

	public void setReplacementCandidates(List<Certificate> candidates) {

		this.replacementCandidates = new CertificateNameId[candidates.size()];
		int i = 0;
		for( Certificate cert: candidates) {
			this.replacementCandidates[i++] = new CertificateNameId(cert);
		}
	}

}
