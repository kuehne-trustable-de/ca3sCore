package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AuthorizationTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeAuthorization.class);
        AcmeAuthorization authorization1 = new AcmeAuthorization();
        authorization1.setId(1L);
        AcmeAuthorization authorization2 = new AcmeAuthorization();
        authorization2.setId(authorization1.getId());
        assertThat(authorization1).isEqualTo(authorization2);
        authorization2.setId(2L);
        assertThat(authorization1).isNotEqualTo(authorization2);
        authorization1.setId(null);
        assertThat(authorization1).isNotEqualTo(authorization2);
    }
}
