package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class CAConnectorConfigTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CAConnectorConfig.class);
        CAConnectorConfig cAConnectorConfig1 = new CAConnectorConfig();
        cAConnectorConfig1.setId(1L);
        CAConnectorConfig cAConnectorConfig2 = new CAConnectorConfig();
        cAConnectorConfig2.setId(cAConnectorConfig1.getId());
        assertThat(cAConnectorConfig1).isEqualTo(cAConnectorConfig2);
        cAConnectorConfig2.setId(2L);
        assertThat(cAConnectorConfig1).isNotEqualTo(cAConnectorConfig2);
        cAConnectorConfig1.setId(null);
        assertThat(cAConnectorConfig1).isNotEqualTo(cAConnectorConfig2);
    }
}
