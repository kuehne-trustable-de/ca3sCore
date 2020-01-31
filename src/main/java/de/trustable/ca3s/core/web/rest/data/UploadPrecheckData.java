package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadPrecheckData {

	@JsonProperty("user")
	private String user;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("csr")
	private String csr;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCsr() {
		return csr;
	}

	public void setCsr(String csr) {
		this.csr = csr;
	}
	
	
}
