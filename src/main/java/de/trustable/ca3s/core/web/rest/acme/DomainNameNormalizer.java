package de.trustable.ca3s.core.web.rest.acme;

import java.net.IDN;
import java.util.Locale;

public final class DomainNameNormalizer {

    private DomainNameNormalizer() {
    }

    /**
     * Normalize a DNS domain name.
     * Examples:
     *   EXAMPLE.COM.      -> example.com
     *   bücher.example    -> xn--bcher-kva.example
     */
    public static String normalize(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain must not be null");
        }

        domain = domain.trim();

        if (domain.isEmpty()) {
            throw new IllegalArgumentException("Domain must not be empty");
        }

        /*
         * Remove the trailing root label.
         *
         * example.com. == example.com
         */
        if (domain.endsWith(".")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        if (domain.isEmpty()) {
            throw new IllegalArgumentException("Domain must not be the root domain");
        }

        /*
         * Convert Unicode domain names to ASCII/Punycode.
         */
        String asciiDomain = IDN.toASCII(
            domain,
            IDN.USE_STD3_ASCII_RULES
        );

        /*
         * DNS names are case-insensitive.
         */
        return asciiDomain.toLowerCase(Locale.ROOT);
    }

    public static String normalizeIdentifier(String identifier) {

        if (identifier == null) {
            throw new IllegalArgumentException("Identifier must not be null");
        }

        if (identifier.startsWith("*.")) {
            String domain = identifier.substring(2);
            return "*." + normalize(domain);
        }

        return normalize(identifier);
    }

    public static void main(String[] args) {

        String[] domains = {
            "EXAMPLE.COM",
            "example.com.",
            "Example.COM.",
            "bücher.example",
            "XN--BCHER-KVA.EXAMPLE."
        };

        for (String domain : domains) {
            System.out.printf(
                "%-30s -> %s%n",
                domain,
                normalize(domain)
            );
        }
    }
}
