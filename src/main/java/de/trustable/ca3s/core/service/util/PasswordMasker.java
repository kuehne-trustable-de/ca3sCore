package de.trustable.ca3s.core.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordMasker {

    Logger LOG = LoggerFactory.getLogger(PasswordMasker.class);

    final private int maskCleartextLen;

    public PasswordMasker(@Value("${ca3s.log.password.mask-cleartext-len:0}") int maskCleartextLen) {

        if(maskCleartextLen <= 4) {
            this.maskCleartextLen = maskCleartextLen;
        }else{
            this.maskCleartextLen = 4;
        }
        LOG.info("masking password with {} cleartext chars",this.maskCleartextLen);
    }

    public String maskPassword(String password) {

        final String mask = "************";

        if( password == null || (password.length() < maskCleartextLen + 4)){
            password = mask + "s3cr3t";
        }

        String paddedSecret = mask + password;
        return mask + paddedSecret.substring(paddedSecret.length() - maskCleartextLen);
    }

}
