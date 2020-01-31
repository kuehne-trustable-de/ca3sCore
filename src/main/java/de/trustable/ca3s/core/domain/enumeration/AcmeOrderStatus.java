package de.trustable.ca3s.core.domain.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The AcmeOrderStatus enumeration.
 */
public enum AcmeOrderStatus {
    PENDING, READY, PROCESSING, VALID, INVALID;
    
	private static final Logger LOG = LoggerFactory.getLogger(AcmeOrderStatus.class);

	@JsonCreator
	public static AcmeOrderStatus forValues(String value) {

		for (AcmeOrderStatus stat : AcmeOrderStatus.values()) {
			if (stat.toString().equalsIgnoreCase(value)) {
				return stat;
			}
		}
		LOG.warn("trying to build AcmeOrderStatus from an unexpected value '{}'", value);
		return null;
	}
	
	@JsonValue
    public String getValue() {
        return this.toString().toLowerCase();
    }


}
