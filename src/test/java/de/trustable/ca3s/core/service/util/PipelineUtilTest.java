package de.trustable.ca3s.core.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.RDNRestriction;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import de.trustable.util.Pkcs10RequestHolder;

class PipelineUtilTest {

	static final char[] password = {'1','2','3','4','5','6'};

	static KeyPair keyPair;

	CryptoUtil cryptoUtil = new CryptoUtil();

	PipelineUtil pu = new PipelineUtil();


	@BeforeAll
	public static void setUpBeforeClass() throws Exception {

		JCAManager.getInstance();

		keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

	}

	@Test
	void testCheckPipelineRestrictions() throws GeneralSecurityException, IOException {


		List<String> messageList = new ArrayList<String>();;
//		Pkcs10RequestHolder p10ReqHolder;

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


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 3, messageList.size());
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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

		}

	}

	@Test
	void testCheckPipelineRestrictionsCardinality() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil();

		List<String> messageList = new ArrayList<String>();;
//		Pkcs10RequestHolder p10ReqHolder;

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


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());
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

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());

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

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 2, messageList.size());

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

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());

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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());

		}

	}

	@Test
	void testCheckPipelineRestrictionsConstantValue() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil();

		List<String> messageList = new ArrayList<String>();;
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
			pv_CN_Only.getRestriction_C().setContentTemplate("foo");
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


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CN_Only, p10ReqHolder, messageList);

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());
	    }

	    {
			PipelineView pv_C_de = new PipelineView();
	    	pv_C_de.setRestriction_C(new RDNRestriction());
			pv_C_de.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_C_de.getRestriction_C().setContentTemplate("de");
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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	    {
			PipelineView pv_C_de = new PipelineView();
	    	pv_C_de.setRestriction_C(new RDNRestriction());
			pv_C_de.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_C_de.setRestriction_CN(new RDNRestriction());
			pv_C_de.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_C_de.getRestriction_CN().setContentTemplate("trustable.eu");
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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
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
			pv_C_de.getRestriction_OU().setContentTemplate("ca3s");
	    	pv_C_de.setRestriction_S(new RDNRestriction());
			pv_C_de.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_C_de, p10ReqHolder, messageList);

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 2, messageList.size());
	    }

	}

	@Test
	void testCheckPipelineRestrictionsRegExp() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil();

		List<String> messageList = new ArrayList<String>();;
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
			pv_CRegexBroken.getRestriction_C().setContentTemplate("*.foo");
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


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}


			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());
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
			pv_CRegexBroken.getRestriction_OU().setContentTemplate("(ca3s|foo|bar)");
			pv_CRegexBroken.getRestriction_OU().setRegExMatch(true);
	    	pv_CRegexBroken.setRestriction_S(new RDNRestriction());
			pv_CRegexBroken.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);


			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CRegexBroken, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_CRegexBroken.getRestriction_CN().setContentTemplate(".*\\.eu");
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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	    {
			PipelineView pv_CRegexBroken = new PipelineView();
	    	pv_CRegexBroken.setRestriction_C(new RDNRestriction());
			pv_CRegexBroken.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
	    	pv_CRegexBroken.setRestriction_CN(new RDNRestriction());
			pv_CRegexBroken.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
			pv_CRegexBroken.getRestriction_CN().setContentTemplate("trustable.*");
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

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	}

	@Test
	void testCheckPipelineRestrictionsIPHasSubject() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil();

		List<String> messageList = new ArrayList<String>();;
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

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPNotAllowed, p10ReqHolder, messageList);

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSubjectAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
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

			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSubjectAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }

	}


	@Test
	void testCheckPipelineRestrictionsIPHasSAN() throws GeneralSecurityException, IOException {

		PipelineUtil pu = new PipelineUtil();

		List<String> messageList = new ArrayList<String>();;
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

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPNotAllowed, p10ReqHolder, messageList);

			for(String msg:messageList) {
				System.out.println(msg);
			}


			assertFalse("Expect to fail ", bResult);
			assertEquals("Expecting given number of messages", 1, messageList.size());
	    }

	    {
			PipelineView pv_CIPallowed = new PipelineView();
	    	pv_CIPallowed.setRestriction_CN(new RDNRestriction());
			pv_CIPallowed.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);

			pv_CIPallowed.setIpAsSANAllowed(true);

			messageList.clear();
			boolean bResult = pu.isPipelineRestrictionsResolved(pv_CIPallowed, p10ReqHolder, messageList);

			assertTrue("Expect to pass ", bResult);
			assertEquals("Expecting given number of messages", 0, messageList.size());
	    }


	}


}
