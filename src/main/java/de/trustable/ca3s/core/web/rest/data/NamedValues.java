package de.trustable.ca3s.core.web.rest.data;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedValues {

	public NamedValues() {
	}
	
	public NamedValues( final String name, final String[] values) {
		this.name = name;
		this.values = values;
	}
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("values")
	private String[] values;

}
