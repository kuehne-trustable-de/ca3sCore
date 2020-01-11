package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Nonce;
import de.trustable.ca3s.core.repository.NonceRepository;
import de.trustable.ca3s.core.service.NonceService;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link NonceResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class NonceResourceIT {

    private static final String DEFAULT_NONCE_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_NONCE_VALUE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EXPIRES_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRES_AT = LocalDate.now(ZoneId.systemDefault());

    @Autowired
    private NonceRepository nonceRepository;

    @Autowired
    private NonceService nonceService;

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

    private MockMvc restNonceMockMvc;

    private Nonce nonce;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final NonceResource nonceResource = new NonceResource(nonceService);
        this.restNonceMockMvc = MockMvcBuilders.standaloneSetup(nonceResource)
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
    public static Nonce createEntity(EntityManager em) {
        Nonce nonce = new Nonce()
            .nonceValue(DEFAULT_NONCE_VALUE)
            .expiresAt(DEFAULT_EXPIRES_AT);
        return nonce;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Nonce createUpdatedEntity(EntityManager em) {
        Nonce nonce = new Nonce()
            .nonceValue(UPDATED_NONCE_VALUE)
            .expiresAt(UPDATED_EXPIRES_AT);
        return nonce;
    }

    @BeforeEach
    public void initTest() {
        nonce = createEntity(em);
    }

    @Test
    @Transactional
    public void createNonce() throws Exception {
        int databaseSizeBeforeCreate = nonceRepository.findAll().size();

        // Create the Nonce
        restNonceMockMvc.perform(post("/api/nonces")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nonce)))
            .andExpect(status().isCreated());

        // Validate the Nonce in the database
        List<Nonce> nonceList = nonceRepository.findAll();
        assertThat(nonceList).hasSize(databaseSizeBeforeCreate + 1);
        Nonce testNonce = nonceList.get(nonceList.size() - 1);
        assertThat(testNonce.getNonceValue()).isEqualTo(DEFAULT_NONCE_VALUE);
        assertThat(testNonce.getExpiresAt()).isEqualTo(DEFAULT_EXPIRES_AT);
    }

    @Test
    @Transactional
    public void createNonceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = nonceRepository.findAll().size();

        // Create the Nonce with an existing ID
        nonce.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restNonceMockMvc.perform(post("/api/nonces")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nonce)))
            .andExpect(status().isBadRequest());

        // Validate the Nonce in the database
        List<Nonce> nonceList = nonceRepository.findAll();
        assertThat(nonceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllNonces() throws Exception {
        // Initialize the database
        nonceRepository.saveAndFlush(nonce);

        // Get all the nonceList
        restNonceMockMvc.perform(get("/api/nonces?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(nonce.getId().intValue())))
            .andExpect(jsonPath("$.[*].nonceValue").value(hasItem(DEFAULT_NONCE_VALUE)))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())));
    }
    
    @Test
    @Transactional
    public void getNonce() throws Exception {
        // Initialize the database
        nonceRepository.saveAndFlush(nonce);

        // Get the nonce
        restNonceMockMvc.perform(get("/api/nonces/{id}", nonce.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(nonce.getId().intValue()))
            .andExpect(jsonPath("$.nonceValue").value(DEFAULT_NONCE_VALUE))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingNonce() throws Exception {
        // Get the nonce
        restNonceMockMvc.perform(get("/api/nonces/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNonce() throws Exception {
        // Initialize the database
        nonceService.save(nonce);

        int databaseSizeBeforeUpdate = nonceRepository.findAll().size();

        // Update the nonce
        Nonce updatedNonce = nonceRepository.findById(nonce.getId()).get();
        // Disconnect from session so that the updates on updatedNonce are not directly saved in db
        em.detach(updatedNonce);
        updatedNonce
            .nonceValue(UPDATED_NONCE_VALUE)
            .expiresAt(UPDATED_EXPIRES_AT);

        restNonceMockMvc.perform(put("/api/nonces")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedNonce)))
            .andExpect(status().isOk());

        // Validate the Nonce in the database
        List<Nonce> nonceList = nonceRepository.findAll();
        assertThat(nonceList).hasSize(databaseSizeBeforeUpdate);
        Nonce testNonce = nonceList.get(nonceList.size() - 1);
        assertThat(testNonce.getNonceValue()).isEqualTo(UPDATED_NONCE_VALUE);
        assertThat(testNonce.getExpiresAt()).isEqualTo(UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingNonce() throws Exception {
        int databaseSizeBeforeUpdate = nonceRepository.findAll().size();

        // Create the Nonce

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNonceMockMvc.perform(put("/api/nonces")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nonce)))
            .andExpect(status().isBadRequest());

        // Validate the Nonce in the database
        List<Nonce> nonceList = nonceRepository.findAll();
        assertThat(nonceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteNonce() throws Exception {
        // Initialize the database
        nonceService.save(nonce);

        int databaseSizeBeforeDelete = nonceRepository.findAll().size();

        // Delete the nonce
        restNonceMockMvc.perform(delete("/api/nonces/{id}", nonce.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Nonce> nonceList = nonceRepository.findAll();
        assertThat(nonceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
