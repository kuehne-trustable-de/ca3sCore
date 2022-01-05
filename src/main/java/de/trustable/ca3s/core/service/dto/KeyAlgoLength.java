package de.trustable.ca3s.core.service.dto;

public enum KeyAlgoLength {
	RSA_2048, RSA_4096;

    public String algoName(){
        return "RSA";
    }

    public int keyLength(){
        if( this == RSA_2048){
            return 2048;
        }
        return 4096;
    }

}
