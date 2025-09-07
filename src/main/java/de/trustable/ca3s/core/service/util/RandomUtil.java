package de.trustable.ca3s.core.service.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating random Strings.
 */
@Component
public class RandomUtil {

    private static final int DEFAULT_LENGTH = 20;
    private static final int TOKEN_LENGTH = 32;
    private static final int MAC_KEY_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    public  SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }

    private  String generateRandomAlphanumericString() {
        return RandomStringUtils.random(DEFAULT_LENGTH, 0, 0, true, true, null, SECURE_RANDOM);
    }

    private  String generateRandomAlphanumericString(final int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, SECURE_RANDOM);
    }

    /**
     * Generate a password.
     *
     * @return the generated password.
     */
    public String generatePassword() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key.
     */
    public String generateActivationKey() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key.
     */
    public String generateResetKey() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate a api token.
     *
     * @return the generated reset key.
     */
    public String generateApiToken() {
        return generateRandomAlphanumericString(TOKEN_LENGTH);
    }

    /**
     * Generate mac key with 256 bits.
     *
     * @return the generated reset key.
     */
    public String generateMacKey() {
        byte[] randBytes = new byte[MAC_KEY_LENGTH];
        getSecureRandom().nextBytes(randBytes);
        return Base64.getUrlEncoder().encodeToString(randBytes);
    }
}
