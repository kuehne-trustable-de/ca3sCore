package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.CertificateService;
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

/**
 * Integration tests for the {@link CertificateResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class CertificateResourceIT {

    private static final String DEFAULT_TBS_DIGEST = "AAAAAAAAAA";
    private static final String UPDATED_TBS_DIGEST = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final String DEFAULT_ISSUER = "AAAAAAAAAA";
    private static final String UPDATED_ISSUER = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_FINGERPRINT = "AAAAAAAAAA";
    private static final String UPDATED_FINGERPRINT = "BBBBBBBBBB";

    private static final String DEFAULT_SERIAL = "AAAAAAAAAA";
    private static final String UPDATED_SERIAL = "BBBBBBBBBB";

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_VALID_TO = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_KEY_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_KEY_ALGORITHM = "BBBBBBBBBB";

    private static final Integer DEFAULT_KEY_LENGTH = 1;
    private static final Integer UPDATED_KEY_LENGTH = 2;

    private static final String DEFAULT_CURVE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_CURVE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_HASHING_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_HASHING_ALGORITHM = "BBBBBBBBBB";

    private static final String DEFAULT_PADDING_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_PADDING_ALGORITHM = "BBBBBBBBBB";

    private static final String DEFAULT_SIGNING_ALGORITHM = "AAAAAAAAAA";
    private static final String UPDATED_SIGNING_ALGORITHM = "BBBBBBBBBB";

    private static final String DEFAULT_CREATION_EXECUTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_CREATION_EXECUTION_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_CONTENT_ADDED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CONTENT_ADDED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_REVOKED_SINCE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REVOKED_SINCE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_REVOCATION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REVOCATION_REASON = "BBBBBBBBBB";

    private static final Boolean DEFAULT_REVOKED = false;
    private static final Boolean UPDATED_REVOKED = true;

    private static final String DEFAULT_REVOCATION_EXECUTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_REVOCATION_EXECUTION_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_END_ENTITY = false;
    private static final Boolean UPDATED_END_ENTITY = true;

    private static final Boolean DEFAULT_SELFSIGNED = false;
    private static final Boolean UPDATED_SELFSIGNED = true;

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateService certificateService;

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

    private MockMvc restCertificateMockMvc;

    private Certificate certificate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CertificateResource certificateResource = new CertificateResource(certificateService);
        this.restCertificateMockMvc = MockMvcBuilders.standaloneSetup(certificateResource)
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
    public static Certificate createEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .tbsDigest(DEFAULT_TBS_DIGEST)
            .subject(DEFAULT_SUBJECT)
            .issuer(DEFAULT_ISSUER)
            .type(DEFAULT_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .fingerprint(DEFAULT_FINGERPRINT)
            .serial(DEFAULT_SERIAL)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .keyAlgorithm(DEFAULT_KEY_ALGORITHM)
            .keyLength(DEFAULT_KEY_LENGTH)
            .curveName(DEFAULT_CURVE_NAME)
            .hashingAlgorithm(DEFAULT_HASHING_ALGORITHM)
            .paddingAlgorithm(DEFAULT_PADDING_ALGORITHM)
            .signingAlgorithm(DEFAULT_SIGNING_ALGORITHM)
            .creationExecutionId(DEFAULT_CREATION_EXECUTION_ID)
            .contentAddedAt(DEFAULT_CONTENT_ADDED_AT)
            .revokedSince(DEFAULT_REVOKED_SINCE)
            .revocationReason(DEFAULT_REVOCATION_REASON)
            .revoked(DEFAULT_REVOKED)
            .revocationExecutionId(DEFAULT_REVOCATION_EXECUTION_ID)
            .endEntity(DEFAULT_END_ENTITY)
            .selfsigned(DEFAULT_SELFSIGNED)
            .content(DEFAULT_CONTENT);
        return certificate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Certificate createUpdatedEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .tbsDigest(UPDATED_TBS_DIGEST)
            .subject(UPDATED_SUBJECT)
            .issuer(UPDATED_ISSUER)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .fingerprint(UPDATED_FINGERPRINT)
            .serial(UPDATED_SERIAL)
            .validFrom(UPDATED_VALID_FROM)
            .validTo(UPDATED_VALID_TO)
            .keyAlgorithm(UPDATED_KEY_ALGORITHM)
            .keyLength(UPDATED_KEY_LENGTH)
            .curveName(UPDATED_CURVE_NAME)
            .hashingAlgorithm(UPDATED_HASHING_ALGORITHM)
            .paddingAlgorithm(UPDATED_PADDING_ALGORITHM)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .creationExecutionId(UPDATED_CREATION_EXECUTION_ID)
            .contentAddedAt(UPDATED_CONTENT_ADDED_AT)
            .revokedSince(UPDATED_REVOKED_SINCE)
            .revocationReason(UPDATED_REVOCATION_REASON)
            .revoked(UPDATED_REVOKED)
            .revocationExecutionId(UPDATED_REVOCATION_EXECUTION_ID)
            .endEntity(UPDATED_END_ENTITY)
            .selfsigned(UPDATED_SELFSIGNED)
            .content(UPDATED_CONTENT);
        return certificate;
    }

    @BeforeEach
    public void initTest() {
        certificate = createEntity(em);
    }

    @Test
    @Transactional
    public void createCertificate() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();

        // Create the Certificate
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isCreated());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate + 1);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getTbsDigest()).isEqualTo(DEFAULT_TBS_DIGEST);
        assertThat(testCertificate.getSubject()).isEqualTo(DEFAULT_SUBJECT);
        assertThat(testCertificate.getIssuer()).isEqualTo(DEFAULT_ISSUER);
        assertThat(testCertificate.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testCertificate.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCertificate.getFingerprint()).isEqualTo(DEFAULT_FINGERPRINT);
        assertThat(testCertificate.getSerial()).isEqualTo(DEFAULT_SERIAL);
        assertThat(testCertificate.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testCertificate.getValidTo()).isEqualTo(DEFAULT_VALID_TO);
        assertThat(testCertificate.getKeyAlgorithm()).isEqualTo(DEFAULT_KEY_ALGORITHM);
        assertThat(testCertificate.getKeyLength()).isEqualTo(DEFAULT_KEY_LENGTH);
        assertThat(testCertificate.getCurveName()).isEqualTo(DEFAULT_CURVE_NAME);
        assertThat(testCertificate.getHashingAlgorithm()).isEqualTo(DEFAULT_HASHING_ALGORITHM);
        assertThat(testCertificate.getPaddingAlgorithm()).isEqualTo(DEFAULT_PADDING_ALGORITHM);
        assertThat(testCertificate.getSigningAlgorithm()).isEqualTo(DEFAULT_SIGNING_ALGORITHM);
        assertThat(testCertificate.getCreationExecutionId()).isEqualTo(DEFAULT_CREATION_EXECUTION_ID);
        assertThat(testCertificate.getContentAddedAt()).isEqualTo(DEFAULT_CONTENT_ADDED_AT);
        assertThat(testCertificate.getRevokedSince()).isEqualTo(DEFAULT_REVOKED_SINCE);
        assertThat(testCertificate.getRevocationReason()).isEqualTo(DEFAULT_REVOCATION_REASON);
        assertThat(testCertificate.isRevoked()).isEqualTo(DEFAULT_REVOKED);
        assertThat(testCertificate.getRevocationExecutionId()).isEqualTo(DEFAULT_REVOCATION_EXECUTION_ID);
        assertThat(testCertificate.isEndEntity()).isEqualTo(DEFAULT_END_ENTITY);
        assertThat(testCertificate.isSelfsigned()).isEqualTo(DEFAULT_SELFSIGNED);
        assertThat(testCertificate.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    public void createCertificateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();

        // Create the Certificate with an existing ID
        certificate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTbsDigestIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setTbsDigest(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSubjectIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setSubject(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIssuerIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setIssuer(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setType(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSerialIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setSerial(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidFromIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setValidFrom(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidToIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setValidTo(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCertificates() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get all the certificateList
        restCertificateMockMvc.perform(get("/api/certificates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(certificate.getId().intValue())))
            .andExpect(jsonPath("$.[*].tbsDigest").value(hasItem(DEFAULT_TBS_DIGEST)))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].issuer").value(hasItem(DEFAULT_ISSUER)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].fingerprint").value(hasItem(DEFAULT_FINGERPRINT)))
            .andExpect(jsonPath("$.[*].serial").value(hasItem(DEFAULT_SERIAL)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].keyAlgorithm").value(hasItem(DEFAULT_KEY_ALGORITHM)))
            .andExpect(jsonPath("$.[*].keyLength").value(hasItem(DEFAULT_KEY_LENGTH)))
            .andExpect(jsonPath("$.[*].curveName").value(hasItem(DEFAULT_CURVE_NAME)))
            .andExpect(jsonPath("$.[*].hashingAlgorithm").value(hasItem(DEFAULT_HASHING_ALGORITHM)))
            .andExpect(jsonPath("$.[*].paddingAlgorithm").value(hasItem(DEFAULT_PADDING_ALGORITHM)))
            .andExpect(jsonPath("$.[*].signingAlgorithm").value(hasItem(DEFAULT_SIGNING_ALGORITHM)))
            .andExpect(jsonPath("$.[*].creationExecutionId").value(hasItem(DEFAULT_CREATION_EXECUTION_ID)))
            .andExpect(jsonPath("$.[*].contentAddedAt").value(hasItem(DEFAULT_CONTENT_ADDED_AT.toString())))
            .andExpect(jsonPath("$.[*].revokedSince").value(hasItem(DEFAULT_REVOKED_SINCE.toString())))
            .andExpect(jsonPath("$.[*].revocationReason").value(hasItem(DEFAULT_REVOCATION_REASON)))
            .andExpect(jsonPath("$.[*].revoked").value(hasItem(DEFAULT_REVOKED.booleanValue())))
            .andExpect(jsonPath("$.[*].revocationExecutionId").value(hasItem(DEFAULT_REVOCATION_EXECUTION_ID)))
            .andExpect(jsonPath("$.[*].endEntity").value(hasItem(DEFAULT_END_ENTITY.booleanValue())))
            .andExpect(jsonPath("$.[*].selfsigned").value(hasItem(DEFAULT_SELFSIGNED.booleanValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())));
    }
    
    @Test
    @Transactional
    public void getCertificate() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", certificate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(certificate.getId().intValue()))
            .andExpect(jsonPath("$.tbsDigest").value(DEFAULT_TBS_DIGEST))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT))
            .andExpect(jsonPath("$.issuer").value(DEFAULT_ISSUER))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.fingerprint").value(DEFAULT_FINGERPRINT))
            .andExpect(jsonPath("$.serial").value(DEFAULT_SERIAL))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()))
            .andExpect(jsonPath("$.keyAlgorithm").value(DEFAULT_KEY_ALGORITHM))
            .andExpect(jsonPath("$.keyLength").value(DEFAULT_KEY_LENGTH))
            .andExpect(jsonPath("$.curveName").value(DEFAULT_CURVE_NAME))
            .andExpect(jsonPath("$.hashingAlgorithm").value(DEFAULT_HASHING_ALGORITHM))
            .andExpect(jsonPath("$.paddingAlgorithm").value(DEFAULT_PADDING_ALGORITHM))
            .andExpect(jsonPath("$.signingAlgorithm").value(DEFAULT_SIGNING_ALGORITHM))
            .andExpect(jsonPath("$.creationExecutionId").value(DEFAULT_CREATION_EXECUTION_ID))
            .andExpect(jsonPath("$.contentAddedAt").value(DEFAULT_CONTENT_ADDED_AT.toString()))
            .andExpect(jsonPath("$.revokedSince").value(DEFAULT_REVOKED_SINCE.toString()))
            .andExpect(jsonPath("$.revocationReason").value(DEFAULT_REVOCATION_REASON))
            .andExpect(jsonPath("$.revoked").value(DEFAULT_REVOKED.booleanValue()))
            .andExpect(jsonPath("$.revocationExecutionId").value(DEFAULT_REVOCATION_EXECUTION_ID))
            .andExpect(jsonPath("$.endEntity").value(DEFAULT_END_ENTITY.booleanValue()))
            .andExpect(jsonPath("$.selfsigned").value(DEFAULT_SELFSIGNED.booleanValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCertificate() throws Exception {
        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCertificate() throws Exception {
        // Initialize the database
        certificateService.save(certificate);

        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // Update the certificate
        Certificate updatedCertificate = certificateRepository.findById(certificate.getId()).get();
        // Disconnect from session so that the updates on updatedCertificate are not directly saved in db
        em.detach(updatedCertificate);
        updatedCertificate
            .tbsDigest(UPDATED_TBS_DIGEST)
            .subject(UPDATED_SUBJECT)
            .issuer(UPDATED_ISSUER)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .fingerprint(UPDATED_FINGERPRINT)
            .serial(UPDATED_SERIAL)
            .validFrom(UPDATED_VALID_FROM)
            .validTo(UPDATED_VALID_TO)
            .keyAlgorithm(UPDATED_KEY_ALGORITHM)
            .keyLength(UPDATED_KEY_LENGTH)
            .curveName(UPDATED_CURVE_NAME)
            .hashingAlgorithm(UPDATED_HASHING_ALGORITHM)
            .paddingAlgorithm(UPDATED_PADDING_ALGORITHM)
            .signingAlgorithm(UPDATED_SIGNING_ALGORITHM)
            .creationExecutionId(UPDATED_CREATION_EXECUTION_ID)
            .contentAddedAt(UPDATED_CONTENT_ADDED_AT)
            .revokedSince(UPDATED_REVOKED_SINCE)
            .revocationReason(UPDATED_REVOCATION_REASON)
            .revoked(UPDATED_REVOKED)
            .revocationExecutionId(UPDATED_REVOCATION_EXECUTION_ID)
            .endEntity(UPDATED_END_ENTITY)
            .selfsigned(UPDATED_SELFSIGNED)
            .content(UPDATED_CONTENT);

        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCertificate)))
            .andExpect(status().isOk());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getTbsDigest()).isEqualTo(UPDATED_TBS_DIGEST);
        assertThat(testCertificate.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testCertificate.getIssuer()).isEqualTo(UPDATED_ISSUER);
        assertThat(testCertificate.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testCertificate.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCertificate.getFingerprint()).isEqualTo(UPDATED_FINGERPRINT);
        assertThat(testCertificate.getSerial()).isEqualTo(UPDATED_SERIAL);
        assertThat(testCertificate.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testCertificate.getValidTo()).isEqualTo(UPDATED_VALID_TO);
        assertThat(testCertificate.getKeyAlgorithm()).isEqualTo(UPDATED_KEY_ALGORITHM);
        assertThat(testCertificate.getKeyLength()).isEqualTo(UPDATED_KEY_LENGTH);
        assertThat(testCertificate.getCurveName()).isEqualTo(UPDATED_CURVE_NAME);
        assertThat(testCertificate.getHashingAlgorithm()).isEqualTo(UPDATED_HASHING_ALGORITHM);
        assertThat(testCertificate.getPaddingAlgorithm()).isEqualTo(UPDATED_PADDING_ALGORITHM);
        assertThat(testCertificate.getSigningAlgorithm()).isEqualTo(UPDATED_SIGNING_ALGORITHM);
        assertThat(testCertificate.getCreationExecutionId()).isEqualTo(UPDATED_CREATION_EXECUTION_ID);
        assertThat(testCertificate.getContentAddedAt()).isEqualTo(UPDATED_CONTENT_ADDED_AT);
        assertThat(testCertificate.getRevokedSince()).isEqualTo(UPDATED_REVOKED_SINCE);
        assertThat(testCertificate.getRevocationReason()).isEqualTo(UPDATED_REVOCATION_REASON);
        assertThat(testCertificate.isRevoked()).isEqualTo(UPDATED_REVOKED);
        assertThat(testCertificate.getRevocationExecutionId()).isEqualTo(UPDATED_REVOCATION_EXECUTION_ID);
        assertThat(testCertificate.isEndEntity()).isEqualTo(UPDATED_END_ENTITY);
        assertThat(testCertificate.isSelfsigned()).isEqualTo(UPDATED_SELFSIGNED);
        assertThat(testCertificate.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void updateNonExistingCertificate() throws Exception {
        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // Create the Certificate

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCertificate() throws Exception {
        // Initialize the database
        certificateService.save(certificate);

        int databaseSizeBeforeDelete = certificateRepository.findAll().size();

        // Delete the certificate
        restCertificateMockMvc.perform(delete("/api/certificates/{id}", certificate.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
