package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ProtectedContentUtilTest {

    @Test
    void deriveSecret() throws NoSuchAlgorithmException, InvalidKeySpecException {

        ProtectedContentRepository protContentRepository = mock(ProtectedContentRepository.class);

        ProtectedContentUtil subject = new ProtectedContentUtil(
            protContentRepository,
            "mJvR25yt4NHTIqe5Hz7nUHhQNUuM",
            "S3cr3t#s3cr3t$s3cr3t",
            "ca3sSalt",
            4567,
            "PBKDF2WithHmacSHA256");

        byte[] sharedSecretBytes = subject.deriveSecret("S3cr3t!S");
        String sharedSecretString = Base64.getEncoder().encodeToString(sharedSecretBytes);

        assertEquals( "Bm9rujt6U/jym7/lSb1RF1j1FyRXCDeh4WHHczmPSK0=", sharedSecretString);
    }
}
