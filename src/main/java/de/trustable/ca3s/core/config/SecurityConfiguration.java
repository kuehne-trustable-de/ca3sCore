package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.config.oidc.CustomOAuth2AuthenticationSuccessHandler;
import de.trustable.ca3s.core.config.oidc.CustomOAuth2LoginAuthenticationProvider;
import de.trustable.ca3s.core.config.saml.CustomSAMLBootstrap;
import de.trustable.ca3s.core.security.AuthenticationProviderWrapper;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.DomainUserDetailsService;
import de.trustable.ca3s.core.security.apikey.APIKeyAuthFilter;
import de.trustable.ca3s.core.security.apikey.APIKeyAuthenticationManager;
import de.trustable.ca3s.core.security.apikey.NullAuthFilter;
import de.trustable.ca3s.core.security.jwt.JWTConfigurer;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.saml.*;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Order(2)
public class SecurityConfiguration{

	private final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

	@Value("${server.port:8080}")
	int httpPort;

	@Value("${ca3s.tlsAccess.port:0}")
	int tlsPort;

	@Value("${ca3s.adminAccess.port:0}")
	int adminPort;

	@Value("${ca3s.raAccess.port:0}")
	int raPort;

	@Value("${ca3s.acmeAccess.port:0}")
	int acmePort;

    @Value("${ca3s.scepAccess.port:0}")
    int scepPort;

    @Value("${ca3s.estAccess.port:0}")
    int estPort;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${ca3s.saml.activate:true}")
    private boolean samlActivate;

    @Value("${ca3s.saml.sp}")
    private String samlAudience;

    @Value("${ca3s.saml.entity.base-url:#{null}}")
    private String samlEntityBaseurl;

    @Value("${ca3s.auth.kerberos.service-principal:#{null}}")
    private String servicePrincipal;

    @Value("${ca3s.auth.kerberos.keytab-location:#{null}}")
    private String keytabLocation;

    @Autowired
    CustomOAuth2LoginAuthenticationProvider oAuth2LoginAuthenticationProvider;

    @Autowired
    CustomOAuth2AuthenticationSuccessHandler oauthSuccessHandler;

    @Autowired
    @Qualifier("saml")
    private SavedRequestAwareAuthenticationSuccessHandler samlAuthSuccessHandler;

    @Autowired
    @Qualifier("saml")
    private SimpleUrlAuthenticationFailureHandler samlAuthFailureHandler;

    @Autowired
    @Lazy
    private SAMLEntryPoint samlEntryPoint;

    @Autowired
    private SAMLLogoutFilter samlLogoutFilter;

    @Autowired
    private SAMLLogoutProcessingFilter samlLogoutProcessingFilter;

    @Autowired
    private SAMLAuthenticationProvider samlAuthenticationProvider;

    @Autowired
    private ExtendedMetadata extendedMetadata;

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationConfiguration configuration;

    private final TokenProvider tokenProvider;

    private final CorsFilter corsFilter;
    private final DomainUserDetailsService userDetailsService;

    private final ClientAuthConfig clientAuthConfig;

    private final boolean apiKeyEnabled;
    private final String apiKeyRequestHeader;
    private final String apiKeyAdminValue;

    public SecurityConfiguration(TokenProvider tokenProvider,
                                 CorsFilter corsFilter,
                                 DomainUserDetailsService userDetailsService,
                                 ClientAuthConfig clientAuthConfig,
                                 @Value("${ca3s.auth.api-key.enabled:false}") boolean apiKeyEnabled,
                                 @Value("${ca3s.auth.api-key.auth-token-header-name:X-API-KEY}")String apiKeyRequestHeader,
                                 @Value("${ca3s.auth.api-key.auth-token-admin:}") String apiKeyAdminValue) {

        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.userDetailsService = userDetailsService;
        this.clientAuthConfig = clientAuthConfig;
        this.apiKeyEnabled = apiKeyEnabled;
        this.apiKeyRequestHeader = apiKeyRequestHeader;
        this.apiKeyAdminValue = apiKeyAdminValue;
        if( apiKeyAdminValue != null && apiKeyAdminValue.trim().isEmpty()){
            apiKeyAdminValue = null;
        }

        if( apiKeyAdminValue != null && apiKeyAdminValue.trim().length() < 100) {
            throw new InvalidConfigurationPropertyValueException("ca3s.auth.api-key.auth-token-admin", apiKeyAdminValue,
                "API Key too short, at least 100 character required");
        }
    }

    @Bean
    public SAMLDiscovery samlDiscovery() {
        return new SAMLDiscovery();
    }

    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setEntityId(samlAudience);
        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager);
        if( samlEntityBaseurl != null ) {
            metadataGenerator.setEntityBaseURL(samlEntityBaseurl);
        }
        return metadataGenerator;
    }

    @Bean
    public static SAMLBootstrap SAMLBootstrap() {
        return new CustomSAMLBootstrap();
    }

    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter(AuthenticationManager authenticationManager) throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager);
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(samlAuthSuccessHandler);
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(samlAuthFailureHandler);
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(){
        LOG.info("SecurityConfiguration daoAuthenticationProvider()");

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);

        return new AuthenticationProviderWrapper(daoAuthenticationProvider);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
            .authenticationProvider(samlAuthenticationProvider)
            .authenticationProvider(daoAuthenticationProvider());

        if( isOAuthProviderRegistered()) {
            authenticationManagerBuilder
                .authenticationProvider(oAuth2LoginAuthenticationProvider);
            LOG.info("registered oAuth2LoginAuthenticationProvider");
        }

        if (keytabLocation != null && !keytabLocation.isEmpty() &&
            servicePrincipal != null && !servicePrincipal.isEmpty()) {
            authenticationManagerBuilder
                .authenticationProvider(kerberosAuthenticationProvider())
                .authenticationProvider(kerberosServiceAuthenticationProvider());
            LOG.info("registered kerberosAuthenticationProvider");
        }
        return authenticationManagerBuilder.build();
    }

    @Bean
    @ConditionalOnProperty("ca3s.auth.kerberos.service-principal")
    public KerberosAuthenticationProvider kerberosAuthenticationProvider() {
        LOG.info("Instantiating bean for kerberosAuthenticationProvider()");
        KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
        SunJaasKerberosClient client = new SunJaasKerberosClient();
        client.setDebug(true);
        provider.setKerberosClient(client);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    @ConditionalOnProperty("ca3s.auth.kerberos.service-principal")
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
        LOG.info("Instantiating bean for kerberosServiceAuthenticationProvider()");
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    @ConditionalOnProperty("ca3s.auth.kerberos.service-principal")
    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
        LOG.info("Instantiating bean for SunJaasKerberosTicketValidator() with principal '{}' and keytab at '{}'",
            servicePrincipal, new File(keytabLocation).getAbsolutePath());

        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
        ticketValidator.setServicePrincipal(servicePrincipal);
        ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
        ticketValidator.setDebug(true);
        return ticketValidator;
    }

    @Bean
    @ConditionalOnProperty("ca3s.auth.kerberos.service-principal")
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
        AuthenticationManager authenticationManager) {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }


    @Bean
    public FilterChainProxy samlFilter(AuthenticationManager authenticationManager) throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();
        if (samlActivate) {
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter(authenticationManager)));
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                samlDiscovery()));
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint));
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter));
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter));
        }
        return new FilterChainProxy(chains);
    }

    @Bean
    public WebSecurityCustomizer configure() {

        return (web) ->
            web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/images/*.{jpg,png}")
                .antMatchers("/css/*.css")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/.well-known/est/**")
                .antMatchers("/h2-console/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/test/**")
            ;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    	LOG.info("SecurityConfiguration.filterChain ");

    	if(scepPort == 0 ) {
    		scepPort = httpPort;
    	}

    	if(adminPort == 0 ) {
    		adminPort = tlsPort;
    	}

    	if(raPort == 0 ) {
    		raPort = tlsPort;
    	}

    	if(acmePort == 0 ) {
    		acmePort = tlsPort;
    	}

        AuthenticationManager authenticationManager = authManager(http);

        // @formatter:off
        HttpSecurity httpSecurity =
            http
            .cors(Customizer.withDefaults())
            .csrf().disable()

            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

            .addFilterBefore(apiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class)

            .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
            .addFilterAfter(samlFilter(authenticationManager), BasicAuthenticationFilter.class)
            .addFilterBefore(samlFilter(authenticationManager), CsrfFilter.class);

        if (servicePrincipal != null && !servicePrincipal.isEmpty()) {
            LOG.debug("add Filter : spnegoAuthenticationProcessingFilter");
            httpSecurity = httpSecurity.addFilterBefore(spnegoAuthenticationProcessingFilter(authenticationManager), BasicAuthenticationFilter.class);
        }

        httpSecurity
            .exceptionHandling()
//            .accessDeniedHandler(problemSupport)
            .and()
            .headers()
            .contentSecurityPolicy("default-src 'self';" +
                " frame-src 'self' data:;" +
                " script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com;" +
                " style-src 'self' 'unsafe-inline';" +
                " img-src 'self' data:;" +
                " font-src 'self' data:;" +
                " connect-src 'self' blob: data: https://accounts.google.com; " + clientAuthConfig.getClientAuthTarget())
        .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
        .and()
            .frameOptions()
            .deny()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/languages").permitAll()
            .antMatchers("/api/account").permitAll()
            .antMatchers("/api/saml/jwt").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/authenticateLDAP").permitAll()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/authorities").permitAll()
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()

            .antMatchers("/api/profile-info").permitAll()
            .antMatchers("/api/ui/config").permitAll()
            .antMatchers("/api/certificateSelectionAttributes").permitAll()

            // really all??
            .antMatchers("/api/pipelineViews").permitAll()
            .antMatchers("/api/pipeline-attributes").permitAll()
            .antMatchers("/api/pipeline/activeWeb").permitAll()

            .antMatchers("/auth").permitAll()
            .antMatchers("/saml/SSO").permitAll()
            .antMatchers("/saml/**").permitAll()
            .antMatchers("/spnego/**").permitAll()
            .antMatchers("/publicapi/**").permitAll()

            .antMatchers("/management/loggers").permitAll()
            .antMatchers("/api/audits").permitAll()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()


            // @ToDo check relevance
            .antMatchers("/login/oauth2/code/**").permitAll()
            .antMatchers("/loginFailure").permitAll()


            .antMatchers("/api/userProperties/filterList/CertList").authenticated()

            .requestMatchers( forPortAndPath(new Integer[]{raPort, adminPort}, "/api/administerRequest"))
              .hasAnyAuthority(AuthoritiesConstants.RA_OFFICER,AuthoritiesConstants.DOMAIN_RA_OFFICER, AuthoritiesConstants.ADMIN)
            .antMatchers("/api/administerRequest").denyAll()

            .requestMatchers( forPortAndPath(new Integer[]{raPort, adminPort}, "/api/administerCertificate"))
            .hasAnyAuthority(AuthoritiesConstants.RA_OFFICER,AuthoritiesConstants.DOMAIN_RA_OFFICER, AuthoritiesConstants.ADMIN)
            .antMatchers("/api/administerCertificate").denyAll()

            // Check this block for usefulness of endpoints
//            .antMatchers("/camunda/**").permitAll()
            .antMatchers("/api/cockpit/**").permitAll()
            .antMatchers("/api/tasklist/**").permitAll()
            .antMatchers("/api/engine/**").permitAll()
            .antMatchers("/api/executeProcess/**").permitAll()

            .antMatchers("/api/request-proxy-configs/remote-config/*").permitAll()
            .antMatchers("/api/acme-challenges/pending/request-proxy-configs/*").permitAll()
            .antMatchers("/api/acme-challenges/validation").permitAll()

            .requestMatchers(forPortAndPath(adminPort, "/api/acme-accounts")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/acme-authorizations")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/acme-challenges")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/acme-contacts")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/acme-nonces")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/acme-orders")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/certificate-attributes")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/certificates")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/crl-expiration-notifications")).hasAuthority(AuthoritiesConstants.ADMIN)

            .requestMatchers(forPortAndPath(adminPort, "/api/csr-attributes")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/csrs")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/pipeline-attributes")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/pipelines")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/rdn-attributes")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/rdns")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/request-attributes")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/request-attribute-values")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/timed-element-notifications")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/tenants")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/tenants/*")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/timed-element-notifications")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/timed-element-notifications/*")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/request-proxy-configs")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/crl-expiration-notifications/*")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/bpmn-process-infos")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/audit-traces")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/algorithm-restrictions")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/algorithm-restrictions/*")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/ca-connector-configs")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/ca-connector-configs/*")).hasAuthority(AuthoritiesConstants.ADMIN)


            .antMatchers("/api/acme-accounts").denyAll()
            .antMatchers("/api/acme-authorizations").denyAll()
            .antMatchers("/api/acme-challenges").denyAll()
            .antMatchers("/api/acme-contacts").denyAll()
            .antMatchers("/api/acme-identifiers").denyAll()
            .antMatchers("/api/acme-nonces").denyAll()
            .antMatchers("/api/acme-orders").denyAll()

            .antMatchers("/api/certificate-attributes").denyAll()
            .antMatchers("/api/certificates").denyAll()
            .antMatchers("/api/crl-expiration-notifications").denyAll()
            .antMatchers("/api/csr-attributes").denyAll()
            .antMatchers("/api/csrs").denyAll()
            .antMatchers("/api/imported-urls").denyAll()
            .antMatchers("/api/pipeline-attributes").denyAll()
            .antMatchers("/api/pipelines").denyAll()
            .antMatchers("/api/rdn-attributes").denyAll()
            .antMatchers("/api/rdns").denyAll()
            .antMatchers("/api/request-attributes").denyAll()
            .antMatchers("/api/request-attribute-values").denyAll()
            .antMatchers("/api/timed-element-notifications").denyAll()


            .requestMatchers(forPortAndPath(acmePort, "/acme/**")).permitAll()
            .antMatchers("/acme/**").denyAll()

            /*
            .requestMatchers(forPortAndPath(estPort, "/.well-known/est/**")).permitAll()
            .antMatchers("/.well-known/est/**").denyAll()
*/
            .requestMatchers(forPortAndPath(scepPort, "/scep/**")).permitAll()
            .antMatchers("/scep/**").denyAll()

            .requestMatchers(forPortAndPath(scepPort, "/ca3sScep/**")).permitAll()
            .antMatchers("/ca3sScep/**").denyAll()

            .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/websocket/**").permitAll()

            .requestMatchers(forPortAndPath(adminPort, "/api/notification/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/schedule/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/notification/**").denyAll()

            .requestMatchers(forPortAndPath(adminPort, "/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/management/**").denyAll()

//            .antMatchers("/api/uploadContent").permitAll() // allow general properties
//            .antMatchers("/api/preference/1").permitAll() // allow general properties

            .antMatchers("/api/**").authenticated()


        .and()
            .httpBasic()

        .and().authenticationManager(authenticationManager)
        .apply(securityConfigurerAdapter());

        if( isOAuthProviderRegistered()){
            httpSecurity
                .oauth2Login()
                .authorizationEndpoint().baseUri("/oauth2/authorize-client")
                .authorizationRequestRepository(authorizationRequestRepository())
                .and()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .successHandler(oauthSuccessHandler)
                .failureUrl("/loginFailure");
            LOG.info("registered OAuth2 configuration ");
        }

        // @formatter:on

        LOG.info("registered JWT-based configuration ");

        return http.build();
    }


    public boolean isOAuthProviderRegistered() {

        // check upfront whether the oauth registry is present
        // if not, don't even try to establish the OAuth filter and handler
        return !applicationContext.getBeansOfType( ClientRegistrationRepository.class ).isEmpty();
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }

    private AbstractPreAuthenticatedProcessingFilter apiKeyAuthFilter() {

        if (apiKeyEnabled) {
            APIKeyAuthFilter filter = new APIKeyAuthFilter(apiKeyRequestHeader);
            filter.setAuthenticationManager(new APIKeyAuthenticationManager(userService, apiKeyAdminValue));
            LOG.info("registered authentication by APIKey");
            return filter;
        } else {
            LOG.info("authentication by APIKey disabled");
            return new NullAuthFilter();
        }
    }


    /**
     * Creates a request matcher which only matches requests for a specific local port and path (using an
     * {@link AntPathRequestMatcher} for the path part).
     *
     * @param   port         the port to match
     * @param   pathPattern  the pattern for the path.
     *
     * @return  the new request matcher.
     */
    private RequestMatcher forPortAndPath(final int port, @Nonnull final String pathPattern) {
        return new AndRequestMatcher(forPort(port), new AntPathRequestMatcher(pathPattern));
    }

    /**
     * Creates a request matcher which only matches requests for a specific local port and path (using an
     * {@link AntPathRequestMatcher} for the path part).
     *
     * @param   portList     the ports to match
     * @param   pathPattern  the pattern for the path.
     *
     * @return  the new request matcher.
     */
    private RequestMatcher forPortAndPath(final List<Integer> portList, @Nonnull final String pathPattern) {
        return new AndRequestMatcher(forPort(portList), new AntPathRequestMatcher(pathPattern));
    }

    /**
     * Creates a request matcher which only matches requests for a specific local port and path (using an
     * {@link AntPathRequestMatcher} for the path part).
     *
     * @param   portArr     the ports to match
     * @param   pathPattern  the pattern for the path.
     *
     * @return  the new request matcher.
     */
    private RequestMatcher forPortAndPath(final Integer[] portArr, @Nonnull final String pathPattern) {
        return new AndRequestMatcher(forPort(Arrays.asList(portArr)), new AntPathRequestMatcher(pathPattern));
    }

    /**
     * A request matcher which matches just a port.
     *
     * @param   port  the port to match.
     *
     * @return  the new matcher.
     */
    private RequestMatcher forPort(final int port) {
        return (HttpServletRequest request) -> {
            boolean result =  (port == 0) || (port == request.getLocalPort());
            LOG.debug("checking local port {} against target port {} evaluates to {}", request.getLocalPort(), port, result);
            return result;
        };
    }

    /**
     * A request matcher which matches just a port list.
     *
     * @param   portList the ports to match.
     *
     * @return  the new matcher.
     */
    private RequestMatcher forPort(final List<Integer> portList) {
        return (HttpServletRequest request) -> {
            for( Integer port: portList) {
                boolean result = (port == 0) || (port == request.getLocalPort());
                LOG.debug("checking local port {} against target port {} evaluates to {}", request.getLocalPort(), port, result);
                if(result){
                    return true;
                }
            }
            return false;
        };
    }


    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        return accessTokenResponseClient;
    }

}
