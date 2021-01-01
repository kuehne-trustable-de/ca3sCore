package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.repository.AcmeContactRepository;
import de.trustable.ca3s.core.service.AcmeContactService;

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
 * Integration tests for the {@link AcmeContactResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class AcmeContactResourceIT {

    private static final Long DEFAULT_CONTACT_ID = 1L;
    private static final Long UPDATED_CONTACT_ID = 2L;

    private static final String DEFAULT_CONTACT_URL = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_URL = "BBBBBBBBBB";

    @Autowired
    private AcmeContactRepository acmeContactRepository;

    @Autowired
    private AcmeContactService acmeContactService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAcmeContactMockMvc;

    private AcmeContact acmeContact;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeContact createEntity(EntityManager em) {
        AcmeContact acmeContact = new AcmeContact()
            .contactId(DEFAULT_CONTACT_ID)
            .contactUrl(DEFAULT_CONTACT_URL);
        return acmeContact;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeContact createUpdatedEntity(EntityManager em) {
        AcmeContact acmeContact = new AcmeContact()
            .contactId(UPDATED_CONTACT_ID)
            .contactUrl(UPDATED_CONTACT_URL);
        return acmeContact;
    }

    @BeforeEach
    public void initTest() {
        acmeContact = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeContact() throws Exception {
        int databaseSizeBeforeCreate = acmeContactRepository.findAll().size();

        // Create the AcmeContact
        restAcmeContactMockMvc.perform(post("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeContact)))
            .andExpect(status().isCreated());

        // Validate the AcmeContact in the database
        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeContact testAcmeContact = acmeContactList.get(acmeContactList.size() - 1);
        assertThat(testAcmeContact.getContactId()).isEqualTo(DEFAULT_CONTACT_ID);
        assertThat(testAcmeContact.getContactUrl()).isEqualTo(DEFAULT_CONTACT_URL);
    }

    @Test
    @Transactional
    public void createAcmeContactWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeContactRepository.findAll().size();

        // Create the AcmeContact with an existing ID
        acmeContact.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeContactMockMvc.perform(post("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeContact)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeContact in the database
        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkContactIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeContactRepository.findAll().size();
        // set the field null
        acmeContact.setContactId(null);

        // Create the AcmeContact, which fails.

        restAcmeContactMockMvc.perform(post("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeContact)))
            .andExpect(status().isBadRequest());

        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContactUrlIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeContactRepository.findAll().size();
        // set the field null
        acmeContact.setContactUrl(null);

        // Create the AcmeContact, which fails.

        restAcmeContactMockMvc.perform(post("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeContact)))
            .andExpect(status().isBadRequest());

        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeContacts() throws Exception {
        // Initialize the database
        acmeContactRepository.saveAndFlush(acmeContact);

        // Get all the acmeContactList
        restAcmeContactMockMvc.perform(get("/api/acme-contacts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeContact.getId().intValue())))
            .andExpect(jsonPath("$.[*].contactId").value(hasItem(DEFAULT_CONTACT_ID.intValue())))
            .andExpect(jsonPath("$.[*].contactUrl").value(hasItem(DEFAULT_CONTACT_URL)));
    }
    
    @Test
    @Transactional
    public void getAcmeContact() throws Exception {
        // Initialize the database
        acmeContactRepository.saveAndFlush(acmeContact);

        // Get the acmeContact
        restAcmeContactMockMvc.perform(get("/api/acme-contacts/{id}", acmeContact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(acmeContact.getId().intValue()))
            .andExpect(jsonPath("$.contactId").value(DEFAULT_CONTACT_ID.intValue()))
            .andExpect(jsonPath("$.contactUrl").value(DEFAULT_CONTACT_URL));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeContact() throws Exception {
        // Get the acmeContact
        restAcmeContactMockMvc.perform(get("/api/acme-contacts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeContact() throws Exception {
        // Initialize the database
        acmeContactService.save(acmeContact);

        int databaseSizeBeforeUpdate = acmeContactRepository.findAll().size();

        // Update the acmeContact
        AcmeContact updatedAcmeContact = acmeContactRepository.findById(acmeContact.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeContact are not directly saved in db
        em.detach(updatedAcmeContact);
        updatedAcmeContact
            .contactId(UPDATED_CONTACT_ID)
            .contactUrl(UPDATED_CONTACT_URL);

        restAcmeContactMockMvc.perform(put("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeContact)))
            .andExpect(status().isOk());

        // Validate the AcmeContact in the database
        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeUpdate);
        AcmeContact testAcmeContact = acmeContactList.get(acmeContactList.size() - 1);
        assertThat(testAcmeContact.getContactId()).isEqualTo(UPDATED_CONTACT_ID);
        assertThat(testAcmeContact.getContactUrl()).isEqualTo(UPDATED_CONTACT_URL);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeContact() throws Exception {
        int databaseSizeBeforeUpdate = acmeContactRepository.findAll().size();

        // Create the AcmeContact

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeContactMockMvc.perform(put("/api/acme-contacts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(acmeContact)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeContact in the database
        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeContact() throws Exception {
        // Initialize the database
        acmeContactService.save(acmeContact);

        int databaseSizeBeforeDelete = acmeContactRepository.findAll().size();

        // Delete the acmeContact
        restAcmeContactMockMvc.perform(delete("/api/acme-contacts/{id}", acmeContact.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeContact> acmeContactList = acmeContactRepository.findAll();
        assertThat(acmeContactList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
