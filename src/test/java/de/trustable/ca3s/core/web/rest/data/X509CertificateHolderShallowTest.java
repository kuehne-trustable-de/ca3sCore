package de.trustable.ca3s.core.web.rest.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.GeneralSecurityException;

import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.JCAManager;


class X509CertificateHolderShallowTest {

	public static final String CertAsPem = "\r\n" +
			"-----BEGIN CERTIFICATE-----\r\n" +
			"MIIGcjCCBVqgAwIBAgISAwwHqLfPLbfM2U7/47pzmhWQMA0GCSqGSIb3DQEBCwUA\r\n" +
			"MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\r\n" +
			"ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xODEwMjkxMjMxMjdaFw0x\r\n" +
			"OTAxMjcxMjMxMjdaMBcxFTATBgNVBAMTDHRydXN0YWJsZS5ldTCCASIwDQYJKoZI\r\n" +
			"hvcNAQEBBQADggEPADCCAQoCggEBAK7bRxIOWl4Hg9AciV5b5queKHziy75cGMRt\r\n" +
			"gvd8aSmjHVP1U+ygOSbn40swwl8nJjPUpJU2d4Vi5Q9PPGLCFG6488yddsurdUmQ\r\n" +
			"TGrtYbo+GswIDDK62/riyZYSfx11Ul8U66TPizLy2blPPV47kTn29yuYz/y+sv+1\r\n" +
			"tE/V5KO2QEL79W4DAXwXkJmNDT405l06tP3XNz6eWAP7TEnulypRl6g1PkaIrKeb\r\n" +
			"KQKdMxekT+2a49J0sp1mazI431J0gxQ1djuNxeY3x4a2BKq/ZcyqcLfULmOA2yI2\r\n" +
			"0IhTxpaxdLKK8iXuh3FP0WAXYIxTFtCWaBIpvEJsChAUPUsHROUCAwEAAaOCA4Mw\r\n" +
			"ggN/MA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH\r\n" +
			"AwIwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQUI1n9a6qiuiH6Jq+rvxKEw3R+DYAw\r\n" +
			"HwYDVR0jBBgwFoAUqEpqYwR93brm0Tm3pkVl7/Oo7KEwbwYIKwYBBQUHAQEEYzBh\r\n" +
			"MC4GCCsGAQUFBzABhiJodHRwOi8vb2NzcC5pbnQteDMubGV0c2VuY3J5cHQub3Jn\r\n" +
			"MC8GCCsGAQUFBzAChiNodHRwOi8vY2VydC5pbnQteDMubGV0c2VuY3J5cHQub3Jn\r\n" +
			"LzCBhQYDVR0RBH4wfIIQZGV2LnRydXN0YWJsZS5ldYIQZ2l0LnRydXN0YWJsZS5l\r\n" +
			"dYIcbWFpbC50cnVzdGFibGUuZG9udGV4aXN0Lm9yZ4IRbWFpbC50cnVzdGFibGUu\r\n" +
			"ZXWCF3RydXN0YWJsZS5kb250ZXhpc3Qub3Jnggx0cnVzdGFibGUuZXUwgf4GA1Ud\r\n" +
			"IASB9jCB8zAIBgZngQwBAgEwgeYGCysGAQQBgt8TAQEBMIHWMCYGCCsGAQUFBwIB\r\n" +
			"FhpodHRwOi8vY3BzLmxldHNlbmNyeXB0Lm9yZzCBqwYIKwYBBQUHAgIwgZ4MgZtU\r\n" +
			"aGlzIENlcnRpZmljYXRlIG1heSBvbmx5IGJlIHJlbGllZCB1cG9uIGJ5IFJlbHlp\r\n" +
			"bmcgUGFydGllcyBhbmQgb25seSBpbiBhY2NvcmRhbmNlIHdpdGggdGhlIENlcnRp\r\n" +
			"ZmljYXRlIFBvbGljeSBmb3VuZCBhdCBodHRwczovL2xldHNlbmNyeXB0Lm9yZy9y\r\n" +
			"ZXBvc2l0b3J5LzCCAQQGCisGAQQB1nkCBAIEgfUEgfIA8AB2ACk8UZZUyDlluqpQ\r\n" +
			"/FgH1Ldvv1h6KXLcpMMM9OVFR/R4AAABZsAFpZgAAAQDAEcwRQIhAMNBIIpH8dv3\r\n" +
			"Uy/Xrl2dA2rBBB22UhHmnqtJRrn9u7zjAiB7Sqisqbgp/6kovbwroeABZgTs2Ffr\r\n" +
			"Q3CyqPIAH8AVWQB2AHR+2oMxrTMQkSGcziVPQnDCv/1eQiAIxjc1eeYQe8xWAAAB\r\n" +
			"ZsAFppoAAAQDAEcwRQIhANNGW8l3nuf01N8aDM4ncIQq4asqyMqxYbccrdmCq9Gg\r\n" +
			"AiBJ6AWOvf7NAOoT1xZq2of6k3bFPl62MnSwaWcgQnHzqzANBgkqhkiG9w0BAQsF\r\n" +
			"AAOCAQEASlGkEPMRFi6jMMga0PpOlIOdQ1vDeoVZU2ixBY1vXcvYSLNFaF3ZrBR+\r\n" +
			"fNKPpdvTCi3rLOtxKZqMSbyUDyqVXZv4mUAVCVmwmGEB2WyLfoRjEu3oPoIODAby\r\n" +
			"uALcjWeZtiApwxo6rEOlQgXSps+/QXisgkjO0lCwhxpcDJ7HaFYNpFAeCWRYNsh5\r\n" +
			"CAYtG+snYWz2R0zv/Rz0kReD60xheSAmhqDCvJBy4Pv3VjpE94x5aFJXXMW6OV5M\r\n" +
			"dnNlsWiWhbSBUi6pO0qHeIhfQhfgMiztZDKGYFZ8yGq06S2Kzqcl2IRA6t147cB4\r\n" +
			"ggXivrtcBmP/OSi2MAxh6CQclz8lJA==\r\n" +
			"-----END CERTIFICATE-----\r\n";

	@BeforeAll
	public static void setUpBeforeClass() {
		JCAManager.getInstance();
	}

	@Test
	void testX509CertificateHolderShallow() throws GeneralSecurityException {

		X509CertificateHolder holder = CertificateUtil.convertPemToCertificateHolder(CertAsPem);

		X509CertificateHolderShallow x509Holder = new X509CertificateHolderShallow(holder);

		assertEquals( "CN=trustable.eu", x509Holder.getSubject());
		assertEquals( "C=US,O=Let's Encrypt,CN=Let's Encrypt Authority X3", x509Holder.getIssuer());
		assertEquals( "265430426828270065898530867527273204553104", x509Holder.getSerial());
		assertEquals( "E96gS+Zf0kEztTa8sAsnp0S7Qqo=", x509Holder.getFingerprint());
		assertEquals( "V3", x509Holder.getType());
		assertEquals( "2018-10-29T12:31:27", x509Holder.getValidFrom().toString());
		assertEquals( "2019-01-27T12:31:27", x509Holder.getValidTo().toString());


		assertEquals( 6, x509Holder.getSans().length);
		for( String ext : x509Holder.getExtensions() ) {
			System.out.println("ext:" + ext);
		}
	}

}
