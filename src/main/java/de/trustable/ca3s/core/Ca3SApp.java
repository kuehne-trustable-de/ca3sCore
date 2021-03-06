package de.trustable.ca3s.core;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;
import de.trustable.ca3s.core.config.ApplicationProperties;
import de.trustable.ca3s.core.config.DefaultProfileUtil;
import de.trustable.ca3s.core.security.provider.Ca3sFallbackBundleFactory;
import de.trustable.ca3s.core.security.provider.Ca3sKeyManagerProvider;
import de.trustable.ca3s.core.security.provider.Ca3sKeyStoreProvider;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.util.JCAManager;
import io.github.jhipster.config.JHipsterConstants;
import io.undertow.Undertow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class Ca3SApp implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Ca3SApp.class);

    public static final String  SERVER_TLS_PREFIX = "ca3s.tlsAccess.";
    public static final String  SERVER_ADMIN_PREFIX = "ca3s.adminAccess.";
    public static final String  SERVER_RA_PREFIX = "ca3s.raAccess.";
    public static final String  SERVER_ACME_PREFIX = "ca3s.acmeAccess.";
    public static final String  SERVER_SCEP_PREFIX = "ca3s.scepAccess.";

    private final Environment env;

    public Ca3SApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes ca3s_jh.
     * <p>
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " +
                "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " +
                "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Ca3SApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}{}\n\t" +
                "External: \t{}://{}:{}{}\n\t" +
                "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            env.getActiveProfiles());
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig
            = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }
/*
    @Bean
    public AnnotatedHibernateEventListenerInvoker annotatedHibernateEventHandlerInvoker() {
        AnnotatedHibernateEventListenerInvoker invoker = new AnnotatedHibernateEventListenerInvoker();
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.prependListeners(EventType.PRE_UPDATE, invoker);
        registry.prependListeners(EventType.PRE_DELETE, invoker);
        registry.prependListeners(EventType.PRE_INSERT, invoker);

        return invoker;
    }

 */
    @Bean
    public TimedRenewalCertMapHolder registerJCEProvider() {
		JCAManager.getInstance();

        String dnSuffix = env.getProperty("ca3s.https.certificate.dnSuffix", "O=trustable solutions, C=DE");

        TimedRenewalCertMap certMap = new TimedRenewalCertMap(null, new Ca3sFallbackBundleFactory(dnSuffix));
		Security.addProvider(new Ca3sKeyStoreProvider(certMap, "ca3s"));
    	Security.addProvider(new Ca3sKeyManagerProvider(certMap));

    	TimedRenewalCertMapHolder trcmh = new TimedRenewalCertMapHolder();
    	trcmh.setCertMap(certMap);

    	log.info("JCAManager and Provider initialized ..." );
        return trcmh;

    }

    @Bean
    public UndertowServletWebServerFactory embeddedServletContainerFactory() {

    	registerJCEProvider();

        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();

        EndpointConfigs endpointConfigs = getEndpointConfigs();


        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {

            @Override
            public void customize(Undertow.Builder builder) {


            	try {
	                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Ca3sKeyManagerProvider.SERVICE_NAME);


	                KeyStore ks = KeyStore.getInstance("ca3s");
	                ks.load(null, null);

	                keyManagerFactory.init(ks, "password".toCharArray());
	                KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

	                SSLContext sslContext;
	                sslContext = SSLContext.getInstance("TLS");
	                sslContext.init(keyManagers, null, null);

	                for( EndpointConfig epc : endpointConfigs.getPortConfigMap().values()) {

	                	if( epc.isHttps()) {
	                		log.debug("added TLS listen port {} for {}", epc.port, epc.getUsage());
	                		builder.addHttpsListener(epc.getPort(), epc.getBindingHost(), sslContext);
	//                builder.setSocketOption(Options.SSL_CLIENT_AUTH_MODE, SslClientAuthMode.REQUESTED);

	                	} else {
	                		log.debug("added plain text listen port {} for {}", epc.port, epc.getUsage());
	                		builder.addHttpListener(epc.getPort(), epc.getBindingHost());
	                	}
	                }
            	} catch(GeneralSecurityException | IOException gse) {
                	log.error("problem configuring listen ports ", gse);
            	}
            }
        });

        return factory;
    }

    EndpointConfigs getEndpointConfigs() {

    	EndpointConfigs epc = new EndpointConfigs();

    	epc.addConfig(getPortForUsage(SERVER_TLS_PREFIX, 8442),
    			getHTTPSForUsage(SERVER_TLS_PREFIX, true),
    			getBindingHostForUsage( SERVER_TLS_PREFIX, "0.0.0.0"), "TLS Port");

    	epc.addConfig(getPortForUsage(SERVER_ADMIN_PREFIX, 8442),
    			getHTTPSForUsage(SERVER_ADMIN_PREFIX, true),
    			getBindingHostForUsage( SERVER_ADMIN_PREFIX, "0.0.0.0"), "Admin Port");

    	epc.addConfig(getPortForUsage(SERVER_RA_PREFIX, 8442),
    			getHTTPSForUsage(SERVER_RA_PREFIX, true),
    			getBindingHostForUsage( SERVER_RA_PREFIX, "0.0.0.0"),"RA Port");

    	epc.addConfig(getPortForUsage(SERVER_ACME_PREFIX, 8442),
    			getHTTPSForUsage(SERVER_ACME_PREFIX, true),
    			getBindingHostForUsage( SERVER_ACME_PREFIX, "0.0.0.0"), "ACME Port");

    	int httpPort = getPortForUsage("server.port", 8080);
    	int scepPort = getPortForUsage(SERVER_SCEP_PREFIX, 8081);
    	if( scepPort != httpPort) {
    		epc.addConfig(scepPort, getHTTPSForUsage(SERVER_SCEP_PREFIX, false), getBindingHostForUsage( SERVER_SCEP_PREFIX, "0.0.0.0"), "SCEP Port");
    	}
    	return epc;
    }

    int getPortForUsage(final String usage, int defaultPort) {
		int port = defaultPort;

		String envPort = env.getProperty( usage + "port");
		if( envPort == null) {
	    	log.debug("Port for usage '{}' undefined, using default port #{}", usage, defaultPort);
		}else {
			port = Integer.parseUnsignedInt(envPort);
		}
		return port;
    }

    boolean getHTTPSForUsage(final String usage, boolean defaultHTTPS) {
		boolean isHttps = defaultHTTPS;

		String envPort = env.getProperty( usage + "https");
		if( envPort == null) {
	    	log.debug("Use HTTPS for usage '{}' undefined, using default mode {}", usage, defaultHTTPS);
		}else {
			isHttps = Boolean.valueOf(envPort);
		}
		return isHttps;
    }

    String getBindingHostForUsage(final String usage, String defaultBindingHost) {
		String bindingHost = defaultBindingHost;

		String envPort = env.getProperty( usage + "bindingHost");
		if( envPort == null) {
	    	log.debug("Binding host for usage '{}' undefined, using default '{}'", usage, defaultBindingHost);
		}else {
			bindingHost = envPort;
		}
		return bindingHost;
    }

}

class EndpointConfigs{

    private static final Logger log = LoggerFactory.getLogger(EndpointConfigs.class);

	HashMap<Integer,EndpointConfig> portConfigMap = new HashMap<>();

	public void addConfig(int port, boolean isHttps, String bindingHost, String usage) {
		if( portConfigMap.containsKey(port)) {
			EndpointConfig existingConfig = portConfigMap.get(port);
			if( existingConfig.isHttps() != isHttps ) {
		    	log.warn("Https redefinition for port {}, ignoring definition for '{}'", port, usage);
			}
			if( !existingConfig.getBindingHost().equalsIgnoreCase(bindingHost)) {
		    	log.warn("Binding Host redefinition for port {}, ignoring definition for '{}'", port, usage);
			}

			existingConfig.usage += ", " + usage;
		}else {
			portConfigMap.put(port, new EndpointConfig(port, isHttps, bindingHost, usage));
		}
	}

	public HashMap<Integer, EndpointConfig> getPortConfigMap() {
		return portConfigMap;
	}

}

class EndpointConfig{

	public EndpointConfig(int port, boolean isHttps, String bindingHost, String usage) {
		this.port = port;
		this.isHttps = isHttps;
		this.bindingHost = bindingHost;
		this.usage = usage;
	}

	int port;
	boolean isHttps;
	String bindingHost;
	String usage;


	public int getPort() {
		return port;
	}


	public boolean isHttps() {
		return isHttps;
	}


	public String getBindingHost() {
		return bindingHost;
	}


	public String getUsage() {
		return usage;
	}


}
