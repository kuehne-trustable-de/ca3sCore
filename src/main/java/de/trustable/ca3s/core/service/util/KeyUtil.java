package de.trustable.ca3s.core.service.util;


import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Service
public class KeyUtil {

    final private String defaultKeySpec;
    private final RandomUtil randomUtil;


    private static final Logger LOG = LoggerFactory.getLogger(KeyUtil.class);

    public KeyUtil(@Value("${ca3s.keyspec.default:RSA_4096}") String defaultKeySpec, RandomUtil randomUtil) {
        // @ToDo check back with the list of valid algos
        this.defaultKeySpec = defaultKeySpec;
        this.randomUtil = randomUtil;
    }

    public KeyPair createKeyPair(){
        try {
            return createKeyPair(defaultKeySpec);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("unexpected vale for 'ca3s.keyspec.default', using RSA-4096 as fallback.");
            try {
                return createKeyPair("RSA_4096");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("Problem using default algorithm! Maybe a general crypto setup problem?", ex);
            }

        }
    }
    public KeyPair createKeyPair(final String keySpec) throws NoSuchAlgorithmException {
        KeyAlgoLengthOrSpec kal = KeyAlgoLengthOrSpec.from(keySpec);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(kal.getKeyFactoryAlgo());
        keyPairGenerator.initialize(kal.getKeyLength(), randomUtil.getSecureRandom());
        return keyPairGenerator.generateKeyPair();
    }
}
