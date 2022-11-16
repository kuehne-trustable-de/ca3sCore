package de.trustable.ca3s.core.web.rest;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import de.trustable.ca3s.core.service.AcmeAccountService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;
/**
 * Integration tests for the {@link AcmeAccountResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
@ActiveProfiles("dev")
public class AcmeAccountResourceIT {

    private static final Long DEFAULT_ACCOUNT_ID = 1L;
    private static final Long UPDATED_ACCOUNT_ID = 2L;

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
    private AcmeAccountRepository aCMEAccountRepository;

    @Autowired
    private AcmeAccountService aCMEAccountService;

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

    private MockMvc restAcmeAccountMockMvc;

    private AcmeAccount aCMEAccount;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AcmeAccountResource aCMEAccountResource = new AcmeAccountResource(aCMEAccountService);
        this.restAcmeAccountMockMvc = MockMvcBuilders.standaloneSetup(aCMEAccountResource)
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
    public static AcmeAccount createEntity(EntityManager em) {
        AcmeAccount aCMEAccount = new AcmeAccount()
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
    public static AcmeAccount createUpdatedEntity(EntityManager em) {
        AcmeAccount aCMEAccount = new AcmeAccount()
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
    public void createAcmeAccount() throws Exception {
        int databaseSizeBeforeCreate = aCMEAccountRepository.findAll().size();

        // Create the AcmeAccount
        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isCreated());

        // Validate the AcmeAccount in the database
        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeAccount testAcmeAccount = aCMEAccountList.get(aCMEAccountList.size() - 1);
        assertThat(testAcmeAccount.getAccountId()).isEqualTo(DEFAULT_ACCOUNT_ID);
        assertThat(testAcmeAccount.getRealm()).isEqualTo(DEFAULT_REALM);
        assertThat(testAcmeAccount.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAcmeAccount.isTermsOfServiceAgreed()).isEqualTo(DEFAULT_TERMS_OF_SERVICE_AGREED);
        assertThat(testAcmeAccount.getPublicKeyHash()).isEqualTo(DEFAULT_PUBLIC_KEY_HASH);
        assertThat(testAcmeAccount.getPublicKey()).isEqualTo(DEFAULT_PUBLIC_KEY);
    }

    @Test
    @Transactional
    public void createAcmeAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = aCMEAccountRepository.findAll().size();

        // Create the AcmeAccount with an existing ID
        aCMEAccount.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeAccount in the database
        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAccountIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setAccountId(null);

        // Create the AcmeAccount, which fails.

        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRealmIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setRealm(null);

        // Create the AcmeAccount, which fails.

        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTermsOfServiceAgreedIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setTermsOfServiceAgreed(null);

        // Create the AcmeAccount, which fails.

        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPublicKeyHashIsRequired() throws Exception {
        int databaseSizeBeforeTest = aCMEAccountRepository.findAll().size();
        // set the field null
        aCMEAccount.setPublicKeyHash(null);

        // Create the AcmeAccount, which fails.

        restAcmeAccountMockMvc.perform(post("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeAccounts() throws Exception {
        // Initialize the database
        aCMEAccountRepository.saveAndFlush(aCMEAccount);

        // Get all the aCMEAccountList
        restAcmeAccountMockMvc.perform(get("/api/acme-accounts?sort=id,desc"))
            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(aCMEAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].accountId").value(hasItem(DEFAULT_ACCOUNT_ID.intValue())))
            .andExpect(jsonPath("$.[*].realm").value(hasItem(DEFAULT_REALM)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.getValue())))
            .andExpect(jsonPath("$.[*].termsOfServiceAgreed").value(hasItem(DEFAULT_TERMS_OF_SERVICE_AGREED.booleanValue())))
            .andExpect(jsonPath("$.[*].publicKeyHash").value(hasItem(DEFAULT_PUBLIC_KEY_HASH)))
            .andExpect(jsonPath("$.[*].publicKey").value(hasItem(DEFAULT_PUBLIC_KEY.toString())));
    }

    @Test
    @Transactional
    public void getAcmeAccount() throws Exception {
        // Initialize the database
        aCMEAccountRepository.saveAndFlush(aCMEAccount);

        // Get the aCMEAccount
        restAcmeAccountMockMvc.perform(get("/api/acme-accounts/{id}", aCMEAccount.getId()))
            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(aCMEAccount.getId().intValue()))
            .andExpect(jsonPath("$.accountId").value(DEFAULT_ACCOUNT_ID.intValue()))
            .andExpect(jsonPath("$.realm").value(DEFAULT_REALM))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.getValue()))
            .andExpect(jsonPath("$.termsOfServiceAgreed").value(DEFAULT_TERMS_OF_SERVICE_AGREED.booleanValue()))
            .andExpect(jsonPath("$.publicKeyHash").value(DEFAULT_PUBLIC_KEY_HASH))
            .andExpect(jsonPath("$.publicKey").value(DEFAULT_PUBLIC_KEY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeAccount() throws Exception {
        // Get the aCMEAccount
        restAcmeAccountMockMvc.perform(get("/api/acme-accounts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeAccount() throws Exception {
        // Initialize the database
        aCMEAccountService.save(aCMEAccount);

        int databaseSizeBeforeUpdate = aCMEAccountRepository.findAll().size();

        // Update the aCMEAccount
        AcmeAccount updatedAcmeAccount = aCMEAccountRepository.findById(aCMEAccount.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeAccount are not directly saved in db
        em.detach(updatedAcmeAccount);
        updatedAcmeAccount
            .accountId(UPDATED_ACCOUNT_ID)
            .realm(UPDATED_REALM)
            .status(UPDATED_STATUS)
            .termsOfServiceAgreed(UPDATED_TERMS_OF_SERVICE_AGREED)
            .publicKeyHash(UPDATED_PUBLIC_KEY_HASH)
            .publicKey(UPDATED_PUBLIC_KEY);

        restAcmeAccountMockMvc.perform(put("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeAccount)))
            .andExpect(status().isOk());

        // Validate the AcmeAccount in the database
        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeUpdate);
        AcmeAccount testAcmeAccount = aCMEAccountList.get(aCMEAccountList.size() - 1);
        assertThat(testAcmeAccount.getAccountId()).isEqualTo(UPDATED_ACCOUNT_ID);
        assertThat(testAcmeAccount.getRealm()).isEqualTo(UPDATED_REALM);
        assertThat(testAcmeAccount.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAcmeAccount.isTermsOfServiceAgreed()).isEqualTo(UPDATED_TERMS_OF_SERVICE_AGREED);
        assertThat(testAcmeAccount.getPublicKeyHash()).isEqualTo(UPDATED_PUBLIC_KEY_HASH);
        assertThat(testAcmeAccount.getPublicKey()).isEqualTo(UPDATED_PUBLIC_KEY);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeAccount() throws Exception {
        int databaseSizeBeforeUpdate = aCMEAccountRepository.findAll().size();

        // Create the AcmeAccount

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeAccountMockMvc.perform(put("/api/acme-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(aCMEAccount)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeAccount in the database
        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeAccount() throws Exception {
        // Initialize the database
        aCMEAccountService.save(aCMEAccount);

        int databaseSizeBeforeDelete = aCMEAccountRepository.findAll().size();

        // Delete the aCMEAccount
        restAcmeAccountMockMvc.perform(delete("/api/acme-accounts/{id}", aCMEAccount.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeAccount> aCMEAccountList = aCMEAccountRepository.findAll();
        assertThat(aCMEAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
