package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;

import java.io.Serializable;

public class UIConfigView implements Serializable {

    private final String appName;
    private final CryptoConfigView cryptoConfigView;
    private final boolean autoSSOLogin;
    private final String[] ssoProvider;
    private final String samlEntityBaseUrl;
    private final AuthSecondFactor[] scndFactorTypes;


    public UIConfigView(String appName, CryptoConfigView cryptoConfigView,
                        boolean autoSSOLogin,
                        String[] ssoProvider,
                        String samlEntityBaseUrl,
                        AuthSecondFactor[] scndFactorTypes) {
        this.appName = appName;
        this.cryptoConfigView = cryptoConfigView;
        this.autoSSOLogin = autoSSOLogin;
        this.samlEntityBaseUrl = samlEntityBaseUrl;
        this.ssoProvider = ssoProvider;
        this.scndFactorTypes = scndFactorTypes;
    }

    public CryptoConfigView getCryptoConfigView() {
        return cryptoConfigView;
    }

    public String getAppName() {
        return appName;
    }
    public boolean isAutoSSOLogin() {
        return autoSSOLogin;
    }

    public String[] getSsoProvider() {
        return ssoProvider;
    }

    public String getSamlEntityBaseUrl() {
        return samlEntityBaseUrl;
    }

    public AuthSecondFactor[] getScndFactorTypes() {
        return scndFactorTypes;
    }
}
