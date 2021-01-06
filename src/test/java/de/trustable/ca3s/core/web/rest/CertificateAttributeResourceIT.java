package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CertificateAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class CertificateAttributeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private CertificateAttributeRepository certificateAttributeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCertificateAttributeMockMvc;

    private CertificateAttribute certificateAttribute;

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
            .contentType(MediaType.APPLICATION_JSON)
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
            .contentType(MediaType.APPLICATION_JSON)
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
            .contentType(MediaType.APPLICATION_JSON)
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(certificateAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }

    @Test
    @Transactional
    public void getCertificateAttribute() throws Exception {
        // Initialize the database
        certificateAttributeRepository.saveAndFlush(certificateAttribute);

        // Get the certificateAttribute
        restCertificateAttributeMockMvc.perform(get("/api/certificate-attributes/{id}", certificateAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(certificateAttribute.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
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
            .contentType(MediaType.APPLICATION_JSON)
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
            .contentType(MediaType.APPLICATION_JSON)
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
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CertificateAttribute> certificateAttributeList = certificateAttributeRepository.findAll();
        assertThat(certificateAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
