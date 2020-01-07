package de.trustable.ca3s.core.web.rest;

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

import de.trustable.ca3s.core.Ca3SJhApp;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import de.trustable.ca3s.core.service.AcmeChallengeService;
import de.trustable.ca3s.core.web.rest.AcmeChallengeResource;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Integration tests for the {@link AcmeChallengeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class AcmeChallengeResourceIT {

    private static final Long DEFAULT_CHALLENGE_ID = 1L;
    private static final Long UPDATED_CHALLENGE_ID = 2L;
    private static final Long SMALLER_CHALLENGE_ID = 1L - 1L;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_VALIDATED = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VALIDATED = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_VALIDATED = LocalDate.ofEpochDay(-1L);

    private static final ChallengeStatus DEFAULT_STATUS = ChallengeStatus.PENDING;
    private static final ChallengeStatus UPDATED_STATUS = ChallengeStatus.VALID;

    @Autowired
    private AcmeChallengeRepository acmeChallengeRepository;

    @Autowired
    private AcmeChallengeService acmeChallengeService;

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

    private MockMvc restAcmeChallengeMockMvc;

    private AcmeChallenge acmeChallenge;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AcmeChallengeResource acmeChallengeResource = new AcmeChallengeResource(acmeChallengeService);
        this.restAcmeChallengeMockMvc = MockMvcBuilders.standaloneSetup(acmeChallengeResource)
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
    public static AcmeChallenge createEntity(EntityManager em) {
        AcmeChallenge acmeChallenge = new AcmeChallenge()
            .challengeId(DEFAULT_CHALLENGE_ID)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE)
            .token(DEFAULT_TOKEN)
            .validated(DEFAULT_VALIDATED)
            .status(DEFAULT_STATUS);
        return acmeChallenge;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeChallenge createUpdatedEntity(EntityManager em) {
        AcmeChallenge acmeChallenge = new AcmeChallenge()
            .challengeId(UPDATED_CHALLENGE_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE)
            .token(UPDATED_TOKEN)
            .validated(UPDATED_VALIDATED)
            .status(UPDATED_STATUS);
        return acmeChallenge;
    }

    @BeforeEach
    public void initTest() {
        acmeChallenge = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeChallenge() throws Exception {
        int databaseSizeBeforeCreate = acmeChallengeRepository.findAll().size();

        // Create the AcmeChallenge
        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isCreated());

        // Validate the AcmeChallenge in the database
        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeChallenge testAcmeChallenge = acmeChallengeList.get(acmeChallengeList.size() - 1);
        assertThat(testAcmeChallenge.getChallengeId()).isEqualTo(DEFAULT_CHALLENGE_ID);
        assertThat(testAcmeChallenge.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAcmeChallenge.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testAcmeChallenge.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testAcmeChallenge.getValidated()).isEqualTo(DEFAULT_VALIDATED);
        assertThat(testAcmeChallenge.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createAcmeChallengeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeChallengeRepository.findAll().size();

        // Create the AcmeChallenge with an existing ID
        acmeChallenge.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeChallenge in the database
        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkChallengeIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeChallengeRepository.findAll().size();
        // set the field null
        acmeChallenge.setChallengeId(null);

        // Create the AcmeChallenge, which fails.

        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeChallengeRepository.findAll().size();
        // set the field null
        acmeChallenge.setType(null);

        // Create the AcmeChallenge, which fails.

        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeChallengeRepository.findAll().size();
        // set the field null
        acmeChallenge.setValue(null);

        // Create the AcmeChallenge, which fails.

        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeChallengeRepository.findAll().size();
        // set the field null
        acmeChallenge.setToken(null);

        // Create the AcmeChallenge, which fails.

        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeChallengeRepository.findAll().size();
        // set the field null
        acmeChallenge.setStatus(null);

        // Create the AcmeChallenge, which fails.

        restAcmeChallengeMockMvc.perform(post("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeChallenges() throws Exception {
        // Initialize the database
        acmeChallengeRepository.saveAndFlush(acmeChallenge);

        // Get all the acmeChallengeList
        restAcmeChallengeMockMvc.perform(get("/api/acme-challenges?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeChallenge.getId().intValue())))
            .andExpect(jsonPath("$.[*].challengeId").value(hasItem(DEFAULT_CHALLENGE_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN.toString())))
            .andExpect(jsonPath("$.[*].validated").value(hasItem(DEFAULT_VALIDATED.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
    
    @Test
    @Transactional
    public void getAcmeChallenge() throws Exception {
        // Initialize the database
        acmeChallengeRepository.saveAndFlush(acmeChallenge);

        // Get the acmeChallenge
        restAcmeChallengeMockMvc.perform(get("/api/acme-challenges/{id}", acmeChallenge.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(acmeChallenge.getId().intValue()))
            .andExpect(jsonPath("$.challengeId").value(DEFAULT_CHALLENGE_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN.toString()))
            .andExpect(jsonPath("$.validated").value(DEFAULT_VALIDATED.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeChallenge() throws Exception {
        // Get the acmeChallenge
        restAcmeChallengeMockMvc.perform(get("/api/acme-challenges/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeChallenge() throws Exception {
        // Initialize the database
        acmeChallengeService.save(acmeChallenge);

        int databaseSizeBeforeUpdate = acmeChallengeRepository.findAll().size();

        // Update the acmeChallenge
        AcmeChallenge updatedAcmeChallenge = acmeChallengeRepository.findById(acmeChallenge.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeChallenge are not directly saved in db
        em.detach(updatedAcmeChallenge);
        updatedAcmeChallenge
            .challengeId(UPDATED_CHALLENGE_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE)
            .token(UPDATED_TOKEN)
            .validated(UPDATED_VALIDATED)
            .status(UPDATED_STATUS);

        restAcmeChallengeMockMvc.perform(put("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeChallenge)))
            .andExpect(status().isOk());

        // Validate the AcmeChallenge in the database
        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeUpdate);
        AcmeChallenge testAcmeChallenge = acmeChallengeList.get(acmeChallengeList.size() - 1);
        assertThat(testAcmeChallenge.getChallengeId()).isEqualTo(UPDATED_CHALLENGE_ID);
        assertThat(testAcmeChallenge.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAcmeChallenge.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testAcmeChallenge.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testAcmeChallenge.getValidated()).isEqualTo(UPDATED_VALIDATED);
        assertThat(testAcmeChallenge.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeChallenge() throws Exception {
        int databaseSizeBeforeUpdate = acmeChallengeRepository.findAll().size();

        // Create the AcmeChallenge

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeChallengeMockMvc.perform(put("/api/acme-challenges")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeChallenge)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeChallenge in the database
        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeChallenge() throws Exception {
        // Initialize the database
        acmeChallengeService.save(acmeChallenge);

        int databaseSizeBeforeDelete = acmeChallengeRepository.findAll().size();

        // Delete the acmeChallenge
        restAcmeChallengeMockMvc.perform(delete("/api/acme-challenges/{id}", acmeChallenge.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeChallenge> acmeChallengeList = acmeChallengeRepository.findAll();
        assertThat(acmeChallengeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AcmeChallenge.class);
        AcmeChallenge acmeChallenge1 = new AcmeChallenge();
        acmeChallenge1.setId(1L);
        AcmeChallenge acmeChallenge2 = new AcmeChallenge();
        acmeChallenge2.setId(acmeChallenge1.getId());
        assertThat(acmeChallenge1).isEqualTo(acmeChallenge2);
        acmeChallenge2.setId(2L);
        assertThat(acmeChallenge1).isNotEqualTo(acmeChallenge2);
        acmeChallenge1.setId(null);
        assertThat(acmeChallenge1).isNotEqualTo(acmeChallenge2);
    }
}
