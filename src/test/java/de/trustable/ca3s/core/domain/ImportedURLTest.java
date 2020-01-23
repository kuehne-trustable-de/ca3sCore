package de.trustable.ca3s.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.trustable.ca3s.core.web.rest.TestUtil;

public class ImportedURLTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ImportedURL.class);
        ImportedURL importedURL1 = new ImportedURL();
        importedURL1.setId(1L);
        ImportedURL importedURL2 = new ImportedURL();
        importedURL2.setId(importedURL1.getId());
        assertThat(importedURL1).isEqualTo(importedURL2);
        importedURL2.setId(2L);
        assertThat(importedURL1).isNotEqualTo(importedURL2);
        importedURL1.setId(null);
        assertThat(importedURL1).isNotEqualTo(importedURL2);
    }
}
