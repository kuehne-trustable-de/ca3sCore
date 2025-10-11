package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AsyncNotificationService;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.KeyGenerationService;
import de.trustable.ca3s.core.service.badkeys.BadKeysService;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.TypedValue;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.ca3s.core.service.dto.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
import de.trustable.ca3s.core.service.dto.X509CertificateHolderShallow;
import de.trustable.ca3s.core.web.rest.support.ContentUploadProcessor;
import de.trustable.util.CryptoUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Integration tests for the {@link CertificateResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class CertificateUploadIT {


    @Autowired
    private CryptoUtil cryptoUtil;
    @Autowired
    private ProtectedContentUtil protUtil;
    @Autowired
    private CertificateUtil certUtil;
    @Autowired
    private CSRUtil csrUtil;
    @Autowired
    private BPMNUtil bpmnUtil;
    @Autowired
    private CSRRepository csrRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private PipelineRepository pipelineRepository;
    @Autowired
    private PipelineUtil pipelineUtil;
    @Autowired
    private PreferenceUtil preferenceUtil;
    @Autowired
    private CertificateProcessingUtil cpUtil;
    @Autowired
    private AsyncNotificationService notificationService;
    @Autowired
    private BadKeysService badKeysService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private KeyGenerationService keyGenerationService;

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    Pipeline webDirectPipeline;

    ContentUploadProcessor target;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        target = new ContentUploadProcessor( cryptoUtil,
             protUtil,
             certUtil,
             csrUtil,
             bpmnUtil,
            csrRepository,
             certificateRepository,
             userUtil,
             pipelineRepository,
             pipelineUtil,
             preferenceUtil,
             cpUtil,
             notificationService,
             badKeysService,
             auditService,
            "^(.{6,10})$",
            keyGenerationService);
    }

    @BeforeEach
    public void init() throws InterruptedException {

        webDirectPipeline = ptc.getInternalWebDirectTestPipeline();
        ptc.getInternalWebRACheckTestPipeline();
        prefTC.getTestUserPreference();
    }

    @Test
    @Transactional
    public void requestServersideCertificate() {

        Authentication authentication = createAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setSecret("abcef");
        ResponseEntity<PkcsXXData> response = target.buildServerSideKeyAndRequest( uploaded, "requestorName");
        Assertions.assertEquals(400, response.getStatusCodeValue());

        uploaded = new UploadPrecheckData();
        uploaded.setSecret("Abcef12");
        uploaded.setPipelineId( webDirectPipeline.getId());
        uploaded.setKeyAlgoLength("RSA-4096");

        uploaded.setTosAgreed(true);

        NamedValues nvs = new NamedValues();
        nvs.setName("CN");
        TypedValue tv = new TypedValue();
        tv.setType("CN");
        tv.setValue("foo.test.org");
        TypedValue[] tvArr = new TypedValue[]{tv};
        nvs.setValues(tvArr);
        NamedValues[] nvsArr = new NamedValues[]{nvs};
        uploaded.setCertificateAttributes(nvsArr);

        uploaded.setArAttributes(new NamedValues[0]);

        response = target.buildServerSideKeyAndRequest( uploaded, "requestorName");

        Assertions.assertEquals(201, response.getStatusCodeValue());

        Assertions.assertNotNull( response.getBody());
        Assertions.assertNotNull( response.getBody().getCertsHolder());
        Assertions.assertTrue( response.getBody().getCertsHolder().length > 0);

        X509CertificateHolderShallow x509CertificateHolderShallow = response.getBody().getCertsHolder()[0];

        Assertions.assertEquals("V3", x509CertificateHolderShallow.getType());

    }

    private Authentication createAuthentication() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        return new UsernamePasswordAuthenticationToken("user", "user", authorities);
    }
}
