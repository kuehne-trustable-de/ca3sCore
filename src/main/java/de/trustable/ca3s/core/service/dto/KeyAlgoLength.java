package de.trustable.ca3s.core.service.dto;

public class KeyAlgoLength {

    public static final KeyAlgoLength RSA_2048 = new KeyAlgoLength("RSA", 2048);
    public static final KeyAlgoLength RSA_4096 = new KeyAlgoLength("RSA", 4096);

    String algoName = "RSA";
    int keyLength = 4096;

    public static KeyAlgoLength from( String value){
        KeyAlgoLength keyAlgoLength = new KeyAlgoLength();
        String[] parts = value.split("[_-]");
        if(parts.length > 0){
            keyAlgoLength.algoName = parts[0];
        }
        if(parts.length > 1){
            keyAlgoLength.keyLength = Integer.parseInt( parts[1]);
        }
        return keyAlgoLength;
    }

    public KeyAlgoLength(){}

    public KeyAlgoLength( String algoName, int keyLength){
        this.algoName = algoName;
        this.keyLength = keyLength;
    }

    public String getAlgoName() {
        return algoName;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public String toString(){
        return algoName + "-" + keyLength;
    }
}
