package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.Ca3SApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ClientAuthConfig {

    private final Logger LOG = LoggerFactory.getLogger(ClientAuthConfig.class);

    @Value("${" + Ca3SApp.SERVER_TLS_CLIENT_AUTH_PREFIX + ".port:8442}")
    int tlsClientAuthPort;

    @Value("${" + Ca3SApp.SERVER_TLS_CLIENT_AUTH_PREFIX + ".external.port:#{null}}")
    Integer tlsClientAuthExternalPort;

    @Value("${" + Ca3SApp.SERVER_TLS_CLIENT_AUTH_PREFIX + ".external.host:#{null}}")
    String tlsClientAuthHost;


    public String getClientAuthTarget(){

        String host = tlsClientAuthHost;
        if (tlsClientAuthHost == null) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                LOG.info("Problem resolving hostname for TLSClientAuth Cors", e);
                host = "localhost";
            }
        }

        String clientAuthTarget;
        if( tlsClientAuthExternalPort == null){
            clientAuthTarget = "https://"+ host + ":" + tlsClientAuthPort;
        }else{
            clientAuthTarget = "https://"+ host + ":" + tlsClientAuthExternalPort;
        }

        LOG.info("ClientAuthTarget : {}", clientAuthTarget );
        return clientAuthTarget;
    }

}
