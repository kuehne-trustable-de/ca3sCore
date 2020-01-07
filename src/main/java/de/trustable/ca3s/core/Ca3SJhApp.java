package de.trustable.ca3s.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

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
import org.springframework.core.env.Environment;

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

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class Ca3SJhApp implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Ca3SJhApp.class);

	public static TimedRenewalCertMap certMap;

    private final Environment env;

    public Ca3SJhApp(Environment env) {
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
    	
    	System.out.println("####### Starting Ca3SJhApp #######");
    	
		JCAManager.getInstance();

		TimedRenewalCertMap certMap = new TimedRenewalCertMap(null, new Ca3sFallbackBundleFactory());
		Security.addProvider(new Ca3sKeyStoreProvider(certMap, "ca3s"));
    	Security.addProvider(new Ca3sKeyManagerProvider(certMap));
    	new TimedRenewalCertMapHolder().setCertMap(certMap);
    	
        SpringApplication app = new SpringApplication(Ca3SJhApp.class);
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
    public UndertowServletWebServerFactory embeddedServletContainerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        
        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
        	
            @Override
            public void customize(Undertow.Builder builder) {

        		int port = 8442;
        		
            	try {
	                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Ca3sKeyManagerProvider.SERVICE_NAME);
	                
	                
	                KeyStore ks = KeyStore.getInstance("ca3s");
	                ks.load(null, null);
	                
	                keyManagerFactory.init(ks, "password".toCharArray());
	                KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
	
	                SSLContext sslContext;
	                sslContext = SSLContext.getInstance("TLS");
	                sslContext.init(keyManagers, null, null);
	
	                builder.addHttpsListener(port, null, sslContext);
	//                builder.setSocketOption(Options.SSL_CLIENT_AUTH_MODE, SslClientAuthMode.REQUESTED);
	                
	            	log.debug("added TLS listen port {} programmatically", port);
	                
            	} catch(GeneralSecurityException | IOException gse) {
                	log.error("problem configuring TLS port " + port, gse);
            	}
            }
        });
        
        return factory;
    }

}
