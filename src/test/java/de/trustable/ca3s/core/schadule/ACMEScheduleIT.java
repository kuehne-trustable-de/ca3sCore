package de.trustable.ca3s.core.schadule;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.schedule.ACMEExpiryScheduler;
import org.jscep.client.ClientException;
import org.jscep.transaction.TransactionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ACMEScheduleIT {

    Random random = new SecureRandom();
    String realm = "TestRealm_" + random.nextInt();

    @Autowired
    ACMEExpiryScheduler acmeExpiryScheduler;

    @Autowired
    AcmeOrderRepository acmeOrderRepository;

    @Test
    public void testACMEOrderExpiry() {

        Instant now = Instant.now();

        AcmeOrder acmeOrderPendingExpired = newTestOrder();
        acmeOrderPendingExpired.setStatus(AcmeOrderStatus.PENDING);
        acmeOrderPendingExpired.setExpires( now.minusSeconds(3600));

        AcmeOrder acmeOrderPendingValid = newTestOrder();
        acmeOrderPendingValid.setStatus(AcmeOrderStatus.PENDING);
        acmeOrderPendingValid.setExpires( now.plusSeconds(3600));

        AcmeOrder acmeOrderValidExpired = newTestOrder();
        acmeOrderValidExpired.setStatus(AcmeOrderStatus.VALID);
        acmeOrderValidExpired.setExpires( now.minusSeconds(3600));

        AcmeOrder acmeOrderValidValid = newTestOrder();
        acmeOrderValidValid.setStatus(AcmeOrderStatus.VALID);
        acmeOrderValidValid.setExpires( now.plusSeconds(3600));

        acmeOrderRepository.save(acmeOrderPendingExpired);
        acmeOrderRepository.save(acmeOrderPendingValid);
        acmeOrderRepository.save(acmeOrderValidExpired);
        acmeOrderRepository.save(acmeOrderValidValid);

        acmeExpiryScheduler.runMinute();

        Optional<AcmeOrder> optOrder = acmeOrderRepository.findById(acmeOrderPendingExpired.getId());
        Assertions.assertEquals(AcmeOrderStatus.INVALID, optOrder.get().getStatus());

        optOrder = acmeOrderRepository.findById(acmeOrderPendingValid.getId());
        Assertions.assertEquals(AcmeOrderStatus.PENDING, optOrder.get().getStatus());

        optOrder = acmeOrderRepository.findById(acmeOrderValidExpired.getId());
        Assertions.assertEquals(AcmeOrderStatus.VALID, optOrder.get().getStatus());

        optOrder = acmeOrderRepository.findById(acmeOrderValidValid.getId());
        Assertions.assertEquals(AcmeOrderStatus.VALID, optOrder.get().getStatus());

    }

    private AcmeOrder newTestOrder(){
        AcmeOrder acmeOrder = new AcmeOrder();
        acmeOrder.setOrderId(random.nextLong());
        acmeOrder.setRealm(realm);
        return acmeOrder;
    }
}
