package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.RequestAttribute;
import de.trustable.ca3s.core.repository.RequestAttributeRepository;
import de.trustable.ca3s.core.service.RequestAttributeService;

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
 * Integration tests for the {@link RequestAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class RequestAttributeResourceIT {

    private static final String DEFAULT_ATTRIBUTE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ATTRIBUTE_TYPE = "BBBBBBBBBB";

    @Autowired
    private RequestAttributeRepository requestAttributeRepository;

    @Autowired
    private RequestAttributeService requestAttributeService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRequestAttributeMockMvc;

    private RequestAttribute requestAttribute;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RequestAttribute createEntity(EntityManager em) {
        RequestAttribute requestAttribute = new RequestAttribute()
            .attributeType(DEFAULT_ATTRIBUTE_TYPE);
        return requestAttribute;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RequestAttribute createUpdatedEntity(EntityManager em) {
        RequestAttribute requestAttribute = new RequestAttribute()
            .attributeType(UPDATED_ATTRIBUTE_TYPE);
        return requestAttribute;
    }

    @BeforeEach
    public void initTest() {
        requestAttribute = createEntity(em);
    }

    @Test
    @Transactional
    public void createRequestAttribute() throws Exception {
        int databaseSizeBeforeCreate = requestAttributeRepository.findAll().size();

        // Create the RequestAttribute
        restRequestAttributeMockMvc.perform(post("/api/request-attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(requestAttribute)))
            .andExpect(status().isCreated());

        // Validate the RequestAttribute in the database
        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeCreate + 1);
        RequestAttribute testRequestAttribute = requestAttributeList.get(requestAttributeList.size() - 1);
        assertThat(testRequestAttribute.getAttributeType()).isEqualTo(DEFAULT_ATTRIBUTE_TYPE);
    }

    @Test
    @Transactional
    public void createRequestAttributeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = requestAttributeRepository.findAll().size();

        // Create the RequestAttribute with an existing ID
        requestAttribute.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRequestAttributeMockMvc.perform(post("/api/request-attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(requestAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the RequestAttribute in the database
        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAttributeTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestAttributeRepository.findAll().size();
        // set the field null
        requestAttribute.setAttributeType(null);

        // Create the RequestAttribute, which fails.

        restRequestAttributeMockMvc.perform(post("/api/request-attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(requestAttribute)))
            .andExpect(status().isBadRequest());

        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRequestAttributes() throws Exception {
        // Initialize the database
        requestAttributeRepository.saveAndFlush(requestAttribute);

        // Get all the requestAttributeList
        restRequestAttributeMockMvc.perform(get("/api/request-attributes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(requestAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].attributeType").value(hasItem(DEFAULT_ATTRIBUTE_TYPE)));
    }
    
    @Test
    @Transactional
    public void getRequestAttribute() throws Exception {
        // Initialize the database
        requestAttributeRepository.saveAndFlush(requestAttribute);

        // Get the requestAttribute
        restRequestAttributeMockMvc.perform(get("/api/request-attributes/{id}", requestAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(requestAttribute.getId().intValue()))
            .andExpect(jsonPath("$.attributeType").value(DEFAULT_ATTRIBUTE_TYPE));
    }

    @Test
    @Transactional
    public void getNonExistingRequestAttribute() throws Exception {
        // Get the requestAttribute
        restRequestAttributeMockMvc.perform(get("/api/request-attributes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRequestAttribute() throws Exception {
        // Initialize the database
        requestAttributeService.save(requestAttribute);

        int databaseSizeBeforeUpdate = requestAttributeRepository.findAll().size();

        // Update the requestAttribute
        RequestAttribute updatedRequestAttribute = requestAttributeRepository.findById(requestAttribute.getId()).get();
        // Disconnect from session so that the updates on updatedRequestAttribute are not directly saved in db
        em.detach(updatedRequestAttribute);
        updatedRequestAttribute
            .attributeType(UPDATED_ATTRIBUTE_TYPE);

        restRequestAttributeMockMvc.perform(put("/api/request-attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedRequestAttribute)))
            .andExpect(status().isOk());

        // Validate the RequestAttribute in the database
        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeUpdate);
        RequestAttribute testRequestAttribute = requestAttributeList.get(requestAttributeList.size() - 1);
        assertThat(testRequestAttribute.getAttributeType()).isEqualTo(UPDATED_ATTRIBUTE_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingRequestAttribute() throws Exception {
        int databaseSizeBeforeUpdate = requestAttributeRepository.findAll().size();

        // Create the RequestAttribute

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRequestAttributeMockMvc.perform(put("/api/request-attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(requestAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the RequestAttribute in the database
        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRequestAttribute() throws Exception {
        // Initialize the database
        requestAttributeService.save(requestAttribute);

        int databaseSizeBeforeDelete = requestAttributeRepository.findAll().size();

        // Delete the requestAttribute
        restRequestAttributeMockMvc.perform(delete("/api/request-attributes/{id}", requestAttribute.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RequestAttribute> requestAttributeList = requestAttributeRepository.findAll();
        assertThat(requestAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
