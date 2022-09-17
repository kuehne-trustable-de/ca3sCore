package de.trustable.ca3s.core.service.dto;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedValues {

	public NamedValues() {
	}

    public NamedValues( final String name, final TypedValue[] values) {
        this.name = name;
        this.values = values;
    }

    public NamedValues( final String name, final String[] stringValues) {
        this.name = name;
        this.values = new TypedValue[stringValues.length];
        for( int i = 0; i < stringValues.length; i++){
            this.values[i] = new TypedValue(stringValues[i]);
        }
    }

    @JsonProperty("name")
	private String name;

	@JsonProperty("values")
	private TypedValue[] values;

	public String getName() {
		return name;
	}

	public TypedValue[] getValues() {
		return values;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValues(TypedValue[] values) {
		this.values = values;
	}


}
