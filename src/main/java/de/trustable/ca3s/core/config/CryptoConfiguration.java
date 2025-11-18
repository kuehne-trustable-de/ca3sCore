package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.service.dto.CryptoConfigView;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.Provider;
import java.security.Security;
import java.util.Set;
import java.util.TreeSet;

@Configuration
public class CryptoConfiguration {

    private final Logger log = LoggerFactory.getLogger(CryptoConfiguration.class);

    private final String[] validPBEAlgoArr;
    private final String[] allHashAlgoArr;
    private final String[] allSignAlgoArr;
    private final String regexpPkcs12Description;
    private final String pkcs12SecretRegexp;
    private final String regexpPasswordDescription;
    private final String passwordRegexp;
    private final ClientAuthConfig clientAuthConfig;


    public CryptoConfiguration(@Value("${ca3s.pkcs12.pbe.algos:PBEWithHmacSHA256AndAES_256}") String[] validPBEAlgoArr,
                               @Value("${ca3s.catalog.algos.hash.selected:sha-1,sha-256,sha-384,sha-512}") String[] allHashAlgoArr,
                               @Value("${ca3s.catalog.algos.sign.selected:rsa-1024,rsa-2048,rsa-3072,rsa-4096,rsa-8192}") String[] allSignAlgoArr,
                               @Value("${ca3s.pkcs12.secret.description:min6NumberUpperLower}") String regexpPkcs12Description,
                               @Value("${ca3s.pkcs12.secret.regexp:^(?=.*\\d)(?=.*[a-z]).{6,100}$}") String pkcs12SecretRegexp,
                               @Value("${ca3s.ui.password.check.description:min6NumberUpperLower}") String regexpPasswordDescription,
                               @Value("${ca3s.ui.password.check.regexp:^(?=.*\\d)(?=.*[a-z]).{6,100}$}") String passwordRegexp,
                               ClientAuthConfig clientAuthConfig) {

        this.validPBEAlgoArr = validPBEAlgoArr;
        this.allHashAlgoArr = allHashAlgoArr;
        this.allSignAlgoArr = allSignAlgoArr;
        this.regexpPkcs12Description = regexpPkcs12Description;
        this.pkcs12SecretRegexp = pkcs12SecretRegexp;
        this.regexpPasswordDescription = regexpPasswordDescription;
        this.passwordRegexp = passwordRegexp;
        this.clientAuthConfig = clientAuthConfig;

        for (Provider provider : Security.getProviders()) {

            Set<String> algs = new TreeSet<>();
            provider.getServices().stream()
//                .filter(s -> "Cipher".equals(s.getType()))
                .map(Provider.Service::getAlgorithm)
                .forEach(algs::add);
/*
            for( String algo:algs){
                log.debug("provider "+provider.getName()+" support algorithm : " + algo);
            }
 */
        }
    }

    public CryptoConfigView getCryptoConfigView() {
        return new CryptoConfigView(validPBEAlgoArr, getDefaultPBEAlgo(),
            allHashAlgoArr, allSignAlgoArr,
            regexpPkcs12Description, pkcs12SecretRegexp,
            regexpPasswordDescription, passwordRegexp,
            clientAuthConfig.getClientAuthTarget());
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
