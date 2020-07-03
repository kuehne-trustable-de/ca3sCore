package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CertificateFilterList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5633124350070620605L;

	
	@JsonProperty("filterList")
	private CertificateFilter[] filterList;

	@JsonProperty("orderList")
	private CertificateOrder orderList;
}
