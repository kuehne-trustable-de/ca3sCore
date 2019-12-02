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
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.web.rest.CertificateAttributeResource;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CertificateAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class CertificateAttributeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private CertificateAttributeRepository certificateAttributeRepository;

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

    private MockMvc restCertificateAttributeMockMvc;

    private CertificateAttribute certificateAttribute;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CertificateAttributeResource certificateAttributeResource = new CertificateAttributeResource(certificateAttributeRepository);
        this.restCertificateAttributeMockMvc = MockMvcBuilders.standaloneSetup(certificateAttributeResource)
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
    public static CertificateAttribute createEntity(EntityManager em) {
        CertificateAttribute certificateAttribute = new CertificateAttribute()
            .name(DEFAULT_NAME)
            .value(DEFAULT_VALUE);
        return certificateAttribute;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CertificateAttribute createUpdatedEntity(EntityManager em) {
        CertificateAttribute certificateAttribute = new CertificateAttribute()
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);
        return certificateAttribute;
    }

    @BeforeEach
    public void initTest() {
        certificateAttribute = createEntity(em);
    }

    @Test
    @Transactional
    public void createCertificateAttribute() throws Exception {
        int databaseSizeBeforeCreate = certificateAttributeRepository.findAll().size();

        // Create the CertificateAttribute
        restCertificateAttributeMockMvc.perform(post("/api/certificate-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificateAttribute)))
            .andExpect(status().isCreated());

        // Validate the CertificateAttribute in the database
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeCreate + 1);
        CertificateAttribute testCertificateAttribute = certificateAttributeList.get(certificateAttributeList.size() - 1);
        assertThat(testCertificateAttribute.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCertificateAttribute.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createCertificateAttributeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = certificateAttributeRepository.findAll().size();

        // Create the CertificateAttribute with an existing ID
        certificateAttribute.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCertificateAttributeMockMvc.perform(post("/api/certificate-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificateAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the CertificateAttribute in the database
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateAttributeRepository.findAll().size();
        // set the field null
        certificateAttribute.setName(null);

        // Create the CertificateAttribute, which fails.

        restCertificateAttributeMockMvc.perform(post("/api/certificate-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificateAttribute)))
            .andExpect(status().isBadRequest());

        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCertificateAttributes() throws Exception {
        // Initialize the database
        certificateAttributeRepository.saveAndFlush(certificateAttribute);

        // Get all the certificateAttributeList
        restCertificateAttributeMockMvc.perform(get("/api/certificate-attributes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(certificateAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));
    }
    
    @Test
    @Transactional
    public void getCertificateAttribute() throws Exception {
        // Initialize the database
        certificateAttributeRepository.saveAndFlush(certificateAttribute);

        // Get the certificateAttribute
        restCertificateAttributeMockMvc.perform(get("/api/certificate-attributes/{id}", certificateAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(certificateAttribute.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCertificateAttribute() throws Exception {
        // Get the certificateAttribute
        restCertificateAttributeMockMvc.perform(get("/api/certificate-attributes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCertificateAttribute() throws Exception {
        // Initialize the database
        certificateAttributeRepository.saveAndFlush(certificateAttribute);

        int databaseSizeBeforeUpdate = certificateAttributeRepository.findAll().size();

        // Update the certificateAttribute
        CertificateAttribute updatedCertificateAttribute = certificateAttributeRepository.findById(certificateAttribute.getId()).get();
        // Disconnect from session so that the updates on updatedCertificateAttribute are not directly saved in db
        em.detach(updatedCertificateAttribute);
        updatedCertificateAttribute
            .name(UPDATED_NAME)
            .value(UPDATED_VALUE);

        restCertificateAttributeMockMvc.perform(put("/api/certificate-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCertificateAttribute)))
            .andExpect(status().isOk());

        // Validate the CertificateAttribute in the database
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeUpdate);
        CertificateAttribute testCertificateAttribute = certificateAttributeList.get(certificateAttributeList.size() - 1);
        assertThat(testCertificateAttribute.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCertificateAttribute.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingCertificateAttribute() throws Exception {
        int databaseSizeBeforeUpdate = certificateAttributeRepository.findAll().size();

        // Create the CertificateAttribute

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCertificateAttributeMockMvc.perform(put("/api/certificate-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificateAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the CertificateAttribute in the database
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCertificateAttribute() throws Exception {
        // Initialize the database
        certificateAttributeRepository.saveAndFlush(certificateAttribute);

        int databaseSizeBeforeDelete = certificateAttributeRepository.findAll().size();

        // Delete the certificateAttribute
        restCertificateAttributeMockMvc.perform(delete("/api/certificate-attributes/{id}", certificateAttribute.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CertificateAttribute.class);
        CertificateAttribute certificateAttribute1 = new CertificateAttribute();
        certificateAttribute1.setId(1L);
        CertificateAttribute certificateAttribute2 = new CertificateAttribute();
        certificateAttribute2.setId(certificateAttribute1.getId());
        assertThat(certificateAttribute1).isEqualTo(certificateAttribute2);
        certificateAttribute2.setId(2L);
        assertThat(certificateAttribute1).isNotEqualTo(certificateAttribute2);
        certificateAttribute1.setId(null);
        assertThat(certificateAttribute1).isNotEqualTo(certificateAttribute2);
    }
}
