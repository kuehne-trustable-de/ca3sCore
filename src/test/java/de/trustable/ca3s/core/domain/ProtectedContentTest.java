package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class ProtectedContentTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProtectedContent.class);
        ProtectedContent protectedContent1 = new ProtectedContent();
        protectedContent1.setId(1L);
        ProtectedContent protectedContent2 = new ProtectedContent();
        protectedContent2.setId(protectedContent1.getId());
        assertThat(protectedContent1).isEqualTo(protectedContent2);
        protectedContent2.setId(2L);
        assertThat(protectedContent1).isNotEqualTo(protectedContent2);
        protectedContent1.setId(null);
        assertThat(protectedContent1).isNotEqualTo(protectedContent2);
    }
}
