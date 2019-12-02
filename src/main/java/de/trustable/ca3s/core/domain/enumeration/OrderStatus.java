package de.trustable.ca3s.core.domain.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The OrderStatus enumeration.
 */
public enum OrderStatus {
    Pending, Ready, Processing, Valid, Invalid;
	
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
}
