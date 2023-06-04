package de.trustable.ca3s.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PreferenceDefaults {

    private final String[] availableHashes; //  = {"sha-256", "sha-512"};
    private final String[] availableSigningAlgos; //  = {"rsa-2048","rsa-3072","rsa-4096", "rsa-8192"};

    public PreferenceDefaults( @Value("${ca3s.algorithm.hash:sha-256,sha-512}") String[] availableHashes,
                               @Value("${ca3s.algorithm.signingAlgo:" +
                                   "rsa-2048,rsa-3072,rsa-4096,rsa-6144,rsa-8192," +
                                   "ecdsa-224, ecdsa-256, ecdsa-384, ecdsa-512," +
                                   "ed25519-255}") String[] availableSigningAlgos) {
        this.availableHashes = availableHashes;
        this.availableSigningAlgos = availableSigningAlgos;
    }

    public String[] getAvailableHashes() {
        return availableHashes;
    }

    public String[] getAvailableSigningAlgos() {
        return availableSigningAlgos;
    }
}
