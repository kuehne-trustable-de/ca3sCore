package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.service.dto.CryptoConfigView;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfiguration {

    private final String[] validPBEAlgoArr;

    public CryptoConfiguration(@Value("${ca3s.pkcs12.pbe.algos:PBEWithHmacSHA256AndAES_256}") String[] validPBEAlgoArr
        ) {

        this.validPBEAlgoArr = validPBEAlgoArr;
    }

    public CryptoConfigView getCryptoConfigView() {
        return new CryptoConfigView(validPBEAlgoArr, getDefaultPBEAlgo());
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
