package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Authorization;
import de.trustable.ca3s.core.repository.AuthorizationRepository;
import de.trustable.ca3s.core.service.AuthorizationService;

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
 * Integration tests for the {@link AuthorizationResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class AuthorizationResourceIT {

    private static final Long DEFAULT_AUTHORIZATION_ID = 1L;
    private static final Long UPDATED_AUTHORIZATION_ID = 2L;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorizationMockMvc;

    private Authorization authorization;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authorization createEntity(EntityManager em) {
        Authorization authorization = new Authorization()
            .authorizationId(DEFAULT_AUTHORIZATION_ID)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE);
        return authorization;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authorization createUpdatedEntity(EntityManager em) {
        Authorization authorization = new Authorization()
            .authorizationId(UPDATED_AUTHORIZATION_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);
        return authorization;
    }

    @BeforeEach
    public void initTest() {
        authorization = createEntity(em);
    }

    @Test
    @Transactional
    public void createAuthorization() throws Exception {
        int databaseSizeBeforeCreate = authorizationRepository.findAll().size();

        // Create the Authorization
        restAuthorizationMockMvc.perform(post("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isCreated());

        // Validate the Authorization in the database
        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeCreate + 1);
        Authorization testAuthorization = authorizationList.get(authorizationList.size() - 1);
        assertThat(testAuthorization.getAuthorizationId()).isEqualTo(DEFAULT_AUTHORIZATION_ID);
        assertThat(testAuthorization.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAuthorization.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createAuthorizationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = authorizationRepository.findAll().size();

        // Create the Authorization with an existing ID
        authorization.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorizationMockMvc.perform(post("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isBadRequest());

        // Validate the Authorization in the database
        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAuthorizationIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = authorizationRepository.findAll().size();
        // set the field null
        authorization.setAuthorizationId(null);

        // Create the Authorization, which fails.

        restAuthorizationMockMvc.perform(post("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isBadRequest());

        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = authorizationRepository.findAll().size();
        // set the field null
        authorization.setType(null);

        // Create the Authorization, which fails.

        restAuthorizationMockMvc.perform(post("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isBadRequest());

        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = authorizationRepository.findAll().size();
        // set the field null
        authorization.setValue(null);

        // Create the Authorization, which fails.

        restAuthorizationMockMvc.perform(post("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isBadRequest());

        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAuthorizations() throws Exception {
        // Initialize the database
        authorizationRepository.saveAndFlush(authorization);

        // Get all the authorizationList
        restAuthorizationMockMvc.perform(get("/api/authorizations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(authorization.getId().intValue())))
            .andExpect(jsonPath("$.[*].authorizationId").value(hasItem(DEFAULT_AUTHORIZATION_ID.intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)));
    }
    
    @Test
    @Transactional
    public void getAuthorization() throws Exception {
        // Initialize the database
        authorizationRepository.saveAndFlush(authorization);

        // Get the authorization
        restAuthorizationMockMvc.perform(get("/api/authorizations/{id}", authorization.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(authorization.getId().intValue()))
            .andExpect(jsonPath("$.authorizationId").value(DEFAULT_AUTHORIZATION_ID.intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingAuthorization() throws Exception {
        // Get the authorization
        restAuthorizationMockMvc.perform(get("/api/authorizations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAuthorization() throws Exception {
        // Initialize the database
        authorizationService.save(authorization);

        int databaseSizeBeforeUpdate = authorizationRepository.findAll().size();

        // Update the authorization
        Authorization updatedAuthorization = authorizationRepository.findById(authorization.getId()).get();
        // Disconnect from session so that the updates on updatedAuthorization are not directly saved in db
        em.detach(updatedAuthorization);
        updatedAuthorization
            .authorizationId(UPDATED_AUTHORIZATION_ID)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);

        restAuthorizationMockMvc.perform(put("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedAuthorization)))
            .andExpect(status().isOk());

        // Validate the Authorization in the database
        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeUpdate);
        Authorization testAuthorization = authorizationList.get(authorizationList.size() - 1);
        assertThat(testAuthorization.getAuthorizationId()).isEqualTo(UPDATED_AUTHORIZATION_ID);
        assertThat(testAuthorization.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAuthorization.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingAuthorization() throws Exception {
        int databaseSizeBeforeUpdate = authorizationRepository.findAll().size();

        // Create the Authorization

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorizationMockMvc.perform(put("/api/authorizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(authorization)))
            .andExpect(status().isBadRequest());

        // Validate the Authorization in the database
        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAuthorization() throws Exception {
        // Initialize the database
        authorizationService.save(authorization);

        int databaseSizeBeforeDelete = authorizationRepository.findAll().size();

        // Delete the authorization
        restAuthorizationMockMvc.perform(delete("/api/authorizations/{id}", authorization.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Authorization> authorizationList = authorizationRepository.findAll();
        assertThat(authorizationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
