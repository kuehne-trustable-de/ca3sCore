package de.trustable.ca3s.core.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordMaskerTest {

    PasswordMasker passwordMasker0 = new PasswordMasker(0);
    PasswordMasker passwordMasker1 = new PasswordMasker(1);
    PasswordMasker passwordMasker2 = new PasswordMasker(2);
    PasswordMasker passwordMasker3 = new PasswordMasker(3);
    PasswordMasker passwordMasker4 = new PasswordMasker(4);
    PasswordMasker passwordMasker5 = new PasswordMasker(5);


    @Test
    void maskPassword() {

        String password = "abc";
        assertEquals("************", passwordMasker0.maskPassword(password));
        assertEquals("************t", passwordMasker1.maskPassword(password));
        assertEquals("************3t", passwordMasker2.maskPassword(password));
        assertEquals("************r3t", passwordMasker3.maskPassword(password));
        assertEquals("************cr3t", passwordMasker4.maskPassword(password));
        assertEquals("************cr3t", passwordMasker5.maskPassword(password));

        password = "abc1";
        assertEquals("************", passwordMasker0.maskPassword(password));

        assertEquals("************t", passwordMasker1.maskPassword(password));
        assertEquals("************3t", passwordMasker2.maskPassword(password));
        assertEquals("************r3t", passwordMasker3.maskPassword(password));
        assertEquals("************cr3t", passwordMasker4.maskPassword(password));
        assertEquals("************cr3t", passwordMasker5.maskPassword(password));

        password = "abc123#$§%OAU";
        assertEquals("************", passwordMasker0.maskPassword(password));

        assertEquals("************U", passwordMasker1.maskPassword(password));
        assertEquals("************AU", passwordMasker2.maskPassword(password));
        assertEquals("************OAU", passwordMasker3.maskPassword(password));
        assertEquals("************%OAU", passwordMasker4.maskPassword(password));
        assertEquals("************%OAU", passwordMasker5.maskPassword(password));

        password = "abc123#$§_-/%OAU";
        assertEquals("************", passwordMasker0.maskPassword(password));

        assertEquals("************U", passwordMasker1.maskPassword(password));
        assertEquals("************AU", passwordMasker2.maskPassword(password));
        assertEquals("************OAU", passwordMasker3.maskPassword(password));
        assertEquals("************%OAU", passwordMasker4.maskPassword(password));
        assertEquals("************%OAU", passwordMasker5.maskPassword(password));

    }
}
