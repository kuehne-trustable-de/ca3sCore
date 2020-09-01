package de.trustable.ca3s.core.config;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.jwt.JWTConfigurer;
import de.trustable.ca3s.core.security.jwt.TokenProvider;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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

	@Value("${win-auth.ad-domain:}")
	private String adDomain;

	@Value("${win-auth.ad-server:}")
	private String adServer;

	@Value("${win-auth.service-principal:}")
	private String servicePrincipal;

	@Value("${win-auth.keytab-location:ca3s.keytab}")
	private String keytabLocation;

	@Value("${win-auth.ldap-search-base:}")
	private String ldapSearchBase;

	@Value("${win-auth.ldap-search-filter:(| (userPrincipalName={0}) (sAMAccountName={0}))}")
	private String ldapSearchFilter;


    private final TokenProvider tokenProvider;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(TokenProvider tokenProvider, CorsFilter corsFilter, SecurityProblemSupport problemSupport) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
/*
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		super.configure(auth);

		if( !adDomain.isEmpty()) {
			auth
				.authenticationProvider(activeDirectoryLdapAuthenticationProvider())
				.authenticationProvider(kerberosServiceAuthenticationProvider());
		}
		
	}

	@Bean
	public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
		return new ActiveDirectoryLdapAuthenticationProvider(adDomain, adServer);
	}
*/

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/h2-console/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
    	
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
    	
        // @formatter:off
        http
            .csrf().disable()
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
//			.addFilterBefore(spnegoAuthenticationProcessingFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
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
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()
            
            .antMatchers("/api/profile-info").permitAll()

            .antMatchers("/publicapi/**").permitAll()

            .requestMatchers(forPortAndPath(raPort, "/api/administerRequest")).hasAuthority(AuthoritiesConstants.RA_OFFICER) 
            .antMatchers("/api/administerRequest").denyAll()
            .requestMatchers(forPortAndPath(raPort, "/api/administerCertificate")).hasAuthority(AuthoritiesConstants.RA_OFFICER) 
            .antMatchers("/api/administerCertificate").denyAll()

            .requestMatchers(forPortAndPath(acmePort, "/acme/**")).permitAll() 
            .antMatchers("/acme/**").denyAll()

            .requestMatchers(forPortAndPath(scepPort, "/ca3sScep/**")).permitAll() 
            .antMatchers("/ca3sScep/**").denyAll()

            // to be checked
            .antMatchers("/api/admin/**").permitAll()
            .antMatchers("/api/cockpit/**").permitAll()
            .antMatchers("/api/tasklist/**").permitAll()
            .antMatchers("/api/engine/**").permitAll()
            .antMatchers("/api/executeProcess/**").permitAll()

//            .antMatchers("/api/**").authenticated()
            .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/websocket/**").permitAll()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            
            .requestMatchers(forPortAndPath(adminPort, "/management/**")).hasAuthority(AuthoritiesConstants.ADMIN) 
            .antMatchers("/management/**").denyAll()
        .and()
            .httpBasic()
        .and()
            .apply(securityConfigurerAdapter());
        // @formatter:on
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
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
    private RequestMatcher forPortAndPath(final int port, @Nonnull final HttpMethod method,
            @Nonnull final String pathPattern) {
        return new AndRequestMatcher(forPort(port), new AntPathRequestMatcher(pathPattern, method.name()));
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
        	LOG.info("checking local port {} against target port {} evaluates to {}", request.getLocalPort(), port, result);
        	return result; 
        };
    }
/*    
	@Bean
	public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
			AuthenticationManager authenticationManager) {
		SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
		filter.setAuthenticationManager(authenticationManager);
		return filter;
	}

	@Bean
	public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws Exception {
		KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
		provider.setTicketValidator(sunJaasKerberosTicketValidator());
		provider.setUserDetailsService(ldapUserDetailsService());
		return provider;
	}

	@Bean
	public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
		SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
		ticketValidator.setServicePrincipal(servicePrincipal);
		ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
		ticketValidator.setDebug(true);
		return ticketValidator;
	}

	@Bean
	public KerberosLdapContextSource kerberosLdapContextSource() throws Exception {
		if( adServer.isEmpty()) {
			return null;
		}
		KerberosLdapContextSource contextSource = new KerberosLdapContextSource(adServer);
		contextSource.setLoginConfig(loginConfig());
		return contextSource;
	}

	public SunJaasKrb5LoginConfig loginConfig() throws Exception {
		SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
		loginConfig.setKeyTabLocation(new FileSystemResource(keytabLocation));
		loginConfig.setServicePrincipal(servicePrincipal);
		loginConfig.setDebug(true);
		loginConfig.setIsInitiator(true);
		loginConfig.afterPropertiesSet();
		return loginConfig;
	}

	@Bean
	public LdapUserDetailsService ldapUserDetailsService() throws Exception {
		FilterBasedLdapUserSearch userSearch =
				new FilterBasedLdapUserSearch(ldapSearchBase, ldapSearchFilter, kerberosLdapContextSource());
		LdapUserDetailsService service =
				new LdapUserDetailsService(userSearch, new ActiveDirectoryLdapAuthoritiesPopulator());
		service.setUserDetailsMapper(new LdapUserDetailsMapper());
		return service;
	}


	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
*/
}
