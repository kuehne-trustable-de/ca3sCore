package de.trustable.ca3sjh.web.rest;

import de.trustable.ca3sjh.Ca3SJhApp;
import de.trustable.ca3sjh.domain.SelectorToTemplate;
import de.trustable.ca3sjh.repository.SelectorToTemplateRepository;
import de.trustable.ca3sjh.service.SelectorToTemplateService;
import de.trustable.ca3sjh.web.rest.errors.ExceptionTranslator;

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

import static de.trustable.ca3sjh.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link SelectorToTemplateResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class SelectorToTemplateResourceIT {

    private static final String DEFAULT_SELECTOR = "AAAAAAAAAA";
    private static final String UPDATED_SELECTOR = "BBBBBBBBBB";

    private static final String DEFAULT_TEMPLATE = "AAAAAAAAAA";
    private static final String UPDATED_TEMPLATE = "BBBBBBBBBB";

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    @Autowired
    private SelectorToTemplateRepository selectorToTemplateRepository;

    @Autowired
    private SelectorToTemplateService selectorToTemplateService;

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

    private MockMvc restSelectorToTemplateMockMvc;

    private SelectorToTemplate selectorToTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SelectorToTemplateResource selectorToTemplateResource = new SelectorToTemplateResource(selectorToTemplateService);
        this.restSelectorToTemplateMockMvc = MockMvcBuilders.standaloneSetup(selectorToTemplateResource)
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
    public static SelectorToTemplate createEntity(EntityManager em) {
        SelectorToTemplate selectorToTemplate = new SelectorToTemplate()
            .selector(DEFAULT_SELECTOR)
            .template(DEFAULT_TEMPLATE)
            .comment(DEFAULT_COMMENT);
        return selectorToTemplate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SelectorToTemplate createUpdatedEntity(EntityManager em) {
        SelectorToTemplate selectorToTemplate = new SelectorToTemplate()
            .selector(UPDATED_SELECTOR)
            .template(UPDATED_TEMPLATE)
            .comment(UPDATED_COMMENT);
        return selectorToTemplate;
    }

    @BeforeEach
    public void initTest() {
        selectorToTemplate = createEntity(em);
    }

    @Test
    @Transactional
    public void createSelectorToTemplate() throws Exception {
        int databaseSizeBeforeCreate = selectorToTemplateRepository.findAll().size();

        // Create the SelectorToTemplate
        restSelectorToTemplateMockMvc.perform(post("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(selectorToTemplate)))
            .andExpect(status().isCreated());

        // Validate the SelectorToTemplate in the database
        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeCreate + 1);
        SelectorToTemplate testSelectorToTemplate = selectorToTemplateList.get(selectorToTemplateList.size() - 1);
        assertThat(testSelectorToTemplate.getSelector()).isEqualTo(DEFAULT_SELECTOR);
        assertThat(testSelectorToTemplate.getTemplate()).isEqualTo(DEFAULT_TEMPLATE);
        assertThat(testSelectorToTemplate.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    @Transactional
    public void createSelectorToTemplateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = selectorToTemplateRepository.findAll().size();

        // Create the SelectorToTemplate with an existing ID
        selectorToTemplate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSelectorToTemplateMockMvc.perform(post("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(selectorToTemplate)))
            .andExpect(status().isBadRequest());

        // Validate the SelectorToTemplate in the database
        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkSelectorIsRequired() throws Exception {
        int databaseSizeBeforeTest = selectorToTemplateRepository.findAll().size();
        // set the field null
        selectorToTemplate.setSelector(null);

        // Create the SelectorToTemplate, which fails.

        restSelectorToTemplateMockMvc.perform(post("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(selectorToTemplate)))
            .andExpect(status().isBadRequest());

        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTemplateIsRequired() throws Exception {
        int databaseSizeBeforeTest = selectorToTemplateRepository.findAll().size();
        // set the field null
        selectorToTemplate.setTemplate(null);

        // Create the SelectorToTemplate, which fails.

        restSelectorToTemplateMockMvc.perform(post("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(selectorToTemplate)))
            .andExpect(status().isBadRequest());

        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSelectorToTemplates() throws Exception {
        // Initialize the database
        selectorToTemplateRepository.saveAndFlush(selectorToTemplate);

        // Get all the selectorToTemplateList
        restSelectorToTemplateMockMvc.perform(get("/api/selector-to-templates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(selectorToTemplate.getId().intValue())))
            .andExpect(jsonPath("$.[*].selector").value(hasItem(DEFAULT_SELECTOR.toString())))
            .andExpect(jsonPath("$.[*].template").value(hasItem(DEFAULT_TEMPLATE.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));
    }
    
    @Test
    @Transactional
    public void getSelectorToTemplate() throws Exception {
        // Initialize the database
        selectorToTemplateRepository.saveAndFlush(selectorToTemplate);

        // Get the selectorToTemplate
        restSelectorToTemplateMockMvc.perform(get("/api/selector-to-templates/{id}", selectorToTemplate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(selectorToTemplate.getId().intValue()))
            .andExpect(jsonPath("$.selector").value(DEFAULT_SELECTOR.toString()))
            .andExpect(jsonPath("$.template").value(DEFAULT_TEMPLATE.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSelectorToTemplate() throws Exception {
        // Get the selectorToTemplate
        restSelectorToTemplateMockMvc.perform(get("/api/selector-to-templates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSelectorToTemplate() throws Exception {
        // Initialize the database
        selectorToTemplateService.save(selectorToTemplate);

        int databaseSizeBeforeUpdate = selectorToTemplateRepository.findAll().size();

        // Update the selectorToTemplate
        SelectorToTemplate updatedSelectorToTemplate = selectorToTemplateRepository.findById(selectorToTemplate.getId()).get();
        // Disconnect from session so that the updates on updatedSelectorToTemplate are not directly saved in db
        em.detach(updatedSelectorToTemplate);
        updatedSelectorToTemplate
            .selector(UPDATED_SELECTOR)
            .template(UPDATED_TEMPLATE)
            .comment(UPDATED_COMMENT);

        restSelectorToTemplateMockMvc.perform(put("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedSelectorToTemplate)))
            .andExpect(status().isOk());

        // Validate the SelectorToTemplate in the database
        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeUpdate);
        SelectorToTemplate testSelectorToTemplate = selectorToTemplateList.get(selectorToTemplateList.size() - 1);
        assertThat(testSelectorToTemplate.getSelector()).isEqualTo(UPDATED_SELECTOR);
        assertThat(testSelectorToTemplate.getTemplate()).isEqualTo(UPDATED_TEMPLATE);
        assertThat(testSelectorToTemplate.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void updateNonExistingSelectorToTemplate() throws Exception {
        int databaseSizeBeforeUpdate = selectorToTemplateRepository.findAll().size();

        // Create the SelectorToTemplate

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSelectorToTemplateMockMvc.perform(put("/api/selector-to-templates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(selectorToTemplate)))
            .andExpect(status().isBadRequest());

        // Validate the SelectorToTemplate in the database
        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSelectorToTemplate() throws Exception {
        // Initialize the database
        selectorToTemplateService.save(selectorToTemplate);

        int databaseSizeBeforeDelete = selectorToTemplateRepository.findAll().size();

        // Delete the selectorToTemplate
        restSelectorToTemplateMockMvc.perform(delete("/api/selector-to-templates/{id}", selectorToTemplate.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SelectorToTemplate> selectorToTemplateList = selectorToTemplateRepository.findAll();
        assertThat(selectorToTemplateList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SelectorToTemplate.class);
        SelectorToTemplate selectorToTemplate1 = new SelectorToTemplate();
        selectorToTemplate1.setId(1L);
        SelectorToTemplate selectorToTemplate2 = new SelectorToTemplate();
        selectorToTemplate2.setId(selectorToTemplate1.getId());
        assertThat(selectorToTemplate1).isEqualTo(selectorToTemplate2);
        selectorToTemplate2.setId(2L);
        assertThat(selectorToTemplate1).isNotEqualTo(selectorToTemplate2);
        selectorToTemplate1.setId(null);
        assertThat(selectorToTemplate1).isNotEqualTo(selectorToTemplate2);
    }
}
