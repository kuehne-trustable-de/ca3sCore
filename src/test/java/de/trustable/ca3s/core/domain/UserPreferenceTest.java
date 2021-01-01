package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class UserPreferenceTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserPreference.class);
        UserPreference userPreference1 = new UserPreference();
        userPreference1.setId(1L);
        UserPreference userPreference2 = new UserPreference();
        userPreference2.setId(userPreference1.getId());
        assertThat(userPreference1).isEqualTo(userPreference2);
        userPreference2.setId(2L);
        assertThat(userPreference1).isNotEqualTo(userPreference2);
        userPreference1.setId(null);
        assertThat(userPreference1).isNotEqualTo(userPreference2);
    }
}
