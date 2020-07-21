package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CertificateOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 568332405006060L;
	
	@JsonProperty("orderBy")
	private String orderBy;

	@JsonProperty("orderDir")
	private String orderDir;


}
