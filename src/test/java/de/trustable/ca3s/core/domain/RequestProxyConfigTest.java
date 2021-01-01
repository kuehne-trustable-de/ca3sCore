package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class RequestProxyConfigTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RequestProxyConfig.class);
        RequestProxyConfig requestProxyConfig1 = new RequestProxyConfig();
        requestProxyConfig1.setId(1L);
        RequestProxyConfig requestProxyConfig2 = new RequestProxyConfig();
        requestProxyConfig2.setId(requestProxyConfig1.getId());
        assertThat(requestProxyConfig1).isEqualTo(requestProxyConfig2);
        requestProxyConfig2.setId(2L);
        assertThat(requestProxyConfig1).isNotEqualTo(requestProxyConfig2);
        requestProxyConfig1.setId(null);
        assertThat(requestProxyConfig1).isNotEqualTo(requestProxyConfig2);
    }
}
