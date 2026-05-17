package de.trustable.ca3s.core.ui.helper;

import javax.mail.Folder;

public class EMailInfo {

    private final String username;
    private final String emailAccountPassword;
    private final Folder userFolder;

    public EMailInfo(String username, String emailAccountPassword, Folder userFolder){
        this.username = username;
        this.emailAccountPassword = emailAccountPassword;
        this.userFolder = userFolder;
    }

    public String getUserName() {
        return username;
    }

    public String getEmailAccountPassword() {
        return emailAccountPassword;
    }

    public Folder getUserFolder() {
        return userFolder;
    }

}
