package de.trustable.ca3s.core.service.util;

public class AlgorithmInfo {

    private String sigAlgName ;
    private String hashAlgName = "undefined";
    private String paddingAlgName = "PKCS1";

    public AlgorithmInfo(final String algoNames){

        // extract signature algo
        sigAlgName = algoNames.toLowerCase();

        if (sigAlgName.contains("with")) {
            String[] parts = sigAlgName.split("with");
            if (parts.length > 1) {
                hashAlgName = parts[0];
                if (parts[1].contains("and")) {
                    String[] parts2 = parts[1].split("and");
                    sigAlgName = parts2[0];
                    if (parts2.length > 1) {
                        paddingAlgName = parts2[1];
                    }
                } else {
                    sigAlgName = parts[1];
                }
            }
        }

    }

    public String getSigAlgName() {
        return sigAlgName;
    }

    public String getSigAlgFriendlyName() {
        if( "RSAEncryption".equalsIgnoreCase(sigAlgName)){
            return "RSA";
        }
        return sigAlgName;
    }

    public String getHashAlgName() {
        return hashAlgName;
    }

    public String getPaddingAlgName() {
        return paddingAlgName;
    }
}
