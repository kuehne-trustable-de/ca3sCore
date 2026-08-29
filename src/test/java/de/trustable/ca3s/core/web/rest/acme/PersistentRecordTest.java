package de.trustable.ca3s.core.web.rest.acme;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersistentRecordTest {

    public static final String SIMPLE_RECORD = "https://acme-staging-v02.api.letsencrypt.org/directory;accounturi=https://acme-staging-v02.api.letsencrypt.org/acme/acct/123456;persistUntil=1699999999;policy=default";
    public static final String SPEC_SAMPLE_RECORD ="authority.example;" +
        " accounturi=https://ca.example/acct/123;" +
        " policy=wildcard;" +
        " persistUntil=1721952000";
    public static final String SPEC_SAMPLE_RECORD_MOD ="Authority.example;    foo=bar;;;" +
        " accounturi=https://ca.example/acct/123;" +
        " accounturi=https://ca.example/acct/124;" +
        " poLicy=wildcard\n; \n\n" +
        "          PersistUntil=1721952000";

    @Test
    void getCaIssuer() {

        PersistentRecord pr = new PersistentRecord(SPEC_SAMPLE_RECORD);
        Assertions.assertEquals("authority.example", pr.getCaIssuer());
        Assertions.assertEquals("https://ca.example/acct/123", pr.getAccountUri());
        Assertions.assertEquals("wildcard", pr.getPolicy());
        Assertions.assertEquals(1721952000L * 1000L, pr.getPersistUntilMilliSec());

        pr = new PersistentRecord(SPEC_SAMPLE_RECORD_MOD);
        Assertions.assertEquals("authority.example", pr.getCaIssuer());
        Assertions.assertEquals("https://ca.example/acct/123", pr.getAccountUri());
        Assertions.assertEquals("wildcard", pr.getPolicy());
        Assertions.assertEquals(1721952000L * 1000L, pr.getPersistUntilMilliSec());

    }
}
