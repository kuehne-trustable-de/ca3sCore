package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class CSRTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CSR.class);
        CSR cSR1 = new CSR();
        cSR1.setId(1L);
        CSR cSR2 = new CSR();
        cSR2.setId(cSR1.getId());
        assertThat(cSR1).isEqualTo(cSR2);
        cSR2.setId(2L);
        assertThat(cSR1).isNotEqualTo(cSR2);
        cSR1.setId(null);
        assertThat(cSR1).isNotEqualTo(cSR2);
    }
}
