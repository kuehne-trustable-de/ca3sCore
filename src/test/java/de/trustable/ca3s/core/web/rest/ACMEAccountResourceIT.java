package de.trustable.ca3s.core.web.rest;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.persistence.EntityManager;

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
import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.ACMEAccountRepository;
import de.trustable.ca3s.core.service.ACMEAccountService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;
/**
 * Integration tests for the {@link ACMEAccountResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class ACMEAccountResourceIT {

    private static final Long DEFAULT_ACCOUNT_ID = 1L;
    private static final Long UPDATED_ACCOUNT_ID = 2L;
    private static final Long SMALLER_ACCOUNT_ID = 1L - 1L;

    private static final String DEFAULT_REALM = "AAAAAAAAAA";
    private static final String UPDATED_REALM = "BBBBBBBBBB";

    private static final AccountStatus DEFAULT_STATUS = AccountStatus.VALID;
    private static final AccountStatus UPDATED_STATUS = AccountStatus.DEACTIVATED;

    private static final Boolean DEFAULT_TERMS_OF_SERVICE_AGREED = false;
    private static final Boolean UPDATED_TERMS_OF_SERVICE_AGREED = true;

    private static final String DEFAULT_PUBLIC_KEY_HASH = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_KEY_HASH = "BBBBBBBBBB";

    private static final String DEFAULT_PUBLIC_KEY = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_KEY = "BBBBBBBBBB";

    @Autowired
    private ACMEAccountRepository aCMEAccountRepository;

    @Autowired
    private ACMEAccountService aCMEAccountService;

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

    private MockMvc restACMEAccountMockMvc;

    private ACMEAccount aCMEAccount;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ACMEAccountResource aCMEAccountResource = new ACMEAccountResource(aCMEAccountService);
        this.restACMEAccountMockMvc = MockMvcBuilders.standaloneSetup(aCMEAccountResource)
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
    public static ACMEAccount createEntity(EntityManager em) {
        ACMEAccount aCMEAccount = new ACMEAccount()
            .accountId(DEFAULT_ACCOUNT_ID)
            .realm(DEFAULT_REALM)
            .status(DEFAULT_STATUS)
            .termsOfServiceAgreed(DEFAULT_TERMS_OF_SERVICE_AGREED)
            .publicKeyHash(DEFAULT_PUBLIC_KEY_HASH)
            .publicKey(DEFAULT_PUBLIC_KEY);
        return aCMEAccount;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ACMEAccount createUpdatedEntity(EntityManager em) {
        ACMEAccount aCMEAccount = new ACMEAccount()
            .accountId(UPDATED_ACCOUNT_ID)
            .realm(UPDATED_REALM)
            .status(UPDATED_STATUS)
            .termsOfServiceAgreed(UPDATED_TERMS_OF_SERVICE_AGREED)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .publicKey(UPDATED_PUBLIC_KEY);
        return aCMEAccount;
    }

    @BeforeEach
    public void initTest() {
        aCMEAccount = createEntity(em);
    }

    @Test
    @Transactional
    public void createACMEAccount() throws Exception {
        int databaseSizeBeforeCreate = aCMEAccountRepository.findAll().size();

        // Create the ACMEAccount
        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isCreated());

        // Validate the ACMEAccount in the database
        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeCreate + 1);
        ACMEAccount testACMEAccount = aCMEAccountList.get(aCMEAccountList.size() - 1);
        assertThat(testACMEAccount.getAccountId()).isEqualTo(DEFAULT_ACCOUNT_ID);
        assertThat(testACMEAccount.getRealm()).isEqualTo(DEFAULT_REALM);
        assertThat(testACMEAccount.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testACMEAccount.isTermsOfServiceAgreed()).isEqualTo(DEFAULT_TERMS_OF_SERVICE_AGREED);
        assertThat(testACMEAccount.getPublicKeyHash()).isEqualTo(DEFAULT_PUBLIC_KEY_HASH);
        assertThat(testACMEAccount.getPublicKey()).isEqualTo(DEFAULT_PUBLIC_KEY);
    }

    @Test
    @Transactional
    public void createACMEAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = aCMEAccountRepository.findAll().size();

        // Create the ACMEAccount with an existing ID
        aCMEAccount.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        // Validate the ACMEAccount in the database
        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAccountIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setAccountId(null);

        // Create the ACMEAccount, which fails.

        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRealmIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setRealm(null);

        // Create the ACMEAccount, which fails.

        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTermsOfServiceAgreedIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setTermsOfServiceAgreed(null);

        // Create the ACMEAccount, which fails.

        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPublicKeyHashIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setPublicKeyHash(null);

        // Create the ACMEAccount, which fails.

        restACMEAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllACMEAccounts() throws Exception {
        // Initialize the database
        aCMEAccountRepository.saveAndFlush(aCMEAccount);

        // Get all the aCMEAccountList
        restACMEAccountMockMvc.perform(get("/api/acme-accounts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(aCMEAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].accountId").value(hasItem(DEFAULT_ACCOUNT_ID.intValue())))
            .andExpect(jsonPath("$.[*].realm").value(hasItem(DEFAULT_REALM.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].termsOfServiceAgreed").value(hasItem(DEFAULT_TERMS_OF_SERVICE_AGREED.booleanValue())))
            .andExpect(jsonPath("$.[*].publicKeyHash").value(hasItem(DEFAULT_PUBLIC_KEY_HASH.toString())))
            .andExpect(jsonPath("$.[*].publicKey").value(hasItem(DEFAULT_PUBLIC_KEY.toString())));
    }
    
    @Test
    @Transactional
    public void getACMEAccount() throws Exception {
        // Initialize the database
        aCMEAccountRepository.saveAndFlush(aCMEAccount);

        // Get the aCMEAccount
        restACMEAccountMockMvc.perform(get("/api/acme-accounts/{id}", aCMEAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(aCMEAccount.getId().intValue()))
            .andExpect(jsonPath("$.accountId").value(DEFAULT_ACCOUNT_ID.intValue()))
            .andExpect(jsonPath("$.realm").value(DEFAULT_REALM.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.termsOfServiceAgreed").value(DEFAULT_TERMS_OF_SERVICE_AGREED.booleanValue()))
            .andExpect(jsonPath("$.publicKeyHash").value(DEFAULT_PUBLIC_KEY_HASH.toString()))
            .andExpect(jsonPath("$.publicKey").value(DEFAULT_PUBLIC_KEY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingACMEAccount() throws Exception {
        // Get the aCMEAccount
        restACMEAccountMockMvc.perform(get("/api/acme-accounts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateACMEAccount() throws Exception {
        // Initialize the database
        aCMEAccountService.save(aCMEAccount);

        int databaseSizeBeforeUpdate = aCMEAccountRepository.findAll().size();

        // Update the aCMEAccount
        ACMEAccount updatedACMEAccount = aCMEAccountRepository.findById(aCMEAccount.getId()).get();
        // Disconnect from session so that the updates on updatedACMEAccount are not directly saved in db
        em.detach(updatedACMEAccount);
        updatedACMEAccount
            .accountId(UPDATED_ACCOUNT_ID)
            .realm(UPDATED_REALM)
            .status(UPDATED_STATUS)
            .termsOfServiceAgreed(UPDATED_TERMS_OF_SERVICE_AGREED)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .publicKey(UPDATED_PUBLIC_KEY);

        restACMEAccountMockMvc.perform(put("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedACMEAccount)))
            .andExpect(status().isOk());

        // Validate the ACMEAccount in the database
        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeUpdate);
        ACMEAccount testACMEAccount = aCMEAccountList.get(aCMEAccountList.size() - 1);
        assertThat(testACMEAccount.getAccountId()).isEqualTo(UPDATED_ACCOUNT_ID);
        assertThat(testACMEAccount.getRealm()).isEqualTo(UPDATED_REALM);
        assertThat(testACMEAccount.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testACMEAccount.isTermsOfServiceAgreed()).isEqualTo(UPDATED_TERMS_OF_SERVICE_AGREED);
        assertThat(testACMEAccount.getPublicKeyHash()).isEqualTo(UPDATED_PUBLIC_KEY_HASH);
        assertThat(testACMEAccount.getPublicKey()).isEqualTo(UPDATED_PUBLIC_KEY);
    }

    @Test
    @Transactional
    public void updateNonExistingACMEAccount() throws Exception {
        int databaseSizeBeforeUpdate = aCMEAccountRepository.findAll().size();

        // Create the ACMEAccount

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restACMEAccountMockMvc.perform(put("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        // Validate the ACMEAccount in the database
        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteACMEAccount() throws Exception {
        // Initialize the database
        aCMEAccountService.save(aCMEAccount);

        int databaseSizeBeforeDelete = aCMEAccountRepository.findAll().size();

        // Delete the aCMEAccount
        restACMEAccountMockMvc.perform(delete("/api/acme-accounts/{id}", aCMEAccount.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ACMEAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ACMEAccount.class);
        ACMEAccount aCMEAccount1 = new ACMEAccount();
        aCMEAccount1.setId(1L);
        ACMEAccount aCMEAccount2 = new ACMEAccount();
        aCMEAccount2.setId(aCMEAccount1.getId());
        assertThat(aCMEAccount1).isEqualTo(aCMEAccount2);
        aCMEAccount2.setId(2L);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
        aCMEAccount1.setId(null);
        assertThat(aCMEAccount1).isNotEqualTo(aCMEAccount2);
    }
}
