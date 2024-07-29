package de.trustable.ca3s.core.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.IntegrationTest;
import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.repository.CRLExpirationNotificationRepository;
import java.time.Duration;
import java.time.Instant;
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
 * Integration tests for the {@link CRLExpirationNotificationViewResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(roles = { "ADMIN" })
class CRLExpirationNotificationResourceIT {

    private static final String DEFAULT_CRL_URL = "AAAAAAAAAA";
    private static final String UPDATED_CRL_URL = "BBBBBBBBBB";

    private static final Duration DEFAULT_NOTIFY_BEFORE = Duration.ofHours(6);
    private static final Duration UPDATED_NOTIFY_BEFORE = Duration.ofHours(12);

    private static final Instant DEFAULT_NOTIFY_UNTIL = Instant.now();

    private static final String ENTITY_API_URL = "/api/crl-expiration-notifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CRLExpirationNotificationRepository cRLExpirationNotificationRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCRLExpirationNotificationMockMvc;

    private CRLExpirationNotification cRLExpirationNotification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CRLExpirationNotification createEntity(EntityManager em) {
        CRLExpirationNotification cRLExpirationNotification = new CRLExpirationNotification()
            .crlUrl(DEFAULT_CRL_URL)
            .notifyBefore(DEFAULT_NOTIFY_BEFORE)
            .notifyUntil(DEFAULT_NOTIFY_UNTIL);
        return cRLExpirationNotification;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CRLExpirationNotification createUpdatedEntity(EntityManager em) {
        CRLExpirationNotification cRLExpirationNotification = new CRLExpirationNotification()
            .crlUrl(UPDATED_CRL_URL)
            .notifyBefore(UPDATED_NOTIFY_BEFORE);
        return cRLExpirationNotification;
    }

    @BeforeEach
    public void initTest() {
        cRLExpirationNotification = createEntity(em);
    }

    @Test
    @Transactional
    void createCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeCreate = cRLExpirationNotificationRepository.findAll().size();
        // Create the CRLExpirationNotification
        restCRLExpirationNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isCreated());
/*
        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeCreate + 1);
        CRLExpirationNotification testCRLExpirationNotification = cRLExpirationNotificationList.get(
            cRLExpirationNotificationList.size() - 1
        );
        assertThat(testCRLExpirationNotification.getCrlUrl()).isEqualTo(DEFAULT_CRL_URL);
        assertThat(testCRLExpirationNotification.getNotifyBefore()).isEqualTo(DEFAULT_NOTIFY_BEFORE);

 */
    }

    @Test
    @Transactional
    void createCRLExpirationNotificationWithExistingId() throws Exception {
        // Create the CRLExpirationNotification with an existing ID
        cRLExpirationNotification.setId(1L);

        int databaseSizeBeforeCreate = cRLExpirationNotificationRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCRLExpirationNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isBadRequest());


        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNotifyBeforeIsRequired() throws Exception {
        int databaseSizeBeforeTest = cRLExpirationNotificationRepository.findAll().size();
        // set the field null
        cRLExpirationNotification.setNotifyBefore(null);

        // Create the CRLExpirationNotification, which fails.

        restCRLExpirationNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isBadRequest());

        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCRLExpirationNotifications() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        // Get all the cRLExpirationNotificationList
        restCRLExpirationNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk());

/*
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cRLExpirationNotification.getId().intValue())))
            .andExpect(jsonPath("$.[*].crlUrl").value(hasItem(DEFAULT_CRL_URL)))
            .andExpect(jsonPath("$.[*].notifyBefore").value(hasItem(DEFAULT_NOTIFY_BEFORE.toString())));

 */
    }

    @Test
    @Transactional
    void getCRLExpirationNotification() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        // Get the cRLExpirationNotification
        restCRLExpirationNotificationMockMvc
            .perform(get(ENTITY_API_URL_ID, cRLExpirationNotification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cRLExpirationNotification.getId().intValue()))
            .andExpect(jsonPath("$.crlUrl").value(DEFAULT_CRL_URL))
            .andExpect(jsonPath("$.notifyBefore").value(DEFAULT_NOTIFY_BEFORE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingCRLExpirationNotification() throws Exception {
        // Get the cRLExpirationNotification
        restCRLExpirationNotificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCRLExpirationNotification() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();

        // Update the cRLExpirationNotification
        CRLExpirationNotification updatedCRLExpirationNotification = cRLExpirationNotificationRepository
            .findById(cRLExpirationNotification.getId())
            .get();
        // Disconnect from session so that the updates on updatedCRLExpirationNotification are not directly saved in db
        em.detach(updatedCRLExpirationNotification);
        updatedCRLExpirationNotification.crlUrl(UPDATED_CRL_URL).notifyBefore(UPDATED_NOTIFY_BEFORE);

        restCRLExpirationNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCRLExpirationNotification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedCRLExpirationNotification))
            )
            .andExpect(status().isOk());

        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeUpdate);
        CRLExpirationNotification testCRLExpirationNotification = cRLExpirationNotificationList.get(
            cRLExpirationNotificationList.size() - 1
        );
        assertThat(testCRLExpirationNotification.getCrlUrl()).isEqualTo(UPDATED_CRL_URL);
        assertThat(testCRLExpirationNotification.getNotifyBefore()).isEqualTo(UPDATED_NOTIFY_BEFORE);
    }

    @Test
    @Transactional
    void putNonExistingCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();
        cRLExpirationNotification.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCRLExpirationNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cRLExpirationNotification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isBadRequest());

        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();
        cRLExpirationNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCRLExpirationNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isBadRequest());

        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();
        cRLExpirationNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCRLExpirationNotificationMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCRLExpirationNotificationWithPatch() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();

        // Update the cRLExpirationNotification using partial update
        CRLExpirationNotification partialUpdatedCRLExpirationNotification = new CRLExpirationNotification();
        partialUpdatedCRLExpirationNotification.setId(cRLExpirationNotification.getId());

        partialUpdatedCRLExpirationNotification.crlUrl(UPDATED_CRL_URL);

        restCRLExpirationNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCRLExpirationNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCRLExpirationNotification))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void fullUpdateCRLExpirationNotificationWithPatch() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();

        // Update the cRLExpirationNotification using partial update
        CRLExpirationNotification partialUpdatedCRLExpirationNotification = new CRLExpirationNotification();
        partialUpdatedCRLExpirationNotification.setId(cRLExpirationNotification.getId());

        partialUpdatedCRLExpirationNotification.crlUrl(UPDATED_CRL_URL).notifyBefore(UPDATED_NOTIFY_BEFORE);

        restCRLExpirationNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCRLExpirationNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCRLExpirationNotification))
            )
            .andExpect(status().isMethodNotAllowed());

    }


    @Test
    @Transactional
    void patchWithIdMismatchCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();
        cRLExpirationNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCRLExpirationNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCRLExpirationNotification() throws Exception {
        int databaseSizeBeforeUpdate = cRLExpirationNotificationRepository.findAll().size();
        cRLExpirationNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCRLExpirationNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cRLExpirationNotification))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CRLExpirationNotification in the database
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCRLExpirationNotification() throws Exception {
        // Initialize the database
        cRLExpirationNotificationRepository.saveAndFlush(cRLExpirationNotification);

        int databaseSizeBeforeDelete = cRLExpirationNotificationRepository.findAll().size();

        // Delete the cRLExpirationNotification
        restCRLExpirationNotificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, cRLExpirationNotification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CRLExpirationNotification> cRLExpirationNotificationList = cRLExpirationNotificationRepository.findAll();
        assertThat(cRLExpirationNotificationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
