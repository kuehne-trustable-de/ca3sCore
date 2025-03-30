package de.trustable.ca3s.core.service.dto;

public class ARARestriction {

	String name;
    String contentTemplate;
    String regEx;
    String comment;
    ARAContentType contentType;
	boolean regExMatch = false;
	boolean required = false;

	public ARARestriction() {

	}


	public String getContentTemplate() {
		return contentTemplate;
	}

	public boolean isRegExMatch() {
		return regExMatch;
	}

	public void setContentTemplate(String contentTemplate) {
		this.contentTemplate = contentTemplate;
	}

	public void setRegExMatch(boolean regExMatch) {
		this.regExMatch = regExMatch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public boolean isRequired() {
		return required;
	}


	public void setRequired(boolean required) {
		this.required = required;
	}

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ARAContentType getContentType() {
        return contentType;
    }

    public void setContentType(ARAContentType contentType) {
        this.contentType = contentType;
    }
}
