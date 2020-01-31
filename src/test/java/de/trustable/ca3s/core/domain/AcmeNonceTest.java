package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeNonceTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeNonce.class);
        AcmeNonce acmeNonce1 = new AcmeNonce();
        acmeNonce1.setId(1L);
        AcmeNonce acmeNonce2 = new AcmeNonce();
        acmeNonce2.setId(acmeNonce1.getId());
        assertThat(acmeNonce1).isEqualTo(acmeNonce2);
        acmeNonce2.setId(2L);
        assertThat(acmeNonce1).isNotEqualTo(acmeNonce2);
        acmeNonce1.setId(null);
        assertThat(acmeNonce1).isNotEqualTo(acmeNonce2);
    }
}
