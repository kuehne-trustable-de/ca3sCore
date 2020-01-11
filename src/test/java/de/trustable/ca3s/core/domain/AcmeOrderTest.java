package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeOrderTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeOrder.class);
        AcmeOrder acmeOrder1 = new AcmeOrder();
        acmeOrder1.setId(1L);
        AcmeOrder acmeOrder2 = new AcmeOrder();
        acmeOrder2.setId(acmeOrder1.getId());
        assertThat(acmeOrder1).isEqualTo(acmeOrder2);
        acmeOrder2.setId(2L);
        assertThat(acmeOrder1).isNotEqualTo(acmeOrder2);
        acmeOrder1.setId(null);
        assertThat(acmeOrder1).isNotEqualTo(acmeOrder2);
    }
}
