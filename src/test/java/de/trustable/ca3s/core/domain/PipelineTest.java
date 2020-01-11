package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class PipelineTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pipeline.class);
        Pipeline pipeline1 = new Pipeline();
        pipeline1.setId(1L);
        Pipeline pipeline2 = new Pipeline();
        pipeline2.setId(pipeline1.getId());
        assertThat(pipeline1).isEqualTo(pipeline2);
        pipeline2.setId(2L);
        assertThat(pipeline1).isNotEqualTo(pipeline2);
        pipeline1.setId(null);
        assertThat(pipeline1).isNotEqualTo(pipeline2);
    }
}
