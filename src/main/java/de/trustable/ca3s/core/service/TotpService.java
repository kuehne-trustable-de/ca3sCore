package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TotpService {

    private final Logger log = LoggerFactory.getLogger(TotpService.class);
    private final ProtectedContentUtil protectedContentUtil;
    private final String appName;

    public TotpService(ProtectedContentUtil protectedContentUtil,
                       @Value("${ca3s.app.name:ca3s}") String appName
                       ) {
        this.protectedContentUtil = protectedContentUtil;
        this.appName = appName;
    }

    static final String TOTP_URL_TEMPLATE = "otpauth://totp/%s:%s?secret=%s&issuer=%s";

    public String generateTotpUrlForUser(String userLogin, String base32Seed) {
        // Generate the totp url
        String totpUrl = String.format(TOTP_URL_TEMPLATE, appName,
            userLogin,
            base32Seed,
            appName);
        return totpUrl;
    }

    public void checkOtpToken(final User user, final String otp) {

        try {
            Long.parseLong(otp);
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid format of otp");
        }

        List<ProtectedContent> protectedContents = protectedContentUtil.retrieveProtectedContent(
            ProtectedContentType.SECRET,
            ContentRelationType.OTP_SECRET,
            user.getId());

        for(ProtectedContent protectedContent: protectedContents) {
            String secret = protectedContentUtil.unprotectString(protectedContent.getContentBase64());
            if(verifyOTP(secret,otp)) {
                log.debug("otp matches for user {}", user.getLogin());
                return;
            }
        }
        throw new BadCredentialsException("Invalid otp");
    }

    public static boolean verifyOTP(final String seed, final String otp){
        Totp totp = new Totp(seed);
        return totp.verify(otp);
    }
}
