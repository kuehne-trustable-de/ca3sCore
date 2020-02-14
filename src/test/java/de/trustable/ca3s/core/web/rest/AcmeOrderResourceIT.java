package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AcmeOrderService;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
/**
 * Integration tests for the {@link AcmeOrderResource} REST controller.
 */
@SpringBootTest(classes = Ca3SApp.class)
public class AcmeOrderResourceIT {

    private static final Long DEFAULT_ORDER_ID = 1L;
    private static final Long UPDATED_ORDER_ID = 2L;

    private static final AcmeOrderStatus DEFAULT_STATUS = AcmeOrderStatus.PENDING;
    private static final AcmeOrderStatus UPDATED_STATUS = AcmeOrderStatus.READY;

    private static final Instant DEFAULT_EXPIRES = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_NOT_BEFORE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_NOT_BEFORE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_NOT_AFTER = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_NOT_AFTER = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ERROR = "AAAAAAAAAA";
    private static final String UPDATED_ERROR = "BBBBBBBBBB";

    private static final String DEFAULT_FINALIZE_URL = "AAAAAAAAAA";
    private static final String UPDATED_FINALIZE_URL = "BBBBBBBBBB";

    private static final String DEFAULT_CERTIFICATE_URL = "AAAAAAAAAA";
    private static final String UPDATED_CERTIFICATE_URL = "BBBBBBBBBB";

    @Autowired
    private AcmeOrderRepository acmeOrderRepository;

    @Autowired
    private AcmeOrderService acmeOrderService;

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

    private MockMvc restAcmeOrderMockMvc;

    private AcmeOrder acmeOrder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AcmeOrderResource acmeOrderResource = new AcmeOrderResource(acmeOrderService);
        this.restAcmeOrderMockMvc = MockMvcBuilders.standaloneSetup(acmeOrderResource)
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
    public static AcmeOrder createEntity(EntityManager em) {
        AcmeOrder acmeOrder = new AcmeOrder()
            .orderId(DEFAULT_ORDER_ID)
            .status(DEFAULT_STATUS)
            .expires(DEFAULT_EXPIRES)
            .notBefore(DEFAULT_NOT_BEFORE)
            .notAfter(DEFAULT_NOT_AFTER)
            .error(DEFAULT_ERROR)
            .finalizeUrl(DEFAULT_FINALIZE_URL)
            .certificateUrl(DEFAULT_CERTIFICATE_URL);
        return acmeOrder;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AcmeOrder createUpdatedEntity(EntityManager em) {
        AcmeOrder acmeOrder = new AcmeOrder()
            .orderId(UPDATED_ORDER_ID)
            .status(UPDATED_STATUS)
            .expires(UPDATED_EXPIRES)
            .notBefore(UPDATED_NOT_BEFORE)
            .notAfter(UPDATED_NOT_AFTER)
            .error(UPDATED_ERROR)
            .finalizeUrl(UPDATED_FINALIZE_URL)
            .certificateUrl(UPDATED_CERTIFICATE_URL);
        return acmeOrder;
    }

    @BeforeEach
    public void initTest() {
        acmeOrder = createEntity(em);
    }

    @Test
    @Transactional
    public void createAcmeOrder() throws Exception {
        int databaseSizeBeforeCreate = acmeOrderRepository.findAll().size();

        // Create the AcmeOrder
        restAcmeOrderMockMvc.perform(post("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeOrder)))
            .andExpect(status().isCreated());

        // Validate the AcmeOrder in the database
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeCreate + 1);
        AcmeOrder testAcmeOrder = acmeOrderList.get(acmeOrderList.size() - 1);
        assertThat(testAcmeOrder.getOrderId()).isEqualTo(DEFAULT_ORDER_ID);
        assertThat(testAcmeOrder.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAcmeOrder.getExpires()).isEqualTo(DEFAULT_EXPIRES);
        assertThat(testAcmeOrder.getNotBefore()).isEqualTo(DEFAULT_NOT_BEFORE);
        assertThat(testAcmeOrder.getNotAfter()).isEqualTo(DEFAULT_NOT_AFTER);
        assertThat(testAcmeOrder.getError()).isEqualTo(DEFAULT_ERROR);
        assertThat(testAcmeOrder.getFinalizeUrl()).isEqualTo(DEFAULT_FINALIZE_URL);
        assertThat(testAcmeOrder.getCertificateUrl()).isEqualTo(DEFAULT_CERTIFICATE_URL);
    }

    @Test
    @Transactional
    public void createAcmeOrderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = acmeOrderRepository.findAll().size();

        // Create the AcmeOrder with an existing ID
        acmeOrder.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAcmeOrderMockMvc.perform(post("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeOrder)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeOrder in the database
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkOrderIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeOrderRepository.findAll().size();
        // set the field null
        acmeOrder.setOrderId(null);

        // Create the AcmeOrder, which fails.

        restAcmeOrderMockMvc.perform(post("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeOrder)))
            .andExpect(status().isBadRequest());

        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = acmeOrderRepository.findAll().size();
        // set the field null
        acmeOrder.setStatus(null);

        // Create the AcmeOrder, which fails.

        restAcmeOrderMockMvc.perform(post("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeOrder)))
            .andExpect(status().isBadRequest());

        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAcmeOrders() throws Exception {
        // Initialize the database
        acmeOrderRepository.saveAndFlush(acmeOrder);

        // Get all the acmeOrderList
        restAcmeOrderMockMvc.perform(get("/api/acme-orders?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(acmeOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].orderId").value(hasItem(DEFAULT_ORDER_ID.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString().toLowerCase())))
            .andExpect(jsonPath("$.[*].expires").value(hasItem(DEFAULT_EXPIRES.toString())))
            .andExpect(jsonPath("$.[*].notBefore").value(hasItem(DEFAULT_NOT_BEFORE.toString())))
            .andExpect(jsonPath("$.[*].notAfter").value(hasItem(DEFAULT_NOT_AFTER.toString())))
            .andExpect(jsonPath("$.[*].error").value(hasItem(DEFAULT_ERROR)))
            .andExpect(jsonPath("$.[*].finalizeUrl").value(hasItem(DEFAULT_FINALIZE_URL)))
            .andExpect(jsonPath("$.[*].certificateUrl").value(hasItem(DEFAULT_CERTIFICATE_URL)));
    }
    
    @Test
    @Transactional
    public void getAcmeOrder() throws Exception {
        // Initialize the database
        acmeOrderRepository.saveAndFlush(acmeOrder);

        // Get the acmeOrder
        restAcmeOrderMockMvc.perform(get("/api/acme-orders/{id}", acmeOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(acmeOrder.getId().intValue()))
            .andExpect(jsonPath("$.orderId").value(DEFAULT_ORDER_ID.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString().toLowerCase()))
            .andExpect(jsonPath("$.expires").value(DEFAULT_EXPIRES.toString()))
            .andExpect(jsonPath("$.notBefore").value(DEFAULT_NOT_BEFORE.toString()))
            .andExpect(jsonPath("$.notAfter").value(DEFAULT_NOT_AFTER.toString()))
            .andExpect(jsonPath("$.error").value(DEFAULT_ERROR))
            .andExpect(jsonPath("$.finalizeUrl").value(DEFAULT_FINALIZE_URL))
            .andExpect(jsonPath("$.certificateUrl").value(DEFAULT_CERTIFICATE_URL));
    }

    @Test
    @Transactional
    public void getNonExistingAcmeOrder() throws Exception {
        // Get the acmeOrder
        restAcmeOrderMockMvc.perform(get("/api/acme-orders/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAcmeOrder() throws Exception {
        // Initialize the database
        acmeOrderService.save(acmeOrder);

        int databaseSizeBeforeUpdate = acmeOrderRepository.findAll().size();

        // Update the acmeOrder
        AcmeOrder updatedAcmeOrder = acmeOrderRepository.findById(acmeOrder.getId()).get();
        // Disconnect from session so that the updates on updatedAcmeOrder are not directly saved in db
        em.detach(updatedAcmeOrder);
        updatedAcmeOrder
            .orderId(UPDATED_ORDER_ID)
            .status(UPDATED_STATUS)
            .expires(UPDATED_EXPIRES)
            .notBefore(UPDATED_NOT_BEFORE)
            .notAfter(UPDATED_NOT_AFTER)
            .error(UPDATED_ERROR)
            .finalizeUrl(UPDATED_FINALIZE_URL)
            .certificateUrl(UPDATED_CERTIFICATE_URL);

        restAcmeOrderMockMvc.perform(put("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAcmeOrder)))
            .andExpect(status().isOk());

        // Validate the AcmeOrder in the database
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeUpdate);
        AcmeOrder testAcmeOrder = acmeOrderList.get(acmeOrderList.size() - 1);
        assertThat(testAcmeOrder.getOrderId()).isEqualTo(UPDATED_ORDER_ID);
        assertThat(testAcmeOrder.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAcmeOrder.getExpires()).isEqualTo(UPDATED_EXPIRES);
        assertThat(testAcmeOrder.getNotBefore()).isEqualTo(UPDATED_NOT_BEFORE);
        assertThat(testAcmeOrder.getNotAfter()).isEqualTo(UPDATED_NOT_AFTER);
        assertThat(testAcmeOrder.getError()).isEqualTo(UPDATED_ERROR);
        assertThat(testAcmeOrder.getFinalizeUrl()).isEqualTo(UPDATED_FINALIZE_URL);
        assertThat(testAcmeOrder.getCertificateUrl()).isEqualTo(UPDATED_CERTIFICATE_URL);
    }

    @Test
    @Transactional
    public void updateNonExistingAcmeOrder() throws Exception {
        int databaseSizeBeforeUpdate = acmeOrderRepository.findAll().size();

        // Create the AcmeOrder

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAcmeOrderMockMvc.perform(put("/api/acme-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(acmeOrder)))
            .andExpect(status().isBadRequest());

        // Validate the AcmeOrder in the database
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAcmeOrder() throws Exception {
        // Initialize the database
        acmeOrderService.save(acmeOrder);

        int databaseSizeBeforeDelete = acmeOrderRepository.findAll().size();

        // Delete the acmeOrder
        restAcmeOrderMockMvc.perform(delete("/api/acme-orders/{id}", acmeOrder.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findAll();
        assertThat(acmeOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
