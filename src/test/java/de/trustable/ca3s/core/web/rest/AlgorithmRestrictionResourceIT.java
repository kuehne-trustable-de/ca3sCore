package de.trustable.ca3s.core.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.IntegrationTest;
import de.trustable.ca3s.core.domain.AlgorithmRestriction;
import de.trustable.ca3s.core.domain.enumeration.AlgorithmType;
import de.trustable.ca3s.core.repository.AlgorithmRestrictionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlgorithmRestrictionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlgorithmRestrictionResourceIT {

    private static final AlgorithmType DEFAULT_TYPE = AlgorithmType.SIGNING;
    private static final AlgorithmType UPDATED_TYPE = AlgorithmType.PADDING;

    private static final Instant DEFAULT_NOT_AFTER = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_NOT_AFTER = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_IDENTIFIER = "AAAAAAAAAA";
    private static final String UPDATED_IDENTIFIER = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACCEPTABLE = false;
    private static final Boolean UPDATED_ACCEPTABLE = true;

    private static final String ENTITY_API_URL = "/api/algorithm-restrictions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlgorithmRestrictionRepository algorithmRestrictionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlgorithmRestrictionMockMvc;

    private AlgorithmRestriction algorithmRestriction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlgorithmRestriction createEntity(EntityManager em) {
        AlgorithmRestriction algorithmRestriction = new AlgorithmRestriction()
            .type(DEFAULT_TYPE)
            .notAfter(DEFAULT_NOT_AFTER)
            .identifier(DEFAULT_IDENTIFIER)
            .name(DEFAULT_NAME)
            .acceptable(DEFAULT_ACCEPTABLE);
        return algorithmRestriction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlgorithmRestriction createUpdatedEntity(EntityManager em) {
        AlgorithmRestriction algorithmRestriction = new AlgorithmRestriction()
            .type(UPDATED_TYPE)
            .notAfter(UPDATED_NOT_AFTER)
            .identifier(UPDATED_IDENTIFIER)
            .name(UPDATED_NAME)
            .acceptable(UPDATED_ACCEPTABLE);
        return algorithmRestriction;
    }

    @BeforeEach
    public void initTest() {
        algorithmRestriction = createEntity(em);
    }

    @Test
    @Transactional
    void createAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeCreate = algorithmRestrictionRepository.findAll().size();
        // Create the AlgorithmRestriction
        restAlgorithmRestrictionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isCreated());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeCreate + 1);
        AlgorithmRestriction testAlgorithmRestriction = algorithmRestrictionList.get(algorithmRestrictionList.size() - 1);
        assertThat(testAlgorithmRestriction.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAlgorithmRestriction.getNotAfter()).isEqualTo(DEFAULT_NOT_AFTER);
        assertThat(testAlgorithmRestriction.getIdentifier()).isEqualTo(DEFAULT_IDENTIFIER);
        assertThat(testAlgorithmRestriction.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlgorithmRestriction.getAcceptable()).isEqualTo(DEFAULT_ACCEPTABLE);
    }

    @Test
    @Transactional
    void createAlgorithmRestrictionWithExistingId() throws Exception {
        // Create the AlgorithmRestriction with an existing ID
        algorithmRestriction.setId(1L);

        int databaseSizeBeforeCreate = algorithmRestrictionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlgorithmRestrictionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = algorithmRestrictionRepository.findAll().size();
        // set the field null
        algorithmRestriction.setType(null);

        // Create the AlgorithmRestriction, which fails.

        restAlgorithmRestrictionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAcceptableIsRequired() throws Exception {
        int databaseSizeBeforeTest = algorithmRestrictionRepository.findAll().size();
        // set the field null
        algorithmRestriction.setAcceptable(null);

        // Create the AlgorithmRestriction, which fails.

        restAlgorithmRestrictionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlgorithmRestrictions() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        // Get all the algorithmRestrictionList
        restAlgorithmRestrictionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(algorithmRestriction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].notAfter").value(hasItem(DEFAULT_NOT_AFTER.toString())))
            .andExpect(jsonPath("$.[*].identifier").value(hasItem(DEFAULT_IDENTIFIER)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].acceptable").value(hasItem(DEFAULT_ACCEPTABLE.booleanValue())));
    }

    @Test
    @Transactional
    void getAlgorithmRestriction() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        // Get the algorithmRestriction
        restAlgorithmRestrictionMockMvc
            .perform(get(ENTITY_API_URL_ID, algorithmRestriction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(algorithmRestriction.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.notAfter").value(DEFAULT_NOT_AFTER.toString()))
            .andExpect(jsonPath("$.identifier").value(DEFAULT_IDENTIFIER))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.acceptable").value(DEFAULT_ACCEPTABLE.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingAlgorithmRestriction() throws Exception {
        // Get the algorithmRestriction
        restAlgorithmRestrictionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAlgorithmRestriction() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();

        // Update the algorithmRestriction
        AlgorithmRestriction updatedAlgorithmRestriction = algorithmRestrictionRepository.findById(algorithmRestriction.getId()).get();
        // Disconnect from session so that the updates on updatedAlgorithmRestriction are not directly saved in db
        em.detach(updatedAlgorithmRestriction);
        updatedAlgorithmRestriction
            .type(UPDATED_TYPE)
            .notAfter(UPDATED_NOT_AFTER)
            .identifier(UPDATED_IDENTIFIER)
            .name(UPDATED_NAME)
            .acceptable(UPDATED_ACCEPTABLE);

        restAlgorithmRestrictionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAlgorithmRestriction.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAlgorithmRestriction))
            )
            .andExpect(status().isOk());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
        AlgorithmRestriction testAlgorithmRestriction = algorithmRestrictionList.get(algorithmRestrictionList.size() - 1);
        assertThat(testAlgorithmRestriction.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAlgorithmRestriction.getNotAfter()).isEqualTo(UPDATED_NOT_AFTER);
        assertThat(testAlgorithmRestriction.getIdentifier()).isEqualTo(UPDATED_IDENTIFIER);
        assertThat(testAlgorithmRestriction.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlgorithmRestriction.getAcceptable()).isEqualTo(UPDATED_ACCEPTABLE);
    }

    @Test
    @Transactional
    void putNonExistingAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, algorithmRestriction.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlgorithmRestrictionWithPatch() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();

        // Update the algorithmRestriction using partial update
        AlgorithmRestriction partialUpdatedAlgorithmRestriction = new AlgorithmRestriction();
        partialUpdatedAlgorithmRestriction.setId(algorithmRestriction.getId());

        partialUpdatedAlgorithmRestriction.notAfter(UPDATED_NOT_AFTER).name(UPDATED_NAME).acceptable(UPDATED_ACCEPTABLE);

        restAlgorithmRestrictionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlgorithmRestriction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlgorithmRestriction))
            )
            .andExpect(status().isOk());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
        AlgorithmRestriction testAlgorithmRestriction = algorithmRestrictionList.get(algorithmRestrictionList.size() - 1);
        assertThat(testAlgorithmRestriction.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAlgorithmRestriction.getNotAfter()).isEqualTo(UPDATED_NOT_AFTER);
        assertThat(testAlgorithmRestriction.getIdentifier()).isEqualTo(DEFAULT_IDENTIFIER);
        assertThat(testAlgorithmRestriction.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlgorithmRestriction.getAcceptable()).isEqualTo(UPDATED_ACCEPTABLE);
    }

    @Test
    @Transactional
    void fullUpdateAlgorithmRestrictionWithPatch() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();

        // Update the algorithmRestriction using partial update
        AlgorithmRestriction partialUpdatedAlgorithmRestriction = new AlgorithmRestriction();
        partialUpdatedAlgorithmRestriction.setId(algorithmRestriction.getId());

        partialUpdatedAlgorithmRestriction
            .type(UPDATED_TYPE)
            .notAfter(UPDATED_NOT_AFTER)
            .identifier(UPDATED_IDENTIFIER)
            .name(UPDATED_NAME)
            .acceptable(UPDATED_ACCEPTABLE);

        restAlgorithmRestrictionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlgorithmRestriction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlgorithmRestriction))
            )
            .andExpect(status().isOk());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
        AlgorithmRestriction testAlgorithmRestriction = algorithmRestrictionList.get(algorithmRestrictionList.size() - 1);
        assertThat(testAlgorithmRestriction.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAlgorithmRestriction.getNotAfter()).isEqualTo(UPDATED_NOT_AFTER);
        assertThat(testAlgorithmRestriction.getIdentifier()).isEqualTo(UPDATED_IDENTIFIER);
        assertThat(testAlgorithmRestriction.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlgorithmRestriction.getAcceptable()).isEqualTo(UPDATED_ACCEPTABLE);
    }

    @Test
    @Transactional
    void patchNonExistingAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, algorithmRestriction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlgorithmRestriction() throws Exception {
        int databaseSizeBeforeUpdate = algorithmRestrictionRepository.findAll().size();
        algorithmRestriction.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlgorithmRestrictionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(algorithmRestriction))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlgorithmRestriction in the database
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlgorithmRestriction() throws Exception {
        // Initialize the database
        algorithmRestrictionRepository.saveAndFlush(algorithmRestriction);

        int databaseSizeBeforeDelete = algorithmRestrictionRepository.findAll().size();

        // Delete the algorithmRestriction
        restAlgorithmRestrictionMockMvc
            .perform(delete(ENTITY_API_URL_ID, algorithmRestriction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AlgorithmRestriction> algorithmRestrictionList = algorithmRestrictionRepository.findAll();
        assertThat(algorithmRestrictionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
