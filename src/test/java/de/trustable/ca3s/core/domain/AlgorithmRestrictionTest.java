package de.trustable.ca3s.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.trustable.ca3s.core.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlgorithmRestrictionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlgorithmRestriction.class);
        AlgorithmRestriction algorithmRestriction1 = new AlgorithmRestriction();
        algorithmRestriction1.setId(1L);
        AlgorithmRestriction algorithmRestriction2 = new AlgorithmRestriction();
        algorithmRestriction2.setId(algorithmRestriction1.getId());
        assertThat(algorithmRestriction1).isEqualTo(algorithmRestriction2);
        algorithmRestriction2.setId(2L);
        assertThat(algorithmRestriction1).isNotEqualTo(algorithmRestriction2);
        algorithmRestriction1.setId(null);
        assertThat(algorithmRestriction1).isNotEqualTo(algorithmRestriction2);
    }
}
