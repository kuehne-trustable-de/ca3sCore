package de.trustable.ca3s.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.trustable.ca3s.core.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TimedElementNotificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TimedElementNotification.class);
        TimedElementNotification timedElementNotification1 = new TimedElementNotification();
        timedElementNotification1.setId(1L);
        TimedElementNotification timedElementNotification2 = new TimedElementNotification();
        timedElementNotification2.setId(timedElementNotification1.getId());
        assertThat(timedElementNotification1).isEqualTo(timedElementNotification2);
        timedElementNotification2.setId(2L);
        assertThat(timedElementNotification1).isNotEqualTo(timedElementNotification2);
        timedElementNotification1.setId(null);
        assertThat(timedElementNotification1).isNotEqualTo(timedElementNotification2);
    }
}
