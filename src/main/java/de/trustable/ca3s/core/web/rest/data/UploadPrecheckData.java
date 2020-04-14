package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadPrecheckData {

	@JsonProperty("passphrase")
	private String passphrase;
	
	@JsonProperty("requestorcomment")
	private String requestorcomment;
	
	@JsonProperty("pipelineId")
	private Long pipelineId;

	@JsonProperty("content")
	private String content;

	@JsonProperty("namedValues")
	private NamedValue[] namedValues;

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
	
}
