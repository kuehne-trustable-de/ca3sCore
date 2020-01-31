package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class AcmeIdentifierTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeIdentifier.class);
        AcmeIdentifier acmeIdentifier1 = new AcmeIdentifier();
        acmeIdentifier1.setId(1L);
        AcmeIdentifier acmeIdentifier2 = new AcmeIdentifier();
        acmeIdentifier2.setId(acmeIdentifier1.getId());
        assertThat(acmeIdentifier1).isEqualTo(acmeIdentifier2);
        acmeIdentifier2.setId(2L);
        assertThat(acmeIdentifier1).isNotEqualTo(acmeIdentifier2);
        acmeIdentifier1.setId(null);
        assertThat(acmeIdentifier1).isNotEqualTo(acmeIdentifier2);
    }
}
