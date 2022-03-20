package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.service.dto.Selector;

public class CertificateFilter implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5331243950070606605L;


	@JsonProperty("attributeName")
	private String attributeName;

    @JsonProperty("attributeValue")
    private String attributeValue;

    @JsonProperty("attributeValueArr")
    private String[] attributeValueArr;

    @JsonProperty("selector")
	private Selector selector;

}
