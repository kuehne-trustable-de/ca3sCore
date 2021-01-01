package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeChallengeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeChallenge.class);
        AcmeChallenge acmeChallenge1 = new AcmeChallenge();
        acmeChallenge1.setId(1L);
        AcmeChallenge acmeChallenge2 = new AcmeChallenge();
        acmeChallenge2.setId(acmeChallenge1.getId());
        assertThat(acmeChallenge1).isEqualTo(acmeChallenge2);
        acmeChallenge2.setId(2L);
        assertThat(acmeChallenge1).isNotEqualTo(acmeChallenge2);
        acmeChallenge1.setId(null);
        assertThat(acmeChallenge1).isNotEqualTo(acmeChallenge2);
    }
}
