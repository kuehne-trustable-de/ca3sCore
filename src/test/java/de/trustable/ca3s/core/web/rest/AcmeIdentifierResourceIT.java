package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeIdentifier;
import de.trustable.ca3s.core.repository.AcmeIdentifierRepository;
import de.trustable.ca3s.core.service.AcmeIdentifierService;
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
 * Integration tests for the {@link AcmeIdentifierResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class AcmeIdentifierResourceIT {

    private static final Long DEFAULT_ACME_IDENTIFIER_ID = 1L;
    private static final Long UPDATED_ACME_IDENTIFIER_ID = 2L;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private AcmeIdentifierRepository acmeIdentifierRepository;

    @Autowired
    private AcmeIdentifierService acmeIdentifierService;

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

    private MockMvc restAcmeIdentifierMockMvc;

    private AcmeIdentifier acmeIdentifier;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AcmeIdentifierResource acmeIdentifierResource = new AcmeIdentifierResource(acmeIdentifierService);
        this.restAcmeIdentifierMockMvc = MockMvcBuilders.standaloneSetup(acmeIdentifierResource)
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
    public static AcmeIdentifier createEntity(EntityManager em) {
        AcmeIdentifier acmeIdentifier = new AcmeIdentifier()
            .acmeIdentifierId(DEFAULT_ACME_IDENTIFIER_ID)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE);
        return acmeIdentifier;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeIdentifier createUpdatedEntity(EntityManager em) {
        AcmeIdentifier acmeIdentifier = new AcmeIdentifier()
            .acmeIdentifierId(UPDATED_ACME_IDENTIFIER_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);
        return acmeIdentifier;
    }

    @BeforeEach
    public void initTest() {
        acmeIdentifier = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeIdentifier() throws Exception {
        int databaseSizeBeforeCreate = acmeIdentifierRepository.findAll().size();

        // Create the AcmeIdentifier
        restAcmeIdentifierMockMvc.perform(post("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isCreated());

        // Validate the AcmeIdentifier in the database
        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeIdentifier testAcmeIdentifier = acmeIdentifierList.get(acmeIdentifierList.size() - 1);
        assertThat(testAcmeIdentifier.getAcmeIdentifierId()).isEqualTo(DEFAULT_ACME_IDENTIFIER_ID);
        assertThat(testAcmeIdentifier.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAcmeIdentifier.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createAcmeIdentifierWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeIdentifierRepository.findAll().size();

        // Create the AcmeIdentifier with an existing ID
        acmeIdentifier.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeIdentifierMockMvc.perform(post("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeIdentifier in the database
        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAcmeIdentifierIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeIdentifierRepository.findAll().size();
        // set the field null
        acmeIdentifier.setAcmeIdentifierId(null);

        // Create the AcmeIdentifier, which fails.

        restAcmeIdentifierMockMvc.perform(post("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isBadRequest());

        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeIdentifierRepository.findAll().size();
        // set the field null
        acmeIdentifier.setType(null);

        // Create the AcmeIdentifier, which fails.

        restAcmeIdentifierMockMvc.perform(post("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isBadRequest());

        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeIdentifierRepository.findAll().size();
        // set the field null
        acmeIdentifier.setValue(null);

        // Create the AcmeIdentifier, which fails.

        restAcmeIdentifierMockMvc.perform(post("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isBadRequest());

        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeIdentifiers() throws Exception {
        // Initialize the database
        acmeIdentifierRepository.saveAndFlush(acmeIdentifier);

        // Get all the acmeIdentifierList
        restAcmeIdentifierMockMvc.perform(get("/api/acme-identifiers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeIdentifier.getId().intValue())))
            .andExpect(jsonPath("$.[*].acmeIdentifierId").value(hasItem(DEFAULT_ACME_IDENTIFIER_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    public void getAcmeIdentifier() throws Exception {
        // Initialize the database
        acmeIdentifierRepository.saveAndFlush(acmeIdentifier);

        // Get the acmeIdentifier
        restAcmeIdentifierMockMvc.perform(get("/api/acme-identifiers/{id}", acmeIdentifier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(acmeIdentifier.getId().intValue()))
            .andExpect(jsonPath("$.acmeIdentifierId").value(DEFAULT_ACME_IDENTIFIER_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeIdentifier() throws Exception {
        // Get the acmeIdentifier
        restAcmeIdentifierMockMvc.perform(get("/api/acme-identifiers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeIdentifier() throws Exception {
        // Initialize the database
        acmeIdentifierService.save(acmeIdentifier);

        int databaseSizeBeforeUpdate = acmeIdentifierRepository.findAll().size();

        // Update the acmeIdentifier
        AcmeIdentifier updatedAcmeIdentifier = acmeIdentifierRepository.findById(acmeIdentifier.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeIdentifier are not directly saved in db
        em.detach(updatedAcmeIdentifier);
        updatedAcmeIdentifier
            .acmeIdentifierId(UPDATED_ACME_IDENTIFIER_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);

        restAcmeIdentifierMockMvc.perform(put("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeIdentifier)))
            .andExpect(status().isOk());

        // Validate the AcmeIdentifier in the database
        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeUpdate);
        AcmeIdentifier testAcmeIdentifier = acmeIdentifierList.get(acmeIdentifierList.size() - 1);
        assertThat(testAcmeIdentifier.getAcmeIdentifierId()).isEqualTo(UPDATED_ACME_IDENTIFIER_ID);
        assertThat(testAcmeIdentifier.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAcmeIdentifier.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeIdentifier() throws Exception {
        int databaseSizeBeforeUpdate = acmeIdentifierRepository.findAll().size();

        // Create the AcmeIdentifier

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeIdentifierMockMvc.perform(put("/api/acme-identifiers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeIdentifier)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeIdentifier in the database
        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeIdentifier() throws Exception {
        // Initialize the database
        acmeIdentifierService.save(acmeIdentifier);

        int databaseSizeBeforeDelete = acmeIdentifierRepository.findAll().size();

        // Delete the acmeIdentifier
        restAcmeIdentifierMockMvc.perform(delete("/api/acme-identifiers/{id}", acmeIdentifier.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeIdentifier> acmeIdentifierList = acmeIdentifierRepository.findAll();
        assertThat(acmeIdentifierList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
