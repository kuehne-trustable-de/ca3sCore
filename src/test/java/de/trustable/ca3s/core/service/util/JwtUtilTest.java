package de.trustable.ca3s.core.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import de.trustable.ca3s.core.service.dto.AccountCredentialsType;
import de.trustable.ca3s.core.service.dto.TokenResponse;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

class JwtUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtilTest.class);

    JwtUtil  jwtUtil = new JwtUtil("Mjk3NjM4OWI4NWU3ZjE3NjQ3OWRiZjI3OWYwYmZiNWY1NzA2YjAzZTY2ODJhNWM5MjFjYzZmZTZlMDE4YWRhNDg0MjJlNDYzNThhODBjNmU0ZGFjMGY3MTc5OTVlNmEyZWFiZmIwMDIxYTExYzkxNGM3YmM2YmVmMmNlZWE2YmQ=");

    JwtUtilTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
    }

    @Test
    void processCompactJWT() throws MalformedClaimException {
        JwtContext jwtContext =  jwtUtil.processCompactJWT(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTd2lzc1BLSSIsImF1ZCI6IlJFU1QgQVBJIiwic3ViIjoia3VlaG5lQHRydXN0YWJsZS5kZSIsImlhdCI6MTc5MTY0OTE1MCwibmJmIjoxNzkxNjQ5MTUwLCJleHAiOjE3OTE2Nzc5NTB9.Z8fTc_j_gNhGbeM6kDaBxytuADTiCDak8qGFB0F6rwI");
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        LOG.debug( "ExpirationTime : {}", jwtClaims.getExpirationTime());

        Assertions.assertEquals(1791677950L, jwtClaims.getExpirationTime().getValue());
    }

    @Test
    void processJWS() throws JsonProcessingException, JOSEException, GeneralSecurityException, ParseException {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setTokenValue("token");
        tokenResponse.setCredentialType(AccountCredentialsType.API_TOKEN);
        String jws = jwtUtil.signObject(tokenResponse);
        Assertions.assertNotNull(jws);

        LOG.debug("jws : {}", jws);

        TokenResponse response = (TokenResponse)jwtUtil.getJWSPayload( jws, TokenResponse.class);

        LOG.debug("content : {}", response);

    }
}
