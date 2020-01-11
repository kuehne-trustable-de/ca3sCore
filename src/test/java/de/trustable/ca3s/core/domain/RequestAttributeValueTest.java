package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class RequestAttributeValueTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RequestAttributeValue.class);
        RequestAttributeValue requestAttributeValue1 = new RequestAttributeValue();
        requestAttributeValue1.setId(1L);
        RequestAttributeValue requestAttributeValue2 = new RequestAttributeValue();
        requestAttributeValue2.setId(requestAttributeValue1.getId());
        assertThat(requestAttributeValue1).isEqualTo(requestAttributeValue2);
        requestAttributeValue2.setId(2L);
        assertThat(requestAttributeValue1).isNotEqualTo(requestAttributeValue2);
        requestAttributeValue1.setId(null);
        assertThat(requestAttributeValue1).isNotEqualTo(requestAttributeValue2);
    }
}
