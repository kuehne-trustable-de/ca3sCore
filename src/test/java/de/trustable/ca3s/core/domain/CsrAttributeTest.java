package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class CsrAttributeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CsrAttribute.class);
        CsrAttribute csrAttribute1 = new CsrAttribute();
        csrAttribute1.setId(1L);
        CsrAttribute csrAttribute2 = new CsrAttribute();
        csrAttribute2.setId(csrAttribute1.getId());
        assertThat(csrAttribute1).isEqualTo(csrAttribute2);
        csrAttribute2.setId(2L);
        assertThat(csrAttribute1).isNotEqualTo(csrAttribute2);
        csrAttribute1.setId(null);
        assertThat(csrAttribute1).isNotEqualTo(csrAttribute2);
    }
}
