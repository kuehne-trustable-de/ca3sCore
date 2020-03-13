package de.trustable.ca3s.core.service.util;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;

public class CertificateUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JCAManager.getInstance();
	}


	public static final String testCert = 
			"-----BEGIN CERTIFICATE-----\r\n" + 
			"MIIEWzCCA0OgAwIBAgITFQAAAAaNqEngwzk/1QAAAAAABjANBgkqhkiG9w0BAQsF\r\n" + 
			"ADAcMRowGAYDVQQDExFXUy0yMDE5LUlzc3VpbmdDQTAeFw0xOTA0MTgyMjIwNTda\r\n" + 
			"Fw0yMDA0MTgxMjA4NDJaMIGqMQswCQYDVQQGEwJERTEPMA0GA1UECBMGQmVybGlu\r\n" + 
			"MQ8wDQYDVQQHEwZCZXJsaW4xEDAOBgNVBAoTB0VQQi1EZXYxFzAVBgNVBAsTDlNS\r\n" + 
			"RCBBdXRvbWF0aW9uMSMwIQYDVQQDFBpzcmQuYXV0b21hdGVyQGVwb3N0LWRldi5k\r\n" + 
			"ZTEpMCcGCSqGSIb3DQEJARYac3JkLmF1dG9tYXRlckBlcG9zdC1kZXYuZGUwggEi\r\n" + 
			"MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5JemWeMDt5ZmWA35yKOVtUaQM\r\n" + 
			"5B9q10DwslvA+XmNZ+2qZfYf/QAdHatzg9VY8JyE2B+/HgVXVT3KDcxnhu916f85\r\n" + 
			"1qPzQb08uYpH5oHWaq0mw7m6s63ih3mxfU7jp5BQTnSCyrhibDQO2l/tqmz3gtrX\r\n" + 
			"GU23l8sndJaOCYB5Q/8SW8oaHJZ1XGoy3mxDm5xheQ9/CsW31eNTEd4vfUR+CSnj\r\n" + 
			"iIy/OiErOxTEr234XR/j3yec7EMCX/sUYcTXtu2BGSm4CIbn6XFZT1xTm8kgPGP6\r\n" + 
			"bOyQNDhiACEQbtPXW+LplXsgj69TqfmAbPUV79CcsgtNS41FxSjQKUELCrxjAgMB\r\n" + 
			"AAGjggEFMIIBATAdBgNVHQ4EFgQU372t17fGYgJD1hFuk51vnoiTKPMwHwYDVR0j\r\n" + 
			"BBgwFoAU9kGKdLF7DlDoyUcbczPShP6wexgwSgYDVR0fBEMwQTA/oD2gO4Y5Zmls\r\n" + 
			"ZTovLy8vV0lOLUo0RUZDU0FSRU45L0NlcnRFbnJvbGwvV1MtMjAxOS1Jc3N1aW5n\r\n" + 
			"Q0EuY3JsMGUGCCsGAQUFBwEBBFkwVzBVBggrBgEFBQcwAoZJZmlsZTovLy8vV0lO\r\n" + 
			"LUo0RUZDU0FSRU45L0NlcnRFbnJvbGwvV0lOLUo0RUZDU0FSRU45X1dTLTIwMTkt\r\n" + 
			"SXNzdWluZ0NBLmNydDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQAg\r\n" + 
			"i0AyOIqTDLXkPDX1FGOsNYLFVt7A7b2/tz5TUu0YtaEX39VnmSYsrAZRGjx4ri9b\r\n" + 
			"yFg1nJrITAdggGzqwMDkFoTwoYIgYqoeFNwl6vY/KlnRGJpE/yI3GMNqaA7UFRht\r\n" + 
			"IXpQB3BllMTd/EATBowkQpDR41QkqiChaBCgDxD1hCbgot8lCdub3Ltya7i2y3ug\r\n" + 
			"6o2KIc9bfl8kOLtYyYi3CVZW4OC8d6LV1wX6Btp8RDRtr9OghnseiuJt9u3YIoT5\r\n" + 
			"C0t7UYXUOmRUzxZ2thh7CpC7NGR90PejgQFUUnL7JJFmPoe9S0Vo0uJTGefdQZYs\r\n" + 
			"msjEJrorUH1VkmI3IaG/\r\n" + 
			"-----END CERTIFICATE-----";
	
	@Test
	public void testAKIandSKIGeneration() throws GeneralSecurityException {

		X509Certificate x509Cert = CryptoUtil.convertPemToCertificate(testCert);
		assertNotNull(x509Cert);
		
		JcaX509ExtensionUtils util = new JcaX509ExtensionUtils();
		
		SubjectKeyIdentifier ski = util.createSubjectKeyIdentifier(x509Cert.getPublicKey());
		String b46Ski = Base64.encodeBase64String(ski.getKeyIdentifier());
		assertNotNull(b46Ski);
		
		AuthorityKeyIdentifier aki = util.createAuthorityKeyIdentifier(x509Cert.getPublicKey());
		String b46Aki = Base64.encodeBase64String(aki.getKeyIdentifier());
		assertNotNull(b46Aki);
		assertEquals(b46Ski, b46Aki);
	}

	@Test
	public void testGetKeyLength() throws GeneralSecurityException {

		X509Certificate x509Cert = CryptoUtil.convertPemToCertificate(testCert);
		assertNotNull(x509Cert);
		
		int keyLength = CertificateUtil.getAlignedKeyLength(x509Cert.getPublicKey());
		assertEquals(2048, keyLength);
		
	}
	
	@Test
	public void testFileUrl() throws MalformedURLException {
		
		File dir = new File("c:/tmp");
		assertTrue( dir.exists() && dir.canRead());


	}
}
