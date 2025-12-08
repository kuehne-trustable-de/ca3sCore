package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.util.RandomUtil;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

class KeyGenerationServiceTest {

    @BeforeAll
    public static void setUpBeforeClass() {
        JCAManager.getInstance();
    }

    KeyGenerationService  keyGenerationService = new KeyGenerationService( "RSA-2048",new RandomUtil());

    @Test
    void generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        KeyPair kp = keyGenerationService.generateKeyPair(KeyAlgoLengthOrSpec.Brainpool_P256r1);

        Assertions.assertNotNull(kp);
        Assertions.assertEquals("org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey", kp.getPublic().getClass().getName());

        String algoName = KeyAlgoLengthOrSpec.getAlgorithmName(kp.getPublic());
        System.out.println(algoName);

/*
        public static KeyPair generateECKeys() {
            ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("brainpoolpt1");

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            keyPairGenerator.initialize(parameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        }
*/

    }
}
