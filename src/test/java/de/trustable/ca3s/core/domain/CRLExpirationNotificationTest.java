package de.trustable.ca3s.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.trustable.ca3s.core.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CRLExpirationNotificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CRLExpirationNotification.class);
        CRLExpirationNotification cRLExpirationNotification1 = new CRLExpirationNotification();
        cRLExpirationNotification1.setId(1L);
        CRLExpirationNotification cRLExpirationNotification2 = new CRLExpirationNotification();
        cRLExpirationNotification2.setId(cRLExpirationNotification1.getId());
        assertThat(cRLExpirationNotification1).isEqualTo(cRLExpirationNotification2);
        cRLExpirationNotification2.setId(2L);
        assertThat(cRLExpirationNotification1).isNotEqualTo(cRLExpirationNotification2);
        cRLExpirationNotification1.setId(null);
        assertThat(cRLExpirationNotification1).isNotEqualTo(cRLExpirationNotification2);
    }
}
