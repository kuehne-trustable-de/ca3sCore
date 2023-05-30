package de.trustable.ca3s.core.service.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import de.trustable.ca3s.core.domain.ProtectedContent;
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

@Service
public class JWSService {


    private static final Logger log = LoggerFactory.getLogger(JWSService.class);

    private final ProtectedContentUtil protectedContentUtil;

    public JWSService(ProtectedContentUtil protectedContentUtil) {

        this.protectedContentUtil = protectedContentUtil;
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

}
