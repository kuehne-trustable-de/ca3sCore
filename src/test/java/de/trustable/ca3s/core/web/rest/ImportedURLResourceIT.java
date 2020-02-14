package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.repository.ImportedURLRepository;
import de.trustable.ca3s.core.service.ImportedURLService;
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
 * Integration tests for the {@link ImportedURLResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class ImportedURLResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Instant DEFAULT_IMPORT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_IMPORT_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private ImportedURLRepository importedURLRepository;

    @Autowired
    private ImportedURLService importedURLService;

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

    private MockMvc restImportedURLMockMvc;

    private ImportedURL importedURL;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ImportedURLResource importedURLResource = new ImportedURLResource(importedURLService);
        this.restImportedURLMockMvc = MockMvcBuilders.standaloneSetup(importedURLResource)
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
    public static ImportedURL createEntity(EntityManager em) {
        ImportedURL importedURL = new ImportedURL()
            .name(DEFAULT_NAME)
            .importDate(DEFAULT_IMPORT_DATE);
        return importedURL;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ImportedURL createUpdatedEntity(EntityManager em) {
        ImportedURL importedURL = new ImportedURL()
            .name(UPDATED_NAME)
            .importDate(UPDATED_IMPORT_DATE);
        return importedURL;
    }

    @BeforeEach
    public void initTest() {
        importedURL = createEntity(em);
    }

    @Test
    @Transactional
    public void createImportedURL() throws Exception {
        int databaseSizeBeforeCreate = importedURLRepository.findAll().size();

        // Create the ImportedURL
        restImportedURLMockMvc.perform(post("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(importedURL)))
            .andExpect(status().isCreated());

        // Validate the ImportedURL in the database
        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeCreate + 1);
        ImportedURL testImportedURL = importedURLList.get(importedURLList.size() - 1);
        assertThat(testImportedURL.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testImportedURL.getImportDate()).isEqualTo(DEFAULT_IMPORT_DATE);
    }

    @Test
    @Transactional
    public void createImportedURLWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = importedURLRepository.findAll().size();

        // Create the ImportedURL with an existing ID
        importedURL.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restImportedURLMockMvc.perform(post("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(importedURL)))
            .andExpect(status().isBadRequest());

        // Validate the ImportedURL in the database
        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = importedURLRepository.findAll().size();
        // set the field null
        importedURL.setName(null);

        // Create the ImportedURL, which fails.

        restImportedURLMockMvc.perform(post("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(importedURL)))
            .andExpect(status().isBadRequest());

        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkImportDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = importedURLRepository.findAll().size();
        // set the field null
        importedURL.setImportDate(null);

        // Create the ImportedURL, which fails.

        restImportedURLMockMvc.perform(post("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(importedURL)))
            .andExpect(status().isBadRequest());

        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllImportedURLS() throws Exception {
        // Initialize the database
        importedURLRepository.saveAndFlush(importedURL);

        // Get all the importedURLList
        restImportedURLMockMvc.perform(get("/api/imported-urls?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(importedURL.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].importDate").value(hasItem(DEFAULT_IMPORT_DATE.toString())));
    }
    
    @Test
    @Transactional
    public void getImportedURL() throws Exception {
        // Initialize the database
        importedURLRepository.saveAndFlush(importedURL);

        // Get the importedURL
        restImportedURLMockMvc.perform(get("/api/imported-urls/{id}", importedURL.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(importedURL.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.importDate").value(DEFAULT_IMPORT_DATE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingImportedURL() throws Exception {
        // Get the importedURL
        restImportedURLMockMvc.perform(get("/api/imported-urls/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateImportedURL() throws Exception {
        // Initialize the database
        importedURLService.save(importedURL);

        int databaseSizeBeforeUpdate = importedURLRepository.findAll().size();

        // Update the importedURL
        ImportedURL updatedImportedURL = importedURLRepository.findById(importedURL.getId()).get();
        // Disconnect from session so that the updates on updatedImportedURL are not directly saved in db
        em.detach(updatedImportedURL);
        updatedImportedURL
            .name(UPDATED_NAME)
            .importDate(UPDATED_IMPORT_DATE);

        restImportedURLMockMvc.perform(put("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedImportedURL)))
            .andExpect(status().isOk());

        // Validate the ImportedURL in the database
        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeUpdate);
        ImportedURL testImportedURL = importedURLList.get(importedURLList.size() - 1);
        assertThat(testImportedURL.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testImportedURL.getImportDate()).isEqualTo(UPDATED_IMPORT_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingImportedURL() throws Exception {
        int databaseSizeBeforeUpdate = importedURLRepository.findAll().size();

        // Create the ImportedURL

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImportedURLMockMvc.perform(put("/api/imported-urls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(importedURL)))
            .andExpect(status().isBadRequest());

        // Validate the ImportedURL in the database
        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteImportedURL() throws Exception {
        // Initialize the database
        importedURLService.save(importedURL);

        int databaseSizeBeforeDelete = importedURLRepository.findAll().size();

        // Delete the importedURL
        restImportedURLMockMvc.perform(delete("/api/imported-urls/{id}", importedURL.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ImportedURL> importedURLList = importedURLRepository.findAll();
        assertThat(importedURLList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
