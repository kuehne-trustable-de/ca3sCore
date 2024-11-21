package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class CertificateTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Certificate.class);
        Certificate certificate1 = new Certificate();
        certificate1.setId(1L);
        Certificate certificate2 = new Certificate();
        certificate2.setId(certificate1.getId());
        assertThat(certificate1).isEqualTo(certificate2);
        certificate2.setId(2L);
        assertThat(certificate1).isNotEqualTo(certificate2);
        certificate1.setId(null);
        assertThat(certificate1).isNotEqualTo(certificate2);

    }
    @Test
    public void hexVerifier() throws Exception {
        Certificate certificate1 = new Certificate();
        certificate1.setId(1L);
        certificate1.setSerial("123");

        assertThat(certificate1.getSerialAsHex()).isEqualTo("7b");

        Certificate certificate2 = new Certificate();
        certificate2.setId(2L);
        certificate2.setSerial("1226540993534688371776034528138601287326369210");
        assertThat(certificate2.getSerialAsHex()).isEqualTo("37000005bab15750dff0a3cd5d0000000005ba");

    }
}
