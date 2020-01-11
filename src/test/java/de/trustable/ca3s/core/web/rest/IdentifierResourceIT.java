package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Identifier;
import de.trustable.ca3s.core.repository.IdentifierRepository;
import de.trustable.ca3s.core.service.IdentifierService;
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

/**
 * Integration tests for the {@link IdentifierResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class IdentifierResourceIT {

    private static final Long DEFAULT_IDENTIFIER_ID = 1L;
    private static final Long UPDATED_IDENTIFIER_ID = 2L;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private IdentifierRepository identifierRepository;

    @Autowired
    private IdentifierService identifierService;

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

    private MockMvc restIdentifierMockMvc;

    private Identifier identifier;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final IdentifierResource identifierResource = new IdentifierResource(identifierService);
        this.restIdentifierMockMvc = MockMvcBuilders.standaloneSetup(identifierResource)
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
    public static Identifier createEntity(EntityManager em) {
        Identifier identifier = new Identifier()
            .identifierId(DEFAULT_IDENTIFIER_ID)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE);
        return identifier;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Identifier createUpdatedEntity(EntityManager em) {
        Identifier identifier = new Identifier()
            .identifierId(UPDATED_IDENTIFIER_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);
        return identifier;
    }

    @BeforeEach
    public void initTest() {
        identifier = createEntity(em);
    }

    @Test
    @Transactional
    public void createIdentifier() throws Exception {
        int databaseSizeBeforeCreate = identifierRepository.findAll().size();

        // Create the Identifier
        restIdentifierMockMvc.perform(post("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isCreated());

        // Validate the Identifier in the database
        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeCreate + 1);
        Identifier testIdentifier = identifierList.get(identifierList.size() - 1);
        assertThat(testIdentifier.getIdentifierId()).isEqualTo(DEFAULT_IDENTIFIER_ID);
        assertThat(testIdentifier.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testIdentifier.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createIdentifierWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = identifierRepository.findAll().size();

        // Create the Identifier with an existing ID
        identifier.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restIdentifierMockMvc.perform(post("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isBadRequest());

        // Validate the Identifier in the database
        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkIdentifierIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setIdentifierId(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isBadRequest());

        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setType(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isBadRequest());

        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setValue(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isBadRequest());

        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllIdentifiers() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);

        // Get all the identifierList
        restIdentifierMockMvc.perform(get("/api/identifiers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(identifier.getId().intValue())))
            .andExpect(jsonPath("$.[*].identifierId").value(hasItem(DEFAULT_IDENTIFIER_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }
    
    @Test
    @Transactional
    public void getIdentifier() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);

        // Get the identifier
        restIdentifierMockMvc.perform(get("/api/identifiers/{id}", identifier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(identifier.getId().intValue()))
            .andExpect(jsonPath("$.identifierId").value(DEFAULT_IDENTIFIER_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingIdentifier() throws Exception {
        // Get the identifier
        restIdentifierMockMvc.perform(get("/api/identifiers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateIdentifier() throws Exception {
        // Initialize the database
        identifierService.save(identifier);

        int databaseSizeBeforeUpdate = identifierRepository.findAll().size();

        // Update the identifier
        Identifier updatedIdentifier = identifierRepository.findById(identifier.getId()).get();
        // Disconnect from session so that the updates on updatedIdentifier are not directly saved in db
        em.detach(updatedIdentifier);
        updatedIdentifier
            .identifierId(UPDATED_IDENTIFIER_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);

        restIdentifierMockMvc.perform(put("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedIdentifier)))
            .andExpect(status().isOk());

        // Validate the Identifier in the database
        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeUpdate);
        Identifier testIdentifier = identifierList.get(identifierList.size() - 1);
        assertThat(testIdentifier.getIdentifierId()).isEqualTo(UPDATED_IDENTIFIER_ID);
        assertThat(testIdentifier.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testIdentifier.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingIdentifier() throws Exception {
        int databaseSizeBeforeUpdate = identifierRepository.findAll().size();

        // Create the Identifier

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIdentifierMockMvc.perform(put("/api/identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(identifier)))
            .andExpect(status().isBadRequest());

        // Validate the Identifier in the database
        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteIdentifier() throws Exception {
        // Initialize the database
        identifierService.save(identifier);

        int databaseSizeBeforeDelete = identifierRepository.findAll().size();

        // Delete the identifier
        restIdentifierMockMvc.perform(delete("/api/identifiers/{id}", identifier.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Identifier> identifierList = identifierRepository.findAll();
        assertThat(identifierList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
