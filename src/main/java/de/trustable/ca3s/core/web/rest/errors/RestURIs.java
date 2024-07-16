package de.trustable.ca3s.core.web.rest.errors;

import java.net.URI;

public class RestURIs {
    public static final String TRUSTABLE_ERROR_URI_NAMESPACE = "urn:ietf:params:trustable:error";

    public static final URI USER_BLOCKED = URI.create(TRUSTABLE_ERROR_URI_NAMESPACE + ":userBlocked");
    public static final URI CREDENTIALS_EXPIRED = URI.create(TRUSTABLE_ERROR_URI_NAMESPACE + ":credentialsExpired");

}
