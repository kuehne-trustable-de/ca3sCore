package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import de.trustable.ca3s.core.web.rest.acme.ChallengeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

public class ChallengeControllerTest {

/*
    AcmeChallengeRepository challengeRepository
    public ChallengeController(AcmeChallengeRepository challengeRepository,
                               AcmeOrderRepository orderRepository,
                               PreferenceUtil preferenceUtil,
                               @Value("${ca3s.dns.server:}") String resolverHost,
                               @Value("${ca3s.dns.port:53}") int resolverPort,
                               AuditService auditService) throws UnknownHostException {
*/

        @Test
    public void testStartUpProdServletContext() {
/*
        X509Certificate cert;
        boolean result = ChallengeController.checkALPNCertificate("host", 1234, cert);
*/
    }

}
