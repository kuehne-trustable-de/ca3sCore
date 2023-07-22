package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.security.AuthenticationProviderWrapper;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.DomainUserDetailsService;
import de.trustable.ca3s.core.security.apikey.APIKeyAuthFilter;
import de.trustable.ca3s.core.security.apikey.APIKeyAuthenticationManager;
import de.trustable.ca3s.core.security.apikey.NullAuthFilter;
import de.trustable.ca3s.core.security.jwt.JWTConfigurer;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
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


    private final TokenProvider tokenProvider;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;
    private final DomainUserDetailsService userDetailsService;
    private final boolean apiKeyEnabled;
    private final String apiKeyRequestHeader;
    private final String apiKeyAdminValue;

    public SecurityConfiguration(TokenProvider tokenProvider,
    		CorsFilter corsFilter,
    		SecurityProblemSupport problemSupport,
    		DomainUserDetailsService userDetailsService,
             @Value("${ca3s.auth.api-key.enabled:false}") boolean apiKeyEnabled,
             @Value("${ca3s.auth.api-key.auth-token-header-name:X-API-KEY}")String apiKeyRequestHeader,
             @Value("${ca3s.auth.api-key.auth-token-admin:}") String apiKeyAdminValue) {

        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
        this.userDetailsService = userDetailsService;
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
    public WebSecurityCustomizer configure() {

        return (web) ->
            web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/images/*.{jpg,png}")
                .antMatchers("/css/*.css")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
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

        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        // @formatter:off
        http
            .csrf().disable()
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .accessDeniedHandler(problemSupport)
            .and()
            .headers()
            .contentSecurityPolicy("default-src 'self';" +
                " frame-src 'self' data:;" +
                " script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com;" +
                " style-src 'self' 'unsafe-inline';" +
                " img-src 'self' data:;" +
                " font-src 'self' data:;" +
                " connect-src 'self' data:")
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
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/authorities").permitAll()
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()

            .antMatchers("/api/profile-info").permitAll()
            .antMatchers("/api/ui/config").permitAll()
            .antMatchers("/api/certificateSelectionAttributes").permitAll()
            .antMatchers("/api/pipelineViews").permitAll()
            .antMatchers("/api/pipeline-attributes").permitAll()
            .antMatchers("/api/pipeline/activeWeb").permitAll()

            .antMatchers("/publicapi/**").permitAll()

            .requestMatchers(forPortAndPath(raPort, "/api/ca-connector-configs")).hasAuthority(AuthoritiesConstants.ADMIN)

            .requestMatchers(forPortAndPath(raPort, "/api/administerRequest")).hasAnyAuthority(AuthoritiesConstants.RA_OFFICER,AuthoritiesConstants.DOMAIN_RA_OFFICER)
            .antMatchers("/api/administerRequest").denyAll()

            .requestMatchers(forPortAndPath(raPort, "/api/administerCertificate")).hasAnyAuthority(AuthoritiesConstants.ADMIN,AuthoritiesConstants.RA_OFFICER,AuthoritiesConstants.DOMAIN_RA_OFFICER)
            .antMatchers("/api/administerCertificate").denyAll()

            // check on method level
            .antMatchers("/api/request-proxy-configs/remote-config/*").permitAll()
            .antMatchers("/api/acme-challenges/pending/request-proxy-configs/*").permitAll()
            .antMatchers("/api/acme-challenges/validation").permitAll()

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

            .requestMatchers(forPortAndPath(scepPort, "/scep/**")).permitAll()
            .antMatchers("/scep/**").denyAll()

            .requestMatchers(forPortAndPath(scepPort, "/ca3sScep/**")).permitAll()
            .antMatchers("/ca3sScep/**").denyAll()

            .antMatchers("/api/cockpit/**").permitAll()
            .antMatchers("/api/tasklist/**").permitAll()
            .antMatchers("/api/engine/**").permitAll()
            .antMatchers("/api/executeProcess/**").permitAll()

            .antMatchers("/api/**").authenticated()
            .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/websocket/**").permitAll()
            .antMatchers("/management/loggers").permitAll()
            .antMatchers("/management/audits").permitAll()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()

            .requestMatchers(forPortAndPath(adminPort, "/api/notification/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers(forPortAndPath(adminPort, "/api/schedule/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/notification/**").denyAll()

            .requestMatchers(forPortAndPath(adminPort, "/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/management/**").denyAll()
        .and()
            .httpBasic()
        .and()
            .apply(securityConfigurerAdapter());
        // @formatter:on

        LOG.info("registered JWT-based configuration ");

        return http.build();
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }

    private AbstractPreAuthenticatedProcessingFilter apiKeyAuthFilter() {

        if (apiKeyEnabled) {
            APIKeyAuthFilter filter = new APIKeyAuthFilter(apiKeyRequestHeader);
            filter.setAuthenticationManager(new APIKeyAuthenticationManager(apiKeyAdminValue));
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
     * Creates a request matcher which only matches requests for a specific local port, path and request method (using
     * an {@link AntPathRequestMatcher} for the path part).
     *
     * @param   port         the port to match
     * @param   pathPattern  the pattern for the path.
     * @param   method       the HttpMethod to match. Requests for other methods will not be matched.
     *
     * @return  the new request matcher.
     */
//    private RequestMatcher forPortAndPath(final int port, @Nonnull final HttpMethod method,
//            @Nonnull final String pathPattern) {
//        return new AndRequestMatcher(forPort(port), new AntPathRequestMatcher(pathPattern, method.name()));
//    }

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

}
