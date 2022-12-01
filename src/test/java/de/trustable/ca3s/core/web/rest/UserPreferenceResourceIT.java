package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.service.UserPreferenceService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link UserPreferenceResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
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
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restUserPreferenceMockMvc;

    private UserPreference userPreference;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final UserPreferenceResource userPreferenceResource = new UserPreferenceResource(userPreferenceService);
        this.restUserPreferenceMockMvc = MockMvcBuilders.standaloneSetup(userPreferenceResource)
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userPreference)))
            .andExpect(status().isBadRequest());

        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getUserPreference() throws Exception {
        // Initialize the database
        userPreferenceRepository.saveAndFlush(userPreference);

        // Get the userPreference
        restUserPreferenceMockMvc.perform(get("/api/user-preferences/{id}", userPreference.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8_VALUE))
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
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
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserPreference> userPreferenceList = userPreferenceRepository.findAll();
        assertThat(userPreferenceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
