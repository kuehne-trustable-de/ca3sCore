package de.trustable.ca3sjh.web.rest;

import de.trustable.ca3sjh.Ca3SJhApp;
import de.trustable.ca3sjh.domain.CSR;
import de.trustable.ca3sjh.repository.CSRRepository;
import de.trustable.ca3sjh.service.CSRService;
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
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static de.trustable.ca3sjh.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3sjh.domain.enumeration.CsrStatus;
/**
 * Integration tests for the {@link CSRResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class CSRResourceIT {

    private static final String DEFAULT_CSR_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_CSR_BASE_64 = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_REQUESTED_ON = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REQUESTED_ON = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_REQUESTED_ON = LocalDate.ofEpochDay(-1L);

    private static final CsrStatus DEFAULT_STATUS = CsrStatus.Processing;
    private static final CsrStatus UPDATED_STATUS = CsrStatus.Issued;

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

    private static final String DEFAULT_PUBLIC_KEY_HASH = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_KEY_HASH = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64 = "BBBBBBBBBB";

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
            .requestedOn(DEFAULT_REQUESTED_ON)
            .status(DEFAULT_STATUS)
            .processInstanceId(DEFAULT_PROCESS_INSTANCE_ID)
            .signingAlgorithm(DEFAULT_SIGNING_ALGORITHM)
            .isCSRValid(DEFAULT_IS_CSR_VALID)
            .x509KeySpec(DEFAULT_X_509_KEY_SPEC)
            .publicKeyAlgorithm(DEFAULT_PUBLIC_KEY_ALGORITHM)
            .publicKeyHash(DEFAULT_PUBLIC_KEY_HASH)
            .subjectPublicKeyInfoBase64(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
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
            .requestedOn(UPDATED_REQUESTED_ON)
            .status(UPDATED_STATUS)
            .processInstanceId(UPDATED_PROCESS_INSTANCE_ID)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .isCSRValid(UPDATED_IS_CSR_VALID)
            .x509KeySpec(UPDATED_X_509_KEY_SPEC)
            .publicKeyAlgorithm(UPDATED_PUBLIC_KEY_ALGORITHM)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .subjectPublicKeyInfoBase64(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
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
        assertThat(testCSR.getRequestedOn()).isEqualTo(DEFAULT_REQUESTED_ON);
        assertThat(testCSR.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testCSR.getProcessInstanceId()).isEqualTo(DEFAULT_PROCESS_INSTANCE_ID);
        assertThat(testCSR.getSigningAlgorithm()).isEqualTo(DEFAULT_SIGNING_ALGORITHM);
        assertThat(testCSR.isIsCSRValid()).isEqualTo(DEFAULT_IS_CSR_VALID);
        assertThat(testCSR.getx509KeySpec()).isEqualTo(DEFAULT_X_509_KEY_SPEC);
        assertThat(testCSR.getPublicKeyAlgorithm()).isEqualTo(DEFAULT_PUBLIC_KEY_ALGORITHM);
        assertThat(testCSR.getPublicKeyHash()).isEqualTo(DEFAULT_PUBLIC_KEY_HASH);
        assertThat(testCSR.getSubjectPublicKeyInfoBase64()).isEqualTo(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cSR.getId().intValue())))
            .andExpect(jsonPath("$.[*].csrBase64").value(hasItem(DEFAULT_CSR_BASE_64.toString())))
            .andExpect(jsonPath("$.[*].requestedOn").value(hasItem(DEFAULT_REQUESTED_ON.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].processInstanceId").value(hasItem(DEFAULT_PROCESS_INSTANCE_ID.toString())))
            .andExpect(jsonPath("$.[*].signingAlgorithm").value(hasItem(DEFAULT_SIGNING_ALGORITHM.toString())))
            .andExpect(jsonPath("$.[*].isCSRValid").value(hasItem(DEFAULT_IS_CSR_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].x509KeySpec").value(hasItem(DEFAULT_X_509_KEY_SPEC.toString())))
            .andExpect(jsonPath("$.[*].publicKeyAlgorithm").value(hasItem(DEFAULT_PUBLIC_KEY_ALGORITHM.toString())))
            .andExpect(jsonPath("$.[*].publicKeyHash").value(hasItem(DEFAULT_PUBLIC_KEY_HASH.toString())))
            .andExpect(jsonPath("$.[*].subjectPublicKeyInfoBase64").value(hasItem(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64.toString())));
    }
    
    @Test
    @Transactional
    public void getCSR() throws Exception {
        // Initialize the database
        cSRRepository.saveAndFlush(cSR);

        // Get the cSR
        restCSRMockMvc.perform(get("/api/csrs/{id}", cSR.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(cSR.getId().intValue()))
            .andExpect(jsonPath("$.csrBase64").value(DEFAULT_CSR_BASE_64.toString()))
            .andExpect(jsonPath("$.requestedOn").value(DEFAULT_REQUESTED_ON.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.processInstanceId").value(DEFAULT_PROCESS_INSTANCE_ID.toString()))
            .andExpect(jsonPath("$.signingAlgorithm").value(DEFAULT_SIGNING_ALGORITHM.toString()))
            .andExpect(jsonPath("$.isCSRValid").value(DEFAULT_IS_CSR_VALID.booleanValue()))
            .andExpect(jsonPath("$.x509KeySpec").value(DEFAULT_X_509_KEY_SPEC.toString()))
            .andExpect(jsonPath("$.publicKeyAlgorithm").value(DEFAULT_PUBLIC_KEY_ALGORITHM.toString()))
            .andExpect(jsonPath("$.publicKeyHash").value(DEFAULT_PUBLIC_KEY_HASH.toString()))
            .andExpect(jsonPath("$.subjectPublicKeyInfoBase64").value(DEFAULT_SUBJECT_PUBLIC_KEY_INFO_BASE_64.toString()));
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
            .requestedOn(UPDATED_REQUESTED_ON)
            .status(UPDATED_STATUS)
            .processInstanceId(UPDATED_PROCESS_INSTANCE_ID)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .isCSRValid(UPDATED_IS_CSR_VALID)
            .x509KeySpec(UPDATED_X_509_KEY_SPEC)
            .publicKeyAlgorithm(UPDATED_PUBLIC_KEY_ALGORITHM)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .subjectPublicKeyInfoBase64(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64);

        restCSRMockMvc.perform(put("/api/csrs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCSR)))
            .andExpect(status().isOk());

        // Validate the CSR in the database
        List<CSR> cSRList = cSRRepository.findAll();
        assertThat(cSRList).hasSize(databaseSizeBeforeUpdate);
        CSR testCSR = cSRList.get(cSRList.size() - 1);
        assertThat(testCSR.getCsrBase64()).isEqualTo(UPDATED_CSR_BASE_64);
        assertThat(testCSR.getRequestedOn()).isEqualTo(UPDATED_REQUESTED_ON);
        assertThat(testCSR.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testCSR.getProcessInstanceId()).isEqualTo(UPDATED_PROCESS_INSTANCE_ID);
        assertThat(testCSR.getSigningAlgorithm()).isEqualTo(UPDATED_SIGNING_ALGORITHM);
        assertThat(testCSR.isIsCSRValid()).isEqualTo(UPDATED_IS_CSR_VALID);
        assertThat(testCSR.getx509KeySpec()).isEqualTo(UPDATED_X_509_KEY_SPEC);
        assertThat(testCSR.getPublicKeyAlgorithm()).isEqualTo(UPDATED_PUBLIC_KEY_ALGORITHM);
        assertThat(testCSR.getPublicKeyHash()).isEqualTo(UPDATED_PUBLIC_KEY_HASH);
        assertThat(testCSR.getSubjectPublicKeyInfoBase64()).isEqualTo(UPDATED_SUBJECT_PUBLIC_KEY_INFO_BASE_64);
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

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CSR.class);
        CSR cSR1 = new CSR();
        cSR1.setId(1L);
        CSR cSR2 = new CSR();
        cSR2.setId(cSR1.getId());
        assertThat(cSR1).isEqualTo(cSR2);
        cSR2.setId(2L);
        assertThat(cSR1).isNotEqualTo(cSR2);
        cSR1.setId(null);
        assertThat(cSR1).isNotEqualTo(cSR2);
    }
}
