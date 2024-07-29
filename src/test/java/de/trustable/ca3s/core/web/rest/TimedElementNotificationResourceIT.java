package de.trustable.ca3s.core.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.IntegrationTest;
import de.trustable.ca3s.core.domain.TimedElementNotification;
import de.trustable.ca3s.core.domain.enumeration.TimedElementNotificationType;
import de.trustable.ca3s.core.repository.TimedElementNotificationRepository;
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
 * Integration tests for the {@link TimedElementNotificationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(roles = { "ADMIN" })
class TimedElementNotificationResourceIT {

    private static final TimedElementNotificationType DEFAULT_TYPE = TimedElementNotificationType.ON_EXPIRY;
    private static final TimedElementNotificationType UPDATED_TYPE = TimedElementNotificationType.ON_EXPIRY_ACTIVE;

    private static final Instant DEFAULT_NOTIFY_ON = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_NOTIFY_ON = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CUSTOM_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOM_MESSAGE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/timed-element-notifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TimedElementNotificationRepository timedElementNotificationRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTimedElementNotificationMockMvc;

    private TimedElementNotification timedElementNotification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimedElementNotification createEntity(EntityManager em) {
        TimedElementNotification timedElementNotification = new TimedElementNotification()
            .type(DEFAULT_TYPE)
            .notifyOn(DEFAULT_NOTIFY_ON)
            .customMessage(DEFAULT_CUSTOM_MESSAGE);
        return timedElementNotification;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimedElementNotification createUpdatedEntity(EntityManager em) {
        TimedElementNotification timedElementNotification = new TimedElementNotification()
            .type(UPDATED_TYPE)
            .notifyOn(UPDATED_NOTIFY_ON)
            .customMessage(UPDATED_CUSTOM_MESSAGE);
        return timedElementNotification;
    }

    @BeforeEach
    public void initTest() {
        timedElementNotification = createEntity(em);
    }

    @Test
    @Transactional
    void createTimedElementNotification() throws Exception {
        int databaseSizeBeforeCreate = timedElementNotificationRepository.findAll().size();
        // Create the TimedElementNotification
        restTimedElementNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isCreated());

/*
        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeCreate + 1);
        TimedElementNotification testTimedElementNotification = timedElementNotificationList.get(timedElementNotificationList.size() - 1);
        assertThat(testTimedElementNotification.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testTimedElementNotification.getNotifyOn()).isEqualTo(DEFAULT_NOTIFY_ON);
        assertThat(testTimedElementNotification.getCustomMessage()).isEqualTo(DEFAULT_CUSTOM_MESSAGE);

 */
    }

    @Test
    @Transactional
    void createTimedElementNotificationWithExistingId() throws Exception {
        // Create the TimedElementNotification with an existing ID
        timedElementNotification.setId(1L);

        int databaseSizeBeforeCreate = timedElementNotificationRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTimedElementNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = timedElementNotificationRepository.findAll().size();
        // set the field null
        timedElementNotification.setType(null);

        // Create the TimedElementNotification, which fails.

        restTimedElementNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isBadRequest());

        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNotifyOnIsRequired() throws Exception {
        int databaseSizeBeforeTest = timedElementNotificationRepository.findAll().size();
        // set the field null
        timedElementNotification.setNotifyOn(null);

        // Create the TimedElementNotification, which fails.

        restTimedElementNotificationMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isBadRequest());

        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTimedElementNotifications() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        // Get all the timedElementNotificationList
        restTimedElementNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk());

/*
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(timedElementNotification.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].notifyOn").value(hasItem(DEFAULT_NOTIFY_ON.toString())))
            .andExpect(jsonPath("$.[*].customMessage").value(hasItem(DEFAULT_CUSTOM_MESSAGE)));

 */
    }

    @Test
    @Transactional
    void getTimedElementNotification() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        // Get the timedElementNotification
        restTimedElementNotificationMockMvc
            .perform(get(ENTITY_API_URL_ID, timedElementNotification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(timedElementNotification.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.notifyOn").value(DEFAULT_NOTIFY_ON.toString()))
            .andExpect(jsonPath("$.customMessage").value(DEFAULT_CUSTOM_MESSAGE));
    }

    @Test
    @Transactional
    void getNonExistingTimedElementNotification() throws Exception {
        // Get the timedElementNotification
        restTimedElementNotificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTimedElementNotification() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();

        // Update the timedElementNotification
        TimedElementNotification updatedTimedElementNotification = timedElementNotificationRepository
            .findById(timedElementNotification.getId())
            .get();
        // Disconnect from session so that the updates on updatedTimedElementNotification are not directly saved in db
        em.detach(updatedTimedElementNotification);
        updatedTimedElementNotification.type(UPDATED_TYPE).notifyOn(UPDATED_NOTIFY_ON).customMessage(UPDATED_CUSTOM_MESSAGE);

        restTimedElementNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTimedElementNotification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTimedElementNotification))
            )
            .andExpect(status().isOk());

        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeUpdate);
        TimedElementNotification testTimedElementNotification = timedElementNotificationList.get(timedElementNotificationList.size() - 1);
        assertThat(testTimedElementNotification.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testTimedElementNotification.getNotifyOn()).isEqualTo(UPDATED_NOTIFY_ON);
        assertThat(testTimedElementNotification.getCustomMessage()).isEqualTo(UPDATED_CUSTOM_MESSAGE);
    }

    @Test
    @Transactional
    void putNonExistingTimedElementNotification() throws Exception {
        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();
        timedElementNotification.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTimedElementNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, timedElementNotification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTimedElementNotification() throws Exception {
        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();
        timedElementNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimedElementNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTimedElementNotification() throws Exception {
        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();
        timedElementNotification.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimedElementNotificationMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timedElementNotification))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TimedElementNotification in the database
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTimedElementNotificationWithPatch() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();

        // Update the timedElementNotification using partial update
        TimedElementNotification partialUpdatedTimedElementNotification = new TimedElementNotification();
        partialUpdatedTimedElementNotification.setId(timedElementNotification.getId());

        partialUpdatedTimedElementNotification.type(UPDATED_TYPE).customMessage(UPDATED_CUSTOM_MESSAGE);

        restTimedElementNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimedElementNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTimedElementNotification))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void fullUpdateTimedElementNotificationWithPatch() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        int databaseSizeBeforeUpdate = timedElementNotificationRepository.findAll().size();

        // Update the timedElementNotification using partial update
        TimedElementNotification partialUpdatedTimedElementNotification = new TimedElementNotification();
        partialUpdatedTimedElementNotification.setId(timedElementNotification.getId());

        partialUpdatedTimedElementNotification.type(UPDATED_TYPE).notifyOn(UPDATED_NOTIFY_ON).customMessage(UPDATED_CUSTOM_MESSAGE);

        restTimedElementNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimedElementNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTimedElementNotification))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void deleteTimedElementNotification() throws Exception {
        // Initialize the database
        timedElementNotificationRepository.saveAndFlush(timedElementNotification);

        int databaseSizeBeforeDelete = timedElementNotificationRepository.findAll().size();

        // Delete the timedElementNotification
        restTimedElementNotificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, timedElementNotification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TimedElementNotification> timedElementNotificationList = timedElementNotificationRepository.findAll();
        assertThat(timedElementNotificationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
