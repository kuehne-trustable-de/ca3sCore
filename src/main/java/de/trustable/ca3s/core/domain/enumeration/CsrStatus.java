package de.trustable.ca3s.core.domain.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The CsrStatus enumeration.
 */
public enum CsrStatus {
    Processing, Issued, Rejected, Pending;
	
	private static final Logger LOG = LoggerFactory.getLogger(CsrStatus.class);

	@JsonCreator
	public static CsrStatus forValues(String value) {

		for (CsrStatus stat : CsrStatus.values()) {
			if (stat.toString().equalsIgnoreCase(value)) {
				return stat;
			}
		}
		LOG.warn("trying to build CsrStatus from an unexpected value '{}'", value);
		return null;
	}

}
