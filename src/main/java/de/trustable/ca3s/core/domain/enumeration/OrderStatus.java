package de.trustable.ca3s.core.domain.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The OrderStatus enumeration.
 */
public enum OrderStatus {
    PENDING, READY, PROCESSING, VALID, INVALID;
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderStatus.class);

	@JsonCreator
	public static OrderStatus forValues(String value) {

		for (OrderStatus stat : OrderStatus.values()) {
			if (stat.toString().equalsIgnoreCase(value)) {
				return stat;
			}
		}
		LOG.warn("trying to build OrderStatus from an unexpected value '{}'", value);
		return null;
	}
	
	@JsonValue
    public String getValue() {
        return this.toString().toLowerCase();
    }

}
