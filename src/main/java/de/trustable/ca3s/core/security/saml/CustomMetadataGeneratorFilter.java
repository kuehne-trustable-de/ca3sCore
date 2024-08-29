package de.trustable.ca3s.core.security.saml;

import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;

public class CustomMetadataGeneratorFilter extends MetadataGeneratorFilter {
    public CustomMetadataGeneratorFilter(MetadataGenerator generator) {
        super(generator);
    }
}
