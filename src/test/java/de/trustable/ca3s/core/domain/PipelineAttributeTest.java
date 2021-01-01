package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class PipelineAttributeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PipelineAttribute.class);
        PipelineAttribute pipelineAttribute1 = new PipelineAttribute();
        pipelineAttribute1.setId(1L);
        PipelineAttribute pipelineAttribute2 = new PipelineAttribute();
        pipelineAttribute2.setId(pipelineAttribute1.getId());
        assertThat(pipelineAttribute1).isEqualTo(pipelineAttribute2);
        pipelineAttribute2.setId(2L);
        assertThat(pipelineAttribute1).isNotEqualTo(pipelineAttribute2);
        pipelineAttribute1.setId(null);
        assertThat(pipelineAttribute1).isNotEqualTo(pipelineAttribute2);
    }
}
