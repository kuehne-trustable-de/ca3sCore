package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeAccountTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeAccount.class);
        AcmeAccount aCMEAccount1 = new AcmeAccount();
        aCMEAccount1.setId(1L);
        AcmeAccount aCMEAccount2 = new AcmeAccount();
        aCMEAccount2.setId(aCMEAccount1.getId());
        assertThat(aCMEAccount1).isEqualTo(aCMEAccount2);
        aCMEAccount2.setId(2L);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
        aCMEAccount1.setId(null);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
    }
}
