package de.trustable.ca3s.core.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CsrStatus enumeration.
 */
public enum KeyUniqueness {
    KEY_UNIQUE, DOMAIN_REUSE, KEY_REUSE, KEY_UNIQUE_WARN_ONLY, DOMAIN_REUSE_WARN_ONLY;

    private static final Logger LOG = LoggerFactory.getLogger(KeyUniqueness.class);

	@JsonCreator
	public static KeyUniqueness forValues(String value) {

		for (KeyUniqueness stat : KeyUniqueness.values()) {
			if (stat.toString().equalsIgnoreCase(value)) {
				return stat;
			}
		}
		LOG.warn("trying to build KeyUniqueness from an unexpected value '{}'", value);
		return null;
	}

}
