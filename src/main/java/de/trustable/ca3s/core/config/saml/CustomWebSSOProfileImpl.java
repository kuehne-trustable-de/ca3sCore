package de.trustable.ca3s.core.config.saml;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.websso.WebSSOProfileImpl;

import java.net.URI;
import java.net.URISyntaxException;

public class CustomWebSSOProfileImpl extends WebSSOProfileImpl {

    private final Logger LOG = LoggerFactory.getLogger(CustomWebSSOProfileImpl.class);

    final private URI targetAddressURI;

    public CustomWebSSOProfileImpl(URI targetAddressURI) {
        this.targetAddressURI = targetAddressURI;
    }

    protected void buildReturnAddress(org.opensaml.saml2.core.AuthnRequest request, org.opensaml.saml2.metadata.AssertionConsumerService service) throws MetadataProviderException {
        LOG.info("Current SAML service location '{}'", service.getLocation() );
        super.buildReturnAddress(request, service);

        if( targetAddressURI != null) {
            try {
                URI returnAddressURL = new URI(request.getAssertionConsumerServiceURL());
                URI normalizedAddressURL = new URI(targetAddressURI.getScheme(), returnAddressURL.getUserInfo(),
                    targetAddressURI.getHost(), targetAddressURI.getPort(),
                    returnAddressURL.getPath(), returnAddressURL.getQuery(), returnAddressURL.getFragment());

                request.setAssertionConsumerServiceURL(normalizedAddressURL.toString());

            } catch (URISyntaxException e) {
                LOG.error("AssertionConsumerServiceURL could not be parsed! Ignoring the value.", e);
            }
        }
    }
}
