package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.repository.PipelineAttributeRepository;
import de.trustable.ca3s.core.service.PipelineAttributeService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PipelineAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class PipelineAttributeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private PipelineAttributeRepository pipelineAttributeRepository;

    @Autowired
    private PipelineAttributeService pipelineAttributeService;

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

    private MockMvc restPipelineAttributeMockMvc;

    private PipelineAttribute pipelineAttribute;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PipelineAttributeResource pipelineAttributeResource = new PipelineAttributeResource(pipelineAttributeService);
        this.restPipelineAttributeMockMvc = MockMvcBuilders.standaloneSetup(pipelineAttributeResource)
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
    public static PipelineAttribute createEntity(EntityManager em) {
        PipelineAttribute pipelineAttribute = new PipelineAttribute()
            .name(DEFAULT_NAME)
            .value(DEFAULT_VALUE);
        return pipelineAttribute;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PipelineAttribute createUpdatedEntity(EntityManager em) {
        PipelineAttribute pipelineAttribute = new PipelineAttribute()
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);
        return pipelineAttribute;
    }

    @BeforeEach
    public void initTest() {
        pipelineAttribute = createEntity(em);
    }

    @Test
    @Transactional
    public void createPipelineAttribute() throws Exception {
        int databaseSizeBeforeCreate = pipelineAttributeRepository.findAll().size();

        // Create the PipelineAttribute
        restPipelineAttributeMockMvc.perform(post("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipelineAttribute)))
            .andExpect(status().isCreated());

        // Validate the PipelineAttribute in the database
        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeCreate + 1);
        PipelineAttribute testPipelineAttribute = pipelineAttributeList.get(pipelineAttributeList.size() - 1);
        assertThat(testPipelineAttribute.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPipelineAttribute.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createPipelineAttributeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pipelineAttributeRepository.findAll().size();

        // Create the PipelineAttribute with an existing ID
        pipelineAttribute.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPipelineAttributeMockMvc.perform(post("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipelineAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the PipelineAttribute in the database
        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = pipelineAttributeRepository.findAll().size();
        // set the field null
        pipelineAttribute.setName(null);

        // Create the PipelineAttribute, which fails.

        restPipelineAttributeMockMvc.perform(post("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipelineAttribute)))
            .andExpect(status().isBadRequest());

        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = pipelineAttributeRepository.findAll().size();
        // set the field null
        pipelineAttribute.setValue(null);

        // Create the PipelineAttribute, which fails.

        restPipelineAttributeMockMvc.perform(post("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipelineAttribute)))
            .andExpect(status().isBadRequest());

        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPipelineAttributes() throws Exception {
        // Initialize the database
        pipelineAttributeRepository.saveAndFlush(pipelineAttribute);

        // Get all the pipelineAttributeList
        restPipelineAttributeMockMvc.perform(get("/api/pipeline-attributes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pipelineAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    public void getPipelineAttribute() throws Exception {
        // Initialize the database
        pipelineAttributeRepository.saveAndFlush(pipelineAttribute);

        // Get the pipelineAttribute
        restPipelineAttributeMockMvc.perform(get("/api/pipeline-attributes/{id}", pipelineAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(pipelineAttribute.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingPipelineAttribute() throws Exception {
        // Get the pipelineAttribute
        restPipelineAttributeMockMvc.perform(get("/api/pipeline-attributes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePipelineAttribute() throws Exception {
        // Initialize the database
        pipelineAttributeService.save(pipelineAttribute);

        int databaseSizeBeforeUpdate = pipelineAttributeRepository.findAll().size();

        // Update the pipelineAttribute
        PipelineAttribute updatedPipelineAttribute = pipelineAttributeRepository.findById(pipelineAttribute.getId()).get();
        // Disconnect from session so that the updates on updatedPipelineAttribute are not directly saved in db
        em.detach(updatedPipelineAttribute);
        updatedPipelineAttribute
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);

        restPipelineAttributeMockMvc.perform(put("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPipelineAttribute)))
            .andExpect(status().isOk());

        // Validate the PipelineAttribute in the database
        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeUpdate);
        PipelineAttribute testPipelineAttribute = pipelineAttributeList.get(pipelineAttributeList.size() - 1);
        assertThat(testPipelineAttribute.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPipelineAttribute.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingPipelineAttribute() throws Exception {
        int databaseSizeBeforeUpdate = pipelineAttributeRepository.findAll().size();

        // Create the PipelineAttribute

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPipelineAttributeMockMvc.perform(put("/api/pipeline-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipelineAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the PipelineAttribute in the database
        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePipelineAttribute() throws Exception {
        // Initialize the database
        pipelineAttributeService.save(pipelineAttribute);

        int databaseSizeBeforeDelete = pipelineAttributeRepository.findAll().size();

        // Delete the pipelineAttribute
        restPipelineAttributeMockMvc.perform(delete("/api/pipeline-attributes/{id}", pipelineAttribute.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PipelineAttribute> pipelineAttributeList = pipelineAttributeRepository.findAll();
        assertThat(pipelineAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
