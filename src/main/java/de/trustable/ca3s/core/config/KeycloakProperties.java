package de.trustable.ca3s.core.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak", ignoreUnknownFields = false)
public class KeycloakProperties extends KeycloakSpringBootProperties {

}
