package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.CSRService;
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
import org.springframework.util.Base64Utils;
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
        final CSRResource cSRResource = new CSRResource(cSRService);
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
            .andExpect(status().isCreated());

        // Validate the CSR in the database
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeCreate + 1);
        CSR testCSR = cSRList.get(cSRList.size() - 1);
        assertThat(testCSR.getCsrBase64()).isEqualTo(DEFAULT_CSR_BASE_64);
        assertThat(testCSR.getSubject()).isEqualTo(DEFAULT_SUBJECT);
        assertThat(testCSR.getSans()).isEqualTo(DEFAULT_SANS);
        assertThat(testCSR.getRequestedOn()).isEqualTo(DEFAULT_REQUESTED_ON);
        assertThat(testCSR.getRequestedBy()).isEqualTo(DEFAULT_REQUESTED_BY);
        assertThat(testCSR.getPipelineType()).isEqualTo(DEFAULT_PIPELINE_TYPE);
        assertThat(testCSR.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testCSR.getAdministeredBy()).isEqualTo(DEFAULT_ADMINISTERED_BY);
        assertThat(testCSR.getApprovedOn()).isEqualTo(DEFAULT_APPROVED_ON);
        assertThat(testCSR.getRejectedOn()).isEqualTo(DEFAULT_REJECTED_ON);
        assertThat(testCSR.getRejectionReason()).isEqualTo(DEFAULT_REJECTION_REASON);
        assertThat(testCSR.getProcessInstanceId()).isEqualTo(DEFAULT_PROCESS_INSTANCE_ID);
        assertThat(testCSR.getSigningAlgorithm()).isEqualTo(DEFAULT_SIGNING_ALGORITHM);
        assertThat(testCSR.isIsCSRValid()).isEqualTo(DEFAULT_IS_CSR_VALID);
        assertThat(testCSR.getx509KeySpec()).isEqualTo(DEFAULT_X_509_KEY_SPEC);
        assertThat(testCSR.getPublicKeyAlgorithm()).isEqualTo(DEFAULT_PUBLIC_KEY_ALGORITHM);
        assertThat(testCSR.getKeyAlgorithm()).isEqualTo(DEFAULT_KEY_ALGORITHM);
        assertThat(testCSR.getKeyLength()).isEqualTo(DEFAULT_KEY_LENGTH);
        assertThat(testCSR.getPublicKeyHash()).isEqualTo(DEFAULT_PUBLIC_KEY_HASH);
        assertThat(testCSR.isServersideKeyGeneration()).isEqualTo(DEFAULT_SERVERSIDE_KEY_GENERATION);
        assertThat(testCSR.getSubjectPublicKeyInfoBase64()).isEqualTo(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
        assertThat(testCSR.getRequestorComment()).isEqualTo(DEFAULT_REQUESTOR_COMMENT);
        assertThat(testCSR.getAdministrationComment()).isEqualTo(DEFAULT_ADMINISTRATION_COMMENT);
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

    @Test
    @Transactional
    public void getAllCSRS() throws Exception {
        // Initialize the database
        cSRRepository.saveAndFlush(cSR);

        // Get all the cSRList
        restCSRMockMvc.perform(get("/api/csrs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
    
    @Test
    @Transactional
    public void getCSR() throws Exception {
        // Initialize the database
        cSRRepository.saveAndFlush(cSR);

        // Get the cSR
        restCSRMockMvc.perform(get("/api/csrs/{id}", cSR.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cSR.getId().intValue()))
            .andExpect(jsonPath("$.csrBase64").value(DEFAULT_CSR_BASE_64.toString()))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT))
            .andExpect(jsonPath("$.sans").value(DEFAULT_SANS))
            .andExpect(jsonPath("$.requestedOn").value(DEFAULT_REQUESTED_ON.toString()))
            .andExpect(jsonPath("$.requestedBy").value(DEFAULT_REQUESTED_BY))
            .andExpect(jsonPath("$.pipelineType").value(DEFAULT_PIPELINE_TYPE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.administeredBy").value(DEFAULT_ADMINISTERED_BY))
            .andExpect(jsonPath("$.approvedOn").value(DEFAULT_APPROVED_ON.toString()))
            .andExpect(jsonPath("$.rejectedOn").value(DEFAULT_REJECTED_ON.toString()))
            .andExpect(jsonPath("$.rejectionReason").value(DEFAULT_REJECTION_REASON))
            .andExpect(jsonPath("$.processInstanceId").value(DEFAULT_PROCESS_INSTANCE_ID))
            .andExpect(jsonPath("$.signingAlgorithm").value(DEFAULT_SIGNING_ALGORITHM))
            .andExpect(jsonPath("$.isCSRValid").value(DEFAULT_IS_CSR_VALID.booleanValue()))
            .andExpect(jsonPath("$.x509KeySpec").value(DEFAULT_X_509_KEY_SPEC))
            .andExpect(jsonPath("$.publicKeyAlgorithm").value(DEFAULT_PUBLIC_KEY_ALGORITHM))
            .andExpect(jsonPath("$.keyAlgorithm").value(DEFAULT_KEY_ALGORITHM))
            .andExpect(jsonPath("$.keyLength").value(DEFAULT_KEY_LENGTH))
            .andExpect(jsonPath("$.publicKeyHash").value(DEFAULT_PUBLIC_KEY_HASH))
            .andExpect(jsonPath("$.serversideKeyGeneration").value(DEFAULT_SERVERSIDE_KEY_GENERATION.booleanValue()))
            .andExpect(jsonPath("$.subjectPublicKeyInfoBase64").value(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64.toString()))
            .andExpect(jsonPath("$.requestorComment").value(DEFAULT_REQUESTOR_COMMENT.toString()))
            .andExpect(jsonPath("$.administrationComment").value(DEFAULT_ADMINISTRATION_COMMENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCSR() throws Exception {
        // Get the cSR
        restCSRMockMvc.perform(get("/api/csrs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
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
            .andExpect(status().isOk());

        // Validate the CSR in the database
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeUpdate);
        CSR testCSR = cSRList.get(cSRList.size() - 1);
        assertThat(testCSR.getCsrBase64()).isEqualTo(UPDATED_CSR_BASE_64);
        assertThat(testCSR.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testCSR.getSans()).isEqualTo(UPDATED_SANS);
        assertThat(testCSR.getRequestedOn()).isEqualTo(UPDATED_REQUESTED_ON);
        assertThat(testCSR.getRequestedBy()).isEqualTo(UPDATED_REQUESTED_BY);
        assertThat(testCSR.getPipelineType()).isEqualTo(UPDATED_PIPELINE_TYPE);
        assertThat(testCSR.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testCSR.getAdministeredBy()).isEqualTo(UPDATED_ADMINISTERED_BY);
        assertThat(testCSR.getApprovedOn()).isEqualTo(UPDATED_APPROVED_ON);
        assertThat(testCSR.getRejectedOn()).isEqualTo(UPDATED_REJECTED_ON);
        assertThat(testCSR.getRejectionReason()).isEqualTo(UPDATED_REJECTION_REASON);
        assertThat(testCSR.getProcessInstanceId()).isEqualTo(UPDATED_PROCESS_INSTANCE_ID);
        assertThat(testCSR.getSigningAlgorithm()).isEqualTo(UPDATED_SIGNING_ALGORITHM);
        assertThat(testCSR.isIsCSRValid()).isEqualTo(UPDATED_IS_CSR_VALID);
        assertThat(testCSR.getx509KeySpec()).isEqualTo(UPDATED_X_509_KEY_SPEC);
        assertThat(testCSR.getPublicKeyAlgorithm()).isEqualTo(UPDATED_PUBLIC_KEY_ALGORITHM);
        assertThat(testCSR.getKeyAlgorithm()).isEqualTo(UPDATED_KEY_ALGORITHM);
        assertThat(testCSR.getKeyLength()).isEqualTo(UPDATED_KEY_LENGTH);
        assertThat(testCSR.getPublicKeyHash()).isEqualTo(UPDATED_PUBLIC_KEY_HASH);
        assertThat(testCSR.isServersideKeyGeneration()).isEqualTo(UPDATED_SERVERSIDE_KEY_GENERATION);
        assertThat(testCSR.getSubjectPublicKeyInfoBase64()).isEqualTo(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
        assertThat(testCSR.getRequestorComment()).isEqualTo(UPDATED_REQUESTOR_COMMENT);
        assertThat(testCSR.getAdministrationComment()).isEqualTo(UPDATED_ADMINISTRATION_COMMENT);
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
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
