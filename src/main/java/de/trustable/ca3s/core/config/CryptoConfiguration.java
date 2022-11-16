package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.service.dto.CryptoConfigView;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfiguration {

    private final String[] validPBEAlgoArr;
    private final String[] allHashAlgoArr;
    private final String[] allSignAlgoArr;
    private final String pkcs12SecretRegexp;

    public CryptoConfiguration(@Value("${ca3s.pkcs12.pbe.algos:PBEWithHmacSHA256AndAES_256}") String[] validPBEAlgoArr,
                               @Value("${ca3s.catalog.hash.algos:sha-1,sha-256,sha-384,sha-512}") String[] allHashAlgoArr,
                               @Value("${ca3s.catalog.sign.algos:rsa-1024,rsa-2048,rsa-3072,rsa-4096,rsa-8192}") String[] allSignAlgoArr,
                               @Value("${ca3s.pkcs12.secret.regexp:^(?=.*\\d)(?=.*[a-z]).{6,100}$}") String pkcs12SecretRegexp
        ) {

        this.validPBEAlgoArr = validPBEAlgoArr;
        this.allHashAlgoArr = allHashAlgoArr;
        this.allSignAlgoArr = allSignAlgoArr;
        this.pkcs12SecretRegexp = pkcs12SecretRegexp;
    }

    public CryptoConfigView getCryptoConfigView() {
        return new CryptoConfigView(validPBEAlgoArr, getDefaultPBEAlgo(),
            allHashAlgoArr, allSignAlgoArr, pkcs12SecretRegexp);
    }

    public String getDefaultPBEAlgo(){
        String passwordProtectionAlgo = "PBEWithHmacSHA256AndAES_256";
        if( validPBEAlgoArr.length > 0){
            passwordProtectionAlgo = validPBEAlgoArr[0];
        }
        return passwordProtectionAlgo;
    }

    public boolean isPBEAlgoAllowed(final String reqAlgo){
        return ArrayUtils.contains( validPBEAlgoArr, reqAlgo.trim() );

    }

}
