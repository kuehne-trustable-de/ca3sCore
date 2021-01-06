package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeNonce;
import de.trustable.ca3s.core.repository.AcmeNonceRepository;
import de.trustable.ca3s.core.service.AcmeNonceService;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AcmeNonceResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class AcmeNonceResourceIT {

    private static final String DEFAULT_NONCE_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_NONCE_VALUE = "BBBBBBBBBB";

    private static final Instant DEFAULT_EXPIRES_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private AcmeNonceRepository acmeNonceRepository;

    @Autowired
    private AcmeNonceService acmeNonceService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAcmeNonceMockMvc;

    private AcmeNonce acmeNonce;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeNonce createEntity(EntityManager em) {
        AcmeNonce acmeNonce = new AcmeNonce()
            .nonceValue(DEFAULT_NONCE_VALUE)
            .expiresAt(DEFAULT_EXPIRES_AT);
        return acmeNonce;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeNonce createUpdatedEntity(EntityManager em) {
        AcmeNonce acmeNonce = new AcmeNonce()
            .nonceValue(UPDATED_NONCE_VALUE)
            .expiresAt(UPDATED_EXPIRES_AT);
        return acmeNonce;
    }

    @BeforeEach
    public void initTest() {
        acmeNonce = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeNonce() throws Exception {
        int databaseSizeBeforeCreate = acmeNonceRepository.findAll().size();

        // Create the AcmeNonce
        restAcmeNonceMockMvc.perform(post("/api/acme-nonces")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeNonce)))
            .andExpect(status().isCreated());

        // Validate the AcmeNonce in the database
        List<AcmeNonce> acmeNonceList = acmeNonceRepository.findAll();
        assertThat(acmeNonceList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeNonce testAcmeNonce = acmeNonceList.get(acmeNonceList.size() - 1);
        assertThat(testAcmeNonce.getNonceValue()).isEqualTo(DEFAULT_NONCE_VALUE);
        assertThat(testAcmeNonce.getExpiresAt()).isEqualTo(DEFAULT_EXPIRES_AT);
    }

    @Test
    @Transactional
    public void createAcmeNonceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeNonceRepository.findAll().size();

        // Create the AcmeNonce with an existing ID
        acmeNonce.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeNonceMockMvc.perform(post("/api/acme-nonces")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeNonce)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeNonce in the database
        List<AcmeNonce> acmeNonceList = acmeNonceRepository.findAll();
        assertThat(acmeNonceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllAcmeNonces() throws Exception {
        // Initialize the database
        acmeNonceRepository.saveAndFlush(acmeNonce);

        // Get all the acmeNonceList
        restAcmeNonceMockMvc.perform(get("/api/acme-nonces?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeNonce.getId().intValue())))
            .andExpect(jsonPath("$.[*].nonceValue").value(hasItem(DEFAULT_NONCE_VALUE)))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())));
    }

    @Test
    @Transactional
    public void getAcmeNonce() throws Exception {
        // Initialize the database
        acmeNonceRepository.saveAndFlush(acmeNonce);

        // Get the acmeNonce
        restAcmeNonceMockMvc.perform(get("/api/acme-nonces/{id}", acmeNonce.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(acmeNonce.getId().intValue()))
            .andExpect(jsonPath("$.nonceValue").value(DEFAULT_NONCE_VALUE))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeNonce() throws Exception {
        // Get the acmeNonce
        restAcmeNonceMockMvc.perform(get("/api/acme-nonces/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeNonce() throws Exception {
        // Initialize the database
        acmeNonceService.save(acmeNonce);

        int databaseSizeBeforeUpdate = acmeNonceRepository.findAll().size();

        // Update the acmeNonce
        AcmeNonce updatedAcmeNonce = acmeNonceRepository.findById(acmeNonce.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeNonce are not directly saved in db
        em.detach(updatedAcmeNonce);
        updatedAcmeNonce
            .nonceValue(UPDATED_NONCE_VALUE)
            .expiresAt(UPDATED_EXPIRES_AT);

        restAcmeNonceMockMvc.perform(put("/api/acme-nonces")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeNonce)))
            .andExpect(status().isOk());

        // Validate the AcmeNonce in the database
        List<AcmeNonce> acmeNonceList = acmeNonceRepository.findAll();
        assertThat(acmeNonceList).hasSize(databaseSizeBeforeUpdate);
        AcmeNonce testAcmeNonce = acmeNonceList.get(acmeNonceList.size() - 1);
        assertThat(testAcmeNonce.getNonceValue()).isEqualTo(UPDATED_NONCE_VALUE);
        assertThat(testAcmeNonce.getExpiresAt()).isEqualTo(UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeNonce() throws Exception {
        int databaseSizeBeforeUpdate = acmeNonceRepository.findAll().size();

        // Create the AcmeNonce

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeNonceMockMvc.perform(put("/api/acme-nonces")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeNonce)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeNonce in the database
        List<AcmeNonce> acmeNonceList = acmeNonceRepository.findAll();
        assertThat(acmeNonceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeNonce() throws Exception {
        // Initialize the database
        acmeNonceService.save(acmeNonce);

        int databaseSizeBeforeDelete = acmeNonceRepository.findAll().size();

        // Delete the acmeNonce
        restAcmeNonceMockMvc.perform(delete("/api/acme-nonces/{id}", acmeNonce.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeNonce> acmeNonceList = acmeNonceRepository.findAll();
        assertThat(acmeNonceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
