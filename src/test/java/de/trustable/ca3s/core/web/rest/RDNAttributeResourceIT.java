package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.RDNAttribute;
import de.trustable.ca3s.core.repository.RDNAttributeRepository;
import de.trustable.ca3s.core.service.RDNAttributeService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

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

import javax.persistence.EntityManager;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RDNAttributeResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class RDNAttributeResourceIT {

    private static final String DEFAULT_ATTRIBUTE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ATTRIBUTE_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_ATTRIBUTE_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_ATTRIBUTE_VALUE = "BBBBBBBBBB";

    @Autowired
    private RDNAttributeRepository rDNAttributeRepository;

    @Autowired
    private RDNAttributeService rDNAttributeService;

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

    private MockMvc restRDNAttributeMockMvc;

    private RDNAttribute rDNAttribute;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RDNAttributeResource rDNAttributeResource = new RDNAttributeResource(rDNAttributeService);
        this.restRDNAttributeMockMvc = MockMvcBuilders.standaloneSetup(rDNAttributeResource)
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
    public static RDNAttribute createEntity(EntityManager em) {
        RDNAttribute rDNAttribute = new RDNAttribute()
            .attributeType(DEFAULT_ATTRIBUTE_TYPE)
            .attributeValue(DEFAULT_ATTRIBUTE_VALUE);
        return rDNAttribute;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RDNAttribute createUpdatedEntity(EntityManager em) {
        RDNAttribute rDNAttribute = new RDNAttribute()
            .attributeType(UPDATED_ATTRIBUTE_TYPE)
            .attributeValue(UPDATED_ATTRIBUTE_VALUE);
        return rDNAttribute;
    }

    @BeforeEach
    public void initTest() {
        rDNAttribute = createEntity(em);
    }

    @Test
    @Transactional
    public void createRDNAttribute() throws Exception {
        int databaseSizeBeforeCreate = rDNAttributeRepository.findAll().size();

        // Create the RDNAttribute
        restRDNAttributeMockMvc.perform(post("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(rDNAttribute)))
            .andExpect(status().isCreated());

        // Validate the RDNAttribute in the database
        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeCreate + 1);
        RDNAttribute testRDNAttribute = rDNAttributeList.get(rDNAttributeList.size() - 1);
        assertThat(testRDNAttribute.getAttributeType()).isEqualTo(DEFAULT_ATTRIBUTE_TYPE);
        assertThat(testRDNAttribute.getAttributeValue()).isEqualTo(DEFAULT_ATTRIBUTE_VALUE);
    }

    @Test
    @Transactional
    public void createRDNAttributeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = rDNAttributeRepository.findAll().size();

        // Create the RDNAttribute with an existing ID
        rDNAttribute.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRDNAttributeMockMvc.perform(post("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(rDNAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the RDNAttribute in the database
        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAttributeTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = rDNAttributeRepository.findAll().size();
        // set the field null
        rDNAttribute.setAttributeType(null);

        // Create the RDNAttribute, which fails.

        restRDNAttributeMockMvc.perform(post("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(rDNAttribute)))
            .andExpect(status().isBadRequest());

        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAttributeValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = rDNAttributeRepository.findAll().size();
        // set the field null
        rDNAttribute.setAttributeValue(null);

        // Create the RDNAttribute, which fails.

        restRDNAttributeMockMvc.perform(post("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(rDNAttribute)))
            .andExpect(status().isBadRequest());

        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRDNAttributes() throws Exception {
        // Initialize the database
        rDNAttributeRepository.saveAndFlush(rDNAttribute);

        // Get all the rDNAttributeList
        restRDNAttributeMockMvc.perform(get("/api/rdn-attributes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rDNAttribute.getId().intValue())))
            .andExpect(jsonPath("$.[*].attributeType").value(hasItem(DEFAULT_ATTRIBUTE_TYPE)))
            .andExpect(jsonPath("$.[*].attributeValue").value(hasItem(DEFAULT_ATTRIBUTE_VALUE)));
    }
    
    @Test
    @Transactional
    public void getRDNAttribute() throws Exception {
        // Initialize the database
        rDNAttributeRepository.saveAndFlush(rDNAttribute);

        // Get the rDNAttribute
        restRDNAttributeMockMvc.perform(get("/api/rdn-attributes/{id}", rDNAttribute.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(rDNAttribute.getId().intValue()))
            .andExpect(jsonPath("$.attributeType").value(DEFAULT_ATTRIBUTE_TYPE))
            .andExpect(jsonPath("$.attributeValue").value(DEFAULT_ATTRIBUTE_VALUE));
    }

    @Test
    @Transactional
    public void getNonExistingRDNAttribute() throws Exception {
        // Get the rDNAttribute
        restRDNAttributeMockMvc.perform(get("/api/rdn-attributes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRDNAttribute() throws Exception {
        // Initialize the database
        rDNAttributeService.save(rDNAttribute);

        int databaseSizeBeforeUpdate = rDNAttributeRepository.findAll().size();

        // Update the rDNAttribute
        RDNAttribute updatedRDNAttribute = rDNAttributeRepository.findById(rDNAttribute.getId()).get();
        // Disconnect from session so that the updates on updatedRDNAttribute are not directly saved in db
        em.detach(updatedRDNAttribute);
        updatedRDNAttribute
            .attributeType(UPDATED_ATTRIBUTE_TYPE)
            .attributeValue(UPDATED_ATTRIBUTE_VALUE);

        restRDNAttributeMockMvc.perform(put("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRDNAttribute)))
            .andExpect(status().isOk());

        // Validate the RDNAttribute in the database
        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeUpdate);
        RDNAttribute testRDNAttribute = rDNAttributeList.get(rDNAttributeList.size() - 1);
        assertThat(testRDNAttribute.getAttributeType()).isEqualTo(UPDATED_ATTRIBUTE_TYPE);
        assertThat(testRDNAttribute.getAttributeValue()).isEqualTo(UPDATED_ATTRIBUTE_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingRDNAttribute() throws Exception {
        int databaseSizeBeforeUpdate = rDNAttributeRepository.findAll().size();

        // Create the RDNAttribute

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRDNAttributeMockMvc.perform(put("/api/rdn-attributes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(rDNAttribute)))
            .andExpect(status().isBadRequest());

        // Validate the RDNAttribute in the database
        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRDNAttribute() throws Exception {
        // Initialize the database
        rDNAttributeService.save(rDNAttribute);

        int databaseSizeBeforeDelete = rDNAttributeRepository.findAll().size();

        // Delete the rDNAttribute
        restRDNAttributeMockMvc.perform(delete("/api/rdn-attributes/{id}", rDNAttribute.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<RDNAttribute> rDNAttributeList = rDNAttributeRepository.findAll();
        assertThat(rDNAttributeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
