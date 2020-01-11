package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class CertificateAttributeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CertificateAttribute.class);
        CertificateAttribute certificateAttribute1 = new CertificateAttribute();
        certificateAttribute1.setId(1L);
        CertificateAttribute certificateAttribute2 = new CertificateAttribute();
        certificateAttribute2.setId(certificateAttribute1.getId());
        assertThat(certificateAttribute1).isEqualTo(certificateAttribute2);
        certificateAttribute2.setId(2L);
        assertThat(certificateAttribute1).isNotEqualTo(certificateAttribute2);
        certificateAttribute1.setId(null);
        assertThat(certificateAttribute1).isNotEqualTo(certificateAttribute2);
    }
}
