package de.trustable.ca3s.core.web.rest.acme;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DomainNameNormalizerTest {

    @Test
    void normalize() {
        Assertions.assertEquals("example.com", DomainNameNormalizer.normalize("EXAMPLE.COM"));
        Assertions.assertEquals("example.com", DomainNameNormalizer.normalize("example.com."));
        Assertions.assertEquals("example.com", DomainNameNormalizer.normalize("Example.COM."));
        Assertions.assertEquals("xn--bcher-kva.example", DomainNameNormalizer.normalize("bücher.example"));
        Assertions.assertEquals("xn--bcher-kva.example", DomainNameNormalizer.normalize("XN--BCHER-KVA.EXAMPLE."));
    }

    @Test
    void normalizeIdentifier() {

        Assertions.assertEquals("example.com", DomainNameNormalizer.normalizeIdentifier("EXAMPLE.COM"));
        Assertions.assertEquals("example.com", DomainNameNormalizer.normalizeIdentifier("example.com."));
        Assertions.assertEquals("*.example.com", DomainNameNormalizer.normalizeIdentifier("*.example.com."));
        Assertions.assertEquals("example.com", DomainNameNormalizer.normalizeIdentifier("Example.COM."));
        Assertions.assertEquals("xn--bcher-kva.example", DomainNameNormalizer.normalizeIdentifier("bücher.example"));
        Assertions.assertEquals("*.xn--bcher-kva.example", DomainNameNormalizer.normalizeIdentifier("*.bücher.example"));
        Assertions.assertEquals("xn--bcher-kva.example", DomainNameNormalizer.normalizeIdentifier("XN--BCHER-KVA.EXAMPLE."));
        Assertions.assertEquals("*.xn--bcher-kva.example", DomainNameNormalizer.normalizeIdentifier("*.XN--BCHER-KVA.EXAMPLE."));
    }
}
