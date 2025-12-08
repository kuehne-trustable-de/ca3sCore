package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;

public class RDNRestriction {

	String rdnName;
	RDNCardinalityRestriction cardinalityRestriction;
    String contentTemplate;
    boolean templateReadOnly = false;
    String regEx;
	boolean regExMatch = false;


	public RDNRestriction() {

	}

	public String getRdnName() {
		return rdnName;
	}

	public void setRdnName(String rdnName) {
		this.rdnName = rdnName;
	}

	public RDNCardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}

	public String getContentTemplate() {
		return contentTemplate;
	}

    public boolean isTemplateReadOnly() {
        return templateReadOnly;
    }

    public void setTemplateReadOnly(boolean templateReadOnly) {
        this.templateReadOnly = templateReadOnly;
    }

    public boolean isRegExMatch() {
		return regExMatch;
	}

	public void setCardinalityRestriction(RDNCardinalityRestriction cardinalityRestriction) {
		this.cardinalityRestriction = cardinalityRestriction;
	}

	public void setContentTemplate(String contentTemplate) {
		this.contentTemplate = contentTemplate;
	}

	public void setRegExMatch(boolean regExMatch) {
		this.regExMatch = regExMatch;
	}

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }
}
