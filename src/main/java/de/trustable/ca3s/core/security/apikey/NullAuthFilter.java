package de.trustable.ca3s.core.security.apikey;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

public class NullAuthFilter extends AbstractPreAuthenticatedProcessingFilter {


    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
