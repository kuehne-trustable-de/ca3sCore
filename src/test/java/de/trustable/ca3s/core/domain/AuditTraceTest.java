package de.trustable.ca3s.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.trustable.ca3s.core.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditTraceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditTrace.class);
        AuditTrace auditTrace1 = new AuditTrace();
        auditTrace1.setId(1L);
        AuditTrace auditTrace2 = new AuditTrace();
        auditTrace2.setId(auditTrace1.getId());
        assertThat(auditTrace1).isEqualTo(auditTrace2);
        auditTrace2.setId(2L);
        assertThat(auditTrace1).isNotEqualTo(auditTrace2);
        auditTrace1.setId(null);
        assertThat(auditTrace1).isNotEqualTo(auditTrace2);
    }
}
