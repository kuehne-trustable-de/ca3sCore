package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class NonceTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Nonce.class);
        Nonce nonce1 = new Nonce();
        nonce1.setId(1L);
        Nonce nonce2 = new Nonce();
        nonce2.setId(nonce1.getId());
        assertThat(nonce1).isEqualTo(nonce2);
        nonce2.setId(2L);
        assertThat(nonce1).isNotEqualTo(nonce2);
        nonce1.setId(null);
        assertThat(nonce1).isNotEqualTo(nonce2);
    }
}
