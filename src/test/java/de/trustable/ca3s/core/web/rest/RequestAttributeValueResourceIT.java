package de.trustable.ca3s.core.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import de.trustable.ca3s.core.Ca3SJhApp;
import de.trustable.ca3s.core.domain.RequestAttributeValue;
import de.trustable.ca3s.core.repository.RequestAttributeValueRepository;
import de.trustable.ca3s.core.service.RequestAttributeValueService;
import de.trustable.ca3s.core.web.rest.RequestAttributeValueResource;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RequestAttributeValueResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class RequestAttributeValueResourceIT {

    private static final String DEFAULT_ATTRIBUTE_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_ATTRIBUTE_VALUE = "BBBBBBBBBB";

    @Autowired
    private RequestAttributeValueRepository requestAttributeValueRepository;

    @Autowired
    private RequestAttributeValueService requestAttributeValueService;

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

    private MockMvc restRequestAttributeValueMockMvc;

    private RequestAttributeValue requestAttributeValue;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RequestAttributeValueResource requestAttributeValueResource = new RequestAttributeValueResource(requestAttributeValueService);
        this.restRequestAttributeValueMockMvc = MockMvcBuilders.standaloneSetup(requestAttributeValueResource)
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
    public static RequestAttributeValue createEntity(EntityManager em) {
        RequestAttributeValue requestAttributeValue = new RequestAttributeValue()
            .attributeValue(DEFAULT_ATTRIBUTE_VALUE);
        return requestAttributeValue;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RequestAttributeValue createUpdatedEntity(EntityManager em) {
        RequestAttributeValue requestAttributeValue = new RequestAttributeValue()
            .attributeValue(UPDATED_ATTRIBUTE_VALUE);
        return requestAttributeValue;
    }

    @BeforeEach
    public void initTest() {
        requestAttributeValue = createEntity(em);
    }

    @Test
    @Transactional
    public void createRequestAttributeValue() throws Exception {
        int databaseSizeBeforeCreate = requestAttributeValueRepository.findAll().size();

        // Create the RequestAttributeValue
        restRequestAttributeValueMockMvc.perform(post("/api/request-attribute-values")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestAttributeValue)))
            .andExpect(status().isCreated());

        // Validate the RequestAttributeValue in the database
        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeCreate + 1);
        RequestAttributeValue testRequestAttributeValue = requestAttributeValueList.get(requestAttributeValueList.size() - 1);
        assertThat(testRequestAttributeValue.getAttributeValue()).isEqualTo(DEFAULT_ATTRIBUTE_VALUE);
    }

    @Test
    @Transactional
    public void createRequestAttributeValueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = requestAttributeValueRepository.findAll().size();

        // Create the RequestAttributeValue with an existing ID
        requestAttributeValue.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRequestAttributeValueMockMvc.perform(post("/api/request-attribute-values")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestAttributeValue)))
            .andExpect(status().isBadRequest());

        // Validate the RequestAttributeValue in the database
        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAttributeValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestAttributeValueRepository.findAll().size();
        // set the field null
        requestAttributeValue.setAttributeValue(null);

        // Create the RequestAttributeValue, which fails.

        restRequestAttributeValueMockMvc.perform(post("/api/request-attribute-values")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestAttributeValue)))
            .andExpect(status().isBadRequest());

        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRequestAttributeValues() throws Exception {
        // Initialize the database
        requestAttributeValueRepository.saveAndFlush(requestAttributeValue);

        // Get all the requestAttributeValueList
        restRequestAttributeValueMockMvc.perform(get("/api/request-attribute-values?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(requestAttributeValue.getId().intValue())))
            .andExpect(jsonPath("$.[*].attributeValue").value(hasItem(DEFAULT_ATTRIBUTE_VALUE.toString())));
    }
    
    @Test
    @Transactional
    public void getRequestAttributeValue() throws Exception {
        // Initialize the database
        requestAttributeValueRepository.saveAndFlush(requestAttributeValue);

        // Get the requestAttributeValue
        restRequestAttributeValueMockMvc.perform(get("/api/request-attribute-values/{id}", requestAttributeValue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(requestAttributeValue.getId().intValue()))
            .andExpect(jsonPath("$.attributeValue").value(DEFAULT_ATTRIBUTE_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingRequestAttributeValue() throws Exception {
        // Get the requestAttributeValue
        restRequestAttributeValueMockMvc.perform(get("/api/request-attribute-values/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRequestAttributeValue() throws Exception {
        // Initialize the database
        requestAttributeValueService.save(requestAttributeValue);

        int databaseSizeBeforeUpdate = requestAttributeValueRepository.findAll().size();

        // Update the requestAttributeValue
        RequestAttributeValue updatedRequestAttributeValue = requestAttributeValueRepository.findById(requestAttributeValue.getId()).get();
        // Disconnect from session so that the updates on updatedRequestAttributeValue are not directly saved in db
        em.detach(updatedRequestAttributeValue);
        updatedRequestAttributeValue
            .attributeValue(UPDATED_ATTRIBUTE_VALUE);

        restRequestAttributeValueMockMvc.perform(put("/api/request-attribute-values")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRequestAttributeValue)))
            .andExpect(status().isOk());

        // Validate the RequestAttributeValue in the database
        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeUpdate);
        RequestAttributeValue testRequestAttributeValue = requestAttributeValueList.get(requestAttributeValueList.size() - 1);
        assertThat(testRequestAttributeValue.getAttributeValue()).isEqualTo(UPDATED_ATTRIBUTE_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingRequestAttributeValue() throws Exception {
        int databaseSizeBeforeUpdate = requestAttributeValueRepository.findAll().size();

        // Create the RequestAttributeValue

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRequestAttributeValueMockMvc.perform(put("/api/request-attribute-values")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestAttributeValue)))
            .andExpect(status().isBadRequest());

        // Validate the RequestAttributeValue in the database
        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRequestAttributeValue() throws Exception {
        // Initialize the database
        requestAttributeValueService.save(requestAttributeValue);

        int databaseSizeBeforeDelete = requestAttributeValueRepository.findAll().size();

        // Delete the requestAttributeValue
        restRequestAttributeValueMockMvc.perform(delete("/api/request-attribute-values/{id}", requestAttributeValue.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RequestAttributeValue> requestAttributeValueList = requestAttributeValueRepository.findAll();
        assertThat(requestAttributeValueList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RequestAttributeValue.class);
        RequestAttributeValue requestAttributeValue1 = new RequestAttributeValue();
        requestAttributeValue1.setId(1L);
        RequestAttributeValue requestAttributeValue2 = new RequestAttributeValue();
        requestAttributeValue2.setId(requestAttributeValue1.getId());
        assertThat(requestAttributeValue1).isEqualTo(requestAttributeValue2);
        requestAttributeValue2.setId(2L);
        assertThat(requestAttributeValue1).isNotEqualTo(requestAttributeValue2);
        requestAttributeValue1.setId(null);
        assertThat(requestAttributeValue1).isNotEqualTo(requestAttributeValue2);
    }
}
