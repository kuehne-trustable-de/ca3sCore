package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.ca3s.core.service.dto.RDNRestriction;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;

import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_TYPED_SAN;
import static org.mockito.Mockito.*;

class PipelineUtilTest {

	static final char[] password = {'1','2','3','4','5','6'};

    final String defaultKeySpec = "RSA-4096";

	static KeyPair keyPair;

	CryptoUtil cryptoUtil = new CryptoUtil();

    @Mock
    CertificateRepository certRepository = mock(CertificateRepository.class);

    @Mock
    CSRRepository csrRepository= mock(CSRRepository.class);

    @Mock
    CAConnectorConfigRepository caConnRepository= mock(CAConnectorConfigRepository.class);

    @Mock
    PipelineRepository pipelineRepository= mock(PipelineRepository.class);

    @Mock
    PipelineAttributeRepository pipelineAttRepository= mock(PipelineAttributeRepository.class);

    @Mock
    BPMNProcessInfoRepository bpmnPIRepository= mock(BPMNProcessInfoRepository.class);

    @Mock
    ProtectedContentRepository protectedContentRepository= mock(ProtectedContentRepository.class);

    @Mock
    ProtectedContentUtil protectedContentUtil= mock(ProtectedContentUtil.class);

    @Mock
    PreferenceUtil preferenceUtil= mock(PreferenceUtil.class);

    @Mock
    CertificateUtil certUtil= mock(CertificateUtil.class);
    @Mock
    AlgorithmRestrictionUtil algorithmRestrictionUtil= mock(AlgorithmRestrictionUtil.class);

    @Mock
    ConfigUtil configUtil= mock(ConfigUtil.class);

    TenantRepository tenantRepository = mock(TenantRepository.class);

    @Mock
    AuditService auditService= mock(AuditService.class);

    @Mock
    NotificationService notificationService = mock(NotificationService.class);

    @Mock
    RequestProxyConfigRepository requestProxyConfigRepository= mock(RequestProxyConfigRepository.class);

    @Mock
    AuditTraceRepository auditTraceRepository= mock(AuditTraceRepository.class);

    PipelineUtil pu;


	@BeforeAll
	public static void setUpBeforeClass() throws Exception {

		JCAManager.getInstance();

		keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }


    @BeforeEach
    public void setUp() {
        Preferences prefs = new Preferences();
        when(preferenceUtil.getPrefs(anyLong())).thenReturn(prefs);

        when(algorithmRestrictionUtil.isAlgorithmRestrictionsResolved((Pkcs10RequestHolder) any(), anyList())).thenReturn(true);
        pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);
    }

	@Test
	void testCheckPipelineRestrictions() throws GeneralSecurityException, IOException {

        List<String> messageList = new ArrayList<>();

		X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, O=trustable solutions, C=DE");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CN_Only = new PipelineView();
	    	pv_CN_Only.setRestriction_C(new RDNRestriction());
			pv_CN_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_L(new RDNRestriction());
			pv_CN_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_O(new RDNRestriction());
			pv_CN_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_S(new RDNRestriction());
			pv_CN_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(3, messageList.size(), "Expecting given number of messages");
	    }

		{
			PipelineView pv_CN_O_OU_C_Only = new PipelineView();
	    	pv_CN_O_OU_C_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU_C_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU_C_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU_C_Only, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

		}

		{
			PipelineView pv_CN_O_OU_Cn_Only = new PipelineView();
	    	pv_CN_O_OU_Cn_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CN_O_OU_Cn_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_Cn_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU_Cn_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_Cn_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_Cn_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU_Cn_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU_Cn_Only, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

		}
		{
			PipelineView pv_CN_O_OU_C01_Only = new PipelineView();
	    	pv_CN_O_OU_C01_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
	    	pv_CN_O_OU_C01_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C01_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU_C01_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C01_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C01_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU_C01_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU_C01_Only, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

		}

		{
			PipelineView pv_CN_O_OU_C0n_Only = new PipelineView();
	    	pv_CN_O_OU_C0n_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU_C0n_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C0n_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU_C0n_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C0n_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C0n_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU_C0n_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU_C0n_Only, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

		}

	}

	@Test
	void testCheckPipelineRestrictionsCardinality() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

		List<String> messageList = new ArrayList<>();

		X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CN_Only = new PipelineView();
	    	pv_CN_Only.setRestriction_C(new RDNRestriction());
			pv_CN_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_L(new RDNRestriction());
			pv_CN_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_O(new RDNRestriction());
			pv_CN_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_S(new RDNRestriction());
			pv_CN_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");
	    }

		{
			PipelineView pv_CN_O_OU1_C_Only = new PipelineView();
	    	pv_CN_O_OU1_C_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU1_C_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU1_C_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU1_C_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU1_C_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU1_C_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU1_C_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU1_C_Only, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");

		}

		{
			PipelineView pv_CN_O_OU_C_L1n = new PipelineView();
	    	pv_CN_O_OU_C_L1n.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU_C_L1n.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_L1n.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CN_O_OU_C_L1n.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU_C_L1n.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
	    	pv_CN_O_OU_C_L1n.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU_C_L1n.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU_C_L1n, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(2, messageList.size(), "Expecting given number of messages");

		}

		{
			PipelineView pv_CN_O_OU01_C_Only = new PipelineView();
	    	pv_CN_O_OU01_C_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU01_C_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU01_C_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU01_C_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU01_C_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
	    	pv_CN_O_OU01_C_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU01_C_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU01_C_Only, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");

		}

		{
			PipelineView pv_CN_O_OU0n_C_Only = new PipelineView();
	    	pv_CN_O_OU0n_C_Only.setRestriction_C(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU0n_C_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU0n_C_Only.setRestriction_L(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_O_OU0n_C_Only.setRestriction_O(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_O_OU0n_C_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
	    	pv_CN_O_OU0n_C_Only.setRestriction_S(new RDNRestriction());
			pv_CN_O_OU0n_C_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU0n_C_Only, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

		}


        {
            PipelineView pv_CN_O_OU1n_C_Only = new PipelineView();
            pv_CN_O_OU1n_C_Only.setRestriction_C(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
            pv_CN_O_OU1n_C_Only.setRestriction_CN(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_CN_O_OU1n_C_Only.setRestriction_L(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
            pv_CN_O_OU1n_C_Only.setRestriction_O(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_CN_O_OU1n_C_Only.setRestriction_OU(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
            pv_CN_O_OU1n_C_Only.setRestriction_S(new RDNRestriction());
            pv_CN_O_OU1n_C_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

            messageList.clear();
            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_O_OU1n_C_Only, p10ReqHolder, messageList);

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

        }
        {
            PipelineView pv_CN_Only = new PipelineView();
            pv_CN_Only.setRestriction_C(new RDNRestriction());
            pv_CN_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
            pv_CN_Only.setRestriction_CN(new RDNRestriction());
            pv_CN_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_SAN);
            pv_CN_Only.setRestriction_L(new RDNRestriction());
            pv_CN_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
            pv_CN_Only.setRestriction_O(new RDNRestriction());
            pv_CN_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_CN_Only.setRestriction_OU(new RDNRestriction());
            pv_CN_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
            pv_CN_Only.setRestriction_S(new RDNRestriction());
            pv_CN_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

            messageList.clear();
            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");

        }

	}

    @Test
    void testCheckPipelineRestrictionsOneCnOrSAN() throws GeneralSecurityException, IOException {

        PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

        List<String> messageList = new ArrayList<>();

        PipelineView pv_CN_Only = new PipelineView();
        pv_CN_Only.setRestriction_C(new RDNRestriction());
        pv_CN_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
        pv_CN_Only.setRestriction_CN(new RDNRestriction());
        pv_CN_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_SAN);
        pv_CN_Only.setRestriction_L(new RDNRestriction());
        pv_CN_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
        pv_CN_Only.setRestriction_O(new RDNRestriction());
        pv_CN_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
        pv_CN_Only.setRestriction_OU(new RDNRestriction());
        pv_CN_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
        pv_CN_Only.setRestriction_S(new RDNRestriction());
        pv_CN_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

        {
            X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");
            GeneralName[] sanArray = new GeneralName[2];
            sanArray[0] = new GeneralName(GeneralName.dNSName, "foo.com");
            sanArray[1] = new GeneralName(GeneralName.dNSName, "bar.org");
            PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                password,
                null,
                sanArray);

            Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
        }
        {
            X500Principal subject = new X500Principal("OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");
            GeneralName[] sanArray = new GeneralName[2];
            sanArray[0] = new GeneralName(GeneralName.dNSName, "foo.com");
            sanArray[1] = new GeneralName(GeneralName.dNSName, "bar.org");
            PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                password,
                null,
                sanArray);

            messageList.clear();

            Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
        }
        {
            X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");
            PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                password,
                null);

            messageList.clear();

            Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
        }
        {
            X500Principal subject = new X500Principal("CN=trustable.eu,CN=trustable.org, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");
            PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                password,
                null);

            messageList.clear();

            Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertFalse(bResult, "Expect to fail ");
            Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");
        }
        {
            X500Principal subject = new X500Principal("OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");
            PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                password,
                null);

            messageList.clear();

            Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

            Assertions.assertFalse(bResult, "Expect to fail ");
            Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");
        }
    }

    @Test
	void testCheckPipelineRestrictionsConstantValue() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

		List<String> messageList = new ArrayList<>();
		X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CN_Only = new PipelineView();
	    	pv_CN_Only.setRestriction_C(new RDNRestriction());
			pv_CN_Only.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_CN_Only.getRestriction_C().setRegEx("foo");
            pv_CN_Only.getRestriction_C().setRegExMatch(true);

            pv_CN_Only.setRestriction_CN(new RDNRestriction());
			pv_CN_Only.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_L(new RDNRestriction());
			pv_CN_Only.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CN_Only.setRestriction_O(new RDNRestriction());
			pv_CN_Only.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CN_Only.setRestriction_OU(new RDNRestriction());
			pv_CN_Only.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CN_Only.setRestriction_S(new RDNRestriction());
			pv_CN_Only.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_C_de = new PipelineView();
	    	pv_C_de.setRestriction_C(new RDNRestriction());
			pv_C_de.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_C_de.getRestriction_C().setRegEx("(?i)de");
            pv_C_de.getRestriction_C().setRegExMatch(true);

            pv_C_de.setRestriction_CN(new RDNRestriction());
			pv_C_de.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_L(new RDNRestriction());
			pv_C_de.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_C_de.setRestriction_O(new RDNRestriction());
			pv_C_de.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_OU(new RDNRestriction());
			pv_C_de.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_C_de.setRestriction_S(new RDNRestriction());
			pv_C_de.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_C_de, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_C_de = new PipelineView();
	    	pv_C_de.setRestriction_C(new RDNRestriction());
			pv_C_de.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_CN(new RDNRestriction());
			pv_C_de.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_C_de.getRestriction_CN().setRegEx("trustable.eu");
            pv_C_de.getRestriction_CN().setRegExMatch(true);

	    	pv_C_de.setRestriction_L(new RDNRestriction());
			pv_C_de.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_C_de.setRestriction_O(new RDNRestriction());
			pv_C_de.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_OU(new RDNRestriction());
			pv_C_de.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_C_de.setRestriction_S(new RDNRestriction());
			pv_C_de.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_C_de, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_C_de = new PipelineView();
	    	pv_C_de.setRestriction_C(new RDNRestriction());
			pv_C_de.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_CN(new RDNRestriction());
			pv_C_de.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_L(new RDNRestriction());
			pv_C_de.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_C_de.setRestriction_O(new RDNRestriction());
			pv_C_de.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_OU(new RDNRestriction());
			pv_C_de.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
            pv_C_de.getRestriction_OU().setRegEx("ca3s");
            pv_C_de.getRestriction_OU().setRegExMatch(true);

            pv_C_de.setRestriction_S(new RDNRestriction());
			pv_C_de.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_C_de, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(2, messageList.size(), "Expecting given number of messages");
	    }

	}

	@Test
	void testCheckPipelineRestrictionsRegExp() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

		List<String> messageList = new ArrayList<>();
		X500Principal subject = new X500Principal("CN=trustable.eu, OU=ca3s, OU=foo, OU=bar, O=trustable solutions, C=DE");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_CRegexBroken.getRestriction_C().setRegEx("*.foo");
			pv_CRegexBroken.getRestriction_C().setRegExMatch(true);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_L(new RDNRestriction());
			pv_CRegexBroken.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CRegexBroken.setRestriction_O(new RDNRestriction());
			pv_CRegexBroken.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_OU(new RDNRestriction());
			pv_CRegexBroken.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CRegexBroken.setRestriction_S(new RDNRestriction());
			pv_CRegexBroken.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

            boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}


			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(1, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_L(new RDNRestriction());
			pv_CRegexBroken.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CRegexBroken.setRestriction_O(new RDNRestriction());
			pv_CRegexBroken.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_OU(new RDNRestriction());
			pv_CRegexBroken.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
			pv_CRegexBroken.getRestriction_OU().setRegEx("(ca3s|foo|bar)");
			pv_CRegexBroken.getRestriction_OU().setRegExMatch(true);
	    	pv_CRegexBroken.setRestriction_S(new RDNRestriction());
			pv_CRegexBroken.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_CRegexBroken.getRestriction_CN().setRegEx(".*\\.eu");
			pv_CRegexBroken.getRestriction_CN().setRegExMatch(true);
	    	pv_CRegexBroken.setRestriction_L(new RDNRestriction());
			pv_CRegexBroken.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CRegexBroken.setRestriction_O(new RDNRestriction());
			pv_CRegexBroken.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_OU(new RDNRestriction());
			pv_CRegexBroken.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CRegexBroken.setRestriction_S(new RDNRestriction());
			pv_CRegexBroken.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_CRegexBroken.getRestriction_CN().setRegEx("trustable.*");
			pv_CRegexBroken.getRestriction_CN().setRegExMatch(true);
	    	pv_CRegexBroken.setRestriction_L(new RDNRestriction());
			pv_CRegexBroken.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
	    	pv_CRegexBroken.setRestriction_O(new RDNRestriction());
			pv_CRegexBroken.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_OU(new RDNRestriction());
			pv_CRegexBroken.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
	    	pv_CRegexBroken.setRestriction_S(new RDNRestriction());
			pv_CRegexBroken.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

        {
            PipelineView pv_TemplateSet = new PipelineView();
            pv_TemplateSet.setRestriction_C(new RDNRestriction());
            pv_TemplateSet.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_TemplateSet.setRestriction_CN(new RDNRestriction());
            pv_TemplateSet.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_TemplateSet.getRestriction_CN().setContentTemplate("trustable.*");
            pv_TemplateSet.setRestriction_L(new RDNRestriction());
            pv_TemplateSet.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
            pv_TemplateSet.setRestriction_O(new RDNRestriction());
            pv_TemplateSet.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
            pv_TemplateSet.setRestriction_OU(new RDNRestriction());
            pv_TemplateSet.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ONE_OR_MANY);
            pv_TemplateSet.setRestriction_S(new RDNRestriction());
            pv_TemplateSet.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


            messageList.clear();
            boolean bResult = pu.isPipelineRestrictionsResolved(pv_TemplateSet, p10ReqHolder, messageList);

            for(String msg:messageList) {
                System.out.println(msg);
            }

            Assertions.assertTrue(bResult, "Expect to pass ");
            Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
        }

    }

	@Test
	void testCheckPipelineRestrictionsIPHasSubject() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

        List<String> messageList = new ArrayList<>();
		X500Principal subject = new X500Principal("CN=trustable.eu");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CIPNotAllowed = new PipelineView();
	    	pv_CIPNotAllowed.setRestriction_CN(new RDNRestriction());
			pv_CIPNotAllowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPNotAllowed.setIpAsSubjectAllowed(false);

			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPNotAllowed, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSubjectAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

		subject = new X500Principal("CN=127.0.0.1");
	    p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null);

	    p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req);

	    {
			PipelineView pv_CIPNotAllowed = new PipelineView();
	    	pv_CIPNotAllowed.setRestriction_CN(new RDNRestriction());
			pv_CIPNotAllowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPNotAllowed.setIpAsSubjectAllowed(false);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPNotAllowed, p10ReqHolder, messageList);

			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(2, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSubjectAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	}


	@Test
	void testCheckPipelineRestrictionsIPHasSAN() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

		List<String> messageList = new ArrayList<>();
		X500Principal subject = new X500Principal("CN=trustable.eu");

		GeneralName[] gnArr = new GeneralName[2];
		gnArr[0] = new GeneralName(GeneralName.dNSName, "trustable.eu");
		gnArr[1] = new GeneralName(GeneralName.iPAddress, "127.0.0.1");

	    PKCS10CertificationRequest p10Req = CryptoUtil.getCsr(subject,
	    		keyPair.getPublic(),
	    		keyPair.getPrivate(),
                password,
                null,
                gnArr);

	    // tweak ... to pem and back ...
	    PKCS10CertificationRequest p10Req2 = cryptoUtil.convertPemToPKCS10CertificationRequest(CryptoUtil.pkcs10RequestToPem( p10Req));

	    Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(p10Req2);
    	Set<GeneralName> gNameSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());
		System.out.println("-- #" + gNameSet.size() + " SANs present");

	    {
			PipelineView pv_CIPNotAllowed = new PipelineView();
	    	pv_CIPNotAllowed.setRestriction_CN(new RDNRestriction());
			pv_CIPNotAllowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPNotAllowed.setIpAsSANAllowed(false);

			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPNotAllowed, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}


			Assertions.assertFalse(bResult, "Expect to fail ");
			Assertions.assertEquals(2, messageList.size(), "Expecting given number of messages");
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSANAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			Assertions.assertTrue(bResult, "Expect to pass ");
			Assertions.assertEquals(0, messageList.size(), "Expecting given number of messages");
	    }

	}


    @Test
    void testUsedInDomainOnly() throws IOException {
        PipelineUtil pu = new PipelineUtil(certRepository, csrRepository, caConnRepository, pipelineRepository, pipelineAttRepository, bpmnPIRepository, protectedContentRepository, protectedContentUtil, preferenceUtil, certUtil, algorithmRestrictionUtil, configUtil, auditService, auditTraceRepository, notificationService, tenantRepository, requestProxyConfigRepository, defaultKeySpec);

        Assertions.assertTrue(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:localhost"}),
                buildPkcs10RequestHolder("localhost", new String[0]),
                new ArrayList<>()),
            "domain expected to match");

        Assertions.assertTrue(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:localhost"}),
                buildPkcs10RequestHolder("localhost", new String[]{"localhost"}),
                new ArrayList<>()),
            "domain expected to match");

        Assertions.assertTrue(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:localhost", "DNS:ca3s.org"}),
                buildPkcs10RequestHolder("localhost", new String[]{"localhost", "ca3s.org"}),
                new ArrayList<>()),
            "domain expected to match");

        Assertions.assertTrue(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:localhost", "DNS:ca3s.org"}),
                buildPkcs10RequestHolder("localhost", new String[]{"ca3s.org"}),
                new ArrayList<>()),
            "domain expected to match");

        Assertions.assertTrue(pu.usedInDomainOnly(buildCSRPage(new String[]{"IP:127.0.0.1", "DNS:ca3s.org"}),
                buildPkcs10RequestHolder("127.0.0.1", new String[]{"ca3s.org"}),
                new ArrayList<>()),
            "domain expected to match");

        Assertions.assertFalse(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:ca3s.org"}),
                buildPkcs10RequestHolder("localhost", new String[]{"ca3s.org"}),
                new ArrayList<>()),
            "domain expected to fail");

        Assertions.assertFalse(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:foo.org", "DNS:ca3s.org"}),
                buildPkcs10RequestHolder("localhost", new String[]{"ca3s.org"}),
                new ArrayList<>()),
            "domain expected to fail");

        Assertions.assertFalse(pu.usedInDomainOnly(buildCSRPage(new String[]{"DNS:foo.org", "DNS:ca3s.org"}),
                buildPkcs10RequestHolder("foo.org", new String[]{}),
                new ArrayList<>()),
            "domain expected to fail");
    }

    Pkcs10RequestHolder buildPkcs10RequestHolder(String cn, String[] sanArr) throws IOException {
        Pkcs10RequestHolder p10ReqHolder = mock(Pkcs10RequestHolder.class);

        List<org.bouncycastle.asn1.pkcs.Attribute> attributeList = new ArrayList<>();

        for(String san: sanArr) {
            Attribute attribute = buildAttribute(san);
            attributeList.add(attribute);
        }
        when(p10ReqHolder.getReqAttributes()).thenReturn(attributeList.toArray(new org.bouncycastle.asn1.pkcs.Attribute[0]));

        X500NameBuilder x500NameBuilder = new X500NameBuilder(X500Name.getDefaultStyle());
        x500NameBuilder.addRDN(BCStyle.CN, cn);
        when(p10ReqHolder.getSubjectRDNs()).thenReturn(x500NameBuilder.build().getRDNs());

        return p10ReqHolder;
    }

    @NotNull
    private static Attribute buildAttribute(String host) throws IOException {
        // Create the extensions object and add it as an attribute
        Vector oids = new Vector();
        Vector values = new Vector();

        oids.add(X509Extensions.SubjectAlternativeName);
        GeneralNames subjectAltName = new GeneralNames(new GeneralName(GeneralName.dNSName, host));
        values.add(new X509Extension(false, new DEROctetString(subjectAltName)));
        X509Extensions extensions = new X509Extensions(oids, values);
        Attribute attribute =
            new Attribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                new DERSet(extensions));
        return attribute;
    }

    Page<CSR> buildCSRPage(String[] typedSANArr) {
        Page<CSR> csrPage = mock(Page.class);
        List<CSR> csrList = new ArrayList<>();
        CSR existingCSR = mock(CSR.class);
        Set<CsrAttribute> csrAttributeList = new HashSet<>();

        for( String typedSAN: typedSANArr) {
            CsrAttribute csrAttribute = new CsrAttribute();
            csrAttribute.setName(ATTRIBUTE_TYPED_SAN);
            csrAttribute.setValue(typedSAN);
            csrAttributeList.add(csrAttribute);
        }
        when(existingCSR.getCsrAttributes()).thenReturn(csrAttributeList);

        csrList.add(existingCSR);
        when(csrPage.getContent()).thenReturn(csrList);

        return csrPage;
    }

}
