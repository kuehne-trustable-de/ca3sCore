package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedTypedValue {

	public NamedTypedValue() {
	}

    public NamedTypedValue(final String name, final String type, final String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @JsonProperty("name")
	private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("value")
    private String value;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
