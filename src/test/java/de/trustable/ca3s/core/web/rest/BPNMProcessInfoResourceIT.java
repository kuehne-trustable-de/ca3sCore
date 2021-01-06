package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.repository.BPNMProcessInfoRepository;
import de.trustable.ca3s.core.service.BPNMProcessInfoService;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.domain.enumeration.BPNMProcessType;
/**
 * Integration tests for the {@link BPNMProcessInfoResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class BPNMProcessInfoResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_VERSION = "BBBBBBBBBB";

    private static final BPNMProcessType DEFAULT_TYPE = BPNMProcessType.CA_INVOCATION;
    private static final BPNMProcessType UPDATED_TYPE = BPNMProcessType.REQUEST_AUTHORIZATION;

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_CHANGE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHANGE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_SIGNATURE_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_SIGNATURE_BASE_64 = "BBBBBBBBBB";

    @Autowired
    private BPNMProcessInfoRepository bPNMProcessInfoRepository;

    @Autowired
    private BPNMProcessInfoService bPNMProcessInfoService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBPNMProcessInfoMockMvc;

    private BPNMProcessInfo bPNMProcessInfo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BPNMProcessInfo createEntity(EntityManager em) {
        BPNMProcessInfo bPNMProcessInfo = new BPNMProcessInfo()
            .name(DEFAULT_NAME)
            .version(DEFAULT_VERSION)
            .type(DEFAULT_TYPE)
            .author(DEFAULT_AUTHOR)
            .lastChange(DEFAULT_LAST_CHANGE)
            .signatureBase64(DEFAULT_SIGNATURE_BASE_64);
        return bPNMProcessInfo;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BPNMProcessInfo createUpdatedEntity(EntityManager em) {
        BPNMProcessInfo bPNMProcessInfo = new BPNMProcessInfo()
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64);
        return bPNMProcessInfo;
    }

    @BeforeEach
    public void initTest() {
        bPNMProcessInfo = createEntity(em);
    }

    @Test
    @Transactional
    public void createBPNMProcessInfo() throws Exception {
        int databaseSizeBeforeCreate = bPNMProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo
        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isCreated());

        // Validate the BPNMProcessInfo in the database
        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeCreate + 1);
        BPNMProcessInfo testBPNMProcessInfo = bPNMProcessInfoList.get(bPNMProcessInfoList.size() - 1);
        assertThat(testBPNMProcessInfo.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBPNMProcessInfo.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testBPNMProcessInfo.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBPNMProcessInfo.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testBPNMProcessInfo.getLastChange()).isEqualTo(DEFAULT_LAST_CHANGE);
        assertThat(testBPNMProcessInfo.getSignatureBase64()).isEqualTo(DEFAULT_SIGNATURE_BASE_64);
    }

    @Test
    @Transactional
    public void createBPNMProcessInfoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bPNMProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo with an existing ID
        bPNMProcessInfo.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        // Validate the BPNMProcessInfo in the database
        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPNMProcessInfoRepository.findAll().size();
        // set the field null
        bPNMProcessInfo.setName(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPNMProcessInfoRepository.findAll().size();
        // set the field null
        bPNMProcessInfo.setVersion(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPNMProcessInfoRepository.findAll().size();
        // set the field null
        bPNMProcessInfo.setType(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAuthorIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPNMProcessInfoRepository.findAll().size();
        // set the field null
        bPNMProcessInfo.setAuthor(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastChangeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPNMProcessInfoRepository.findAll().size();
        // set the field null
        bPNMProcessInfo.setLastChange(null);

        // Create the BPNMProcessInfo, which fails.

        restBPNMProcessInfoMockMvc.perform(post("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBPNMProcessInfos() throws Exception {
        // Initialize the database
        bPNMProcessInfoRepository.saveAndFlush(bPNMProcessInfo);

        // Get all the bPNMProcessInfoList
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bPNMProcessInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].signatureBase64").value(hasItem(DEFAULT_SIGNATURE_BASE_64.toString())));
    }

    @Test
    @Transactional
    public void getBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPNMProcessInfoRepository.saveAndFlush(bPNMProcessInfo);

        // Get the bPNMProcessInfo
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos/{id}", bPNMProcessInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bPNMProcessInfo.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.lastChange").value(DEFAULT_LAST_CHANGE.toString()))
            .andExpect(jsonPath("$.signatureBase64").value(DEFAULT_SIGNATURE_BASE_64.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingBPNMProcessInfo() throws Exception {
        // Get the bPNMProcessInfo
        restBPNMProcessInfoMockMvc.perform(get("/api/bpnm-process-infos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPNMProcessInfoService.save(bPNMProcessInfo);

        int databaseSizeBeforeUpdate = bPNMProcessInfoRepository.findAll().size();

        // Update the bPNMProcessInfo
        BPNMProcessInfo updatedBPNMProcessInfo = bPNMProcessInfoRepository.findById(bPNMProcessInfo.getId()).get();
        // Disconnect from session so that the updates on updatedBPNMProcessInfo are not directly saved in db
        em.detach(updatedBPNMProcessInfo);
        updatedBPNMProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64);

        restBPNMProcessInfoMockMvc.perform(put("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedBPNMProcessInfo)))
            .andExpect(status().isOk());

        // Validate the BPNMProcessInfo in the database
        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeUpdate);
        BPNMProcessInfo testBPNMProcessInfo = bPNMProcessInfoList.get(bPNMProcessInfoList.size() - 1);
        assertThat(testBPNMProcessInfo.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBPNMProcessInfo.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testBPNMProcessInfo.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBPNMProcessInfo.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testBPNMProcessInfo.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testBPNMProcessInfo.getSignatureBase64()).isEqualTo(UPDATED_SIGNATURE_BASE_64);
    }

    @Test
    @Transactional
    public void updateNonExistingBPNMProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPNMProcessInfoRepository.findAll().size();

        // Create the BPNMProcessInfo

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBPNMProcessInfoMockMvc.perform(put("/api/bpnm-process-infos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bPNMProcessInfo)))
            .andExpect(status().isBadRequest());

        // Validate the BPNMProcessInfo in the database
        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBPNMProcessInfo() throws Exception {
        // Initialize the database
        bPNMProcessInfoService.save(bPNMProcessInfo);

        int databaseSizeBeforeDelete = bPNMProcessInfoRepository.findAll().size();

        // Delete the bPNMProcessInfo
        restBPNMProcessInfoMockMvc.perform(delete("/api/bpnm-process-infos/{id}", bPNMProcessInfo.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BPNMProcessInfo> bPNMProcessInfoList = bPNMProcessInfoRepository.findAll();
        assertThat(bPNMProcessInfoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
