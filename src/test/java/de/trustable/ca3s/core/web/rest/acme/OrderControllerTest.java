package de.trustable.ca3s.core.web.rest.acme;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.acme.OrderResponse;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.InvalidNameException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OrderControllerTest {

    OrderController orderController;

    @Mock
    AcmeOrderRepository acmeOrderRepository = mock(AcmeOrderRepository.class);
    @Mock
    CSRRepository csrRepository = mock(CSRRepository.class);

    @Mock
    JwtUtil jwtUtil = mock(JwtUtil.class);

    @Mock
    CryptoUtil cryptoUtil = mock(CryptoUtil.class);

    @Mock
    CertificateUtil certUtil = mock(CertificateUtil.class);

    @Mock
    CertificateProcessingUtil cpUtil = mock(CertificateProcessingUtil.class);

    @Mock
    PipelineUtil pipelineUtil = mock(PipelineUtil.class);

    @Mock
    AuditService auditService = mock(AuditService.class);

    @Mock
    CertificateAsyncUtil certificateAsyncUtil = mock(CertificateAsyncUtil.class);

    @Mock
    ReplacementCandidateUtil replacementCandidateUtil = mock(ReplacementCandidateUtil.class);

    AcmeOrder acmeOrder = new AcmeOrder();

    final static String SAMPLE_ORDER_URL = "http://foo.test.acme:443/acme/realm/order/1234567";
    URL orderUrl = new URL(SAMPLE_ORDER_URL);

    final static long ORDER_ID = 1234567;

    public OrderControllerTest() throws MalformedURLException {
    }

    @BeforeEach
    public void setUp() {

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRequestURI(orderUrl.toString());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        orderController = new OrderController(acmeOrderRepository,
                csrRepository, jwtUtil,
            cryptoUtil,
            certUtil,
            cpUtil,
            pipelineUtil,
            auditService,
        true, // finalizeLocationBackwardCompat
        true, // iterateAuthenticationsOnGet
        0,
        0,
        0, replacementCandidateUtil, certificateAsyncUtil);

    }

    @Test
	public void testOrderResponse() throws InvalidNameException {

        HttpHeaders additionalHeaders = new HttpHeaders();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(SAMPLE_ORDER_URL);
        UriComponentsBuilder baseUriBuilder = uriComponentsBuilder.path("/../..");

        acmeOrder.setOrderId(ORDER_ID);

        ResponseEntity<OrderResponse> orderResponse = orderController.buildOrderResponse(additionalHeaders,
            acmeOrder,
            baseUriBuilder,
            true);

        assertTrue("location header present ", orderResponse.getHeaders().getLocation() != null);
        assertEquals("location header value expected ", orderUrl.toString(), orderResponse.getHeaders().getLocation().toString());
	}

}
