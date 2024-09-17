package de.trustable.ca3s.core.config;

import de.trustable.ca3s.core.config.saml.DefaultSAMLBootstrap;
import de.trustable.ca3s.core.config.saml.SAMLMappingConfig;
import de.trustable.ca3s.core.config.util.SPeLUtil;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.security.saml.CustomSAMLAuthenticationProvider;
import de.trustable.ca3s.core.security.saml.CustomUrlAuthenticationSuccessHandler;
import org.apache.commons.codec.binary.Base64InputStream;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.signature.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Configuration
public class SamlSecurityConfig {

    private final Logger LOG = LoggerFactory.getLogger(SamlSecurityConfig.class);

    @Value("${ca3s.saml.keystore.location:#{null}}")
    private String samlKeystorePath;

    @Value("${ca3s.saml.keystore.password:s3cr3t}")
    private String samlKeystorePassword;

    @Value("${ca3s.saml.keystore.alias:saml-client}")
    private String samlKeystoreAlias;

    @Value("${ca3s.saml.idp}")
    private String defaultIdp;

    @Value("${ca3s.saml.metadata.location:#{null}}")
    private String ssoMetadataPath;

    @Value("${ca3s.saml.metadata.trust.check:true}")
    private boolean metadataTrustCheck;

    @Value("${ca3s.saml.metadata.requires.signature:false}")
    private boolean metadataRequireSignature;

    @Value("${ca3s.saml.metadata.trust.key.aliases:}")
    private Set<String> metadataTrustedKeyAliases;

    @Value("${ca3s.ui.sso.secureCookie:true}")
    private boolean secureCookie;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private SAMLMappingConfig samlMappingConfig;

    @Autowired
    private SPeLUtil sPeLUtil;

    @Autowired
    private TenantRepository tenantRepository;

    @Value("${ca3s.ui.languages:en,de,pl}")
    String availableLanguages;

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        return new CustomSAMLAuthenticationProvider(userPreferenceRepository,
            userRepository,
            authorityRepository,
            tenantRepository,
            sPeLUtil,
            availableLanguages,
            samlMappingConfig);
    }

    @Bean
    public SAMLContextProviderImpl contextProvider() {
        return new SAMLContextProviderImpl();
    }

    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    @Bean
    @Qualifier("hokWebSSOprofileConsumer")
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileECPImpl ecpProfile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutProfile() {
        return new SingleLogoutProfileImpl();
    }

    @Bean
    public KeyManager keyManager() {
        Resource keystoreResource;
        File samlKeystoreFile = null;
        if(this.samlKeystorePath != null ){
            samlKeystoreFile = new File(this.samlKeystorePath);
        }
        if (samlKeystoreFile == null || !samlKeystoreFile.exists()) {
            LOG.warn("Value of 'ca3s.saml.keystore.location' does not point to a valid location!");
            try {
                File tmpStoreFile = File.createTempFile("samlKeystore_", ".jks");
                LOG.debug("writing dummy keystore content to {}", tmpStoreFile.getAbsolutePath());
                tmpStoreFile.deleteOnExit();
                Files.copy(new Base64InputStream(new ByteArrayInputStream(DUMMY_KEYSTORE_BASE64.getBytes(StandardCharsets.UTF_8))),
                    tmpStoreFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                keystoreResource = new FileSystemResource(tmpStoreFile);
                samlKeystoreAlias = "saml-client";
                samlKeystorePassword = "s3cr3t";
            } catch (IOException ioex) {
                throw new RuntimeException("cannot access 'classpath:/saml/samlKeystore.jks'");
            }
        }else {
            keystoreResource = new FileSystemResource(samlKeystoreFile);
        }
        Map<String, String> passwords = new HashMap<>();
        passwords.put(samlKeystoreAlias, samlKeystorePassword);
        return new JKSKeyManager(keystoreResource, samlKeystorePassword, passwords, samlKeystoreAlias);
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();

        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }

    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }

    @Bean
    @Qualifier("okta")
    public ExtendedMetadataDelegate oktaExtendedMetadataProvider() throws MetadataProviderException {

        File ssoMetadataFile = null;
        if(this.ssoMetadataPath!= null) {
            ssoMetadataFile = new File(this.ssoMetadataPath);
        }
        if( ssoMetadataFile == null || !ssoMetadataFile.exists()){
            LOG.warn("Value of 'ca3s.saml.metadata.file' does not point to a valid location!");
            try {
                File tmpMetadataFile = File.createTempFile("dummy_metadata_", ".xml");
                LOG.debug("writing dummy metadata content to {}", tmpMetadataFile.getAbsolutePath());
                tmpMetadataFile.deleteOnExit();
                Files.copy(new ByteArrayInputStream(DUMMY_SSO_METADATA.getBytes(StandardCharsets.UTF_8)),
                    tmpMetadataFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                ssoMetadataFile = tmpMetadataFile;
            } catch (IOException ioex) {
                throw new MetadataProviderException(ioex);
            }
        }
        FilesystemMetadataProvider provider = new FilesystemMetadataProvider(ssoMetadataFile);

        provider.setParserPool(parserPool());
        ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(provider, extendedMetadata());

        extendedMetadataDelegate.setMetadataTrustCheck(metadataTrustCheck);
        extendedMetadataDelegate.setMetadataRequireSignature( metadataRequireSignature);
        if( (metadataTrustedKeyAliases != null) && !metadataTrustedKeyAliases.isEmpty()) {
            extendedMetadataDelegate.setMetadataTrustedKeys(metadataTrustedKeyAliases);
        }

        return extendedMetadataDelegate;
    }

    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException, ResourceException {
        List<MetadataProvider> providers = new ArrayList<>();
        providers.add(oktaExtendedMetadataProvider());
        CachingMetadataManager metadataManager = new CachingMetadataManager(providers);
        metadataManager.setDefaultIDP(defaultIdp);
        return metadataManager;
    }

    @Bean
    @Qualifier("saml")
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new CustomUrlAuthenticationSuccessHandler(tokenProvider, secureCookie);
        // successRedirectHandler.setDefaultTargetUrl("/");
        return successRedirectHandler;
    }

    @Bean
    @Qualifier("saml")
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new CustomSimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error/error-en.html");
        return failureHandler;
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/");
        return successLogoutHandler;
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
            new LogoutHandler[] { logoutHandler() },
            new LogoutHandler[] { logoutHandler() });
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), VelocityFactory.getEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new DefaultSAMLBootstrap("RSA",
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
            SignatureConstants.ALGO_ID_DIGEST_SHA256);
    }

    @Bean
    public SAMLProcessorImpl processor() {
        ArrayList<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        return new SAMLProcessorImpl(bindings);
    }

    final static String DUMMY_SSO_METADATA = "<md:EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" entityID=\"http://localhost:50080/realms/ca3s\">\n" +
        "    <md:IDPSSODescriptor WantAuthnRequestsSigned=\"true\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
        "        <md:KeyDescriptor use=\"signing\">\n" +
        "            <ds:KeyInfo>\n" +
        "                <ds:KeyName>txwFlIcmJmwIvrk7wPGPwUdZEQSTes3-d6iZXsetYoc</ds:KeyName>\n" +
        "                <ds:X509Data>\n" +
        "                    <ds:X509Certificate>MIIClzCCAX8CBgGKHs1qgjANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDARjYTNzMB4XDTIzMDgyMjE5NDk1M1oXDTMzMDgyMjE5NTEzM1owDzENMAsGA1UEAwwEY2EzczCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOtrSf9m6yiAQ3Wk4DXikrZ3SFI/m9htFIsyxCOq1Eo6KBWjcfy+LDN1dqmgV0zs/csGMBKOUS9jKVQNnEDvhxH5KkUF17ymY9ztVquwMee+FGYgPyeeN/Nfh2fCwHHXKSlzlRLea4XGMAkkcw551kvMH88EvlQGUfEqOSOUu7BmNT4jtD9nsoFg5AXpP8qzgTvQ+hBoU2x5dwCWR8exzfmLeyQmXSE7uX+49YNSE8rHP9cT1YSGlUN4nawuTZWVt/IwQoqyJM5BQwIiUJ5WazjqwXSyQ8BRh1/hmMBwQzGDLBDCKZF60nmMHSIALudCv/8SOdo2eDgp3hnFs9TON3kCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAdrwdYBISA50w+QkpuqsK5O1nV6wtBYYpMeC/XVkXVfCgb+0+r5vKqZCidTFwsQecBfQoy5p8clkjCeFQRtmFrMShiTC1fyXrWPGCZAywUwZK6q4+AfyvVZYhaze1+urv7IvnVK0VCVbFrsTxJ6mr6fG6Vfd3TOkejjRQstGjghqg+xkUlcj67T2UJDISpawTUz7IL/CfwhwgPwPAQ1X2nN7ZK2XQ8OzdZg6jGc3aRsg9jp5sKoB4ZJOqJJxdnJNCL1NsMKlyPu1h6526sCgBJI/BPyOfwHAACFcBWu1cOMsETdl0WccU5cyW3Y1rBZZ6jauR8mP8apAhykC3sUExCQ==</ds:X509Certificate>\n" +
        "                </ds:X509Data>\n" +
        "            </ds:KeyInfo>\n" +
        "        </md:KeyDescriptor>\n" +
        "        <md:ArtifactResolutionService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml/resolve\" index=\"0\"/>\n" +
        "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>\n" +
        "        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>\n" +
        "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" +
        "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>\n" +
        "        <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:SOAP\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "        <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact\" Location=\"http://localhost:50080/realms/ca3s/protocol/saml\"/>\n" +
        "    </md:IDPSSODescriptor>\n" +
        "</md:EntityDescriptor>\n";

    final static String DUMMY_KEYSTORE_BASE64 = "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" +
        "U/9+OgIDAZAA";
}

class CustomSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler{

    private final Logger LOG = LoggerFactory.getLogger(CustomSimpleUrlAuthenticationFailureHandler.class);

    public void onAuthenticationFailure(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, AuthenticationException exception) throws ServletException, IOException {
        LOG.debug("HttpServletRequest: {}", request);
        LOG.debug("HttpServletResponse: {}", response);
        LOG.debug("AuthenticationException:", exception);
        super.onAuthenticationFailure(request, response, exception);
    }

}
