package de.trustable.ca3s.core.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_ACME_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_ADMIN_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_EST_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_RA_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_SCEP_PREFIX;
import static de.trustable.ca3s.core.config.EndpointConfigs.SERVER_TLS_PREFIX;

public class AccessPortTestManager {

    private static final Logger LOG = LoggerFactory.getLogger(AccessPortTestManager.class);

    private int tlsAccessPort;
    private int adminAccessPort;
    private int raAccessPort;
    private int acmeAccessPort;
    private int scepAccessPort;
    private int estAccessPort;

    public AccessPortTestManager() {
        try {
            tlsAccessPort = getFreePort();
            adminAccessPort = getFreePort();
            raAccessPort = getFreePort();
            acmeAccessPort = getFreePort();
            scepAccessPort = getFreePort();
            estAccessPort = getFreePort();
        }catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setUpEnvironment() {

        System.setProperty(SERVER_TLS_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_ADMIN_PREFIX + "port", "" + adminAccessPort);
        System.setProperty(SERVER_RA_PREFIX + "port", "" + raAccessPort);
        System.setProperty(SERVER_ACME_PREFIX + "port", "" + acmeAccessPort);
        System.setProperty(SERVER_SCEP_PREFIX + "port", "" + scepAccessPort);
        System.setProperty(SERVER_EST_PREFIX + "port", "" + estAccessPort);

        System.setProperty(SERVER_TLS_PREFIX + "https", "false");
        System.setProperty(SERVER_ADMIN_PREFIX + "https", "false");
        System.setProperty(SERVER_RA_PREFIX + "https", "false");
        System.setProperty(SERVER_ACME_PREFIX + "https", "false");
        System.setProperty(SERVER_SCEP_PREFIX + "https", "false");
        System.setProperty(SERVER_EST_PREFIX + "https", "false");

    }

    public void setUpEnvironmentSinglePort() {

        System.setProperty(SERVER_TLS_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_ADMIN_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_RA_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_ACME_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_SCEP_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(SERVER_EST_PREFIX + "port", "" + tlsAccessPort);

        System.setProperty(SERVER_TLS_PREFIX + "https", "false");
        System.setProperty(SERVER_ADMIN_PREFIX + "https", "false");
        System.setProperty(SERVER_RA_PREFIX + "https", "false");
        System.setProperty(SERVER_ACME_PREFIX + "https", "false");
        System.setProperty(SERVER_SCEP_PREFIX + "https", "false");
        System.setProperty(SERVER_EST_PREFIX + "https", "false");

    }

    public void tearDownEnvironment(){
        System.clearProperty(SERVER_TLS_PREFIX + "port");
        System.clearProperty(SERVER_ADMIN_PREFIX + "port");
        System.clearProperty(SERVER_RA_PREFIX + "port");
        System.clearProperty(SERVER_ACME_PREFIX + "port");
        System.clearProperty(SERVER_SCEP_PREFIX + "port");
        System.clearProperty(SERVER_EST_PREFIX + "port");

        System.clearProperty(SERVER_TLS_PREFIX + "https");
        System.clearProperty(SERVER_ADMIN_PREFIX + "https");
        System.clearProperty(SERVER_RA_PREFIX + "https");
        System.clearProperty(SERVER_ACME_PREFIX + "https");
        System.clearProperty(SERVER_SCEP_PREFIX + "https");
        System.clearProperty(SERVER_EST_PREFIX + "https");
    }

    public int getTlsAccessPort() {
        return tlsAccessPort;
    }

    public int getAdminAccessPort() {
        return adminAccessPort;
    }

    public int getRaAccessPort() {
        return raAccessPort;
    }

    public int getAcmeAccessPort() {
        return acmeAccessPort;
    }

    public int getScepAccessPort() {
        return scepAccessPort;
    }

    public int getEstAccessPort() {
        return estAccessPort;
    }

    static int getFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            LOG.warn("Could not find any ports", e);
            throw e;
        }
    }
}
