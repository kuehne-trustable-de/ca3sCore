package de.trustable.ca3s.core.service.dto;

public enum Selector {

	EQUALS, LIKE, NOTLIKE, LESSTHAN, GREATERTHAN, ON, BEFORE, AFTER, ISTRUE, ISFALSE;

	public static boolean requiresValue(String attributeSelector) {
		
		if( ISTRUE.toString().equalsIgnoreCase(attributeSelector)) {
			return false;
		}

		if( ISFALSE.toString().equalsIgnoreCase(attributeSelector)) {
			return false;
		}

		return true;
	} 

}
