package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.service.UserPreferenceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link UserPreferenceResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class UserPreferenceResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserPreferenceMockMvc;

    private UserPreference userPreference;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserPreference createEntity(EntityManager em) {
        UserPreference userPreference = new UserPreference()
            .userId(DEFAULT_USER_ID)
            .name(DEFAULT_NAME)
            .content(DEFAULT_CONTENT);
        return userPreference;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserPreference createUpdatedEntity(EntityManager em) {
        UserPreference userPreference = new UserPreference()
            .userId(UPDATED_USER_ID)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT);
        return userPreference;
    }

    @BeforeEach
    public void initTest() {
        userPreference = createEntity(em);
    }

    @Test
    @Transactional
    public void createUserPreference() throws Exception {
        int databaseSizeBeforeCreate = userPreferenceRepository.findAll().size();

        // Create the UserPreference
        restUserPreferenceMockMvc.perform(post("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isCreated());

        // Validate the UserPreference in the database
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeCreate + 1);
        UserPreference testUserPreference = userPreferenceList.get(userPreferenceList.size() - 1);
        assertThat(testUserPreference.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testUserPreference.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUserPreference.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    public void createUserPreferenceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userPreferenceRepository.findAll().size();

        // Create the UserPreference with an existing ID
        userPreference.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserPreferenceMockMvc.perform(post("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isBadRequest());

        // Validate the UserPreference in the database
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkUserIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = userPreferenceRepository.findAll().size();
        // set the field null
        userPreference.setUserId(null);

        // Create the UserPreference, which fails.

        restUserPreferenceMockMvc.perform(post("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isBadRequest());

        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = userPreferenceRepository.findAll().size();
        // set the field null
        userPreference.setName(null);

        // Create the UserPreference, which fails.

        restUserPreferenceMockMvc.perform(post("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isBadRequest());

        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllUserPreferences() throws Exception {
        // Initialize the database
        userPreferenceRepository.saveAndFlush(userPreference);

        // Get all the userPreferenceList
        restUserPreferenceMockMvc.perform(get("/api/user-preferences?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userPreference.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())));
    }

    @Test
    @Transactional
    public void getUserPreference() throws Exception {
        // Initialize the database
        userPreferenceRepository.saveAndFlush(userPreference);

        // Get the userPreference
        restUserPreferenceMockMvc.perform(get("/api/user-preferences/{id}", userPreference.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(userPreference.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingUserPreference() throws Exception {
        // Get the userPreference
        restUserPreferenceMockMvc.perform(get("/api/user-preferences/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUserPreference() throws Exception {
        // Initialize the database
        userPreferenceService.save(userPreference);

        int databaseSizeBeforeUpdate = userPreferenceRepository.findAll().size();

        // Update the userPreference
        UserPreference updatedUserPreference = userPreferenceRepository.findById(userPreference.getId()).get();
        // Disconnect from session so that the updates on updatedUserPreference are not directly saved in db
        em.detach(updatedUserPreference);
        updatedUserPreference
            .userId(UPDATED_USER_ID)
            .name(UPDATED_NAME)
            .content(UPDATED_CONTENT);

        restUserPreferenceMockMvc.perform(put("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedUserPreference)))
            .andExpect(status().isOk());

        // Validate the UserPreference in the database
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeUpdate);
        UserPreference testUserPreference = userPreferenceList.get(userPreferenceList.size() - 1);
        assertThat(testUserPreference.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testUserPreference.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUserPreference.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void updateNonExistingUserPreference() throws Exception {
        int databaseSizeBeforeUpdate = userPreferenceRepository.findAll().size();

        // Create the UserPreference

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserPreferenceMockMvc.perform(put("/api/user-preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isBadRequest());

        // Validate the UserPreference in the database
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteUserPreference() throws Exception {
        // Initialize the database
        userPreferenceService.save(userPreference);

        int databaseSizeBeforeDelete = userPreferenceRepository.findAll().size();

        // Delete the userPreference
        restUserPreferenceMockMvc.perform(delete("/api/user-preferences/{id}", userPreference.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
