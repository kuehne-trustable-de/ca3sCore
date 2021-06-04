package de.trustable.ca3s.core.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CsrStatus enumeration.
 */
public enum CsrUsage {
    TLS_SERVER, TLS_CLIENT, DOC_SIGNING, CODE_SIGNING;

    private static final Logger LOG = LoggerFactory.getLogger(CsrUsage.class);

	@JsonCreator
	public static CsrUsage forValues(String value) {

		for (CsrUsage usages : CsrUsage.values()) {
			if (usages.toString().equalsIgnoreCase(value)) {
				return usages;
			}
		}
		LOG.warn("trying to build CsrStatus from an unexpected value '{}'", value);
		return null;
	}

}
