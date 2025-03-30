package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class TypedValue {

	@JsonProperty("type")
	private String type;

    @JsonProperty("value")
    private String value;

    public TypedValue(){}

    public TypedValue(String value){
        this.type = "";
        this.value = value;
    }
    public TypedValue(String type, String value){
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
