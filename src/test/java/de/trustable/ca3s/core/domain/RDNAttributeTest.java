package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class RDNAttributeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RDNAttribute.class);
        RDNAttribute rDNAttribute1 = new RDNAttribute();
        rDNAttribute1.setId(1L);
        RDNAttribute rDNAttribute2 = new RDNAttribute();
        rDNAttribute2.setId(rDNAttribute1.getId());
        assertThat(rDNAttribute1).isEqualTo(rDNAttribute2);
        rDNAttribute2.setId(2L);
        assertThat(rDNAttribute1).isNotEqualTo(rDNAttribute2);
        rDNAttribute1.setId(null);
        assertThat(rDNAttribute1).isNotEqualTo(rDNAttribute2);
    }
}
