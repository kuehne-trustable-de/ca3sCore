package de.trustable.ca3s.core.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.IntegrationTest;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;

import de.trustable.ca3s.core.service.dto.BPMNProcessInfoView;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link BPMNProcessInfoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(roles = { "ADMIN" })
@ActiveProfiles("dev")
class BPMNProcessInfoResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_VERSION = "BBBBBBBBBB";

    private static final BPMNProcessType DEFAULT_TYPE = BPMNProcessType.CA_INVOCATION;
    private static final BPMNProcessType UPDATED_TYPE = BPMNProcessType.REQUEST_AUTHORIZATION;

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_CHANGE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHANGE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_SIGNATURE_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_SIGNATURE_BASE_64 = "BBBBBBBBBB";

    private static final String DEFAULT_BPMN_HASH_BASE_64 = "AAAAAAAAAA";
    private static final String UPDATED_BPMN_HASH_BASE_64 = "BBBBBBBBBB";

    private static final String DEFAULT_BPMN_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_BPMN_CONTENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/bpmn-process-infos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BPMNProcessInfoRepository bPMNProcessInfoRepository;

    @Autowired
    private BPMNUtil bpmnUtil;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBPMNProcessInfoMockMvc;

    private BPMNProcessInfoView bpmnProcessInfoView;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BPMNProcessInfoView createEntity(EntityManager em) {
        BPMNProcessInfoView bPMNProcessInfo = new BPMNProcessInfoView();

        bPMNProcessInfo.setName(DEFAULT_NAME);
        bPMNProcessInfo.setVersion(DEFAULT_VERSION);
        bPMNProcessInfo.setType(DEFAULT_TYPE);
        bPMNProcessInfo.setAuthor(DEFAULT_AUTHOR);
        bPMNProcessInfo.setLastChange(DEFAULT_LAST_CHANGE);
        bPMNProcessInfo.setSignatureBase64(DEFAULT_SIGNATURE_BASE_64);
        bPMNProcessInfo.setBpmnHashBase64(DEFAULT_BPMN_HASH_BASE_64);
        bPMNProcessInfo.setProcessId(DEFAULT_BPMN_CONTENT);
        bPMNProcessInfo.setSignatureBase64(DEFAULT_SIGNATURE_BASE_64);
        return bPMNProcessInfo;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BPMNProcessInfoView createUpdatedEntity(EntityManager em) {
        BPMNProcessInfoView bPMNProcessInfo = new BPMNProcessInfoView();
        bPMNProcessInfo.setName(UPDATED_NAME);
        bPMNProcessInfo.setVersion(UPDATED_VERSION);
        bPMNProcessInfo.setType(UPDATED_TYPE);
        bPMNProcessInfo.setAuthor(UPDATED_AUTHOR);
        bPMNProcessInfo.setLastChange(UPDATED_LAST_CHANGE);
        bPMNProcessInfo.setSignatureBase64(UPDATED_SIGNATURE_BASE_64);
        bPMNProcessInfo.setBpmnHashBase64(UPDATED_BPMN_HASH_BASE_64);
        bPMNProcessInfo.setProcessId(UPDATED_BPMN_CONTENT);
        bPMNProcessInfo.setSignatureBase64(UPDATED_SIGNATURE_BASE_64);

        return bPMNProcessInfo;
    }

    @BeforeEach
    public void initTest() {
        bpmnProcessInfoView = createEntity(em);
    }

    @Test
    @Transactional
    void createBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeCreate = bPMNProcessInfoRepository.findAll().size();
        // Create the BPMNProcessInfo
        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isCreated());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeCreate + 1);
        BPMNProcessInfo testBPMNProcessInfo = bPMNProcessInfoList.get(bPMNProcessInfoList.size() - 1);
        assertThat(testBPMNProcessInfo.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBPMNProcessInfo.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testBPMNProcessInfo.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBPMNProcessInfo.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testBPMNProcessInfo.getLastChange()).isEqualTo(DEFAULT_LAST_CHANGE);
        assertThat(testBPMNProcessInfo.getSignatureBase64()).isEqualTo(DEFAULT_SIGNATURE_BASE_64);
        assertThat(testBPMNProcessInfo.getBpmnHashBase64()).isEqualTo(DEFAULT_BPMN_HASH_BASE_64);
        assertThat(testBPMNProcessInfo.getProcessId()).isEqualTo(DEFAULT_BPMN_CONTENT);

    }

    @Test
    @Transactional
    void createBPMNProcessInfoWithExistingId() throws Exception {
        // Create the BPMNProcessInfo with an existing ID
        bpmnProcessInfoView.setId(1L);

        int databaseSizeBeforeCreate = bPMNProcessInfoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setName(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setVersion(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setType(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAuthorIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setAuthor(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastChangeIsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setLastChange(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBpmnHashBase64IsRequired() throws Exception {
        int databaseSizeBeforeTest = bPMNProcessInfoRepository.findAll().size();
        // set the field null
        bpmnProcessInfoView.setBpmnHashBase64(null);

        // Create the BPMNProcessInfo, which fails.

        restBPMNProcessInfoMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isBadRequest());

//        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
//        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBPMNProcessInfos() throws Exception {
        bpmnProcessInfoView.setId(count.incrementAndGet());

        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        // Get all the bPMNProcessInfoList
        restBPMNProcessInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(bpmnProcessInfoView.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].signatureBase64").value(hasItem(DEFAULT_SIGNATURE_BASE_64.toString())))
            .andExpect(jsonPath("$.[*].bpmnHashBase64").value(hasItem(DEFAULT_BPMN_HASH_BASE_64)));
    }

    @Test
    @Disabled
    @Transactional
    void getBPMNProcessInfo() throws Exception {

            bpmnProcessInfoView.setId(count.incrementAndGet());

        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        // Get the bPMNProcessInfo
        restBPMNProcessInfoMockMvc
            .perform(get(ENTITY_API_URL_ID, bpmnProcessInfoView.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bpmnProcessInfoView.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.lastChange").value(DEFAULT_LAST_CHANGE.toString()))
            .andExpect(jsonPath("$.signatureBase64").value(DEFAULT_SIGNATURE_BASE_64.toString()))
            .andExpect(jsonPath("$.bpmnHashBase64").value(DEFAULT_BPMN_HASH_BASE_64));

    }

    @Test
    @Transactional
    void getNonExistingBPMNProcessInfo() throws Exception {
        // Get the bPMNProcessInfo
        restBPMNProcessInfoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Disabled
    @Transactional
    void putNewBPMNProcessInfo() throws Exception {

        bpmnProcessInfoView.setId(count.incrementAndGet());

        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Update the bPMNProcessInfo
        BPMNProcessInfo updatedBPMNProcessInfo = bPMNProcessInfoRepository.findById(bpmnProcessInfoView.getId()).get();
        // Disconnect from session so that the updates on updatedBPMNProcessInfo are not directly saved in db
        em.detach(updatedBPMNProcessInfo);
        updatedBPMNProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64)
            .bpmnHashBase64(UPDATED_BPMN_HASH_BASE_64)
            .processId(UPDATED_BPMN_CONTENT);

        restBPMNProcessInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedBPMNProcessInfo.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedBPMNProcessInfo))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void putNonExistingBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();
        bpmnProcessInfoView.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBPMNProcessInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bpmnProcessInfoView.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Transactional
    void putWithIdMismatchBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();
        bpmnProcessInfoView.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBPMNProcessInfoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @Disabled
    @Transactional
    void putWithMissingIdPathParamBPMNProcessInfo() throws Exception {
        // Initialize the database
        bpmnProcessInfoView.setId(count.incrementAndGet());
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Update the bPMNProcessInfo
        BPMNProcessInfo updatedBPMNProcessInfo = bPMNProcessInfoRepository.findById(bpmnProcessInfoView.getId()).get();
        // Disconnect from session so that the updates on updatedBPMNProcessInfo are not directly saved in db
        em.detach(updatedBPMNProcessInfo);
        updatedBPMNProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64)
            .bpmnHashBase64(UPDATED_BPMN_HASH_BASE_64)
            .processId(UPDATED_BPMN_CONTENT);

        restBPMNProcessInfoMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bpmnProcessInfoView))
            )
            .andExpect(status().isOk());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBPMNProcessInfoWithPatch() throws Exception {
        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Update the bPMNProcessInfo using partial update
        BPMNProcessInfo partialUpdatedBPMNProcessInfo = new BPMNProcessInfo();
        partialUpdatedBPMNProcessInfo.setId(bpmnProcessInfoView.getId());

        partialUpdatedBPMNProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .bpmnHashBase64(UPDATED_BPMN_HASH_BASE_64)
            .processId(UPDATED_BPMN_CONTENT);

        restBPMNProcessInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBPMNProcessInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBPMNProcessInfo))
            )
            .andExpect(status().isMethodNotAllowed());

    }
/*
    @Test
    @Transactional
    void fullUpdateBPMNProcessInfoWithPatch() throws Exception {
        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bPMNProcessInfo);

        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();

        // Update the bPMNProcessInfo using partial update
        BPMNProcessInfo partialUpdatedBPMNProcessInfo = new BPMNProcessInfo();
        partialUpdatedBPMNProcessInfo.setId(bPMNProcessInfo.getId());

        partialUpdatedBPMNProcessInfo
            .name(UPDATED_NAME)
            .version(UPDATED_VERSION)
            .type(UPDATED_TYPE)
            .author(UPDATED_AUTHOR)
            .lastChange(UPDATED_LAST_CHANGE)
            .signatureBase64(UPDATED_SIGNATURE_BASE_64)
            .bpmnHashBase64(UPDATED_BPMN_HASH_BASE_64)
            .processId(UPDATED_BPMN_CONTENT);

        restBPMNProcessInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBPMNProcessInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBPMNProcessInfo))
            )
            .andExpect(status().isOk());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
        BPMNProcessInfo testBPMNProcessInfo = bPMNProcessInfoList.get(bPMNProcessInfoList.size() - 1);
        assertThat(testBPMNProcessInfo.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBPMNProcessInfo.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testBPMNProcessInfo.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBPMNProcessInfo.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testBPMNProcessInfo.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testBPMNProcessInfo.getSignatureBase64()).isEqualTo(UPDATED_SIGNATURE_BASE_64);
        assertThat(testBPMNProcessInfo.getBpmnHashBase64()).isEqualTo(UPDATED_BPMN_HASH_BASE_64);
        assertThat(testBPMNProcessInfo.getProcessId()).isEqualTo(UPDATED_BPMN_CONTENT);
    }

    @Test
    @Transactional
    void patchNonExistingBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();
        bPMNProcessInfo.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBPMNProcessInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, bPMNProcessInfo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();
        bPMNProcessInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBPMNProcessInfoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo))
            )
            .andExpect(status().isBadRequest());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBPMNProcessInfo() throws Exception {
        int databaseSizeBeforeUpdate = bPMNProcessInfoRepository.findAll().size();
        bPMNProcessInfo.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBPMNProcessInfoMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(bPMNProcessInfo))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BPMNProcessInfo in the database
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeUpdate);
    }
*/

    /*
    @Test
    @Transactional
    void deleteBPMNProcessInfo() throws Exception {
        // Initialize the database
        bPMNProcessInfoRepository.saveAndFlush(bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView));

        int databaseSizeBeforeDelete = bPMNProcessInfoRepository.findAll().size();

        // Delete the bPMNProcessInfo
        restBPMNProcessInfoMockMvc
            .perform(delete(ENTITY_API_URL_ID, bpmnProcessInfoView.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BPMNProcessInfo> bPMNProcessInfoList = bPMNProcessInfoRepository.findAll();
        assertThat(bPMNProcessInfoList).hasSize(databaseSizeBeforeDelete - 1);
    }

     */
}
