package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class ACMEAccountTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ACMEAccount.class);
        ACMEAccount aCMEAccount1 = new ACMEAccount();
        aCMEAccount1.setId(1L);
        ACMEAccount aCMEAccount2 = new ACMEAccount();
        aCMEAccount2.setId(aCMEAccount1.getId());
        assertThat(aCMEAccount1).isEqualTo(aCMEAccount2);
        aCMEAccount2.setId(2L);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
        aCMEAccount1.setId(null);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
    }
}
