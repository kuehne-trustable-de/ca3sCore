package de.trustable.ca3s.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class EndpointConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfig.class);

    int port;
    boolean isHttps;
    boolean isClientAuth;
    String bindingHost;
    String usageDescription;

    public EndpointConfig(int port, boolean isHttps, boolean isClientAuth, String bindingHost, String usageDescription) {
        this.port = port;
        this.isHttps = isHttps;
        this.bindingHost = bindingHost;
        this.usageDescription = usageDescription;
        this.isClientAuth = isClientAuth;
    }

    public int getPort() {
        return port;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public String getBindingHost() {
        return bindingHost;
    }

    public String getUsageDescription() {
        return usageDescription;
    }
    public void setUsageDescription(String usageDescription) {
        this.usageDescription = usageDescription;
    }

    public boolean isClientAuth() {
        return isClientAuth;
    }

    public boolean matchesRequest(HttpServletRequest request) {
        if( request.getServerPort() == getPort() ){
            return true;
        }else{
            LOG.warn("Port {} does not match expected port {}", request.getServerPort(), getPort());
            return false;
        }
    }
}
