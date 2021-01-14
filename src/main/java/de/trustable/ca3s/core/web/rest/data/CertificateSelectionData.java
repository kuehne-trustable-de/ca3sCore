package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.service.dto.Selector;

public class CertificateSelectionData implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 53312595607061667L;


    @JsonProperty("itemName")
    private String itemName;

    @JsonProperty("itemType")
	private String itemType;

	@JsonProperty("itemDefaultSelector")
	private Selector itemDefaultSelector;

	@JsonProperty("itemDefaultValue")
	private String itemDefaultValue;

	@JsonProperty("values")
	private String[] values;


}
