package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.acme.AcmeHappyPathIT;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtilTest.class);

    JwtUtil  jwtUtil = new JwtUtil();
    @Test
    void processCompactJWT() throws MalformedClaimException {
        JwtContext jwtContext =  jwtUtil.processCompactJWT(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTd2lzc1BLSSIsImF1ZCI6IlJFU1QgQVBJIiwic3ViIjoia3VlaG5lQHRydXN0YWJsZS5kZSIsImlhdCI6MTc5MTY0OTE1MCwibmJmIjoxNzkxNjQ5MTUwLCJleHAiOjE3OTE2Nzc5NTB9.Z8fTc_j_gNhGbeM6kDaBxytuADTiCDak8qGFB0F6rwI");
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        LOG.debug( "ExpirationTime : {}", jwtClaims.getExpirationTime());

        Assertions.assertEquals(1791677950L, jwtClaims.getExpirationTime().getValue());
    }
}
