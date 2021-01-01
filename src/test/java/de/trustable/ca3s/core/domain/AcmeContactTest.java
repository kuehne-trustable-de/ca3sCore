package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeContactTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeContact.class);
        AcmeContact acmeContact1 = new AcmeContact();
        acmeContact1.setId(1L);
        AcmeContact acmeContact2 = new AcmeContact();
        acmeContact2.setId(acmeContact1.getId());
        assertThat(acmeContact1).isEqualTo(acmeContact2);
        acmeContact2.setId(2L);
        assertThat(acmeContact1).isNotEqualTo(acmeContact2);
        acmeContact1.setId(null);
        assertThat(acmeContact1).isNotEqualTo(acmeContact2);
    }
}
