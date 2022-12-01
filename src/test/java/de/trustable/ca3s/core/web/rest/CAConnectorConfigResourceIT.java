package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.CAConnectorConfigService;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;
/**
 * Integration tests for the {@link CAConnectorConfigResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev")
public class CAConnectorConfigResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final CAConnectorType DEFAULT_CA_CONNECTOR_TYPE = CAConnectorType.INTERNAL;
    private static final CAConnectorType UPDATED_CA_CONNECTOR_TYPE = CAConnectorType.CMP;

    private static final String DEFAULT_CA_URL = "AAAAAAAAAA";
    private static final String UPDATED_CA_URL = "BBBBBBBBBB";

    private static final Integer DEFAULT_POLLING_OFFSET = 1;
    private static final Integer UPDATED_POLLING_OFFSET = 2;

    private static final Boolean DEFAULT_DEFAULT_CA = false;
    private static final Boolean UPDATED_DEFAULT_CA = true;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_SELECTOR = "AAAAAAAAAA";
    private static final String UPDATED_SELECTOR = "BBBBBBBBBB";

    private static final Interval DEFAULT_INTERVAL = Interval.MINUTE;
    private static final Interval UPDATED_INTERVAL = Interval.HOUR;

    private static final String DEFAULT_PLAIN_SECRET = "AAAAAAAAAA";
    private static final String UPDATED_PLAIN_SECRET = "BBBBBBBBBB";

    @Autowired
    private CAConnectorConfigRepository cAConnectorConfigRepository;

    @Autowired
    private CAConnectorConfigService cAConnectorConfigService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    @Autowired
    private ProtectedContentUtil protUtil;

    @Autowired
    private ProtectedContentRepository protContentRepository;

    @Autowired
    private CAConnectorConfigRepository caConfigRepository;

    @Autowired
    private CaConnectorAdapter caConnectorAdapter;

    @Autowired
    private AuditService auditService;

    private MockMvc restCAConnectorConfigMockMvc;

    private CAConnectorConfig cAConnectorConfig;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CAConnectorConfigResource cAConnectorConfigResource = new CAConnectorConfigResource(cAConnectorConfigService, protUtil, protContentRepository, caConfigRepository, caConnectorAdapter, auditService);

        this.restCAConnectorConfigMockMvc = MockMvcBuilders.standaloneSetup(cAConnectorConfigResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CAConnectorConfig createEntity(EntityManager em) {
        CAConnectorConfig cAConnectorConfig = new CAConnectorConfig()
            .name(DEFAULT_NAME)
            .caConnectorType(DEFAULT_CA_CONNECTOR_TYPE)
            .caUrl(DEFAULT_CA_URL)
            .pollingOffset(DEFAULT_POLLING_OFFSET)
            .defaultCA(DEFAULT_DEFAULT_CA)
            .active(DEFAULT_ACTIVE)
            .selector(DEFAULT_SELECTOR)
            .interval(DEFAULT_INTERVAL)
            .plainSecret(DEFAULT_PLAIN_SECRET);
        return cAConnectorConfig;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CAConnectorConfig createUpdatedEntity(EntityManager em) {
        CAConnectorConfig cAConnectorConfig = new CAConnectorConfig()
            .name(UPDATED_NAME)
            .caConnectorType(UPDATED_CA_CONNECTOR_TYPE)
            .caUrl(UPDATED_CA_URL)
            .pollingOffset(UPDATED_POLLING_OFFSET)
            .defaultCA(UPDATED_DEFAULT_CA)
            .active(UPDATED_ACTIVE)
            .selector(UPDATED_SELECTOR)
            .interval(UPDATED_INTERVAL)
            .plainSecret(UPDATED_PLAIN_SECRET);
        return cAConnectorConfig;
    }

    @BeforeEach
    public void initTest() {
        cAConnectorConfig = createEntity(em);
    }

    @Test
    @Transactional
    public void createCAConnectorConfig() throws Exception {
        int databaseSizeBeforeCreate = cAConnectorConfigRepository.findAll().size();

        // Create the CAConnectorConfig
        restCAConnectorConfigMockMvc.perform(post("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cAConnectorConfig)))
            .andExpect(status().isCreated());

        // Validate the CAConnectorConfig in the database
        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeCreate + 1);
        CAConnectorConfig testCAConnectorConfig = cAConnectorConfigList.get(cAConnectorConfigList.size() - 1);
        assertThat(testCAConnectorConfig.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCAConnectorConfig.getCaConnectorType()).isEqualTo(DEFAULT_CA_CONNECTOR_TYPE);
        assertThat(testCAConnectorConfig.getCaUrl()).isEqualTo(DEFAULT_CA_URL);
        assertThat(testCAConnectorConfig.getPollingOffset()).isEqualTo(DEFAULT_POLLING_OFFSET);
        assertThat(testCAConnectorConfig.isDefaultCA()).isEqualTo(DEFAULT_DEFAULT_CA);
        assertThat(testCAConnectorConfig.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testCAConnectorConfig.getSelector()).isEqualTo(DEFAULT_SELECTOR);
        assertThat(testCAConnectorConfig.getInterval()).isEqualTo(DEFAULT_INTERVAL);

        assertThat(testCAConnectorConfig.getPlainSecret()).isEqualTo(CAConnectorConfigResource.PLAIN_SECRET_PLACEHOLDER);
    }

    @Test
    @Transactional
    public void createCAConnectorConfigWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = cAConnectorConfigRepository.findAll().size();

        // Create the CAConnectorConfig with an existing ID
        cAConnectorConfig.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCAConnectorConfigMockMvc.perform(post("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cAConnectorConfig)))
            .andExpect(status().isBadRequest());

        // Validate the CAConnectorConfig in the database
        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = cAConnectorConfigRepository.findAll().size();
        // set the field null
        cAConnectorConfig.setName(null);

        // Create the CAConnectorConfig, which fails.

        restCAConnectorConfigMockMvc.perform(post("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cAConnectorConfig)))
            .andExpect(status().isBadRequest());

        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCaConnectorTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = cAConnectorConfigRepository.findAll().size();
        // set the field null
        cAConnectorConfig.setCaConnectorType(null);

        // Create the CAConnectorConfig, which fails.

        restCAConnectorConfigMockMvc.perform(post("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cAConnectorConfig)))
            .andExpect(status().isBadRequest());

        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCAConnectorConfigs() throws Exception {
        // Initialize the database
        cAConnectorConfigRepository.saveAndFlush(cAConnectorConfig);

        // Get all the cAConnectorConfigList
        restCAConnectorConfigMockMvc.perform(get("/api/ca-connector-configs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cAConnectorConfig.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].caConnectorType").value(hasItem(DEFAULT_CA_CONNECTOR_TYPE.toString())))
            .andExpect(jsonPath("$.[*].caUrl").value(hasItem(DEFAULT_CA_URL)))
            .andExpect(jsonPath("$.[*].pollingOffset").value(hasItem(DEFAULT_POLLING_OFFSET)))
            .andExpect(jsonPath("$.[*].defaultCA").value(hasItem(DEFAULT_DEFAULT_CA.booleanValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].selector").value(hasItem(DEFAULT_SELECTOR)))
            .andExpect(jsonPath("$.[*].interval").value(hasItem(DEFAULT_INTERVAL.toString())))
            .andExpect(jsonPath("$.[*].plainSecret").value(hasItem(DEFAULT_PLAIN_SECRET)));
    }

    @Test
    @Transactional
    public void getCAConnectorConfig() throws Exception {
        // Initialize the database
        cAConnectorConfigRepository.saveAndFlush(cAConnectorConfig);

        // Get the cAConnectorConfig
        restCAConnectorConfigMockMvc.perform(get("/api/ca-connector-configs/{id}", cAConnectorConfig.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(cAConnectorConfig.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.caConnectorType").value(DEFAULT_CA_CONNECTOR_TYPE.toString()))
            .andExpect(jsonPath("$.caUrl").value(DEFAULT_CA_URL))
            .andExpect(jsonPath("$.pollingOffset").value(DEFAULT_POLLING_OFFSET))
            .andExpect(jsonPath("$.defaultCA").value(DEFAULT_DEFAULT_CA.booleanValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.selector").value(DEFAULT_SELECTOR))
            .andExpect(jsonPath("$.interval").value(DEFAULT_INTERVAL.toString()))
            .andExpect(jsonPath("$.plainSecret").value(DEFAULT_PLAIN_SECRET));
    }

    @Test
    @Transactional
    public void getNonExistingCAConnectorConfig() throws Exception {
        // Get the cAConnectorConfig
        restCAConnectorConfigMockMvc.perform(get("/api/ca-connector-configs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCAConnectorConfig() throws Exception {
        // Initialize the database
        cAConnectorConfigService.save(cAConnectorConfig);

        int databaseSizeBeforeUpdate = cAConnectorConfigRepository.findAll().size();

        // Update the cAConnectorConfig
        CAConnectorConfig updatedCAConnectorConfig = cAConnectorConfigRepository.findById(cAConnectorConfig.getId()).get();
        // Disconnect from session so that the updates on updatedCAConnectorConfig are not directly saved in db
        em.detach(updatedCAConnectorConfig);
        updatedCAConnectorConfig
            .name(UPDATED_NAME)
            .caConnectorType(UPDATED_CA_CONNECTOR_TYPE)
            .caUrl(UPDATED_CA_URL)
            .pollingOffset(UPDATED_POLLING_OFFSET)
            .defaultCA(UPDATED_DEFAULT_CA)
            .active(UPDATED_ACTIVE)
            .selector(UPDATED_SELECTOR)
            .interval(UPDATED_INTERVAL)
            .plainSecret(UPDATED_PLAIN_SECRET);

        restCAConnectorConfigMockMvc.perform(put("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCAConnectorConfig)))
            .andExpect(status().isOk());

        // Validate the CAConnectorConfig in the database
        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeUpdate);
        CAConnectorConfig testCAConnectorConfig = cAConnectorConfigList.get(cAConnectorConfigList.size() - 1);
        assertThat(testCAConnectorConfig.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCAConnectorConfig.getCaConnectorType()).isEqualTo(UPDATED_CA_CONNECTOR_TYPE);
        assertThat(testCAConnectorConfig.getCaUrl()).isEqualTo(UPDATED_CA_URL);
        assertThat(testCAConnectorConfig.getPollingOffset()).isEqualTo(UPDATED_POLLING_OFFSET);
        assertThat(testCAConnectorConfig.isDefaultCA()).isEqualTo(UPDATED_DEFAULT_CA);
        assertThat(testCAConnectorConfig.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testCAConnectorConfig.getSelector()).isEqualTo(UPDATED_SELECTOR);
        assertThat(testCAConnectorConfig.getInterval()).isEqualTo(UPDATED_INTERVAL);
        assertThat(testCAConnectorConfig.getPlainSecret()).isEqualTo(CAConnectorConfigResource.PLAIN_SECRET_PLACEHOLDER);
    }

    @Test
    @Transactional
    public void updateNonExistingCAConnectorConfig() throws Exception {
        int databaseSizeBeforeUpdate = cAConnectorConfigRepository.findAll().size();

        // Create the CAConnectorConfig

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCAConnectorConfigMockMvc.perform(put("/api/ca-connector-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cAConnectorConfig)))
            .andExpect(status().isBadRequest());

        // Validate the CAConnectorConfig in the database
        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCAConnectorConfig() throws Exception {
        // Initialize the database
        cAConnectorConfigService.save(cAConnectorConfig);

        int databaseSizeBeforeDelete = cAConnectorConfigRepository.findAll().size();

        // Delete the cAConnectorConfig
        restCAConnectorConfigMockMvc.perform(delete("/api/ca-connector-configs/{id}", cAConnectorConfig.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CAConnectorConfig> cAConnectorConfigList = cAConnectorConfigRepository.findAll();
        assertThat(cAConnectorConfigList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
