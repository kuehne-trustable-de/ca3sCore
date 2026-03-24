package de.trustable.ca3s.core.service.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.GeneralSecurityException;

class AlgorithmRestrictionUtilTest {

    private final PreferenceUtil preferenceUtil = null;
    private final AlgorithmRestrictionUtil algorithmRestrictionUtil = new AlgorithmRestrictionUtil(preferenceUtil);


    String[] algoArr = {"rsa-2048","rsa-3072","rsa-4096","rsa-6144","rsa-8192",
        "ecdsa-224", "ecdsa-256", "ecdsa-384", "ecdsa-512",
        "Ed25519",
        "brainpoolP256r1"};

    @Test
    public void testAlgoMatches() throws GeneralSecurityException {

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-2048", "rsa", 1024));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-2048", "rsa", 2048));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-2048", "rsa", 3072));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-2048", "rsa", 4096));

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-3072", "rsa", 1024));
        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-3072", "rsa", 2048));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-3072", "rsa", 3072));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-3072", "rsa", 4096));

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-4096", "rsa", 1024));
        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-4096", "rsa", 2048));
        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("rsa-4096", "rsa", 3072));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("rsa-4096", "rsa", 4096));

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("ecdsa-224", "ecdsa", 192));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-224", "ecdsa", 224));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-224", "ecdsa", 256));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-224", "ecdsa", 512));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-224", "ecdsa", 384));

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("ecdsa-256", "ecdsa", 192));
        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("ecdsa-256", "ecdsa", 224));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-256", "ecdsa", 256));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-256", "ecdsa", 512));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("ecdsa-256", "ecdsa", 384));

        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("Ed25519", "ed25519", 0));

        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("brainpoolP256r1", "brainpoolP256r1", 0));
        Assertions.assertTrue( algorithmRestrictionUtil.matchesAlgo("brainpoolP512r1", "brainpoolP512r1", 0));

        Assertions.assertFalse( algorithmRestrictionUtil.matchesAlgo("Ed25519", "ecdsa", 192));

    }

}
