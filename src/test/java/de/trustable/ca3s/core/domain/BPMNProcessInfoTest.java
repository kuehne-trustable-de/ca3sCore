package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class BPMNProcessInfoTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BPMNProcessInfo.class);
        BPMNProcessInfo bPNMProcessInfo1 = new BPMNProcessInfo();
        bPNMProcessInfo1.setId(1L);
        BPMNProcessInfo bPNMProcessInfo2 = new BPMNProcessInfo();
        bPNMProcessInfo2.setId(bPNMProcessInfo1.getId());
        assertThat(bPNMProcessInfo1).isEqualTo(bPNMProcessInfo2);
        bPNMProcessInfo2.setId(2L);
        assertThat(bPNMProcessInfo1).isNotEqualTo(bPNMProcessInfo2);
        bPNMProcessInfo1.setId(null);
        assertThat(bPNMProcessInfo1).isNotEqualTo(bPNMProcessInfo2);
    }
}
