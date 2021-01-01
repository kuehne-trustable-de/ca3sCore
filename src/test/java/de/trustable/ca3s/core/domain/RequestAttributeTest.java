package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class RequestAttributeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RequestAttribute.class);
        RequestAttribute requestAttribute1 = new RequestAttribute();
        requestAttribute1.setId(1L);
        RequestAttribute requestAttribute2 = new RequestAttribute();
        requestAttribute2.setId(requestAttribute1.getId());
        assertThat(requestAttribute1).isEqualTo(requestAttribute2);
        requestAttribute2.setId(2L);
        assertThat(requestAttribute1).isNotEqualTo(requestAttribute2);
        requestAttribute1.setId(null);
        assertThat(requestAttribute1).isNotEqualTo(requestAttribute2);
    }
}
