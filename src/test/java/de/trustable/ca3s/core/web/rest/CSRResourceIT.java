package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.CSRService;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import de.trustable.ca3s.core.web.rest.util.CurrentUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
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

import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
/**
 * Integration tests for the {@link CSRResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class CSRResourceIT {

    private static final String DEFAULT_CSR_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_CSR_BASE_64 = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final String DEFAULT_SANS = "AAAAAAAAAA";
    private static final String UPDATED_SANS = "BBBBBBBBBB";

    private static final Instant DEFAULT_REQUESTED_ON = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUESTED_ON = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_REQUESTED_BY = "AAAAAAAAAA";
    private static final String UPDATED_REQUESTED_BY = "BBBBBBBBBB";

    private static final PipelineType DEFAULT_PIPELINE_TYPE = PipelineType.ACME;
    private static final PipelineType UPDATED_PIPELINE_TYPE = PipelineType.SCEP;

    private static final CsrStatus DEFAULT_STATUS = CsrStatus.PROCESSING;
    private static final CsrStatus UPDATED_STATUS = CsrStatus.ISSUED;

    private static final String DEFAULT_ADMINISTERED_BY = "AAAAAAAAAA";
    private static final String UPDATED_ADMINISTERED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_APPROVED_ON = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_APPROVED_ON = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_REJECTED_ON = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REJECTED_ON = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_REJECTION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REJECTION_REASON = "BBBBBBBBBB";

    private static final String DEFAULT_PROCESS_INSTANCE_ID = "AAAAAAAAAA";
    private static final String UPDATED_PROCESS_INSTANCE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_SIGNING_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_SIGNING_ALGORITHM = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_CSR_VALID = false;
    private static final Boolean UPDATED_IS_CSR_VALID = true;

    private static final String DEFAULT_X_509_KEY_SPEC = "AAAAAAAAAA";
    private static final String UPDATED_X_509_KEY_SPEC = "BBBBBBBBBB";

    private static final String DEFAULT_PUBLIC_KEY_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_KEY_ALGORITHM = "BBBBBBBBBB";

    private static final String DEFAULT_KEY_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_KEY_ALGORITHM = "BBBBBBBBBB";

    private static final Integer DEFAULT_KEY_LENGTH = 1;
    private static final Integer UPDATED_KEY_LENGTH = 2;

    private static final String DEFAULT_PUBLIC_KEY_HASH = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_KEY_HASH = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SERVERSIDE_KEY_GENERATION = false;
    private static final Boolean UPDATED_SERVERSIDE_KEY_GENERATION = true;

    private static final String DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64 = "BBBBBBBBBB";

    private static final String DEFAULT_REQUESTOR_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_REQUESTOR_COMMENT = "BBBBBBBBBB";

    private static final String DEFAULT_ADMINISTRATION_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_ADMINISTRATION_COMMENT = "BBBBBBBBBB";

    @Autowired
    private CSRRepository cSRRepository;

    @Autowired
    private CSRService cSRService;

    @Autowired
    private CSRUtil csrUtil;

    @Autowired
    private PipelineUtil pipelineUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserUtil currentUserUtil;

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

    private MockMvc restCSRMockMvc;

    private CSR cSR;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CSRResource cSRResource = new CSRResource(cSRService, csrUtil, pipelineUtil, userRepository, currentUserUtil,
            "none", false);
        this.restCSRMockMvc = MockMvcBuilders.standaloneSetup(cSRResource)
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
    public static CSR createEntity(EntityManager em) {
        CSR cSR = new CSR()
            .csrBase64(DEFAULT_CSR_BASE_64)
            .subject(DEFAULT_SUBJECT)
            .sans(DEFAULT_SANS)
            .requestedOn(DEFAULT_REQUESTED_ON)
            .requestedBy(DEFAULT_REQUESTED_BY)
            .pipelineType(DEFAULT_PIPELINE_TYPE)
            .status(DEFAULT_STATUS)
            .administeredBy(DEFAULT_ADMINISTERED_BY)
            .approvedOn(DEFAULT_APPROVED_ON)
            .rejectedOn(DEFAULT_REJECTED_ON)
            .rejectionReason(DEFAULT_REJECTION_REASON)
            .processInstanceId(DEFAULT_PROCESS_INSTANCE_ID)
            .signingAlgorithm(DEFAULT_SIGNING_ALGORITHM)
            .isCSRValid(DEFAULT_IS_CSR_VALID)
            .x509KeySpec(DEFAULT_X_509_KEY_SPEC)
            .publicKeyAlgorithm(DEFAULT_PUBLIC_KEY_ALGORITHM)
            .keyAlgorithm(DEFAULT_KEY_ALGORITHM)
            .keyLength(DEFAULT_KEY_LENGTH)
            .publicKeyHash(DEFAULT_PUBLIC_KEY_HASH)
            .serversideKeyGeneration(DEFAULT_SERVERSIDE_KEY_GENERATION)
            .subjectPublicKeyInfoBase64(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64)
            .requestorComment(DEFAULT_REQUESTOR_COMMENT)
            .administrationComment(DEFAULT_ADMINISTRATION_COMMENT);
        return cSR;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CSR createUpdatedEntity(EntityManager em) {
        CSR cSR = new CSR()
            .csrBase64(UPDATED_CSR_BASE_64)
            .subject(UPDATED_SUBJECT)
            .sans(UPDATED_SANS)
            .requestedOn(UPDATED_REQUESTED_ON)
            .requestedBy(UPDATED_REQUESTED_BY)
            .pipelineType(UPDATED_PIPELINE_TYPE)
            .status(UPDATED_STATUS)
            .administeredBy(UPDATED_ADMINISTERED_BY)
            .approvedOn(UPDATED_APPROVED_ON)
            .rejectedOn(UPDATED_REJECTED_ON)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .processInstanceId(UPDATED_PROCESS_INSTANCE_ID)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .isCSRValid(UPDATED_IS_CSR_VALID)
            .x509KeySpec(UPDATED_X_509_KEY_SPEC)
            .publicKeyAlgorithm(UPDATED_PUBLIC_KEY_ALGORITHM)
            .keyAlgorithm(UPDATED_KEY_ALGORITHM)
            .keyLength(UPDATED_KEY_LENGTH)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .serversideKeyGeneration(UPDATED_SERVERSIDE_KEY_GENERATION)
            .subjectPublicKeyInfoBase64(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64)
            .requestorComment(UPDATED_REQUESTOR_COMMENT)
            .administrationComment(UPDATED_ADMINISTRATION_COMMENT);
        return cSR;
    }

    @BeforeEach
    public void initTest() {
        cSR = createEntity(em);
    }

    @Test
    @Transactional
    public void createCSR() throws Exception {
        int databaseSizeBeforeCreate = cSRRepository.findAll().size();

        // Create the CSR
        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    public void createCSRWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = cSRRepository.findAll().size();

        // Create the CSR with an existing ID
        cSR.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        // Validate the CSR in the database
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkSubjectIsRequired() throws Exception {
        int databaseSizeBeforeTest = cSRRepository.findAll().size();
        // set the field null
        cSR.setSubject(null);

        // Create the CSR, which fails.

        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestedOnIsRequired() throws Exception {
        int databaseSizeBeforeTest = cSRRepository.findAll().size();
        // set the field null
        cSR.setRequestedOn(null);

        // Create the CSR, which fails.

        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestedByIsRequired() throws Exception {
        int databaseSizeBeforeTest = cSRRepository.findAll().size();
        // set the field null
        cSR.setRequestedBy(null);

        // Create the CSR, which fails.

        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPipelineTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = cSRRepository.findAll().size();
        // set the field null
        cSR.setPipelineType(null);

        // Create the CSR, which fails.

        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = cSRRepository.findAll().size();
        // set the field null
        cSR.setStatus(null);

        // Create the CSR, which fails.

        restCSRMockMvc.perform(post("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeTest);
    }

    /*
    @Test
    @Transactional
    public void getAllCSRS() throws Exception {
        // Initialize the database
        cSRRepository.saveAndFlush(cSR);

        // Get all the cSRList
        restCSRMockMvc.perform(get("/api/csrs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE ))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cSR.getId().intValue())))
            .andExpect(jsonPath("$.[*].csrBase64").value(hasItem(DEFAULT_CSR_BASE_64.toString())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].sans").value(hasItem(DEFAULT_SANS)))
            .andExpect(jsonPath("$.[*].requestedOn").value(hasItem(DEFAULT_REQUESTED_ON.toString())))
            .andExpect(jsonPath("$.[*].requestedBy").value(hasItem(DEFAULT_REQUESTED_BY)))
            .andExpect(jsonPath("$.[*].pipelineType").value(hasItem(DEFAULT_PIPELINE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].administeredBy").value(hasItem(DEFAULT_ADMINISTERED_BY)))
            .andExpect(jsonPath("$.[*].approvedOn").value(hasItem(DEFAULT_APPROVED_ON.toString())))
            .andExpect(jsonPath("$.[*].rejectedOn").value(hasItem(DEFAULT_REJECTED_ON.toString())))
            .andExpect(jsonPath("$.[*].rejectionReason").value(hasItem(DEFAULT_REJECTION_REASON)))
            .andExpect(jsonPath("$.[*].processInstanceId").value(hasItem(DEFAULT_PROCESS_INSTANCE_ID)))
            .andExpect(jsonPath("$.[*].signingAlgorithm").value(hasItem(DEFAULT_SIGNING_ALGORITHM)))
            .andExpect(jsonPath("$.[*].isCSRValid").value(hasItem(DEFAULT_IS_CSR_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].x509KeySpec").value(hasItem(DEFAULT_X_509_KEY_SPEC)))
            .andExpect(jsonPath("$.[*].publicKeyAlgorithm").value(hasItem(DEFAULT_PUBLIC_KEY_ALGORITHM)))
            .andExpect(jsonPath("$.[*].keyAlgorithm").value(hasItem(DEFAULT_KEY_ALGORITHM)))
            .andExpect(jsonPath("$.[*].keyLength").value(hasItem(DEFAULT_KEY_LENGTH)))
            .andExpect(jsonPath("$.[*].publicKeyHash").value(hasItem(DEFAULT_PUBLIC_KEY_HASH)))
            .andExpect(jsonPath("$.[*].serversideKeyGeneration").value(hasItem(DEFAULT_SERVERSIDE_KEY_GENERATION.booleanValue())))
            .andExpect(jsonPath("$.[*].subjectPublicKeyInfoBase64").value(hasItem(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64.toString())))
            .andExpect(jsonPath("$.[*].requestorComment").value(hasItem(DEFAULT_REQUESTOR_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].administrationComment").value(hasItem(DEFAULT_ADMINISTRATION_COMMENT.toString())));
    }
*/

    @Test
    @Transactional
    public void getCSR() throws Exception {
        // Initialize the database
        cSRRepository.saveAndFlush(cSR);

        // Get the cSR
        restCSRMockMvc.perform(get("/api/csrs/{id}", cSR.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void getNonExistingCSR() throws Exception {
        // Get the cSR
        restCSRMockMvc.perform(get("/api/csrs/{id}", Long.MAX_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateCSR() throws Exception {
        // Initialize the database
        cSRService.save(cSR);

        int databaseSizeBeforeUpdate = cSRRepository.findAll().size();

        // Update the cSR
        CSR updatedCSR = cSRRepository.findById(cSR.getId()).get();
        // Disconnect from session so that the updates on updatedCSR are not directly saved in db
        em.detach(updatedCSR);
        updatedCSR
            .csrBase64(UPDATED_CSR_BASE_64)
            .subject(UPDATED_SUBJECT)
            .sans(UPDATED_SANS)
            .requestedOn(UPDATED_REQUESTED_ON)
            .requestedBy(UPDATED_REQUESTED_BY)
            .pipelineType(UPDATED_PIPELINE_TYPE)
            .status(UPDATED_STATUS)
            .administeredBy(UPDATED_ADMINISTERED_BY)
            .approvedOn(UPDATED_APPROVED_ON)
            .rejectedOn(UPDATED_REJECTED_ON)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .processInstanceId(UPDATED_PROCESS_INSTANCE_ID)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .isCSRValid(UPDATED_IS_CSR_VALID)
            .x509KeySpec(UPDATED_X_509_KEY_SPEC)
            .publicKeyAlgorithm(UPDATED_PUBLIC_KEY_ALGORITHM)
            .keyAlgorithm(UPDATED_KEY_ALGORITHM)
            .keyLength(UPDATED_KEY_LENGTH)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .serversideKeyGeneration(UPDATED_SERVERSIDE_KEY_GENERATION)
            .subjectPublicKeyInfoBase64(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64)
            .requestorComment(UPDATED_REQUESTOR_COMMENT)
            .administrationComment(UPDATED_ADMINISTRATION_COMMENT);

        restCSRMockMvc.perform(put("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCSR)))
            .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    public void updateNonExistingCSR() throws Exception {
        int databaseSizeBeforeUpdate = cSRRepository.findAll().size();

        // Create the CSR

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCSRMockMvc.perform(put("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cSR)))
            .andExpect(status().isBadRequest());

        // Validate the CSR in the database
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCSR() throws Exception {
        // Initialize the database
        cSRService.save(cSR);

        int databaseSizeBeforeDelete = cSRRepository.findAll().size();

        // Delete the cSR
        restCSRMockMvc.perform(delete("/api/csrs/{id}", cSR.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<CSR> cSRList = cSRRepository.findAll();
        // no effect expected
        assertThat(cSRList).hasSize(databaseSizeBeforeDelete );
    }
}
