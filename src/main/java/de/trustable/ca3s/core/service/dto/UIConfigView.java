package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;

import java.io.Serializable;

public class UIConfigView implements Serializable {

    private final String appName;
    private final CryptoConfigView cryptoConfigView;
    private final boolean autoSSOLogin;
    private final String[] ssoProvider;
    private final String ssoProviderName;
    private final String ldapLoginDomainName;
    private final String samlEntityBaseUrl;
    private final String spnegoEntityBaseUrl;
    private final AuthSecondFactor[] scndFactorTypes;
    private final String[] extUsageArr;
    private final String infoMsg;
    private final String testMode;


    public UIConfigView(String appName, CryptoConfigView cryptoConfigView,
                        boolean autoSSOLogin,
                        String[] ssoProvider,
                        String ssoProviderName,
                        String ldapLoginDomainName,
                        String samlEntityBaseUrl,
                        AuthSecondFactor[] scndFactorTypes,
                        String[] extUsageArr,
                        String infoMsg,
                        String testMode) {
        this.appName = appName;
        this.cryptoConfigView = cryptoConfigView;
        this.autoSSOLogin = autoSSOLogin;
        this.ssoProviderName = ssoProviderName;
        this.ldapLoginDomainName = ldapLoginDomainName;
        this.samlEntityBaseUrl = samlEntityBaseUrl;
        this.ssoProvider = ssoProvider;
        this.scndFactorTypes = scndFactorTypes;
        this.extUsageArr = extUsageArr;
        this.infoMsg = infoMsg;
        this.testMode = testMode;
        this.spnegoEntityBaseUrl = "";
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

    public String getSsoProviderName() {
        return ssoProviderName;
    }
    public String getLdapLoginDomainName() {
        return ldapLoginDomainName;
    }

    public String getSamlEntityBaseUrl() {
        return samlEntityBaseUrl;
    }

    public String getSpnegoEntityBaseUrl() {
        return spnegoEntityBaseUrl;
    }

    public AuthSecondFactor[] getScndFactorTypes() {
        return scndFactorTypes;
    }

    public String[] getExtUsageArr() {
        return extUsageArr;
    }

    public String getInfoMsg() {return infoMsg;}

    public String getTestMode() {
        return testMode;
    }
}
