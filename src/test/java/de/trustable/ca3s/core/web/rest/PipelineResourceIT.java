package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.PipelineService;
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
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.domain.enumeration.PipelineType;
/**
 * Integration tests for the {@link PipelineResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class PipelineResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final PipelineType DEFAULT_TYPE = PipelineType.ACME;
    private static final PipelineType UPDATED_TYPE = PipelineType.SCEP;

    private static final String DEFAULT_URL_PART = "AAAAAAAAAA";
    private static final String UPDATED_URL_PART = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_APPROVAL_REQUIRED = false;
    private static final Boolean UPDATED_APPROVAL_REQUIRED = true;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineService pipelineService;

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

    private MockMvc restPipelineMockMvc;

    private Pipeline pipeline;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PipelineResource pipelineResource = new PipelineResource(pipelineService);
        this.restPipelineMockMvc = MockMvcBuilders.standaloneSetup(pipelineResource)
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
    public static Pipeline createEntity(EntityManager em) {
        Pipeline pipeline = new Pipeline()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .urlPart(DEFAULT_URL_PART)
            .description(DEFAULT_DESCRIPTION)
            .approvalRequired(DEFAULT_APPROVAL_REQUIRED);
        return pipeline;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pipeline createUpdatedEntity(EntityManager em) {
        Pipeline pipeline = new Pipeline()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .urlPart(UPDATED_URL_PART)
            .description(UPDATED_DESCRIPTION)
            .approvalRequired(UPDATED_APPROVAL_REQUIRED);
        return pipeline;
    }

    @BeforeEach
    public void initTest() {
        pipeline = createEntity(em);
    }

    @Test
    @Transactional
    public void createPipeline() throws Exception {
        int databaseSizeBeforeCreate = pipelineRepository.findAll().size();

        // Create the Pipeline
        restPipelineMockMvc.perform(post("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipeline)))
            .andExpect(status().isCreated());

        // Validate the Pipeline in the database
        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeCreate + 1);
        Pipeline testPipeline = pipelineList.get(pipelineList.size() - 1);
        assertThat(testPipeline.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPipeline.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPipeline.getUrlPart()).isEqualTo(DEFAULT_URL_PART);
        assertThat(testPipeline.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testPipeline.isApprovalRequired()).isEqualTo(DEFAULT_APPROVAL_REQUIRED);
    }

    @Test
    @Transactional
    public void createPipelineWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pipelineRepository.findAll().size();

        // Create the Pipeline with an existing ID
        pipeline.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPipelineMockMvc.perform(post("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipeline)))
            .andExpect(status().isBadRequest());

        // Validate the Pipeline in the database
        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = pipelineRepository.findAll().size();
        // set the field null
        pipeline.setName(null);

        // Create the Pipeline, which fails.

        restPipelineMockMvc.perform(post("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipeline)))
            .andExpect(status().isBadRequest());

        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = pipelineRepository.findAll().size();
        // set the field null
        pipeline.setType(null);

        // Create the Pipeline, which fails.

        restPipelineMockMvc.perform(post("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipeline)))
            .andExpect(status().isBadRequest());

        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPipelines() throws Exception {
        // Initialize the database
        pipelineRepository.saveAndFlush(pipeline);

        // Get all the pipelineList
        restPipelineMockMvc.perform(get("/api/pipelines?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pipeline.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].urlPart").value(hasItem(DEFAULT_URL_PART)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].approvalRequired").value(hasItem(DEFAULT_APPROVAL_REQUIRED.booleanValue())));
    }
    
    @Test
    @Transactional
    public void getPipeline() throws Exception {
        // Initialize the database
        pipelineRepository.saveAndFlush(pipeline);

        // Get the pipeline
        restPipelineMockMvc.perform(get("/api/pipelines/{id}", pipeline.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pipeline.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.urlPart").value(DEFAULT_URL_PART))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.approvalRequired").value(DEFAULT_APPROVAL_REQUIRED.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPipeline() throws Exception {
        // Get the pipeline
        restPipelineMockMvc.perform(get("/api/pipelines/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePipeline() throws Exception {
        // Initialize the database
        pipelineService.save(pipeline);

        int databaseSizeBeforeUpdate = pipelineRepository.findAll().size();

        // Update the pipeline
        Pipeline updatedPipeline = pipelineRepository.findById(pipeline.getId()).get();
        // Disconnect from session so that the updates on updatedPipeline are not directly saved in db
        em.detach(updatedPipeline);
        updatedPipeline
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .urlPart(UPDATED_URL_PART)
            .description(UPDATED_DESCRIPTION)
            .approvalRequired(UPDATED_APPROVAL_REQUIRED);

        restPipelineMockMvc.perform(put("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPipeline)))
            .andExpect(status().isOk());

        // Validate the Pipeline in the database
        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeUpdate);
        Pipeline testPipeline = pipelineList.get(pipelineList.size() - 1);
        assertThat(testPipeline.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPipeline.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPipeline.getUrlPart()).isEqualTo(UPDATED_URL_PART);
        assertThat(testPipeline.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testPipeline.isApprovalRequired()).isEqualTo(UPDATED_APPROVAL_REQUIRED);
    }

    @Test
    @Transactional
    public void updateNonExistingPipeline() throws Exception {
        int databaseSizeBeforeUpdate = pipelineRepository.findAll().size();

        // Create the Pipeline

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPipelineMockMvc.perform(put("/api/pipelines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pipeline)))
            .andExpect(status().isBadRequest());

        // Validate the Pipeline in the database
        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePipeline() throws Exception {
        // Initialize the database
        pipelineService.save(pipeline);

        int databaseSizeBeforeDelete = pipelineRepository.findAll().size();

        // Delete the pipeline
        restPipelineMockMvc.perform(delete("/api/pipelines/{id}", pipeline.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Pipeline> pipelineList = pipelineRepository.findAll();
        assertThat(pipelineList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
