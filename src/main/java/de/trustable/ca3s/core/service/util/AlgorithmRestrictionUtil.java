package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class AlgorithmRestrictionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AlgorithmRestrictionUtil.class);

    private final PreferenceUtil preferenceUtil;

    public AlgorithmRestrictionUtil(PreferenceUtil preferenceUtil) {
        this.preferenceUtil = preferenceUtil;
    }

    public boolean isAlgorithmRestrictionsResolved(PublicKey publicKey, List<String> messageList) {

        String oid = OidNameMapper.lookupOid( publicKey.getAlgorithm());
        String shortName = AlgorithmInfo.getSigAlgoShortName(oid);
        return isAlgorithmRestrictionsResolved(shortName,
            publicKey,
            null,
            messageList);
    }

    public boolean isAlgorithmRestrictionsResolved(Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

        return isAlgorithmRestrictionsResolved(p10ReqHolder.getPublicKeyAlgorithmShortName(),
            p10ReqHolder.getPublicSigningKey(),
            p10ReqHolder.getAlgorithmInfo().getHashAlgName(),
            messageList);
    }

    public boolean isAlgorithmRestrictionsResolved(String signingAlgo,
                                                   PublicKey publicKey,
                                                   String hashAlgName,
                                                   List<String> messageList) {
        Preferences preferences = preferenceUtil.getPrefs(PreferenceUtil.SYSTEM_PREFERENCE_ID);

        int keyLength = CertificateUtil.getAlignedKeyLength(publicKey);

        if (Arrays.stream(preferences.getSelectedSigningAlgos()).noneMatch(a -> matchesAlgo(a, signingAlgo, keyLength))) {
            String msg = "restriction mismatch: signature algo / length '" + signingAlgo + "/" + keyLength + "' does not match expected set!";
            messageList.add(msg);
            LOG.info(msg);
            return false;
        }

        if((hashAlgName != null ) && CertificateUtil.isHashRequired(signingAlgo)) {
            if (Arrays.stream(preferences.getSelectedHashes())
                .noneMatch(a -> a.equalsIgnoreCase(hashAlgName))) {
                String msg = "restriction mismatch: hash algo '" + hashAlgName + "' does not match expected set!";
                messageList.add(msg);
                LOG.debug(msg);
                return false;
            }
        }

        return true;
    }

    private boolean matchesAlgo(String a, String signingAlgo, int keyLength) {

        String algoNameLC = a.toLowerCase();
        if( algoNameLC.startsWith("dilithium") ||
            algoNameLC.startsWith("falcon")) {

            if (algoNameLC.startsWith("dilithium2")) {
                return signingAlgo.equalsIgnoreCase("dilithium2");
            } else if (algoNameLC.startsWith("dilithium3")) {
                return signingAlgo.equalsIgnoreCase( "dilithium3");
            } else if (algoNameLC.startsWith("dilithium5")) {
                return signingAlgo.equalsIgnoreCase("dilithium5");
            } else if (algoNameLC.startsWith("falcon-512")) {
                return signingAlgo.equalsIgnoreCase("falcon-512");
            } else if (algoNameLC.startsWith("falcon-1024")) {
                return signingAlgo.equalsIgnoreCase("falcon-1024");
            }
        }

        if( signingAlgo.equalsIgnoreCase(KeyAlgoLengthOrSpec.Ed25519.getAlgoName() )){
            return true;
        }
        if( signingAlgo.startsWith("brainpool")) {
            if (algoNameLC.startsWith(KeyAlgoLengthOrSpec.Brainpool_P256r1.getAlgoName().toLowerCase(Locale.ROOT))) {
                return true;
            }else if (algoNameLC.startsWith(KeyAlgoLengthOrSpec.Brainpool_P384r1.getAlgoName().toLowerCase(Locale.ROOT))) {
                return true;
            }else if (algoNameLC.startsWith(KeyAlgoLengthOrSpec.Brainpool_P512r1.getAlgoName().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        String[] parts = a.split("-");
        if (parts.length != 2) {
            LOG.warn("unexpected keyLength / type descriptor: '{}'", a);
            return false;
        }

        if (!parts[0].equalsIgnoreCase(signingAlgo)) {
            LOG.debug("type check mismatch: '{}' / '{}'", parts[0], signingAlgo);
            return false;
        }

        try {
            int keyLengthRestriction = Integer.parseInt(parts[1]);
            return keyLengthRestriction <= keyLength;
        } catch (NumberFormatException nfe) {
            LOG.warn("unexpected number in keyLengthdescriptor: '" + a + "'", nfe);
        }
        return false;
    }

}
