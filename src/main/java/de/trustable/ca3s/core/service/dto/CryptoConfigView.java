package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class CryptoConfigView implements Serializable {

    private final String[] validPBEAlgoArr;
    private final String defaultPBEAlgo;

    public CryptoConfigView(String[] validPBEAlgoArr, String defaultPBEAlgo) {
        this.validPBEAlgoArr = validPBEAlgoArr;
        this.defaultPBEAlgo = defaultPBEAlgo;
    }

    public String[] getValidPBEAlgoArr() {
        return validPBEAlgoArr;
    }

    public String getDefaultPBEAlgo() {
        return defaultPBEAlgo;
    }
}
