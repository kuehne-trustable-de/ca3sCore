package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class RDNTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RDN.class);
        RDN rDN1 = new RDN();
        rDN1.setId(1L);
        RDN rDN2 = new RDN();
        rDN2.setId(rDN1.getId());
        assertThat(rDN1).isEqualTo(rDN2);
        rDN2.setId(2L);
        assertThat(rDN1).isNotEqualTo(rDN2);
        rDN1.setId(null);
        assertThat(rDN1).isNotEqualTo(rDN2);
    }
}
