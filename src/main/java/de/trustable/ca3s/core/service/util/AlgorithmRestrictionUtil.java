package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

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
        boolean outcome = true;

        Preferences preferences = preferenceUtil.getPrefs(PreferenceUtil.SYSTEM_PREFERENCE_ID);

        int keyLength = CertificateUtil.getAlignedKeyLength(publicKey);

        if (Arrays.stream(preferences.getSelectedSigningAlgos()).noneMatch(a -> matchesAlgo(a, signingAlgo, keyLength))) {
            String msg = "restriction mismatch: signature algo / length '" + signingAlgo + "/" + keyLength + "' does not match expected set!";
            messageList.add(msg);
            LOG.info(msg);
            outcome = false;
        }

        if((hashAlgName != null ) && CertificateUtil.isHashRequired(signingAlgo)) {
            if (Arrays.stream(preferences.getSelectedHashes())
                .noneMatch(a -> a.equalsIgnoreCase(hashAlgName))) {
                String msg = "restriction mismatch: hash algo '" + hashAlgName + "' does not match expected set!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        }

        return outcome;
    }

    private boolean matchesAlgo(String a, String signingAlgo, int keyLength) {

        if( a.toLowerCase().startsWith("dilithium") ||
            a.toLowerCase().startsWith("falcon")) {

            if (a.toLowerCase().startsWith("dilithium2")) {
                return signingAlgo.equalsIgnoreCase("dilithium2");
            } else if (a.toLowerCase().startsWith("dilithium3")) {
                return signingAlgo.equalsIgnoreCase( "dilithium3");
            } else if (a.toLowerCase().startsWith("dilithium5")) {
                return signingAlgo.equalsIgnoreCase("dilithium5");
            } else if (a.toLowerCase().startsWith("falcon-512")) {
                return signingAlgo.equalsIgnoreCase("falcon-512");
            } else if (a.toLowerCase().startsWith("falcon-1024")) {
                return signingAlgo.equalsIgnoreCase("falcon-1024");
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
