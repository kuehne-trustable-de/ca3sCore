package de.trustable.ca3s.core.web.rest.vm;

import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * View Model object for storing a user's credentials.
 */
public class LoginData {

    @NotNull
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @Size(min = 4, max = 100)
    private String password;

    private boolean rememberMe;

    private String secondSecret;

    private AuthSecondFactor authSecondFactor;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecondSecret() {
        return secondSecret;
    }

    public void setSecondSecret(String secondSecret) {
        this.secondSecret = secondSecret;
    }

    public AuthSecondFactor getAuthSecondFactor() {
        return authSecondFactor;
    }

    public void setAuthSecondFactor(AuthSecondFactor authSecondFactor) {
        this.authSecondFactor = authSecondFactor;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LoginVM{" +
            "username='" + username + '\'' +
            ", rememberMe=" + rememberMe +
            '}';
    }
}
