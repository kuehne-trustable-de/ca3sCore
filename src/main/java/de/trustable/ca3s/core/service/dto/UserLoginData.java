package de.trustable.ca3s.core.service.dto;

public class UserLoginData {

    private String login;
    private String password;
    private boolean rememberMe;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "UserLoginData{" +
            "login='" + login + '\'' +
            ", password='*******'" +
            ", rememberMe=" + rememberMe +
            '}';
    }
}
