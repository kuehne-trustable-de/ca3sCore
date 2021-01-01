package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class BPNMProcessInfoTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BPNMProcessInfo.class);
        BPNMProcessInfo bPNMProcessInfo1 = new BPNMProcessInfo();
        bPNMProcessInfo1.setId(1L);
        BPNMProcessInfo bPNMProcessInfo2 = new BPNMProcessInfo();
        bPNMProcessInfo2.setId(bPNMProcessInfo1.getId());
        assertThat(bPNMProcessInfo1).isEqualTo(bPNMProcessInfo2);
        bPNMProcessInfo2.setId(2L);
        assertThat(bPNMProcessInfo1).isNotEqualTo(bPNMProcessInfo2);
        bPNMProcessInfo1.setId(null);
        assertThat(bPNMProcessInfo1).isNotEqualTo(bPNMProcessInfo2);
    }
}
