package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class UIConfigView implements Serializable {

    private final CryptoConfigView cryptoConfigView;
    private final boolean autoSSOLogin;
    private final String[] ssoProvider;

    public UIConfigView(CryptoConfigView cryptoConfigView, boolean autoSSOLogin, String[] ssoProvider) {
        this.cryptoConfigView = cryptoConfigView;
        this.autoSSOLogin = autoSSOLogin;
        this.ssoProvider = ssoProvider;
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
}
