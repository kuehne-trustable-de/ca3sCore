package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.ProtectedContentService;
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

import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
/**
 * Integration tests for the {@link ProtectedContentResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class ProtectedContentResourceIT {

    private static final String DEFAULT_CONTENT_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT_BASE_64 = "BBBBBBBBBB";

    private static final ProtectedContentType DEFAULT_TYPE = ProtectedContentType.KEY;
    private static final ProtectedContentType UPDATED_TYPE = ProtectedContentType.SECRET;

    private static final ContentRelationType DEFAULT_RELATION_TYPE = ContentRelationType.CERTIFICATE;
    private static final ContentRelationType UPDATED_RELATION_TYPE = ContentRelationType.CONNECTION;

    private static final Long DEFAULT_RELATED_ID = 1L;
    private static final Long UPDATED_RELATED_ID = 2L;

    @Autowired
    private ProtectedContentRepository protectedContentRepository;

    @Autowired
    private ProtectedContentService protectedContentService;

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

    private MockMvc restProtectedContentMockMvc;

    private ProtectedContent protectedContent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ProtectedContentResource protectedContentResource = new ProtectedContentResource(protectedContentService);
        this.restProtectedContentMockMvc = MockMvcBuilders.standaloneSetup(protectedContentResource)
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
    public static ProtectedContent createEntity(EntityManager em) {
        ProtectedContent protectedContent = new ProtectedContent()
            .contentBase64(DEFAULT_CONTENT_BASE_64)
            .type(DEFAULT_TYPE)
            .relationType(DEFAULT_RELATION_TYPE)
            .relatedId(DEFAULT_RELATED_ID);
        return protectedContent;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProtectedContent createUpdatedEntity(EntityManager em) {
        ProtectedContent protectedContent = new ProtectedContent()
            .contentBase64(UPDATED_CONTENT_BASE_64)
            .type(UPDATED_TYPE)
            .relationType(UPDATED_RELATION_TYPE)
            .relatedId(UPDATED_RELATED_ID);
        return protectedContent;
    }

    @BeforeEach
    public void initTest() {
        protectedContent = createEntity(em);
    }

    @Test
    @Transactional
    public void createProtectedContent() throws Exception {
        int databaseSizeBeforeCreate = protectedContentRepository.findAll().size();

        // Create the ProtectedContent
        restProtectedContentMockMvc.perform(post("/api/protected-contents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(protectedContent)))
            .andExpect(status().isCreated());

        // Validate the ProtectedContent in the database
        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeCreate + 1);
        ProtectedContent testProtectedContent = protectedContentList.get(protectedContentList.size() - 1);
        assertThat(testProtectedContent.getContentBase64()).isEqualTo(DEFAULT_CONTENT_BASE_64);
        assertThat(testProtectedContent.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testProtectedContent.getRelationType()).isEqualTo(DEFAULT_RELATION_TYPE);
        assertThat(testProtectedContent.getRelatedId()).isEqualTo(DEFAULT_RELATED_ID);
    }

    @Test
    @Transactional
    public void createProtectedContentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = protectedContentRepository.findAll().size();

        // Create the ProtectedContent with an existing ID
        protectedContent.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProtectedContentMockMvc.perform(post("/api/protected-contents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(protectedContent)))
            .andExpect(status().isBadRequest());

        // Validate the ProtectedContent in the database
        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = protectedContentRepository.findAll().size();
        // set the field null
        protectedContent.setType(null);

        // Create the ProtectedContent, which fails.

        restProtectedContentMockMvc.perform(post("/api/protected-contents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(protectedContent)))
            .andExpect(status().isBadRequest());

        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProtectedContents() throws Exception {
        // Initialize the database
        protectedContentRepository.saveAndFlush(protectedContent);

        // Get all the protectedContentList
        restProtectedContentMockMvc.perform(get("/api/protected-contents?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(protectedContent.getId().intValue())))
            .andExpect(jsonPath("$.[*].contentBase64").value(hasItem(DEFAULT_CONTENT_BASE_64.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].relationType").value(hasItem(DEFAULT_RELATION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].relatedId").value(hasItem(DEFAULT_RELATED_ID.intValue())));
    }

    @Test
    @Transactional
    public void getProtectedContent() throws Exception {
        // Initialize the database
        protectedContentRepository.saveAndFlush(protectedContent);

        // Get the protectedContent
        restProtectedContentMockMvc.perform(get("/api/protected-contents/{id}", protectedContent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(protectedContent.getId().intValue()))
            .andExpect(jsonPath("$.contentBase64").value(DEFAULT_CONTENT_BASE_64.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.relationType").value(DEFAULT_RELATION_TYPE.toString()))
            .andExpect(jsonPath("$.relatedId").value(DEFAULT_RELATED_ID.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingProtectedContent() throws Exception {
        // Get the protectedContent
        restProtectedContentMockMvc.perform(get("/api/protected-contents/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProtectedContent() throws Exception {
        // Initialize the database
        protectedContentService.save(protectedContent);

        int databaseSizeBeforeUpdate = protectedContentRepository.findAll().size();

        // Update the protectedContent
        ProtectedContent updatedProtectedContent = protectedContentRepository.findById(protectedContent.getId()).get();
        // Disconnect from session so that the updates on updatedProtectedContent are not directly saved in db
        em.detach(updatedProtectedContent);
        updatedProtectedContent
            .contentBase64(UPDATED_CONTENT_BASE_64)
            .type(UPDATED_TYPE)
            .relationType(UPDATED_RELATION_TYPE)
            .relatedId(UPDATED_RELATED_ID);

        restProtectedContentMockMvc.perform(put("/api/protected-contents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedProtectedContent)))
            .andExpect(status().isOk());

        // Validate the ProtectedContent in the database
        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeUpdate);
        ProtectedContent testProtectedContent = protectedContentList.get(protectedContentList.size() - 1);
        assertThat(testProtectedContent.getContentBase64()).isEqualTo(UPDATED_CONTENT_BASE_64);
        assertThat(testProtectedContent.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testProtectedContent.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);
        assertThat(testProtectedContent.getRelatedId()).isEqualTo(UPDATED_RELATED_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingProtectedContent() throws Exception {
        int databaseSizeBeforeUpdate = protectedContentRepository.findAll().size();

        // Create the ProtectedContent

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProtectedContentMockMvc.perform(put("/api/protected-contents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(protectedContent)))
            .andExpect(status().isBadRequest());

        // Validate the ProtectedContent in the database
        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProtectedContent() throws Exception {
        // Initialize the database
        protectedContentService.save(protectedContent);

        int databaseSizeBeforeDelete = protectedContentRepository.findAll().size();

        // Delete the protectedContent
        restProtectedContentMockMvc.perform(delete("/api/protected-contents/{id}", protectedContent.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ProtectedContent> protectedContentList = protectedContentRepository.findAll();
        assertThat(protectedContentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
