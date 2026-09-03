package de.trustable.ca3s.core.web.rest.acme;

import java.util.Locale;

class PersistentRecord {

    private String caIssuer;
    private String accountUri;
    private String policy;
    private Long persistUntilMilliSec;

    PersistentRecord(String value) {
        String[] parts = value.replace("\"", "").split(";");

        caIssuer = null;
        accountUri = null;
        policy = null;
        persistUntilMilliSec = null;

        for (String part : parts) {
            String item = part.trim();

            if (item.isEmpty()) {
                continue;
            }

            if (!item.contains("=")) {
                // First field is the CA issuer identity.
                if (caIssuer == null) {
                    caIssuer = item.toLowerCase(Locale.ROOT);
                }
                continue;
            }

            String[] kv = item.split("=", 2);
            String key = kv[0].trim();
            String val = kv[1].trim();

            switch (key) {
                case "accounturi" -> accountUri = val;

                case "persistUntil" -> persistUntilMilliSec = Long.parseLong(val) * 1000L;

                case "policy" -> policy = val.toLowerCase(Locale.ROOT);

                default -> {
                    // Ignore extension fields for forward compatibility.
                }
            }
        }
    }

    public String getCaIssuer() {
        return caIssuer;
    }

    public String getAccountUri() {
        return accountUri;
    }

    public String getPolicy() {
        return policy;
    }

    public Long getPersistUntilMilliSec() {
        return persistUntilMilliSec;
    }

    @Override
    public String toString() {
        return "PersistentRecord{" +
            "caIssuer='" + caIssuer + '\'' +
            ", accountUri='" + accountUri + '\'' +
            ", policy='" + policy + '\'' +
            ", persistUntilMilliSec=" + persistUntilMilliSec +
            '}';
    }
}
