package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.repository.RequestProxyConfigRepository;
import de.trustable.ca3s.core.service.RequestProxyConfigService;
import de.trustable.ca3s.core.service.util.JWSService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RequestProxyConfigResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class RequestProxyConfigResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_PROXY_URL = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_PROXY_URL = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Autowired
    private RequestProxyConfigRepository requestProxyConfigRepository;

    @Autowired
    private RequestProxyConfigService requestProxyConfigService;

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

    private MockMvc restRequestProxyConfigMockMvc;

    private RequestProxyConfig requestProxyConfig;

    private JWSService jwsService = mock(JWSService.class);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RequestProxyConfigResource requestProxyConfigResource = new RequestProxyConfigResource(requestProxyConfigService, jwsService);
        this.restRequestProxyConfigMockMvc = MockMvcBuilders.standaloneSetup(requestProxyConfigResource)
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
    public static RequestProxyConfig createEntity(EntityManager em) {
        RequestProxyConfig requestProxyConfig = new RequestProxyConfig()
            .name(DEFAULT_NAME)
            .requestProxyUrl(DEFAULT_REQUEST_PROXY_URL)
            .active(DEFAULT_ACTIVE);
        return requestProxyConfig;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RequestProxyConfig createUpdatedEntity(EntityManager em) {
        RequestProxyConfig requestProxyConfig = new RequestProxyConfig()
            .name(UPDATED_NAME)
            .requestProxyUrl(UPDATED_REQUEST_PROXY_URL)
            .active(UPDATED_ACTIVE);
        return requestProxyConfig;
    }

    @BeforeEach
    public void initTest() {
        requestProxyConfig = createEntity(em);
    }

    @Test
    @Transactional
    public void createRequestProxyConfig() throws Exception {
        int databaseSizeBeforeCreate = requestProxyConfigRepository.findAll().size();

        // Create the RequestProxyConfig
        restRequestProxyConfigMockMvc.perform(post("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestProxyConfig)))
            .andExpect(status().isCreated());

        // Validate the RequestProxyConfig in the database
        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeCreate + 1);
        RequestProxyConfig testRequestProxyConfig = requestProxyConfigList.get(requestProxyConfigList.size() - 1);
        assertThat(testRequestProxyConfig.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRequestProxyConfig.getRequestProxyUrl()).isEqualTo(DEFAULT_REQUEST_PROXY_URL);
        assertThat(testRequestProxyConfig.isActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    public void createRequestProxyConfigWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = requestProxyConfigRepository.findAll().size();

        // Create the RequestProxyConfig with an existing ID
        requestProxyConfig.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRequestProxyConfigMockMvc.perform(post("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestProxyConfig)))
            .andExpect(status().isBadRequest());

        // Validate the RequestProxyConfig in the database
        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestProxyConfigRepository.findAll().size();
        // set the field null
        requestProxyConfig.setName(null);

        // Create the RequestProxyConfig, which fails.

        restRequestProxyConfigMockMvc.perform(post("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestProxyConfig)))
            .andExpect(status().isBadRequest());

        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestProxyUrlIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestProxyConfigRepository.findAll().size();
        // set the field null
        requestProxyConfig.setRequestProxyUrl(null);

        // Create the RequestProxyConfig, which fails.

        restRequestProxyConfigMockMvc.perform(post("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestProxyConfig)))
            .andExpect(status().isBadRequest());

        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRequestProxyConfigs() throws Exception {
        // Initialize the database
        requestProxyConfigRepository.saveAndFlush(requestProxyConfig);

        // Get all the requestProxyConfigList
        restRequestProxyConfigMockMvc.perform(get("/api/request-proxy-configs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect((content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE)))
            .andExpect(jsonPath("$.[*].id").value(hasItem(requestProxyConfig.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].requestProxyUrl").value(hasItem(DEFAULT_REQUEST_PROXY_URL)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getRequestProxyConfig() throws Exception {
        // Initialize the database
        requestProxyConfigRepository.saveAndFlush(requestProxyConfig);

        // Get the requestProxyConfig
        restRequestProxyConfigMockMvc.perform(get("/api/request-proxy-configs/{id}", requestProxyConfig.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(requestProxyConfig.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.requestProxyUrl").value(DEFAULT_REQUEST_PROXY_URL))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingRequestProxyConfig() throws Exception {
        // Get the requestProxyConfig
        restRequestProxyConfigMockMvc.perform(get("/api/request-proxy-configs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRequestProxyConfig() throws Exception {
        // Initialize the database
        requestProxyConfigService.save(requestProxyConfig);

        int databaseSizeBeforeUpdate = requestProxyConfigRepository.findAll().size();

        // Update the requestProxyConfig
        RequestProxyConfig updatedRequestProxyConfig = requestProxyConfigRepository.findById(requestProxyConfig.getId()).get();
        // Disconnect from session so that the updates on updatedRequestProxyConfig are not directly saved in db
        em.detach(updatedRequestProxyConfig);
        updatedRequestProxyConfig
            .name(UPDATED_NAME)
            .requestProxyUrl(UPDATED_REQUEST_PROXY_URL)
            .active(UPDATED_ACTIVE);

        restRequestProxyConfigMockMvc.perform(put("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRequestProxyConfig)))
            .andExpect(status().isOk());

        // Validate the RequestProxyConfig in the database
        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeUpdate);
        RequestProxyConfig testRequestProxyConfig = requestProxyConfigList.get(requestProxyConfigList.size() - 1);
        assertThat(testRequestProxyConfig.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRequestProxyConfig.getRequestProxyUrl()).isEqualTo(UPDATED_REQUEST_PROXY_URL);
        assertThat(testRequestProxyConfig.isActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void updateNonExistingRequestProxyConfig() throws Exception {
        int databaseSizeBeforeUpdate = requestProxyConfigRepository.findAll().size();

        // Create the RequestProxyConfig

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRequestProxyConfigMockMvc.perform(put("/api/request-proxy-configs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestProxyConfig)))
            .andExpect(status().isBadRequest());

        // Validate the RequestProxyConfig in the database
        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRequestProxyConfig() throws Exception {
        // Initialize the database
        requestProxyConfigService.save(requestProxyConfig);

        int databaseSizeBeforeDelete = requestProxyConfigRepository.findAll().size();

        // Delete the requestProxyConfig
        restRequestProxyConfigMockMvc.perform(delete("/api/request-proxy-configs/{id}", requestProxyConfig.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RequestProxyConfig> requestProxyConfigList = requestProxyConfigRepository.findAll();
        assertThat(requestProxyConfigList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
