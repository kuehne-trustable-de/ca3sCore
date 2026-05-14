package de.trustable.ca3s.core;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;
import de.trustable.ca3s.core.config.ApplicationProperties;
import de.trustable.ca3s.core.config.DefaultProfileUtil;

import de.trustable.ca3s.core.config.EndpointConfig;
import de.trustable.ca3s.core.config.EndpointConfigs;
import de.trustable.ca3s.core.security.provider.*;
import de.trustable.ca3s.core.service.KeyGenerationService;
import de.trustable.util.JCAManager;
import io.undertow.Undertow;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.xnio.Options;
import org.xnio.SslClientAuthMode;
import tech.jhipster.config.JHipsterConstants;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class Ca3SApp implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Ca3SApp.class);

    public static final String HTTPS_CERTIFICATE_DN_SUFFIX = "ca3s.https.certificate.dnSuffix";
    public static final String HTTPS_CERTIFICATE_FALLBACK_VALIDITY_HOURS = "ca3s.https.certificate.fallback.validityHours";
    public static final String O_TRUSTABLE_SOLUTIONS_C_DE = "O=trustable solutions, C=DE";

    private final Environment env;

    @Autowired
    KeyGenerationService keyGenerationService;

    @Autowired
    Ca3sTrustManager ca3sTrustManager;

    @Autowired
    Ca3sClientCertTrustManager ca3sClientCertTrustManager;

    @Autowired
    EndpointConfigs endpointConfigs;

    public Ca3SApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes ca3s.
     * <p>
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @Override
    public void afterPropertiesSet() {
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
        app.addListeners(new PropertiesLogger());
        ApplicationContext ctx = app.run(args);

        logApplicationStartup(ctx);
    }

    private static void logApplicationStartup(ApplicationContext ctx) {
/*
        if( log.isDebugEnabled()) {
            String[] allBeanNames = ctx.getBeanDefinitionNames();
            for (String beanName : allBeanNames) {
                log.debug("bean name: " + beanName);
            }
        }
*/

        Environment env = ctx.getEnvironment();
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

    @Bean
    public TimedRenewalCertMapHolder registerJCEProvider() {
        Security.addProvider(new BouncyCastlePQCProvider());
        JCAManager.getInstance();

        String dnSuffix = env.getProperty(HTTPS_CERTIFICATE_DN_SUFFIX, O_TRUSTABLE_SOLUTIONS_C_DE);
        int fallbackCertValidity = 1;
        try {
            fallbackCertValidity = Integer.parseInt(env.getProperty(HTTPS_CERTIFICATE_FALLBACK_VALIDITY_HOURS, "1"));
        } catch (NumberFormatException e) {
            log.warn("Value of 'ca3s.https.certificate.fallback.validityHours' not parseable, using 1 hour" );
        }

        TimedRenewalCertMap certMap = new TimedRenewalCertMap(null, new Ca3sFallbackBundleFactory(dnSuffix, fallbackCertValidity, keyGenerationService));
        Security.addProvider(new Ca3sKeyStoreProvider(certMap, "ca3s"));
        Security.addProvider(new Ca3sKeyManagerProvider(certMap));

        TimedRenewalCertMapHolder trcmh = new TimedRenewalCertMapHolder();
        trcmh.setCertMap(certMap);

        log.info("JCAManager and Provider initialized ..." );

        List<String> algorithms = Arrays.stream(Security.getProviders())
            .flatMap(provider -> provider.getServices().stream())
            .filter(service -> "Cipher".equals(service.getType()) )
            .map(Provider.Service::getAlgorithm)
            .collect(Collectors.toList());

        algorithms.forEach(algorithm -> {
            log.debug("Algorithm: {}", algorithm);
        });
        return trcmh;

    }

    @Bean
    public UndertowServletWebServerFactory embeddedServletContainerFactory() {

        registerJCEProvider();

        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();

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

                    TrustManager[] trustManagers = {ca3sClientCertTrustManager};
                    SSLContext sslContextClientAuth;
                    sslContextClientAuth = SSLContext.getInstance("TLS");
                    sslContextClientAuth.init(keyManagers, trustManagers, null);

                    for( EndpointConfig epc : endpointConfigs.getPortConfigMap().values()) {

                        if( epc.isHttps()) {
                            if( epc.isClientAuth()) {
                                builder.setSocketOption(Options.SSL_CLIENT_AUTH_MODE, SslClientAuthMode.REQUESTED);
                                builder.addHttpsListener(epc.getPort(), epc.getBindingHost(), sslContextClientAuth);
                                log.debug("added TLS client auth listen port {} for {}", epc.getPort(), epc.getUsageDescription());
                            }else{
                                builder.addHttpsListener(epc.getPort(), epc.getBindingHost(), sslContext);
                                log.debug("added TLS listen port {} for {}", epc.getPort(), epc.getUsageDescription());
                            }
                        } else {
                            log.debug("added plain text listen port {} for {}", epc.getPort(), epc.getUsageDescription());
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
}
