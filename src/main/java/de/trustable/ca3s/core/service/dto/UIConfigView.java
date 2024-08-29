package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class UIConfigView implements Serializable {

    private final CryptoConfigView cryptoConfigView;
    private final boolean autoSSOLogin;
    private final String[] ssoProvider;

    private final String[] scndFactorTypes;


    public UIConfigView(CryptoConfigView cryptoConfigView, boolean autoSSOLogin, String[] ssoProvider, String[] scndFactorTypes) {
        this.cryptoConfigView = cryptoConfigView;
        this.autoSSOLogin = autoSSOLogin;
        this.ssoProvider = ssoProvider;
        this.scndFactorTypes = scndFactorTypes;
    }

    public CryptoConfigView getCryptoConfigView() {
        return cryptoConfigView;
    }

    public boolean isAutoSSOLogin() {
        return autoSSOLogin;
    }

    public String[] getSsoProvider() {
        return ssoProvider;
    }

    public String[] getScndFactorTypes() {
        return scndFactorTypes;
    }
}
