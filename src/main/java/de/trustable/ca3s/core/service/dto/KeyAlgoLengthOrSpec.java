package de.trustable.ca3s.core.service.dto;

import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pqc.jcajce.provider.dilithium.BCDilithiumPublicKey;
import org.bouncycastle.pqc.jcajce.provider.falcon.BCFalconPublicKey;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.FalconParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

public class KeyAlgoLengthOrSpec {

    private static final Logger LOG = LoggerFactory.getLogger(KeyAlgoLengthOrSpec.class);

    public static final KeyAlgoLengthOrSpec RSA_2048 = new KeyAlgoLengthOrSpec("RSA", 2048);
    public static final KeyAlgoLengthOrSpec RSA_4096 = new KeyAlgoLengthOrSpec("RSA", 4096);

    public static final KeyAlgoLengthOrSpec Dilithium_2 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium2","dilithium2", "BCPQC", 2528*8, DilithiumParameterSpec.dilithium2);
    public static final KeyAlgoLengthOrSpec Dilithium_3 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium3","dilithium3", "BCPQC", 4000*8, DilithiumParameterSpec.dilithium3);
    public static final KeyAlgoLengthOrSpec Dilithium_5 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium5","dilithium5", "BCPQC", 4864*8, DilithiumParameterSpec.dilithium5);

    public static final KeyAlgoLengthOrSpec Falcon_512 = new KeyAlgoLengthOrSpec("Falcon", "falcon512","falcon-512", "BCPQC", 7176, FalconParameterSpec.falcon_512);
    public static final KeyAlgoLengthOrSpec Falcon_1024 = new KeyAlgoLengthOrSpec("Falcon", "falcon1024","falcon-1024", "BCPQC", 14344, FalconParameterSpec.falcon_1024);

    String algoName = "RSA";
    String contentBuilderName = "RSA";
    String providerName = null;
    String algoGroup = "RSA";
    int keyLength = 4096;
    AlgorithmParameterSpec algorithmParameterSpec = null;


    public static String getAlgorithmName(PublicKey pk) {

        LOG.debug("getAlgorithmName() for {}", pk.getClass().getName());
        String keyAlgName = pk.getAlgorithm();
        LOG.debug("pk.getAlgorithm() : {}", pk.getAlgorithm());

        if( keyAlgName == null || (keyAlgName.trim().length() == 0)){
            if (pk instanceof BCDilithiumPublicKey) {
                keyAlgName = ((BCDilithiumPublicKey)pk).getParameterSpec().getName();
            }else if (pk instanceof BCFalconPublicKey) {
                keyAlgName = ((BCFalconPublicKey)pk).getParameterSpec().getName();
            }else{
                LOG.warn("getAlgorithmName(): unexpected key class {}", pk.getClass().getName());
            }
        }
        return keyAlgName;
    }


    public static KeyAlgoLengthOrSpec from(AlgorithmParameterSpec spec) throws GeneralSecurityException {
        if( Dilithium_2.getAlgorithmParameterSpec().equals(spec) ) {
            return Dilithium_2;
        }else if( Dilithium_3.getAlgorithmParameterSpec().equals(spec) ){
            return Dilithium_3;
        }else if( Dilithium_5.getAlgorithmParameterSpec().equals(spec) ){
            return Dilithium_5;
        }else if( Falcon_512.getAlgorithmParameterSpec().equals(spec) ){
            return Falcon_512;
        }else if( Falcon_1024.getAlgorithmParameterSpec().equals(spec) ){
            return Falcon_1024;
        }
        throw new GeneralSecurityException("unknown AlgorithmParameterSpec: '" + spec.getClass().getName() + "' !");
    }

    public static KeyAlgoLengthOrSpec from(String value){

        String valueLC = value.toLowerCase();
        if( valueLC.startsWith("dilithium2")) {
            return Dilithium_2;
        } else if( valueLC.startsWith("dilithium3")) {
            return Dilithium_3;
        } else if( valueLC.startsWith("dilithium5")) {
            return Dilithium_5;
        } else if( valueLC.startsWith(Falcon_512.algoName)) {
            return Falcon_512;
        } else if( valueLC.startsWith("falcon-512")) {
            return Falcon_512;
        } else if( valueLC.startsWith("falcon_512")) {
            return Falcon_512;
        } else if( valueLC.startsWith(Falcon_1024.algoName)) {
            return Falcon_1024;
        } else if( valueLC.startsWith("falcon-1024")) {
            return Falcon_1024;
        } else if( valueLC.startsWith("falcon_1024")) {
            return Falcon_1024;
        }

        KeyAlgoLengthOrSpec keyAlgoLength = new KeyAlgoLengthOrSpec();
        String[] parts = value.split("[_-]");
        if(parts.length > 0){
            keyAlgoLength.algoName = parts[0];
        }
        if(parts.length > 1){
            keyAlgoLength.keyLength = Integer.parseInt( parts[1]);
        }
        return keyAlgoLength;
    }

    public KeyAlgoLengthOrSpec(){}

    public KeyAlgoLengthOrSpec(String algoName, int keyLength){
        this.algoName = algoName;
        this.keyLength = keyLength;
    }
    public KeyAlgoLengthOrSpec(String algoGroup, String algoName, String contentBuilderName, String providerName, int keyLength, AlgorithmParameterSpec algorithmParameterSpec){
        this.algoGroup = algoGroup;
        this.algoName = algoName;
        this.keyLength = keyLength;
        this.algorithmParameterSpec = algorithmParameterSpec;
        this.contentBuilderName =  contentBuilderName;
        this.providerName = providerName;
    }

    public JcaContentSignerBuilder buildJcaContentSignerBuilder(){
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(contentBuilderName);
        if( providerName != null) {
            csBuilder.setProvider(providerName);
        }
        return csBuilder;
    }


    public String getAlgoGroup() {
        return algoGroup;
    }

    public String getAlgoName() {
        return algoName;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public String getContentBuilderName() {
        return contentBuilderName;
    }

    public String getProviderName() {
        return providerName;
    }

    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return algorithmParameterSpec;
    }

    public String toString(){
        return algoName + "-" + keyLength;
    }

}
