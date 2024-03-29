package de.trustable.ca3s.core.service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyAlgoLengthTest {

    @Test
    void from() {
        KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa-2048");
        assertEquals(2048, keyAlgoLength.getKeyLength());
        assertEquals("rsa", keyAlgoLength.getAlgoName());

        keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa_2048");
        assertEquals(2048, keyAlgoLength.getKeyLength());
        assertEquals("rsa", keyAlgoLength.getAlgoName());

    }

    @Test
    void testToString() {
        KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from( "rsa-2048");
        assertEquals("rsa-2048", keyAlgoLength.toString());
    }
}
