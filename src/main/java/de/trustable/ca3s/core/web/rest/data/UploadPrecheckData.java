package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.dto.NamedValue;
import de.trustable.ca3s.core.service.dto.NamedValues;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadPrecheckData {

    @JsonProperty("passphrase")
    private String passphrase;

    @JsonProperty("importKey")
    private boolean importKey;

    @JsonProperty("secret")
	private String secret;

	@JsonProperty("requestorcomment")
	private String requestorcomment;

	@JsonProperty("pipelineId")
	private Long pipelineId;

	@JsonProperty("content")
	private String content;

	@JsonProperty("creationMode")
	private CreationMode creationMode = CreationMode.CSR_AVAILABLE;

	@JsonProperty("keyAlgoLength")
	private String keyAlgoLength = KeyAlgoLengthOrSpec.RSA_2048.toString();

	@JsonProperty("containerType")
	private ContainerType containerType = ContainerType.PKCS_12;

	@JsonProperty("namedValues")
	private NamedValue[] namedValues;

	@JsonProperty("certificateAttributes")
	private NamedValues[] certificateAttributes;

    @JsonProperty("arAttributes")
    private NamedValues[] arAttributes;

    @JsonProperty("tosAgreed")
    private boolean tosAgreed;

    public String getPassphrase() {
		return passphrase;
	}

	public String getContent() {
		return content;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public void setContent(String content) {
		this.content = content;
	}

    public boolean isImportKey() {
        return importKey;
    }

    public void setImportKey(boolean importKey) {
        this.importKey = importKey;
    }

    public Long getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(Long pipelineId) {
		this.pipelineId = pipelineId;
	}

	public String getRequestorcomment() {
		return requestorcomment;
	}

	public void setRequestorcomment(String requestorcomment) {
		this.requestorcomment = requestorcomment;
	}

	public CreationMode getCreationMode() {
		return creationMode;
	}

	public String getKeyAlgoLength() {
		return keyAlgoLength;
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public NamedValue[] getNamedValues() {
		return namedValues;
	}

	public NamedValues[] getCertificateAttributes() {
		return certificateAttributes;
	}

	public NamedValues[] getArAttributes() {
		return arAttributes;
	}

    public boolean isTosAgreed() {
        return tosAgreed;
    }

    public void setTosAgreed(boolean tosAgreed) {
        this.tosAgreed = tosAgreed;
    }

    public void setCreationMode(CreationMode creationMode) {
		this.creationMode = creationMode;
	}

	public void setKeyAlgoLength(String keyAlgoLength) {
		this.keyAlgoLength = keyAlgoLength;
	}

	public void setContainerType(ContainerType containerType) {
		this.containerType = containerType;
	}

	public void setNamedValues(NamedValue[] namedValues) {
		this.namedValues = namedValues;
	}

	public void setCertificateAttributes(NamedValues[] certificateAttributes) {
		this.certificateAttributes = certificateAttributes;
	}

	public void setArAttributes(NamedValues[] arAttributes) {
		this.arAttributes = arAttributes;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}


}
