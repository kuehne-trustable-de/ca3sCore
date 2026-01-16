package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.config.oidc.CustomOAuth2LoginAuthenticationProvider;
import de.trustable.ca3s.core.config.oidc.OIDCMappingConfig;
import de.trustable.ca3s.core.config.util.SPeLUtil;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;


@Configuration
public class OidcSecurityConfig {

    private final Logger LOG = LoggerFactory.getLogger(OidcSecurityConfig.class);

    @Lazy
    @Autowired
    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreferenceUtil preferenceUtil;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OIDCMappingConfig oidcMappingConfig;

    @Autowired
    private SPeLUtil sPeLUtil;

    @Autowired
    private TenantRepository tenantRepository;

    @Value("${ca3s.ui.languages:en,de,pl}")
    String availableLanguages;

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean
    public CustomOAuth2LoginAuthenticationProvider oidcAuthenticationProvider() {
        return new CustomOAuth2LoginAuthenticationProvider(accessTokenResponseClient,
            preferenceUtil,
            userRepository,
            authorityRepository,
            tenantRepository,
            sPeLUtil,
            availableLanguages,
            oidcMappingConfig);
    }
}
