package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.security.AuthenticationProviderWrapper;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.DomainUserDetailsService;
import de.trustable.ca3s.core.security.jwt.JWTConfigurer;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

// @EnableWebSecurity
// @Order(1)
public class KerberosSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final Logger LOG = LoggerFactory.getLogger(KerberosSecurityConfiguration.class);

    @Value("${ca3s.auth.kerberos.keytab-location:}")
    private String keytabLocation;

    @Value("${ca3s.auth.kerberos.service-principal:HTTP/admin@ca3s}")
    private String servicePrincipal;

    @Value("${ca3s.auth.ad-domain:}")
	private String adDomain;

    @Value("${ca3s.auth.ldap.url:}")
    private String ldapURL;

    @Value("${ca3s.auth.ldap.search-base:}")
	private String ldapSearchBase;

	@Value("${ca3s.auth.ldap.search-filter:(| (userPrincipalName={0}) (sAMAccountName={0}))}")
	private String ldapSearchFilter;

    @Value("${ca3s.auth.ldap.group-search-base:}")
    private String ldapGroupSearchBase;

    @Value("${ca3s.auth.ldap.baseDN:}")
    private String ldapBaseDN;

    @Value("${ca3s.auth.ldap.principal:}")
    private String ldapPrincipal;

    @Value("${ca3s.auth.ldap.password:}")
    private String ldapPassword;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    public KerberosSecurityConfiguration(CorsFilter corsFilter,
                                         SecurityProblemSupport problemSupport) {
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if(StringUtil.isBlank(keytabLocation) || StringUtil.isBlank(servicePrincipal) ){
            LOG.info("no values provided for 'ca3s.auth.kerberos.keytab-location' or 'ca3s.auth.kerberos.service-principal', AuthenticationManagerBuilder will not be configured for kerberos");
        }else {
            auth.authenticationProvider(kerberosServiceAuthenticationProvider());
        }
    }



    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/images/*.{jpg,png}")
            .antMatchers("/css/*.css")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/h2-console/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

    	LOG.info("configure HttpSecurity ");

        // @formatter:off
        http
            .exceptionHandling()
            .authenticationEntryPoint(spnegoEntryPoint())
        .and()
            .antMatcher("/kerberos/authenticatedUser")
            .authorizeRequests().anyRequest()
            .authenticated()
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
            .csrf().disable()
            .addFilterBefore(
                spnegoAuthenticationProcessingFilter(),
                UsernamePasswordAuthenticationFilter.class)

        ;
        // @formatter:on
    }

	@Bean
	public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter() throws Exception {
        LOG.info("in spnegoAuthenticationProcessingFilter");

        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
		filter.setAuthenticationManager(super.authenticationManagerBean());
		return filter;
	}

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws Exception {
        LOG.info("in kerberosServiceAuthenticationProvider");

        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(getUserDetailsService());

        return provider;
    }

    @Bean
    public KerberosAuthenticationProvider kerberosAuthenticationProvider() throws Exception {
        LOG.info("in kerberosAuthenticationProvider");

        KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
        SunJaasKerberosClient client = new SunJaasKerberosClient();
        client.setDebug(LOG.isDebugEnabled());
        provider.setKerberosClient(client);
        provider.setUserDetailsService(getUserDetailsService());

        return provider;
    }

    @Bean
	public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() throws Exception {
        LOG.info("in sunJaasKerberosTicketValidator with servicePrincipal '{}' and keytabLocation '{}'",
            servicePrincipal, keytabLocation);

		SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
		ticketValidator.setServicePrincipal(servicePrincipal);
		ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
		ticketValidator.setDebug(LOG.isDebugEnabled());
        ticketValidator.afterPropertiesSet();
		return ticketValidator;
	}

    public SunJaasKrb5LoginConfig loginConfig() throws Exception {
        SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
        loginConfig.setKeyTabLocation(new FileSystemResource(keytabLocation));
        loginConfig.setServicePrincipal(servicePrincipal);
        loginConfig.setDebug(LOG.isDebugEnabled());
        loginConfig.setIsInitiator(true);
        loginConfig.afterPropertiesSet();
        return loginConfig;
    }


/*

	@Bean
	public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
		return new ActiveDirectoryLdapAuthenticationProvider(adDomain, adServer);
	}


	@Bean
	public KerberosLdapContextSource kerberosLdapContextSource() throws Exception {
		if( adServer.isEmpty()) {
        	LOG.info("No AD server configured!");
			return null;
		}
		KerberosLdapContextSource contextSource = new KerberosLdapContextSource(adServer);
		contextSource.setLoginConfig(loginConfig());
		return contextSource;
	}


    * do not register the ldapUserDetailsService here as it becomes the single source of details retrieval.
    * Integrate it into he given ca3s-specific DomainUserDetailsService.

	@Bean
	public LdapUserDetailsService ldapUserDetailsService() throws Exception {
        LOG.debug("in ldapUserDetailsService: building LdapUserDetailsService");

		FilterBasedLdapUserSearch userSearch =
				new FilterBasedLdapUserSearch(ldapSearchBase, ldapSearchFilter, ldapContextSource());

		String groupSearchBaseDN = ldapBaseDN;
        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(ldapContextSource(), groupSearchBaseDN);

        authoritiesPopulator.setGroupSearchFilter("(member=uid={1})");
        authoritiesPopulator.setGroupRoleAttribute("ou");
        authoritiesPopulator.setSearchSubtree(false);
        authoritiesPopulator.setIgnorePartialResultException(true);

        LdapUserDetailsService service = new LdapUserDetailsService(userSearch, authoritiesPopulator);

		return service;
	}
*/

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        LOG.debug("in spnegoEntryPoint: forwarding to 'Add header WWW-Authenticate:Negotiate'");
        return new SpnegoEntryPoint();
    }

    @Bean
    public LdapContextSource ldapContextSource() {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapURL);
        contextSource.setBase(ldapBaseDN);
        contextSource.setUserDn(ldapPrincipal);
        contextSource.setPassword(ldapPassword);
        return contextSource;
    }


    private UserDetailsService getUserDetailsService(){

        return new UserDetailsService(){
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                LOG.debug("in loadUserByUsername for '{}'", username);

                /*
                AuthenticationProvider authenticationProvider = getAuthenticationProvider();

                LOG.debug("authenticationProvider built");

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null);

                LOG.debug("UsernamePasswordAuthenticationToken built");

                Authentication authentication = authenticationProvider.authenticate(usernamePasswordAuthenticationToken);

                LOG.debug("authentication built: " + authentication);
*/
                return new org.springframework.security.core.userdetails.User(username,
                    "KerberosToken",
                    AuthorityUtils.createAuthorityList(AuthoritiesConstants.USER));

            }
        };
    }


    public AuthenticationProvider getAuthenticationProvider() {

        AbstractLdapAuthenticationProvider ldapAuthenticationProvider;

        if (StringUtils.isBlank(ldapURL)) {
            throw new AuthenticationServiceException("LDAP configuration is missing.");
        } else {
            if (StringUtils.isBlank(adDomain)) {
                LdapContextSource contextSource = new LdapContextSource();
                contextSource.setUrl(ldapURL);
                contextSource.setBase(ldapBaseDN);
                contextSource.setUserDn(ldapPrincipal);
                contextSource.setPassword(ldapPassword);
                contextSource.setReferral("follow");
                contextSource.afterPropertiesSet();

                LdapUserSearch ldapUserSearch = new FilterBasedLdapUserSearch(ldapSearchBase, ldapSearchFilter, contextSource);

                BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
                bindAuthenticator.setUserSearch(ldapUserSearch);

                DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(contextSource, ldapGroupSearchBase);
                ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator, populator);

            } else {
                ldapAuthenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(adDomain, ldapURL);
                ((ActiveDirectoryLdapAuthenticationProvider) ldapAuthenticationProvider).setConvertSubErrorCodesToExceptions(true);
                if (!StringUtils.isBlank(ldapSearchFilter)) {
                    ((ActiveDirectoryLdapAuthenticationProvider) ldapAuthenticationProvider).setSearchFilter(ldapSearchFilter);
                }
            }

            return ldapAuthenticationProvider;
        }
    }
}
