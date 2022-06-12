package de.trustable.ca3s.core.service.badkeys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BadKeysServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(BadKeysServiceTest.class);

    BadKeysService subject;

    @BeforeEach
    public void init() {
        subject = new BadKeysService("./badkeys-cli", new File("/var/opt/badkeys"));
    }

    @Test
    void checkBadkeysInstallation() {
        if(!subject.isInstalled()){return;}

        assertTrue(subject.getAvailableChecks().size() > 5, "more than 5 checks expected to available in badkeys");
        BadKeysService subjectBroken = new BadKeysService("./badkeys-cli", new File("/unavail/badkeys"));
        BadKeysResult badKeysResult = subjectBroken.checkCSR(validCertificate);
        assertNotNull(badKeysResult);
        assertFalse(badKeysResult.isInstallationValid(), "should not be valid on broken installation");
    }

    @Test
    void checkBrokenInstallation() {
        // try invalid installation
        subject = new BadKeysService("./xxx", new File("/etc"));
        assertFalse(subject.isInstalled(), "expected to detect broken installation ");
    }

    @Test
    void checkValidCertificate() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(validCertificate);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertTrue(badKeysResult.isValid());
    }

    @Test
    void checkROCAKey() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(rocaKey);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("roca", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkEC_P256() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(ec_p256);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("blocklist", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkDebianweak() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(debianweak);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("blocklist", badKeysResult.getResponse().getResults().getResultType());
    }

    @Test
    void checkEd25519() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(Ed25519);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("blocklist", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkRSA_E1() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(rsa_e1);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("rsainvalid", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkRSA_Fermat() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(rsa_fermat);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("fermat", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkRSA_Prime_N() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(rsa_prime_n);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("rsainvalid", badKeysResult.getResponse().getResults().getResultType() );
    }

    @Test
    void checkRSA_Pattern() {
        if(!subject.isInstalled()){return;}
        BadKeysResult badKeysResult = subject.checkCSR(rsa_pattern);
        assertNotNull(badKeysResult);
        assertTrue(badKeysResult.isInstallationValid());
        assertFalse(badKeysResult.isValid());
        assertEquals("pattern", badKeysResult.getResponse().getResults().getResultType());
    }

    static final String rsa_pattern = "-----BEGIN RSA PUBLIC KEY-----\n" +
        "    MIIBCgKCAQEAqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
        "    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
        "    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABHMgAAAAAAAA\n" +
        "    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
        "    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
        "    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAATjsQIDAQAB\n" +
        "-----END RSA PUBLIC KEY-----";

    static final String rsa_prime_n = "-----BEGIN RSA PUBLIC KEY-----\n" +
        "MIIBCgKCAQEAqQSg27883tGr5jtyOaZkEn597cuw1Wz4wWuFp1quvHOyiMId7L7m\n" +
        "KHh2G+WQaEEBKl2A/M/tXgdfbrY0NnW3SMIZ9PMTWJNjtAqjBKVBDXDJbJhOpvya\n" +
        "gL4HBKR6cnB0TE+3m0co6o98xRT7eFBP4V9WyZYIG15XDruFvGkgeqmXefqf5BB5\n" +
        "Erquu6RePYNt25I3SFM12kZTW+HcrDyj34CO4Jxkw5JI5bUtP9wV5ocr/Z5FmvmI\n" +
        "Di3eNbHBVteLN3BIuFax8JQvpcdwEjy7Qdro5Ad3a3Ld4//2Vn/mAkGPop/HmJme\n" +
        "wI1poiKh+VgF87bloijO+izBYk/eo9ZWWQIDAQAB\n" +
        "-----END RSA PUBLIC KEY-----";

    static final String rsa_fermat = "-----BEGIN CERTIFICATE REQUEST-----\n" +
        "MIICVzCCAT8CAQAwEjEQMA4GA1UEAwwHYmFka2V5czCCASIwDQYJKoZIhvcNAQEB\n" +
        "BQADggEPADCCAQoCggEBAJ6H4O2NM+wXV7wYYdf9eoTdu27Qve1Mj3CUJE0wSRv2\n" +
        "kuhF/d8KOtf5FNe4yFeuEPxIRoZvKFBbSRqmO9WwptWrj/Ni7Xvcn1lumiy2W3la\n" +
        "0ZK9YVxdkBEwtc0pUlupbbuzQ1d1YR1GfkXB21zY5bzY0AopytwltsxYUXTjY0on\n" +
        "NrVX+jyqeItDNqgpsOl9ysOde3ndXwQ1+af4KW6KxG2sV03cHRFjTmPNQKHVpj+a\n" +
        "z4JeR8d/2tL7WDnVq/FoCC9ScP0pBHk6Nkf2iLsRRxPMJANCjQCNFb7CtahjD12D\n" +
        "fiD1rHfoLKSoFW37gOo2peCV65gTHYLrLTiwUXTzLrECAwEAAaAAMA0GCSqGSIb3\n" +
        "DQEBCwUAA4IBAQBsil390u1Spbmmad6AHCsAe94fzrsyyzXAoyyVhWaQYcPenXS+\n" +
        "fqz7HNReq9dkrQ/s5bCgTnxVAZ4L8rq9wkTmrrbh+qOoO+K1tS9UNGLq0u+iAKjv\n" +
        "j9Gw4Zkx9SuOs9/UM/xTmJvR25yt4NHTHz7nUHhQNUuMSUHmFFAGT5iA+xIkmBJw\n" +
        "7Z8JU+DGiBul7PR8TkDbeaGi0KuzT0r+gNz7eIqe5yFS8etjL+1IWrDVLeMBYbSF\n" +
        "uvKY1TgRZg6tj5sLFmCn1pbRYOjC/E/iO6THGawbwfFm9VamiN4ZRHGGBaQDleWb\n" +
        "LvwMht+MFSn6WFYQxZ+thq/8O++GVqg21/7o\n" +
        "-----END CERTIFICATE REQUEST-----";

    static final String rsa_e1 = "-----BEGIN RSA PUBLIC KEY-----\n" +
        "MIIBCAKCAQEA2CzMh5UOtdozYHY5rIe0NKE0dDxuEAyuZBsB6kwoMZd0x9gnbN18\n" +
        "K0eepXnFF05VxNNXRBt90Giy6E0iE/x9FOTNB6a71leCBgVCseAVgyEGbuXotSwg\n" +
        "zZkV1jpL81v4bkqLycu9cqaThJUBwxWelSW63eK3izrLrV0tfbjdDO64HZcGQ/VL\n" +
        "jmCgavVFrs3XiiAZAfOb2uLC4NYfZvej/Se0fC/kWlitCvXi/S73LkXfWD4HukfP\n" +
        "RZ61hWAEfQOhflqhGm1/ty5tNK4cXCvH0cXUz6L8YTUgxxlgW1LO508SjzCQLkgB\n" +
        "XSUqHimDSVLugBG2B96P9dmzM4SKk1qWGQIBAQ==\n" +
        "-----END RSA PUBLIC KEY-----";

    static final String debianweak = "-----BEGIN RSA PUBLIC KEY-----\n" +
        "MIIBCgKCAQEAwJZTDExKND/DiP+LbhTIi2F0hZZt0PdX897LLwPf3+b1GOCUj1OH\n" +
        "BZvVqhJPJtOPE53W68I0NgVhaJdY6bFOA/cUUIFnN0y/ZOJOJsPNle1aXQTjxAS+\n" +
        "FXu4CQ6a2pzcU+9+gGwed7XxAkIVCiTprfmRCI2vIKdb61S8kf5D3YdVRH/Tq977\n" +
        "nxyYeosEGYJFBOIT+N0mqca37S8hA9hCJyD3p0AM40dD5M5ARAxpAT7+oqOXkPzf\n" +
        "zLtCTaHYJK3+WAce121Br4NuQJPqYPVxniUPohT4YxFTqB7vwX2C4/gZ2ldpHtlg\n" +
        "JVAHT96nOsnlz+EPa5GtwxtALD43CwOlWQIDAQAB\n" +
        "-----END RSA PUBLIC KEY-----";

    static final String Ed25519 = "-----BEGIN PUBLIC KEY-----\n" +
        "MCowBQYDK2VwAyEAGb9ECWmEzf6FQbrBZ9w7lshQhqowtrbLDFw4rXAxZuE=\n" +
        "-----END PUBLIC KEY-----";

    static final String ec_p256 = "-----BEGIN PUBLIC KEY-----\n" +
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmzGM1VsO+3IqbMF54rQMaYKQftO4\n" +
        "hUYm9wv5wutLgEd9FsiTy3+4+Wa2O7pffOXPC0QzO+yD8hGEXGP/2mZo6w==\n" +
        "-----END PUBLIC KEY-----";

    static final String rocaKey = "-----BEGIN RSA PUBLIC KEY-----\n" +
        "MIIBCgKCAQEAkTKtIR/MDftNrkgyashkwifks5Swm9HEkw4ZcCijyQdpTStInk9p\n" +
        "dUrq4zjG6tk5xX9AaYXLu47fmiBVzClGvAXTZbpn6JbG1IERUs+i0pBRT5CzUu7v\n" +
        "J/63L72NzVvJFaT0SUE5zByhCjJE+pflMnX1e1UptOdnN5AHeY4RKkugVse3XO2x\n" +
        "Ms3yH3vTyZdtnaOIFRU0d6E7PrsgRtEAqFHcAGmPUw0ZOW8A++8sS/FlnrxMf9lV\n" +
        "a1/8k/A8otPfrNIipfAoq0MKJ+oxE2wML1DDxIGTGmNaN3gPANJyeuca7ptA+NGq\n" +
        "5oU1x3pWMXgSvrVSXUooNk8xIkrKxSz3LQIDAQAB\n" +
        "-----END RSA PUBLIC KEY-----";

    static final String validCertificate = "-----BEGIN CERTIFICATE-----\n" +
        "MIIHVTCCBT2gAwIBAgITZgAAB5YiPUi+zZzZMwAAAAAHljANBgkqhkiG9w0BAQsF\n" +
        "ADBOMQswCQYDVQQGEwJERTEcMBoGA1UEChMTVHJ1c3RhYmxlIFNvbHV0aW9uczEN\n" +
        "MAsGA1UECxMEQ0EzUzESMBAGA1UEAxMJQ0ktU1VCLUNBMB4XDTIyMDQxMTE2NDYx\n" +
        "M1oXDTI0MDQxMDE2NDYxM1owGTEXMBUGA1UEAxMOaG9zdC5kZXZlbnYuZGUwggIi\n" +
        "MA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCA3RwxxIc3tn3pm84wJep7QvJA\n" +
        "bByDxBcEUD576mTdNnTEsAle6PZ0U+/JrCSWtONCPSN0qmpFMp7H7hKEMIoTRQET\n" +
        "HTjuXlQaNbgZEPsvlLoOZKudPiEm1o232yGiLDFvclI3ocTFr3dyUIDUNHd3m58J\n" +
        "rfafiNdX05hlnJhCGObFmJ/lVxxamGArUoEMMdueRf7w9rKqH9h+SnO/Xgt5o+0i\n" +
        "dmVp7nEKNOMzPXgMN9uLqT3CZcnaHfXSvOh0Q0zpFBArgwm8ECaVyYuLVJu0D++8\n" +
        "oMx4kWoqxguPepQK3kXo3YXhXMJVi6BnaYLt1/fs+suKC+S3W5w+92qrCrkijMEc\n" +
        "eEE91KUwtBLrmdxjJUC8Mhnw/1co+1mpLXJCsplUmMg152SNqtJMWqWOezHO/2Vc\n" +
        "HrgDFn0XNYO05Ipb3nAe6T3Y6cfpeVQZySifNcdUm5oTRqowdi3Xgmebe2LkX2Y1\n" +
        "sXEbumi/haDiJzMwJ+CHPfecgCBlXRdyUhdzJQC/k1FG8mbrlpT0lN2QHx9nYLxm\n" +
        "OAIb0TerFztfpG9WDvAQPXRBUmhq6ju0wQxqOGFOGcF0rbwduOVWSYg5l2yJwN95\n" +
        "MiCmIkb2c1GUqLsAq/09A91eRWV1lGNUU9NMQ5iUUqt1j9lgC6nc+WlEdoaQhTeI\n" +
        "FGGo5K2+QIMyVys/CwIDAQABo4ICXzCCAlswDgYDVR0PAQH/BAQDAgWgMBMGA1Ud\n" +
        "JQQMMAoGCCsGAQUFBwMBMB0GA1UdEQQWMBSCEnd3dy5ob3N0LmRldmVudi5kZTAd\n" +
        "BgNVHQ4EFgQUrj5Gakepi/JYgjwy3HSVVwymj0MwHwYDVR0jBBgwFoAUIp73DDDo\n" +
        "gkG9feZYiW/iQ7QBeuEwgcEGA1UdHwSBuTCBtjCBs6CBsKCBrYaBqmxkYXA6Ly8v\n" +
        "Q049Q0ktU1VCLUNBLENOPWNpLWFkY3MsQ049Q0RQLENOPVB1YmxpYyUyMEtleSUy\n" +
        "MFNlcnZpY2VzLENOPVNlcnZpY2VzLENOPUNvbmZpZ3VyYXRpb24sREM9Y2ksREM9\n" +
        "ZGM/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNS\n" +
        "TERpc3RyaWJ1dGlvblBvaW50MIG1BggrBgEFBQcBAQSBqDCBpTCBogYIKwYBBQUH\n" +
        "MAKGgZVsZGFwOi8vL0NOPUNJLVNVQi1DQSxDTj1BSUEsQ049UHVibGljJTIwS2V5\n" +
        "JTIwU2VydmljZXMsQ049U2VydmljZXMsQ049Q29uZmlndXJhdGlvbixEQz1jaSxE\n" +
        "Qz1kYz9jQUNlcnRpZmljYXRlP2Jhc2U/b2JqZWN0Q2xhc3M9Y2VydGlmaWNhdGlv\n" +
        "bkF1dGhvcml0eTA8BgkrBgEEAYI3FQcELzAtBiUrBgEEAYI3FQj91yWGlLoMhLGP\n" +
        "J4Tj3DmZsUaBRoaElBuB+75yAgFkAgEGMBsGCSsGAQQBgjcVCgQOMAwwCgYIKwYB\n" +
        "BQUHAwEwDQYJKoZIhvcNAQELBQADggIBAG8uRF7drpGFexrx1wOVcIm5alok+xEn\n" +
        "YcRpvY+lEnAI3QfkR6YemPmy6fGQIJtpvdxBclSTqM+K2oQOJKMJio88jEC7rYEJ\n" +
        "E8Up+0lFRl6SCNeQIecI2gEMyn8oivndHmvvq8dB0YkmAxDgZuWCaIlgF6Z9yS7G\n" +
        "DS5T093Kzbx2YGnqOhApvM/QrzKBAN2/KB5sPjKQDvHjpi7lES0/dql2ujhkCKIa\n" +
        "YLNq/C40R2LopA3AP6q4aRW4QySDVHO8IUey6MRKxbOUsS9xK4k7/bsnZzI0oUs6\n" +
        "bLDcof/0ucrEH62GvN4UbVJwfXGgsPpCDSlSJdP10GP64RE9ChbponYeUYmwbbsf\n" +
        "fOxRv5ZkMybmkb6KjX9HwOPTt5PsaNSpShSZP5BRclhJz8on+aQFeXMzPygBt7iU\n" +
        "DPv8UZ4vD7VwtfddwXODaLHiDY4WCZWw0yEo2Pc6O6bRtlqnO5qHQiIUw/UrISHh\n" +
        "PmHPiM3sA5ddWfpY/c7Ahx+0ZZ7nd7MoNfj32UDedsi6QtMkxBmeMby2Lnz8KG5C\n" +
        "+4g2f0jqZKLzu5L62N2aLWDmcUaaDgxqm6rSU9hImoU4EbPI0UYvFVpsVmEeHJdq\n" +
        "mVRfibIcx8W5TewHE8fMBt3I9ZNPlrAtYHxuLOCfprfWkJe6etY9o/vQDsFtVQnB\n" +
        "pMTaid/rj/hr\n" +
        "-----END CERTIFICATE-----\n";
}
