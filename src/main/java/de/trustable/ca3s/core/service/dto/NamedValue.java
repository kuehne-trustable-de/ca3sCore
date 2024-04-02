package de.trustable.ca3s.core.service.dto;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedValue {

    public NamedValue(){}

    public NamedValue(final String in){
        String[] parts = in.split("=");
        if( parts.length > 0){
            name= parts[0];
        }
        if( parts.length > 1){
            value=parts[1];
        }
    }

	@JsonProperty("name")
	private String name;

	@JsonProperty("value")
	private String value;

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}


    @Override
    public String toString() {
        return "NamedValue{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
