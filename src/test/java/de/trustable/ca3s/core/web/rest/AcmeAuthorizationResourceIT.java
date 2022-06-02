package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.repository.AcmeAuthorizationRepository;
import de.trustable.ca3s.core.service.AcmeAuthorizationService;
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
 * Integration tests for the {@link AcmeAuthorizationResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class AcmeAuthorizationResourceIT {

    private static final Long DEFAULT_ACME_AUTHORIZATION_ID = 1L;
    private static final Long UPDATED_ACME_AUTHORIZATION_ID = 2L;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private AcmeAuthorizationRepository acmeAuthorizationRepository;

    @Autowired
    private AcmeAuthorizationService acmeAuthorizationService;

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

    private MockMvc restAcmeAuthorizationMockMvc;

    private AcmeAuthorization acmeAuthorization;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AcmeAuthorizationResource acmeAuthorizationResource = new AcmeAuthorizationResource(acmeAuthorizationService);
        this.restAcmeAuthorizationMockMvc = MockMvcBuilders.standaloneSetup(acmeAuthorizationResource)
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
    public static AcmeAuthorization createEntity(EntityManager em) {
        AcmeAuthorization acmeAuthorization = new AcmeAuthorization()
            .acmeAuthorizationId(DEFAULT_ACME_AUTHORIZATION_ID)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE);
        return acmeAuthorization;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeAuthorization createUpdatedEntity(EntityManager em) {
        AcmeAuthorization acmeAuthorization = new AcmeAuthorization()
            .acmeAuthorizationId(UPDATED_ACME_AUTHORIZATION_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);
        return acmeAuthorization;
    }

    @BeforeEach
    public void initTest() {
        acmeAuthorization = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeAuthorization() throws Exception {
        int databaseSizeBeforeCreate = acmeAuthorizationRepository.findAll().size();

        // Create the AcmeAuthorization
        restAcmeAuthorizationMockMvc.perform(post("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isCreated());

        // Validate the AcmeAuthorization in the database
        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeAuthorization testAcmeAuthorization = acmeAuthorizationList.get(acmeAuthorizationList.size() - 1);
        assertThat(testAcmeAuthorization.getAcmeAuthorizationId()).isEqualTo(DEFAULT_ACME_AUTHORIZATION_ID);
        assertThat(testAcmeAuthorization.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAcmeAuthorization.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createAcmeAuthorizationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeAuthorizationRepository.findAll().size();

        // Create the AcmeAuthorization with an existing ID
        acmeAuthorization.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeAuthorizationMockMvc.perform(post("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeAuthorization in the database
        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAcmeAuthorizationIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeAuthorizationRepository.findAll().size();
        // set the field null
        acmeAuthorization.setAcmeAuthorizationId(null);

        // Create the AcmeAuthorization, which fails.

        restAcmeAuthorizationMockMvc.perform(post("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isBadRequest());

        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeAuthorizationRepository.findAll().size();
        // set the field null
        acmeAuthorization.setType(null);

        // Create the AcmeAuthorization, which fails.

        restAcmeAuthorizationMockMvc.perform(post("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isBadRequest());

        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeAuthorizationRepository.findAll().size();
        // set the field null
        acmeAuthorization.setValue(null);

        // Create the AcmeAuthorization, which fails.

        restAcmeAuthorizationMockMvc.perform(post("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isBadRequest());

        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeAuthorizations() throws Exception {
        // Initialize the database
        acmeAuthorizationRepository.saveAndFlush(acmeAuthorization);

        // Get all the acmeAuthorizationList
        restAcmeAuthorizationMockMvc.perform(get("/api/acme-authorizations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeAuthorization.getId().intValue())))
            .andExpect(jsonPath("$.[*].acmeAuthorizationId").value(hasItem(DEFAULT_ACME_AUTHORIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    public void getAcmeAuthorization() throws Exception {
        // Initialize the database
        acmeAuthorizationRepository.saveAndFlush(acmeAuthorization);

        // Get the acmeAuthorization
        restAcmeAuthorizationMockMvc.perform(get("/api/acme-authorizations/{id}", acmeAuthorization.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(acmeAuthorization.getId().intValue()))
            .andExpect(jsonPath("$.acmeAuthorizationId").value(DEFAULT_ACME_AUTHORIZATION_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeAuthorization() throws Exception {
        // Get the acmeAuthorization
        restAcmeAuthorizationMockMvc.perform(get("/api/acme-authorizations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeAuthorization() throws Exception {
        // Initialize the database
        acmeAuthorizationService.save(acmeAuthorization);

        int databaseSizeBeforeUpdate = acmeAuthorizationRepository.findAll().size();

        // Update the acmeAuthorization
        AcmeAuthorization updatedAcmeAuthorization = acmeAuthorizationRepository.findById(acmeAuthorization.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeAuthorization are not directly saved in db
        em.detach(updatedAcmeAuthorization);
        updatedAcmeAuthorization
            .acmeAuthorizationId(UPDATED_ACME_AUTHORIZATION_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);

        restAcmeAuthorizationMockMvc.perform(put("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeAuthorization)))
            .andExpect(status().isOk());

        // Validate the AcmeAuthorization in the database
        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeUpdate);
        AcmeAuthorization testAcmeAuthorization = acmeAuthorizationList.get(acmeAuthorizationList.size() - 1);
        assertThat(testAcmeAuthorization.getAcmeAuthorizationId()).isEqualTo(UPDATED_ACME_AUTHORIZATION_ID);
        assertThat(testAcmeAuthorization.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAcmeAuthorization.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeAuthorization() throws Exception {
        int databaseSizeBeforeUpdate = acmeAuthorizationRepository.findAll().size();

        // Create the AcmeAuthorization

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeAuthorizationMockMvc.perform(put("/api/acme-authorizations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeAuthorization)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeAuthorization in the database
        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeAuthorization() throws Exception {
        // Initialize the database
        acmeAuthorizationService.save(acmeAuthorization);

        int databaseSizeBeforeDelete = acmeAuthorizationRepository.findAll().size();

        // Delete the acmeAuthorization
        restAcmeAuthorizationMockMvc.perform(delete("/api/acme-authorizations/{id}", acmeAuthorization.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeAuthorization> acmeAuthorizationList = acmeAuthorizationRepository.findAll();
        assertThat(acmeAuthorizationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
