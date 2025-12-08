package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;

@Service
public class KeyGenerationService {

    private final String defaultKeySpec;
    private final RandomUtil randomUtil;

    private static final Logger LOG = LoggerFactory.getLogger(KeyGenerationService.class);

    public KeyGenerationService(@Value("${ca3s.keyspec.default:RSA_4096}") String defaultKeySpec,  RandomUtil randomUtil) {
        this.defaultKeySpec = defaultKeySpec;
        this.randomUtil = randomUtil;
    }


    public KeyPair createKeyPair(){
        try {
            return createKeyPair(defaultKeySpec);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            LOG.error("unexpected vale for 'ca3s.keyspec.default', using RSA-4096 as fallback.");
            try {
                return createKeyPair("RSA_4096");
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException ex) {
                throw new RuntimeException("Problem using default algorithm! Maybe a general crypto setup problem?", ex);
            }

        }
    }
    public KeyPair createKeyPair(final String keySpec) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyAlgoLengthOrSpec kal = KeyAlgoLengthOrSpec.from(keySpec);
        return generateKeyPair(kal);
    }

    public KeyPair generateKeyPair(KeyAlgoLengthOrSpec algoLengthOrSpec) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {

        KeyPairGenerator kpg;
        if( "falcon".equalsIgnoreCase(algoLengthOrSpec.getAlgoGroup())) {
            kpg = KeyPairGenerator.getInstance(algoLengthOrSpec.getContentBuilderName(), algoLengthOrSpec.getProviderName());
        }else{
            if( algoLengthOrSpec.getProviderName() != null) {
                kpg = KeyPairGenerator.getInstance( algoLengthOrSpec.getKeyFactoryAlgo(), algoLengthOrSpec.getProviderName());
            }else{
                kpg = KeyPairGenerator.getInstance(algoLengthOrSpec.getKeyFactoryAlgo());
            }
        }

        if( algoLengthOrSpec.getAlgorithmParameterSpec() != null){
            kpg.initialize(algoLengthOrSpec.getAlgorithmParameterSpec(), randomUtil.getSecureRandom());
        }else {
            kpg.initialize(algoLengthOrSpec.getKeyLength(), randomUtil.getSecureRandom());
        }
        return kpg.generateKeyPair();
    }

}
