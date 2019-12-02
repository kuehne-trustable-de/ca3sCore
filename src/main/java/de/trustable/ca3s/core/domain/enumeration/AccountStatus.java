package de.trustable.ca3s.core.domain.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The AccountStatus enumeration.
 */
public enum AccountStatus {

	Valid, Deactivated, Revoked;

	private static final Logger LOG = LoggerFactory.getLogger(AccountStatus.class);

	@JsonCreator
	public static AccountStatus forValues(String value) {

		for (AccountStatus stat : AccountStatus.values()) {
			if (stat.toString().equalsIgnoreCase(value)) {
				return stat;
			}
		}
		LOG.warn("trying to build AccountStatus from an unexpected value '{}'", value);
		return null;
	}

}
