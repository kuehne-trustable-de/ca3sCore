package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeAuthorizationTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeAuthorization.class);
        AcmeAuthorization acmeAuthorization1 = new AcmeAuthorization();
        acmeAuthorization1.setId(1L);
        AcmeAuthorization acmeAuthorization2 = new AcmeAuthorization();
        acmeAuthorization2.setId(acmeAuthorization1.getId());
        assertThat(acmeAuthorization1).isEqualTo(acmeAuthorization2);
        acmeAuthorization2.setId(2L);
        assertThat(acmeAuthorization1).isNotEqualTo(acmeAuthorization2);
        acmeAuthorization1.setId(null);
        assertThat(acmeAuthorization1).isNotEqualTo(acmeAuthorization2);
    }
}
