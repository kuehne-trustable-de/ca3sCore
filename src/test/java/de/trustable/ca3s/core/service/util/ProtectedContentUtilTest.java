package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ProtectedContentUtilTest {

    static ProtectedContentUtil subject;

    @BeforeAll
    static void setUp() {
        subject = new ProtectedContentUtil(
            mock(ProtectedContentRepository.class),
            mock(CertificateRepository.class),
            new PasswordMasker(4),
            "mJvR25yt4NHTIqe5Hz7nUHhQNUuM",
            "S3cr3t#s3cr3t$s3cr3t",
            "ca3sSalt",
            4567,
            "PBKDF2WithHmacSHA256");

    }

    @Test
    void deriveSecret() throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] sharedSecretBytes = subject.deriveSecret("S3cr3t!S");
        String sharedSecretString = Base64.getEncoder().encodeToString(sharedSecretBytes);

        assertEquals("Bm9rujt6U/jym7/lSb1RF1j1FyRXCDeh4WHHczmPSK0=", sharedSecretString);
    }

    @Test
    void deriveSecretRepeatable() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String sampleText = "ezrutizunljkhjgfknjbres54576";
        String protectedContent = Base64.getEncoder().encodeToString(subject.deriveSecret(sampleText));
        for( int i = 0; i< 1000; i++) {
            Base64.getEncoder().encodeToString(subject.deriveSecret(sampleText));
        }
        assertEquals(protectedContent, Base64.getEncoder().encodeToString(subject.deriveSecret(sampleText)));
    }
}
