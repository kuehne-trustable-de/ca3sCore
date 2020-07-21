package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.ARACardinalityRestriction;

public class ARARestriction {

	String name;
	ARACardinalityRestriction cardinalityRestriction;
	String contentTemplate;
	boolean regExMatch = false;
	
	public ARARestriction() {
		
	}

	public ARACardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}

	public String getContentTemplate() {
		return contentTemplate;
	}

	public boolean isRegExMatch() {
		return regExMatch;
	}

	public void setCardinalityRestriction(ARACardinalityRestriction cardinalityRestriction) {
		this.cardinalityRestriction = cardinalityRestriction;
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
	
	
	
}
