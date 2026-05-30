package de.trustable.ca3s.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;

@Configuration
public class EndpointConfigs {

    public static final String SERVER_TLS_PREFIX = "ca3s.tlsAccess.";
    public static final String SERVER_TLS_CLIENT_AUTH_PREFIX = "ca3s.tlsClientAuth.";
    public static final String SERVER_ADMIN_PREFIX = "ca3s.adminAccess.";
    public static final String SERVER_RA_PREFIX = "ca3s.raAccess.";
    public static final String SERVER_ACME_PREFIX = "ca3s.acmeAccess.";
    public static final String SERVER_SCEP_PREFIX = "ca3s.scepAccess.";
    public static final String SERVER_EST_PREFIX = "ca3s.estAccess.";

    public static final String DEFAULT_BINDING_HOST = "0.0.0.0";

    private final EndpointConfig tlsEndpointConfig;
    private final EndpointConfig clientAuthEndpointConfig;
    private final EndpointConfig raEndpointConfig;
    private final EndpointConfig adminEndpointConfig;

    private final EndpointConfig estEndpointConfig;
    private final EndpointConfig scepEndpointConfig;
    private final EndpointConfig acmeEndpointConfig;

    private static final Logger log = LoggerFactory.getLogger(EndpointConfigs.class);

    public EndpointConfigs(Environment env){

        int httpsClientAuthPort = getPortForUsage(env, SERVER_TLS_CLIENT_AUTH_PREFIX, 8442);
        clientAuthEndpointConfig = this.addConfig(httpsClientAuthPort,
            true, true,
            getBindingHostForUsage( env, SERVER_TLS_CLIENT_AUTH_PREFIX, DEFAULT_BINDING_HOST), "TLS Client Auth Port");

        tlsEndpointConfig = this.addConfig(getPortForUsage(env, SERVER_TLS_PREFIX, 8443),
            getHTTPSForUsage(env, SERVER_TLS_PREFIX, true),
            getBindingHostForUsage( env, SERVER_TLS_PREFIX, DEFAULT_BINDING_HOST), "TLS Port");

        adminEndpointConfig = this.addConfig(getPortForUsage(env, SERVER_ADMIN_PREFIX, tlsEndpointConfig.getPort()),
            getHTTPSForUsage(env, SERVER_ADMIN_PREFIX, true),
            getBindingHostForUsage(env, SERVER_ADMIN_PREFIX, tlsEndpointConfig.getBindingHost()), "Admin Port");

        raEndpointConfig = this.addConfig(getPortForUsage(env, SERVER_RA_PREFIX, tlsEndpointConfig.getPort()),
            getHTTPSForUsage(env, SERVER_RA_PREFIX, true),
            getBindingHostForUsage(env, SERVER_RA_PREFIX, tlsEndpointConfig.getBindingHost()),"RA Port");

        acmeEndpointConfig = this.addConfig(getPortForUsage(env, SERVER_ACME_PREFIX, tlsEndpointConfig.getPort()),
            getHTTPSForUsage(env, SERVER_ACME_PREFIX, true),
            getBindingHostForUsage(env, SERVER_ACME_PREFIX, tlsEndpointConfig.getBindingHost()), "ACME Port");

        int httpPort = getPortForUsage(env, "server.", 8080);
        int scepPort = getPortForUsage(env, SERVER_SCEP_PREFIX, 8081);
        if( scepPort != httpPort) {
            scepEndpointConfig = this.addConfig(scepPort,
                getHTTPSForUsage(env, SERVER_SCEP_PREFIX, false),
                getBindingHostForUsage(env, SERVER_SCEP_PREFIX, tlsEndpointConfig.getBindingHost()), "SCEP Port");
        }else{
            scepEndpointConfig = tlsEndpointConfig;
        }

        int estPort = getPortForUsage(env, SERVER_EST_PREFIX, tlsEndpointConfig.getPort());
        if( estPort != httpsClientAuthPort) {
            estEndpointConfig = this.addConfig(estPort,
                getHTTPSForUsage(env, SERVER_EST_PREFIX, true),
                getBindingHostForUsage(env, SERVER_EST_PREFIX, tlsEndpointConfig.getBindingHost()), "EST Port");
        }else{
            estEndpointConfig = tlsEndpointConfig;
        }
    }

    HashMap<Integer, EndpointConfig> portConfigMap = new HashMap<>();

    public EndpointConfig addConfig(int port, boolean isHttps, String bindingHost, String usageDescription) {
        return addConfig( port, isHttps, false, bindingHost, usageDescription);
    }

    public EndpointConfig addConfig(int port, boolean isHttps, boolean isClientAuth, String bindingHost, String usageDescription) {
        if( portConfigMap.containsKey(port)) {
            EndpointConfig existingConfig = portConfigMap.get(port);
            if( existingConfig.isHttps() != isHttps ) {
                log.warn("Https redefinition for port {}, ignoring definition for '{}'", port, usageDescription);
            }
            if( !existingConfig.getBindingHost().equalsIgnoreCase(bindingHost)) {
                log.warn("Binding Host redefinition for port {}, ignoring definition for '{}'", port, usageDescription);
            }

            existingConfig.setUsageDescription(existingConfig.getUsageDescription() + ", " + usageDescription );
            return existingConfig;
        }else {
            EndpointConfig endpointConfig =  new EndpointConfig(port, isHttps, isClientAuth, bindingHost, usageDescription);
            portConfigMap.put(port, endpointConfig);
            return endpointConfig;
        }
    }

    public HashMap<Integer, EndpointConfig> getPortConfigMap() {
        return portConfigMap;
    }


    int getPortForUsage(Environment env, final String usage, int defaultPort) {
        int port = defaultPort;

        String item = usage + "port";
        String envPort = env.getProperty(item);
        if( envPort == null) {
            log.debug("Port for usage '{}' undefined, using default port #{}", item, defaultPort);
        }else {
            port = Integer.parseUnsignedInt(envPort);
        }
        return port;
    }

    boolean getHTTPSForUsage(Environment env, final String usage, boolean defaultHTTPS) {
        boolean isHttps = defaultHTTPS;

        String item = usage + "https";
        String envHttpsUsage = env.getProperty(item);
        if( envHttpsUsage == null) {
            log.debug("Use HTTPS for usage '{}' undefined, using default mode {}", item, defaultHTTPS);
        }else {
            isHttps = Boolean.parseBoolean(envHttpsUsage);
        }
        return isHttps;
    }

    String getBindingHostForUsage(Environment env, final String usage, String defaultBindingHost) {
        String bindingHost = defaultBindingHost;

        String item = usage + "bindingHost";
        String envBindingHost = env.getProperty(item);
        if( envBindingHost == null) {
            log.debug("Binding host for usage '{}' undefined, using default '{}'", item, defaultBindingHost);
        }else {
            bindingHost = envBindingHost;
        }
        return bindingHost;
    }

    public EndpointConfig getTlsEndpointConfig() {
        return tlsEndpointConfig;
    }

    public EndpointConfig getClientAuthEndpointConfig() {
        return clientAuthEndpointConfig;
    }

    public EndpointConfig getRaEndpointConfig() {
        return raEndpointConfig;
    }

    public EndpointConfig getAdminEndpointConfig() {
        return adminEndpointConfig;
    }

    public EndpointConfig getEstEndpointConfig() {
        return estEndpointConfig;
    }

    public EndpointConfig getScepEndpointConfig() {
        return scepEndpointConfig;
    }

    public EndpointConfig getAcmeEndpointConfig() {
        return acmeEndpointConfig;
    }
}
