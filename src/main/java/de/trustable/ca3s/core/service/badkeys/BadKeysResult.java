package de.trustable.ca3s.core.service.badkeys;

import java.io.Serializable;

public class BadKeysResult implements Serializable {

    final private boolean valid;
    final private boolean installationValid;
    final private String messsage;

    final Response response;

    public BadKeysResult(Response response) {
        this.installationValid = true;
        this.valid = "valid".equals(response.getResults().getResultType());

        this.messsage = "";

        this.response = response;
    }

    static BadKeysResult validResult(){
        return new BadKeysResult(true, "");
    }

    public BadKeysResult(boolean valid, String messsage) {
        this(true, valid, messsage);
    }

    public BadKeysResult(boolean installationValid, boolean valid, String messsage) {
        this.installationValid = installationValid;
        this.valid = valid;
        this.messsage = messsage;
        this.response = null;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInstallationValid() {
        return installationValid;
    }

    public String getMesssage() {
        return messsage;
    }

    public Response getResponse() {
        return response;
    }
}
