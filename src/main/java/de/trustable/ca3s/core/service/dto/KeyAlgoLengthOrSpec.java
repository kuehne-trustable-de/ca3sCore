package de.trustable.ca3s.core.service.dto;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pqc.jcajce.provider.dilithium.BCDilithiumPublicKey;
import org.bouncycastle.pqc.jcajce.provider.falcon.BCFalconPublicKey;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.FalconParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Locale;

public class KeyAlgoLengthOrSpec {

    private static final Logger LOG = LoggerFactory.getLogger(KeyAlgoLengthOrSpec.class);

    public static final KeyAlgoLengthOrSpec RSA_2048 = new KeyAlgoLengthOrSpec("rsa-2048", 2048);
    public static final KeyAlgoLengthOrSpec RSA_3072 = new KeyAlgoLengthOrSpec("rsa-3072", 3072);
    public static final KeyAlgoLengthOrSpec RSA_4096 = new KeyAlgoLengthOrSpec("rsa-4096", 4096);
    public static final KeyAlgoLengthOrSpec RSA_6144 = new KeyAlgoLengthOrSpec("rsa-6144", 6144);
    public static final KeyAlgoLengthOrSpec RSA_8192 = new KeyAlgoLengthOrSpec("rsa-8192", 8192);

    public static final KeyAlgoLengthOrSpec ECDSA_224 = new KeyAlgoLengthOrSpec("ECDSA","ecdsa-224","ecdsa-224", "BC", 224, ECNamedCurveTable.getParameterSpec("secp224r1"));
    public static final KeyAlgoLengthOrSpec ECDSA_256 = new KeyAlgoLengthOrSpec("ECDSA","ecdsa-256","ecdsa-256", "BC", 256, ECNamedCurveTable.getParameterSpec("secp256r1"));
    public static final KeyAlgoLengthOrSpec ECDSA_384 = new KeyAlgoLengthOrSpec("ECDSA","ecdsa-384","ecdsa-384", "BC", 384, ECNamedCurveTable.getParameterSpec("secp384r1"));
    public static final KeyAlgoLengthOrSpec ECDSA_512 = new KeyAlgoLengthOrSpec("ECDSA","ecdsa-512","ecdsa-512", "BC", 512, ECNamedCurveTable.getParameterSpec("secp512r1"));

    public static final KeyAlgoLengthOrSpec Ed25519	= new KeyAlgoLengthOrSpec("Ed25519", "Ed25519","Ed25519", "BC", 256, NamedParameterSpec.ED25519);

    public static final KeyAlgoLengthOrSpec Brainpool_P256r1 = new KeyAlgoLengthOrSpec("Brainpool", "brainpoolP256r1","brainpoolP256r1", "BC", 256, ECNamedCurveTable.getParameterSpec("brainpoolP256r1"));
    public static final KeyAlgoLengthOrSpec Brainpool_P384r1 = new KeyAlgoLengthOrSpec("Brainpool", "brainpoolP384r1","brainpoolP384r1", "BC", 384, ECNamedCurveTable.getParameterSpec("brainpoolP384r1"));
    public static final KeyAlgoLengthOrSpec Brainpool_P512r1 = new KeyAlgoLengthOrSpec("Brainpool", "brainpoolP512r1","brainpoolP512r1", "BC", 512, ECNamedCurveTable.getParameterSpec("brainpoolP512r1"));

    public static final KeyAlgoLengthOrSpec Dilithium_2 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium2","dilithium2", "BCPQC", 2528*8, DilithiumParameterSpec.dilithium2);
    public static final KeyAlgoLengthOrSpec Dilithium_3 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium3","dilithium3", "BCPQC", 4000*8, DilithiumParameterSpec.dilithium3);
    public static final KeyAlgoLengthOrSpec Dilithium_5 = new KeyAlgoLengthOrSpec("Dilithium", "dilithium5","dilithium5", "BCPQC", 4864*8, DilithiumParameterSpec.dilithium5);

    public static final KeyAlgoLengthOrSpec Falcon_512 = new KeyAlgoLengthOrSpec("Falcon", "falcon512","falcon-512", "BCPQC", 7176, FalconParameterSpec.falcon_512);
    public static final KeyAlgoLengthOrSpec Falcon_1024 = new KeyAlgoLengthOrSpec("Falcon", "falcon1024","falcon-1024", "BCPQC", 14344, FalconParameterSpec.falcon_1024);

    public static final KeyAlgoLengthOrSpec[] NamedAlgoArr = {
        RSA_2048,
        RSA_3072,
        RSA_4096,
        RSA_6144,
        RSA_8192,
        ECDSA_224,
        ECDSA_256,
        ECDSA_384,
        ECDSA_512,
        Ed25519,
        Brainpool_P256r1,
        Brainpool_P384r1,
        Brainpool_P512r1,
        Dilithium_2,
        Dilithium_3,
        Dilithium_5,
        Falcon_512,
        Falcon_1024
    };

    String algoName = "RSA";
    String contentBuilderName = "RSA";
    String providerName = null;
    String algoGroup = "RSA";
    String keyFactoryAlgo = "RSA";
    int keyLength = 4096;
    AlgorithmParameterSpec algorithmParameterSpec = null;

/*
    public static void main(String[] args){

        Iterator it = ECNamedCurveTable.getNames().asIterator();
        for (Iterator iter = it; iter.hasNext(); ) {
            String name = iter.next().toString();
            System.out.println("known curves: " + name);

        }
    }
*/

    public static String getAlgorithmName(PublicKey pk) {

        LOG.debug("getAlgorithmName() for {}", pk.getClass().getName());
        String keyAlgName = pk.getAlgorithm();
        LOG.debug("pk.getAlgorithm() : {}", pk.getAlgorithm());

        if( keyAlgName == null || (keyAlgName.trim().isEmpty())){
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

    public static KeyAlgoLengthOrSpec from(String value) {

        String valueLC = value.toLowerCase(Locale.ROOT).replace('_', '-');

        for( KeyAlgoLengthOrSpec keyAlgoLengthOrSpec: NamedAlgoArr){
            if( valueLC.startsWith(keyAlgoLengthOrSpec.algoName.toLowerCase(Locale.ROOT))) {
                return keyAlgoLengthOrSpec;
            }
        }

        KeyAlgoLengthOrSpec keyAlgoLength = new KeyAlgoLengthOrSpec();

        /*
        String[] parts = value.split("[_-]");
        if(parts.length > 0){
            keyAlgoLength.algoName = parts[0].toLowerCase(Locale.ROOT);
        }
        if(parts.length > 1){
            keyAlgoLength.keyLength = Integer.parseInt( parts[1]);
        }
         */
        return keyAlgoLength;

    }

    public KeyAlgoLengthOrSpec(){}

    public KeyAlgoLengthOrSpec(String algoName, int keyLength){
        this.algoName = algoName;
        this.keyLength = keyLength;
    }
    public KeyAlgoLengthOrSpec(String algoGroup, String algoName, String contentBuilderName, String providerName, int keyLength, AlgorithmParameterSpec algorithmParameterSpec){
        this.algoGroup = algoGroup;
        if( "ECDSA".equalsIgnoreCase(algoGroup) ){
            this.keyFactoryAlgo = "EC";
        } else if( "Brainpool".equalsIgnoreCase(algoGroup) ){
                this.keyFactoryAlgo = "ECDH";
        }else if("Ed25519".equalsIgnoreCase(algoGroup)){
            this.keyFactoryAlgo = "EdDSA";
        }

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

    public String getKeyFactoryAlgo() {
        return keyFactoryAlgo;
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
        return algoName;
    }

}
