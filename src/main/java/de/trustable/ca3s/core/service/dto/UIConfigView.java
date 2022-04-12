package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class UIConfigView implements Serializable {

    private final CryptoConfigView cryptoConfigView;
    private final boolean autoSSOLogin;

    public UIConfigView(CryptoConfigView cryptoConfigView, boolean autoSSOLogin) {
        this.cryptoConfigView = cryptoConfigView;
        this.autoSSOLogin = autoSSOLogin;
    }

    public CryptoConfigView getCryptoConfigView() {
        return cryptoConfigView;
    }

    public boolean isAutoSSOLogin() {
        return autoSSOLogin;
    }
}
