package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadPrecheckResponseData {

	@JsonProperty("type")
	private String type;
	
	@JsonProperty("checkResult")
	private String checkResult;
	
}
