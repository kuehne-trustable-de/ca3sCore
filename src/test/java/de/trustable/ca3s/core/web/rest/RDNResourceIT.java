package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.RDN;
import de.trustable.ca3s.core.repository.RDNRepository;
import de.trustable.ca3s.core.service.RDNService;

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
 * Integration tests for the {@link RDNResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class RDNResourceIT {

    @Autowired
    private RDNRepository rDNRepository;

    @Autowired
    private RDNService rDNService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRDNMockMvc;

    private RDN rDN;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RDN createEntity(EntityManager em) {
        RDN rDN = new RDN();
        return rDN;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RDN createUpdatedEntity(EntityManager em) {
        RDN rDN = new RDN();
        return rDN;
    }

    @BeforeEach
    public void initTest() {
        rDN = createEntity(em);
    }

    @Test
    @Transactional
    public void createRDN() throws Exception {
        int databaseSizeBeforeCreate = rDNRepository.findAll().size();

        // Create the RDN
        restRDNMockMvc.perform(post("/api/rdns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(rDN)))
            .andExpect(status().isCreated());

        // Validate the RDN in the database
        List<RDN> rDNList = rDNRepository.findAll();
        assertThat(rDNList).hasSize(databaseSizeBeforeCreate + 1);
        RDN testRDN = rDNList.get(rDNList.size() - 1);
    }

    @Test
    @Transactional
    public void createRDNWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = rDNRepository.findAll().size();

        // Create the RDN with an existing ID
        rDN.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRDNMockMvc.perform(post("/api/rdns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(rDN)))
            .andExpect(status().isBadRequest());

        // Validate the RDN in the database
        List<RDN> rDNList = rDNRepository.findAll();
        assertThat(rDNList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllRDNS() throws Exception {
        // Initialize the database
        rDNRepository.saveAndFlush(rDN);

        // Get all the rDNList
        restRDNMockMvc.perform(get("/api/rdns?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rDN.getId().intValue())));
    }
    
    @Test
    @Transactional
    public void getRDN() throws Exception {
        // Initialize the database
        rDNRepository.saveAndFlush(rDN);

        // Get the rDN
        restRDNMockMvc.perform(get("/api/rdns/{id}", rDN.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(rDN.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingRDN() throws Exception {
        // Get the rDN
        restRDNMockMvc.perform(get("/api/rdns/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRDN() throws Exception {
        // Initialize the database
        rDNService.save(rDN);

        int databaseSizeBeforeUpdate = rDNRepository.findAll().size();

        // Update the rDN
        RDN updatedRDN = rDNRepository.findById(rDN.getId()).get();
        // Disconnect from session so that the updates on updatedRDN are not directly saved in db
        em.detach(updatedRDN);

        restRDNMockMvc.perform(put("/api/rdns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedRDN)))
            .andExpect(status().isOk());

        // Validate the RDN in the database
        List<RDN> rDNList = rDNRepository.findAll();
        assertThat(rDNList).hasSize(databaseSizeBeforeUpdate);
        RDN testRDN = rDNList.get(rDNList.size() - 1);
    }

    @Test
    @Transactional
    public void updateNonExistingRDN() throws Exception {
        int databaseSizeBeforeUpdate = rDNRepository.findAll().size();

        // Create the RDN

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRDNMockMvc.perform(put("/api/rdns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(rDN)))
            .andExpect(status().isBadRequest());

        // Validate the RDN in the database
        List<RDN> rDNList = rDNRepository.findAll();
        assertThat(rDNList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRDN() throws Exception {
        // Initialize the database
        rDNService.save(rDN);

        int databaseSizeBeforeDelete = rDNRepository.findAll().size();

        // Delete the rDN
        restRDNMockMvc.perform(delete("/api/rdns/{id}", rDN.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RDN> rDNList = rDNRepository.findAll();
        assertThat(rDNList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
