package de.trustable.ca3s.core.service;


import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import de.trustable.ca3s.core.config.LDAPConfig;
import de.trustable.ca3s.core.security.provider.Ca3sTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import static com.unboundid.ldap.sdk.SearchRequest.ALL_USER_ATTRIBUTES;

@Service
public class LDAPCredentialService {

    private final Logger LOG = LoggerFactory.getLogger(LDAPCredentialService.class);

    private final LDAPConfig ldapConfig;
    private final Ca3sTrustManager ca3sTrustManager;

    public LDAPCredentialService(AuthenticationManagerBuilder authenticationManagerBuilder, LDAPConfig ldapConfig, Ca3sTrustManager ca3sTrustManager) {
        this.ldapConfig = ldapConfig;
        this.ca3sTrustManager = ca3sTrustManager;
    }

    public boolean checkUserPasswordWithLDAP(final String username, final String principal, final String password) {

        try {

            if (checkLDAPAccess(username, principal, password)) {
                return true;
            } else {
                LOG.info("user '{}' not valid for LDAP authentication", username);
                return false;
            }
        } catch (MalformedURLException | LDAPException | GeneralSecurityException e) {
            LOG.info("performing LDAP authentication fails with exception", e);
            return false;
        }
    }

    private boolean checkLDAPAccess(final String username, final String principal, final String password)
        throws GeneralSecurityException, LDAPException, MalformedURLException {

        boolean outcome = false;

        SSLUtil sslUtil = new SSLUtil(ca3sTrustManager);
        LDAPConnectionOptions options = new LDAPConnectionOptions();
        options.setConnectTimeoutMillis(5000);

        LOG.info("starting ldap request to server '{}:{}' with LDAP authentication '{}/{}' looking for sAMAccountName '{}'",
            ldapConfig.getLdapHost(),
            ldapConfig.getLdapPort(),
            principal,
            password,
            username);

        try(LDAPConnection conn = new LDAPConnection(sslUtil.createSSLSocketFactory(), options,
            ldapConfig.getLdapHost(),
            ldapConfig.getLdapPort(),
            principal,
            password)){

            Filter filter = Filter.and(
                Filter.equals("objectClass", "user"),
                Filter.equals("sAMAccountName", username)
            );

            SearchRequest searchRequest = new SearchRequest(ldapConfig.getBaseDN(), SearchScope.SUB, filter, ALL_USER_ATTRIBUTES );
            com.unboundid.ldap.sdk.SearchResult result = conn.search(searchRequest);

            LOG.info( "LDAP search for {} has {} result",username, result.getSearchEntries() );

            outcome = !result.getSearchEntries().isEmpty();
        }

        return outcome;
    }
}
