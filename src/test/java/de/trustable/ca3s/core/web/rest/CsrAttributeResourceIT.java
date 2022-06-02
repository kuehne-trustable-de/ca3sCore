package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.service.CsrAttributeService;
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
 * Integration tests for the {@link CsrAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class CsrAttributeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private CsrAttributeRepository csrAttributeRepository;

    @Autowired
    private CsrAttributeService csrAttributeService;

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

    private MockMvc restCsrAttributeMockMvc;

    private CsrAttribute csrAttribute;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CsrAttributeResource csrAttributeResource = new CsrAttributeResource(csrAttributeService);
        this.restCsrAttributeMockMvc = MockMvcBuilders.standaloneSetup(csrAttributeResource)
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
    public static CsrAttribute createEntity(EntityManager em) {
        CsrAttribute csrAttribute = new CsrAttribute()
            .name(DEFAULT_NAME)
            .value(DEFAULT_VALUE);
        return csrAttribute;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CsrAttribute createUpdatedEntity(EntityManager em) {
        CsrAttribute csrAttribute = new CsrAttribute()
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);
        return csrAttribute;
    }

    @BeforeEach
    public void initTest() {
        csrAttribute = createEntity(em);
    }

    @Test
    @Transactional
    public void createCsrAttribute() throws Exception {
        int databaseSizeBeforeCreate = csrAttributeRepository.findAll().size();

        // Create the CsrAttribute
        restCsrAttributeMockMvc.perform(post("/api/csr-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(csrAttribute)))
            .andExpect(status().isCreated());

        // Validate the CsrAttribute in the database
        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeCreate + 1);
        CsrAttribute testCsrAttribute = csrAttributeList.get(csrAttributeList.size() - 1);
        assertThat(testCsrAttribute.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCsrAttribute.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createCsrAttributeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = csrAttributeRepository.findAll().size();

        // Create the CsrAttribute with an existing ID
        csrAttribute.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCsrAttributeMockMvc.perform(post("/api/csr-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(csrAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the CsrAttribute in the database
        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = csrAttributeRepository.findAll().size();
        // set the field null
        csrAttribute.setName(null);

        // Create the CsrAttribute, which fails.

        restCsrAttributeMockMvc.perform(post("/api/csr-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(csrAttribute)))
            .andExpect(status().isBadRequest());

        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCsrAttributes() throws Exception {
        // Initialize the database
        csrAttributeRepository.saveAndFlush(csrAttribute);

        // Get all the csrAttributeList
        restCsrAttributeMockMvc.perform(get("/api/csr-attributes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(csrAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    public void getCsrAttribute() throws Exception {
        // Initialize the database
        csrAttributeRepository.saveAndFlush(csrAttribute);

        // Get the csrAttribute
        restCsrAttributeMockMvc.perform(get("/api/csr-attributes/{id}", csrAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(csrAttribute.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingCsrAttribute() throws Exception {
        // Get the csrAttribute
        restCsrAttributeMockMvc.perform(get("/api/csr-attributes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCsrAttribute() throws Exception {
        // Initialize the database
        csrAttributeService.save(csrAttribute);

        int databaseSizeBeforeUpdate = csrAttributeRepository.findAll().size();

        // Update the csrAttribute
        CsrAttribute updatedCsrAttribute = csrAttributeRepository.findById(csrAttribute.getId()).get();
        // Disconnect from session so that the updates on updatedCsrAttribute are not directly saved in db
        em.detach(updatedCsrAttribute);
        updatedCsrAttribute
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);

        restCsrAttributeMockMvc.perform(put("/api/csr-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCsrAttribute)))
            .andExpect(status().isOk());

        // Validate the CsrAttribute in the database
        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeUpdate);
        CsrAttribute testCsrAttribute = csrAttributeList.get(csrAttributeList.size() - 1);
        assertThat(testCsrAttribute.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCsrAttribute.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingCsrAttribute() throws Exception {
        int databaseSizeBeforeUpdate = csrAttributeRepository.findAll().size();

        // Create the CsrAttribute

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCsrAttributeMockMvc.perform(put("/api/csr-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(csrAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the CsrAttribute in the database
        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCsrAttribute() throws Exception {
        // Initialize the database
        csrAttributeService.save(csrAttribute);

        int databaseSizeBeforeDelete = csrAttributeRepository.findAll().size();

        // Delete the csrAttribute
        restCsrAttributeMockMvc.perform(delete("/api/csr-attributes/{id}", csrAttribute.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CsrAttribute> csrAttributeList = csrAttributeRepository.findAll();
        assertThat(csrAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
