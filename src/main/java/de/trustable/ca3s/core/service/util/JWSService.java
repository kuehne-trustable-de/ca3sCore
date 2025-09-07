package de.trustable.ca3s.core.service.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JWSService {


    private static final Logger log = LoggerFactory.getLogger(JWSService.class);

    private final ProtectedContentUtil protectedContentUtil;
    private final UserUtil userUtil;

    public JWSService(ProtectedContentUtil protectedContentUtil, UserUtil userUtil) {
        this.protectedContentUtil = protectedContentUtil;
        this.userUtil = userUtil;
    }

    /**
     *
     * @param jwsAsString
     * @return
     * @throws JOSEException
     * @throws ParseException
     * @throws GeneralSecurityException
     */
    public String getJWSPayload(String jwsAsString)
        throws JOSEException, ParseException, GeneralSecurityException {

        // To parse the JWS and verify it, e.g. on client-side
        JWSObject jwsObject = JWSObject.parse(jwsAsString);
        String requestProxyIdString = jwsObject.getHeader().getKeyID();
        if( !requestProxyIdString.startsWith("rid-")){
            throw new JOSEException("unrecognized key id '"+requestProxyIdString+"' in JWS");
        }

        int requestProxyId;
        try{
            requestProxyId = Integer.parseInt(requestProxyIdString.substring(4));
        }catch(NumberFormatException nfe){
            throw new JOSEException("invalid key id '"+requestProxyIdString+"' in JWS");
        }

        List<ProtectedContent> protectedContents =
            protectedContentUtil.retrieveProtectedContent(ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, requestProxyId);

        log.debug("checking #{} connection secrets for request proxy '{}'", protectedContents.size(), requestProxyId);
//        log.debug("NOT FOR PRODUCTION: calculated secret as " + java.util.Base64.getEncoder().encodeToString(sharedSecret));

        for( ProtectedContent protectedContent: protectedContents) {

            String secret = protectedContentUtil.unprotectString(protectedContent.getContentBase64());
            log.debug("NOT FOR PRODUCTION: iterating stored secret: " + secret);

            byte[] derivedSecret = protectedContentUtil.deriveSecret(secret);
            log.debug("NOT FOR PRODUCTION: calculated secret as " + Base64.getEncoder().encodeToString(derivedSecret));

            JWSVerifier verifier = new MACVerifier(derivedSecret);
            if (jwsObject.verify(verifier)) {
                return jwsObject.getPayload().toString();
            } else {
                log.debug("jws '{}' failed (trial) verification", jwsAsString);
            }
        }
        throw new JOSEException("verification of JWS failed for request proxy '" + requestProxyId + "'");
    }

    public void checkJWT(String jwtAsString, final int intendedId)
        throws JOSEException, ParseException, GeneralSecurityException {

        // To parse the JWS and verify it, e.g. on client-side
        SignedJWT signedJWT = SignedJWT.parse(jwtAsString);

        if( signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())){
            log.debug("jwt '{}' failed verification, already expired (exp: {})", jwtAsString, signedJWT.getJWTClaimsSet().getExpirationTime() );
            throw new JOSEException("verification of JWT failed, already expired (exp: " + signedJWT.getJWTClaimsSet().getExpirationTime() + ")");
        }

        String requestProxyIdString = signedJWT.getHeader().getKeyID();
        if( !requestProxyIdString.startsWith("rid-")){
            throw new JOSEException("unrecognized key id '"+requestProxyIdString+"' in JWS");
        }

        int requestProxyId;
        try{
            requestProxyId = Integer.parseInt(requestProxyIdString.substring(4));
        }catch(NumberFormatException nfe){
            throw new JOSEException("invalid key id '"+requestProxyIdString+"' in JWS");
        }

        if ( requestProxyId != intendedId){
            throw new JOSEException("invalid key id '"+requestProxyIdString+"' / '" + intendedId + "'" );
        }

        List<ProtectedContent> protectedContents =
            protectedContentUtil.retrieveProtectedContent(ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, requestProxyId);

        log.debug("checking #{} connection secrets for request proxy '{}'", protectedContents.size(), requestProxyId);
//        log.debug("NOT FOR PRODUCTION: calculated secret as " + java.util.Base64.getEncoder().encodeToString(sharedSecret));

        for( ProtectedContent protectedContent: protectedContents) {

            String secret = protectedContentUtil.unprotectString(protectedContent.getContentBase64());
            log.debug("NOT FOR PRODUCTION: stored secret: " + secret);

            byte[] derivedSecret = protectedContentUtil.deriveSecret(secret);
            log.debug("NOT FOR PRODUCTION: calculated secret as " + Base64.getEncoder().encodeToString(derivedSecret));

            JWSVerifier verifier = new MACVerifier(derivedSecret);
            if (signedJWT.verify(verifier)) {
                return;
            } else {
                log.debug("jwt '{}' failed verification", jwtAsString);
            }
        }
        throw new JOSEException("verification of JWS failed for request proxy '" + requestProxyId + "'");
    }

    public JWSObject getJWSObject(Map<String, String> partMap) throws ParseException {
        Base64URL protectedPart = new Base64URL(partMap.get("protected"));
        Base64URL signaturePart = new Base64URL(partMap.get("signature"));
        String payloadDecoded = new String(Base64.getUrlDecoder().decode(partMap.get("payload")));
        return new JWSObject(protectedPart, new Payload(payloadDecoded), signaturePart);
    }

    public User verifyEABGetUser(JWSObject jwsObject, String login) throws ParseException, JOSEException {

        User user;
        try {
            user = userUtil.getUserByLogin(login);
        }catch( RuntimeException runtimeException){
            throw new JOSEException("User with login '"+login+"' not found");
        }

        List<ProtectedContent> protectedContents = protectedContentUtil.retrieveProtectedContent(
            ProtectedContentType.TOKEN,
            ContentRelationType.EAB_PASSWORD,
            user.getId());

        for( ProtectedContent protectedContent: protectedContents) {
            String secretBase64 = protectedContentUtil.unprotectString(protectedContent.getContentBase64());
            JWSVerifier verifier = new MACVerifier(Base64.getUrlDecoder().decode(secretBase64));
            if (jwsObject.verify(verifier)) {
                log.debug("user '{}' succeeded eab verification", login);
                return user;
            } else {
                log.debug("protectedContent #{} failed eab verification",  protectedContent.getId());
            }
        }
        throw new JOSEException("verification of eab failed");
    }
    public static void main(String[] args) throws ParseException, JOSEException {

        JWSObject jwsObject = new JWSObject(new Base64URL("eyJ1cmwiOiJodHRwOi8vbG9jYWxob3N0OjU2NTM3L2FjbWUvYWNtZVRlc3RFYWIvbmV3QWNjb3VudCIsImtpZCI6InVzZXIiLCJhbGciOiJIUzI1NiJ9"),
            new Base64URL("eyJrdHkiOiJSU0EiLCJuIjoidDA3UFdBN1d2QlMyNU92aVBlOExpTElROVk4Tk5YbWYzQ0g5V0xGejFpcDM4RW1zMkNtY1IzSjdxMFh5Ujh1OEN1M19WWDQxS1JnMGZNUUlyOW4zRjVDSWxjUWN0UHdjVXVrWTdjNS1wTVhMbW9vLVpZSFVucGozLUlpb0NtRDc1bEQ4bDhQdHBCd3NKdFZPSThaMUtmWFdhT2pzelVaejh2S1dNVGpNTHlSS1RVWWtwcC1fbkRwY0pQWm82Tkg4MnVMZlRxQXJNQUxTaW1MYXhlelA1THp2dTJnVS05bko5MUxiM3B4QzR6cTVScE5yZmx0dEYzYXRPQ3lOMk1CSUhneDFraEN6MXROaTlQc0NsTzJndkwxU0VmaWxSQjdTS0tsV1V2Q0JHOC1jNmprU1ZWLVhjZ0g0WXcyOWJvbHpoZlF0ZlExZThWOWg3Z0FRT0JWWUNRIiwiZSI6IkFRQUIifQ"),
            new Base64URL("6piHXqLLZ_b1viqHjnBjBprDvBzNM016UXt-sZK2mrg"));

        JWSVerifier verifier = new MACVerifier(Base64.getUrlDecoder().decode("_oLvEiAxPWdeyQcEZlverHeu9hcdsi--ohdgnIJTZy0="));
        System.out.println("verify: " +jwsObject.verify(verifier));
        System.out.println("getParsedString: " +jwsObject.getParsedString());

        }
}
