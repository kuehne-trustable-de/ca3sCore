package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import de.trustable.ca3s.core.service.BPMNProcessInfoService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link BPMNProcessInfoResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class BPMNProcessInfoResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_VERSION = "BBBBBBBBBB";

    private static final BPMNProcessType DEFAULT_TYPE = BPMNProcessType.CA_INVOCATION;
    private static final BPMNProcessType UPDATED_TYPE = BPMNProcessType.REQUEST_AUTHORIZATION;

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_CHANGE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHANGE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_SIGNATURE_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_SIGNATURE_BASE_64 = "BBBBBBBBBB";

    @Autowired
    private BPMNProcessInfoRepository bPMNProcessInfoRepository;

    @Autowired
    private BPMNProcessInfoService bPMNProcessInfoService;

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

    private MockMvc restBPNMProcessInfoMockMvc;

    private BPMNProcessInfo bPMNProcessInfo;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BPMNProcessInfoResource bPMNProcessInfoResource = new BPMNProcessInfoResource(bPMNProcessInfoService);
        this.restBPNMProcessInfoMockMvc = MockMvcBuilders.standaloneSetup(bPMNProcessInfoResource)
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
    public static BPMNProcessInfo createEntity(EntityManager em) {
        BPMNProcessInfo bPMNProcessInfo = new BPMNProcessInfo()
            .name(DEFAULT_NAME)
            .version(DEFAULT_VERSION)
            .type(DEFAULT_TYPE)
            .author(DEFAULT_AUTHOR)
            .lastChange(DEFAULT_LAST_CHANGE)
            .signatureBase64(DEFAULT_SIGNATURE_BASE_64);
        return bPMNProcessInfo;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BPMNProcessInfo createUpdatedEntity(EntityManager em) {
        BPMNProcessInfo bPMNProcessInfo = new BPMNProcessInfo()
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64);
        return bPMNProcessInfo;
    }

    @BeforeEach
    public void initTest() {
        bPMNProcessInfo = createEntity(em);
    }

    @Test
    @Transactional
    public void createBPNMProcessInfo() throws Exception {
        int databaseSizeBeforeCreate = bPMNProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo
        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isCreated());

        // Validate the BPNMProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeCreate + 1);
        BPMNProcessInfo testBPMNProcessInfo = bPMNProcessInfoList.get(bPMNProcessInfoList.size() - 1);
        assertThat(testBPMNProcessInfo.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBPMNProcessInfo.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testBPMNProcessInfo.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBPMNProcessInfo.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testBPMNProcessInfo.getLastChange()).isEqualTo(DEFAULT_LAST_CHANGE);
        assertThat(testBPMNProcessInfo.getSignatureBase64()).isEqualTo(DEFAULT_SIGNATURE_BASE_64);
    }

    @Test
    @Transactional
    public void createBPNMProcessInfoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bPMNProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo with an existing ID
        bPMNProcessInfo.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        // Validate the BPNMProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bPMNProcessInfo.setName(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bPMNProcessInfo.setVersion(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bPMNProcessInfo.setType(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAuthorIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bPMNProcessInfo.setAuthor(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastChangeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bPMNProcessInfo.setLastChange(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBPNMProcessInfos() throws Exception {
        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bPMNProcessInfo);

        // Get all the bPMNProcessInfoList
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bPMNProcessInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].signatureBase64").value(hasItem(DEFAULT_SIGNATURE_BASE_64.toString())));
    }

    @Test
    @Transactional
    public void getBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bPMNProcessInfo);

        // Get the bPMNProcessInfo
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos/{id}", bPMNProcessInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(bPMNProcessInfo.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.lastChange").value(DEFAULT_LAST_CHANGE.toString()))
            .andExpect(jsonPath("$.signatureBase64").value(DEFAULT_SIGNATURE_BASE_64.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingBPNMProcessInfo() throws Exception {
        // Get the bPMNProcessInfo
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPMNProcessInfoService.save(bPMNProcessInfo);

        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Update the bPMNProcessInfo
        BPMNProcessInfo updatedBPMNProcessInfo = bPMNProcessInfoRepository.findById(bPMNProcessInfo.getId()).get();
        // Disconnect from session so that the updates on updatedBPNMProcessInfo are not directly saved in db
        em.detach(updatedBPMNProcessInfo);
        updatedBPMNProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64);

        restBPNMProcessInfoMockMvc.perform(put("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedBPMNProcessInfo)))
            .andExpect(status().isOk());

        // Validate the BPNMProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
        BPMNProcessInfo testBPMNProcessInfo = bPMNProcessInfoList.get(bPMNProcessInfoList.size() - 1);
        assertThat(testBPMNProcessInfo.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBPMNProcessInfo.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testBPMNProcessInfo.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBPMNProcessInfo.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testBPMNProcessInfo.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testBPMNProcessInfo.getSignatureBase64()).isEqualTo(UPDATED_SIGNATURE_BASE_64);
    }

    @Test
    @Transactional
    public void updateNonExistingBPNMProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBPNMProcessInfoMockMvc.perform(put("/api/bpnm-process-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo)))
            .andExpect(status().isBadRequest());

        // Validate the BPNMProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPMNProcessInfoService.save(bPMNProcessInfo);

        int databaseSizeBeforeDelete = bPMNProcessInfoRepository.findAll().size();

        // Delete the bPMNProcessInfo
        restBPNMProcessInfoMockMvc.perform(delete("/api/bpnm-process-infos/{id}", bPMNProcessInfo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
