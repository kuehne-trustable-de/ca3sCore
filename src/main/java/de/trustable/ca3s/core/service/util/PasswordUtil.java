package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.exception.PasswordRestrictionMismatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class PasswordUtil {

    Logger LOG = LoggerFactory.getLogger(PasswordUtil.class);

    private final String passwordCheckRegExp;
    private final Pattern passwordCheckPattern;

    public PasswordUtil(final String passwordCheckRegExp){
        this.passwordCheckRegExp = passwordCheckRegExp;
        this.passwordCheckPattern = Pattern.compile(passwordCheckRegExp);
    }

    public void checkPassword(String password) {
        checkPassword(password, "password");
    }

    public void checkPassword(String password, String elementName) {

            if( password != null && passwordCheckPattern.matcher(password).matches() ){
            LOG.debug("password matches restrictions");
        }else{
            throw new PasswordRestrictionMismatch(elementName + " does not match restriction '" + this.passwordCheckRegExp + "'");
        }
    }

    static String maskPassword(String password) {
        final String mask = "******";
        String paddedSecret = mask + password;
        return mask + paddedSecret.substring(paddedSecret.length() - 4);
    }

}
