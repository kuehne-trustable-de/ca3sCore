package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class CryptoConfigView implements Serializable {

    private final String[] validPBEAlgoArr;
    private final String defaultPBEAlgo;
    private final String[] allHashAlgoArr;
    private final String[] allSignAlgoArr;

    public CryptoConfigView(String[] validPBEAlgoArr, String defaultPBEAlgo, String[] allHashAlgoArr, String[] allSignAlgoArr) {
        this.validPBEAlgoArr = validPBEAlgoArr;
        this.defaultPBEAlgo = defaultPBEAlgo;
        this.allHashAlgoArr = allHashAlgoArr;
        this.allSignAlgoArr = allSignAlgoArr;
    }

    public String[] getAllHashAlgoArr() {
        return allHashAlgoArr;
    }
    public String[] getAllSignAlgoArr() {
        return allSignAlgoArr;
    }
    public String[] getValidPBEAlgoArr() {
        return validPBEAlgoArr;
    }
    public String getDefaultPBEAlgo() {
        return defaultPBEAlgo;
    }
}
