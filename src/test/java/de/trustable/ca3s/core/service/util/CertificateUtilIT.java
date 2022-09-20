package de.trustable.ca3s.core.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;

import javax.naming.InvalidNameException;

@SpringBootTest(classes = Ca3SApp.class)
public class CertificateUtilIT {

    @Autowired
    private CertificateUtil certificateUtil;


	@BeforeAll
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

	public static final String testCertWithSAN =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"MIICozCCAgygAwIBAgIDDpYMMA0GCSqGSIb3DQEBBQUAMFgxCzAJBgNVBAYTAkRF\r\n" +
	"MRIwEAYDVQQKEwlUcnVzdGFibGUxFzAVBgNVBAsTDlNpZ25pbmdTZXJ2aWNlMRww\r\n" +
	"GgYDVQQDDBNUcnVzdGFibGUgUlNBIENBICMxMB4XDTA3MTAyMjA4MDk0M1oXDTA5\r\n" +
	"MTAyMjA4MDk0M1owXTELMAkGA1UEBhMCREUxEjAQBgNVBAoTCVRydXN0YWJsZTEX\r\n" +
	"MBUGA1UECxMOU2lnbmluZ1NlcnZpY2UxITAfBgNVBAMTGE1haWwgc2lnbmluZyBj\r\n" +
	"ZXJ0aWZpY2F0ZTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA0fMVUpkyEQeE\r\n" +
	"8jjzLU/c9IgHK8mZ5DbLXSC4y4ACrQLqf3T10xG0ih/z6TbUXj57igD7+woZyMOI\r\n" +
	"ZQfdLuhQmnAuGCeXZd4JP1f3UXv2yHDNGVKHDzg0pgwwVRWfCxceALyHZ1r7yI6B\r\n" +
	"Qb6E09Eomuzx2j7HMEghCtL/V+lOtacCAwEAAaN2MHQwCwYDVR0PBAQDAgTwMB0G\r\n" +
	"A1UdDgQWBBRsTUEP3S9IkqQPk/4Cxc9GDakMpTAJBgNVHRMEAjAAMB8GA1UdIwQY\r\n" +
	"MBaAFIHOPmhi/nseyX8eRGbIun3t8EtwMBoGA1UdEQQTMBGBD2NhQHRydXN0YWJs\r\n" +
	"ZS5kZTANBgkqhkiG9w0BAQUFAAOBgQAyPo3sCPqCxW5yYuK6Nr2uk76PooTfisKM\r\n" +
	"NHlTTJxsGoIH1ze52fiVT+Oa6Fqh8jXxD7rbSV9CcJok3DtxhjvPPujytNS1t77e\r\n" +
	"XA8rblhuGWdbHovjutIOteumoaPYfb8ap/J9IpViAaR86DRt0+t5RCC2njwWC8qb\r\n" +
	"0ZHs5eCcLA==\r\n" +
	"-----END CERTIFICATE-----\r\n";


	public static final String testCertWithSAN2 =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"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\r\n" +
	"1DrLZ1My2vEtgQ==\r\n" +
	"-----END CERTIFICATE-----\r\n";

	public static final String testCertECCWithManySANs =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"MIIGdTCCBV2gAwIBAgIIIJdg6zrWeKwwDQYJKoZIhvcNAQEFBQAwSTELMAkGA1UE\r\n" +
	"BhMCVVMxEzARBgNVBAoTCkdvb2dsZSBJbmMxJTAjBgNVBAMTHEdvb2dsZSBJbnRl\r\n" +
	"cm5ldCBBdXRob3JpdHkgRzIwHhcNMTQwMTI5MTMxOTU5WhcNMTQwNTI5MDAwMDAw\r\n" +
	"WjBmMQswCQYDVQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwN\r\n" +
	"TW91bnRhaW4gVmlldzETMBEGA1UECgwKR29vZ2xlIEluYzEVMBMGA1UEAwwMKi5n\r\n" +
	"b29nbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEArZQWW1kPh6k0cuy\r\n" +
	"Emdp/t6v/GwqClh1FC0BfjWLe9+Gfb8TolQWRKXPiO7CLJP45q39gk494hTk343X\r\n" +
	"A4DqSaOCBA0wggQJMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjCCAtYG\r\n" +
	"A1UdEQSCAs0wggLJggwqLmdvb2dsZS5jb22CDSouYW5kcm9pZC5jb22CFiouYXBw\r\n" +
	"ZW5naW5lLmdvb2dsZS5jb22CEiouY2xvdWQuZ29vZ2xlLmNvbYIWKi5nb29nbGUt\r\n" +
	"YW5hbHl0aWNzLmNvbYILKi5nb29nbGUuY2GCCyouZ29vZ2xlLmNsgg4qLmdvb2ds\r\n" +
	"ZS5jby5pboIOKi5nb29nbGUuY28uanCCDiouZ29vZ2xlLmNvLnVrgg8qLmdvb2ds\r\n" +
	"ZS5jb20uYXKCDyouZ29vZ2xlLmNvbS5hdYIPKi5nb29nbGUuY29tLmJygg8qLmdv\r\n" +
	"b2dsZS5jb20uY2+CDyouZ29vZ2xlLmNvbS5teIIPKi5nb29nbGUuY29tLnRygg8q\r\n" +
	"Lmdvb2dsZS5jb20udm6CCyouZ29vZ2xlLmRlggsqLmdvb2dsZS5lc4ILKi5nb29n\r\n" +
	"bGUuZnKCCyouZ29vZ2xlLmh1ggsqLmdvb2dsZS5pdIILKi5nb29nbGUubmyCCyou\r\n" +
	"Z29vZ2xlLnBsggsqLmdvb2dsZS5wdIIPKi5nb29nbGVhcGlzLmNughQqLmdvb2ds\r\n" +
	"ZWNvbW1lcmNlLmNvbYIRKi5nb29nbGV2aWRlby5jb22CDSouZ3N0YXRpYy5jb22C\r\n" +
	"DCoudXJjaGluLmNvbYIQKi51cmwuZ29vZ2xlLmNvbYIWKi55b3V0dWJlLW5vY29v\r\n" +
	"a2llLmNvbYINKi55b3V0dWJlLmNvbYIWKi55b3V0dWJlZWR1Y2F0aW9uLmNvbYIL\r\n" +
	"Ki55dGltZy5jb22CC2FuZHJvaWQuY29tggRnLmNvggZnb28uZ2yCFGdvb2dsZS1h\r\n" +
	"bmFseXRpY3MuY29tggpnb29nbGUuY29tghJnb29nbGVjb21tZXJjZS5jb22CCnVy\r\n" +
	"Y2hpbi5jb22CCHlvdXR1LmJlggt5b3V0dWJlLmNvbYIUeW91dHViZWVkdWNhdGlv\r\n" +
	"bi5jb20wCwYDVR0PBAQDAgeAMGgGCCsGAQUFBwEBBFwwWjArBggrBgEFBQcwAoYf\r\n" +
	"aHR0cDovL3BraS5nb29nbGUuY29tL0dJQUcyLmNydDArBggrBgEFBQcwAYYfaHR0\r\n" +
	"cDovL2NsaWVudHMxLmdvb2dsZS5jb20vb2NzcDAdBgNVHQ4EFgQUJ7u52nucWC9C\r\n" +
	"KGtyFseflw5h1d0wDAYDVR0TAQH/BAIwADAfBgNVHSMEGDAWgBRK3QYWG7z2aLV2\r\n" +
	"9YG2u2IaulqBLzAXBgNVHSAEEDAOMAwGCisGAQQB1nkCBQEwMAYDVR0fBCkwJzAl\r\n" +
	"oCOgIYYfaHR0cDovL3BraS5nb29nbGUuY29tL0dJQUcyLmNybDANBgkqhkiG9w0B\r\n" +
	"AQUFAAOCAQEAA8oMeDCkoUzLWErd7oL/k510BrXGMn14wVvrbYTQEXBYLqFUTzGa\r\n" +
	"wNztlIPgv6fdlGBioqV8+KxYmikaSENZSF0guuYM8ZktnNWOerbTlv4dWGJHqP9x\r\n" +
	"/M+7zOOawOBiVqlLocV5q6miRKvwos8GFj02dfeFNutxvk1GmBILtEZMVrhpgBke\r\n" +
	"G7LI+czBem2X/Uoor1fkvMQ08pNEESm4iBif7li8T0U7KJSKI6QHrwvxZ7Q9QGUi\r\n" +
	"tVnq6tqFs7dDC/+mzgdyfU9lyH0YkNs3wXiJlPRZVKqdNkCAQCwyia/kkkYVMVRC\r\n" +
	"k5LpJryOOI3tSVpvG5v04orysl4iiunzKg==\r\n" +
	"-----END CERTIFICATE-----\r\n";

	public static final String testCertWithLongSAN =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"MIIIATCCBumgAwIBAgIQZCUFERfc+LxPQ5fZEYEt0zANBgkqhkiG9w0BAQUFADBq\r\n" +
	"MQswCQYDVQQGEwJFUzERMA8GA1UECgwIRk5NVC1SQ00xDjAMBgNVBAsMBUNFUkVT\r\n" +
	"MRIwEAYDVQQFEwlRMjgyNjAwNEoxJDAiBgNVBAMMG0FDIEFkbWluaXN0cmFjacOz\r\n" +
	"biBQw7pibGljYTAeFw0xMjAyMjExMzEwNDlaFw0xNTAyMjExMzEwNDlaMIHYMQsw\r\n" +
	"CQYDVQQGEwJFUzEyMDAGA1UECgwpTUlOSVNURVJJTyBERSBJTkRVU1RSSUEgRU5F\r\n" +
	"UkdJQSBZIFRVUklTTU8xGjAYBgNVBAsMEXNlZGUgZWxlY3Ryw7NuaWNhMUcwRQYD\r\n" +
	"VQQLDD5TRURFIEVMRUNUUk9OSUNBIERFTCBNSU5JU1RFUklPIERFIElORFVTVFJJ\r\n" +
	"QSBFTkVSR0lBIFkgVFVSSVNNTzESMBAGA1UEBRMJUzI4MDAyMTRFMRwwGgYDVQQD\r\n" +
	"DBNzZWRlLm1pbmV0dXIuZ29iLmVzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\r\n" +
	"CgKCAQEAixW9qs4jjrZu2JY2Qmty1wTT67Oe8DVFxwDgYr4wfYde5ZJ/5avldW1q\r\n" +
	"IZMkCTiFSb26+DUkXt2Qvn2ncVU8z1ePGCL8gKvYNAC55REkF5rH5smVTPpjipuB\r\n" +
	"+kGFc3yCGvEcm2kEYwGbow7VV6dLbN8XeJ8q7DalnimYNzKz0Ihb1tjtCLQG2n+n\r\n" +
	"2SwGzgwrXA13a+wtV1+GoMkz4VWhgDsL2ReyBcfJPn8tznW9DB5+bDna9TUSeEMG\r\n" +
	"H5jYMBEvdkVc4C6EVh3RNAKK2XftrDuOE/CAHVcrmMt1IQEJ74PK9TvzBWBLesaA\r\n" +
	"piKFbVsub5iQYmzR9r/ZeSyH/+PRQQIDAQABo4IEMjCCBC4wggERBgNVHREEggEI\r\n" +
	"MIIBBKSB7DCB6TEiMCAGCWCFVAEDBQECBQwTU0VERS5NSU5FVFVSLkdPQi5FUzFN\r\n" +
	"MEsGCWCFVAEDBQECBAw+U0VERSBFTEVDVFJPTklDQSBERUwgTUlOSVNURVJJTyBE\r\n" +
	"RSBJTkRVU1RSSUEgRU5FUkdJQSBZIFRVUklTTU8xGDAWBglghVQBAwUBAgMMCVMy\r\n" +
	"ODAwMjE0RTE4MDYGCWCFVAEDBQECAgwpTUlOSVNURVJJTyBERSBJTkRVU1RSSUEg\r\n" +
	"RU5FUkdJQSBZIFRVUklTTU8xIDAeBglghVQBAwUBAgEMEXNlZGUgZWxlY3Ryw7Nu\r\n" +
	"aWNhghNTRURFLk1JTkVUVVIuR09CLkVTMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/\r\n" +
	"BAQDAgWgMB0GA1UdDgQWBBSET9coVKYsr0TklX4AQJLK90Ac8zAfBgNVHSMEGDAW\r\n" +
	"gBQUEeK1K7mMmK1o0zFUQORYXwMbfTCB7wYDVR0gBIHnMIHkMIHhBgsrBgEEAaxm\r\n" +
	"AwMCAjCB0TApBggrBgEFBQcCARYdaHR0cDovL3d3dy5jZXJ0LmZubXQuZXMvZHBj\r\n" +
	"cy8wgaMGCCsGAQUFBwICMIGWDIGTU3VqZXRvIGEgbGFzIGNvbmRpY2lvbmVzIGRl\r\n" +
	"IHVzbyBleHB1ZXN0YXMgZW4gbGEgRGVjbGFyYWNpw7NuIGRlIFByw6FjdGljYXMg\r\n" +
	"ZGUgQ2VydGlmaWNhY2nDs24gZGUgbGEgRk5NVC1SQ00gKEMvSm9yZ2UgSnVhbiAx\r\n" +
	"MDYtMjgwMDktTWFkcmlkLUVzcGHDsWEpMDwGCCsGAQUFBwEDBDAwLjAIBgYEAI5G\r\n" +
	"AQEwFQYGBACORgECMAsTA0VVUgIBAAIBADALBgYEAI5GAQMCAQ8wfwYIKwYBBQUH\r\n" +
	"AQEEczBxMDsGCCsGAQUFBzABhi9odHRwOi8vb2NzcGFwLmNlcnQuZm5tdC5lcy9v\r\n" +
	"Y3NwYXAvT2NzcFJlc3BvbmRlcjAyBggrBgEFBQcwAoYmaHR0cDovL3d3dy5jZXJ0\r\n" +
	"LmZubXQuZXMvY2VydHMvQUNBUC5jcnQwGQYDVR0lBBIwEAYIKwYBBQUHAwEGBFUd\r\n" +
	"JQAwgewGA1UdHwSB5DCB4TCB3qCB26CB2IaBqWxkYXA6Ly9sZGFwYXBlLmNlcnQu\r\n" +
	"Zm5tdC5lcy9DTj1DUkwyMyxDTj1BQyUyMEFkbWluaXN0cmFjaSVGM24lMjBQJUZB\r\n" +
	"YmxpY2EsT1U9Q0VSRVMsTz1GTk1ULVJDTSxDPUVTP2NlcnRpZmljYXRlUmV2b2Nh\r\n" +
	"dGlvbkxpc3Q7YmluYXJ5P2Jhc2U/b2JqZWN0Y2xhc3M9Y1JMRGlzdHJpYnV0aW9u\r\n" +
	"UG9pbnSGKmh0dHA6Ly93d3cuY2VydC5mbm10LmVzL2NybHNhY2FwL0NSTDIzLmNy\r\n" +
	"bDANBgkqhkiG9w0BAQUFAAOCAQEAj8W9CFwKt9TyvPrxieoXv8B8G+K16VKsEdAW\r\n" +
	"ksXvVBvw7v90pBw70kTB3fY12I3WyO/ozi97/9KhdKe+FxJeaVNY05sP/ByY2ha6\r\n" +
	"nQ5xwPxekmQ9t0XKR9dsqbwNd5ilgyUzHT/y0brJEgA6y0sus/VpUcpdtdO4YLsl\r\n" +
	"hSuznFCx9TFX8M7yfJgykE0zGe9yW+SSMJFaGoV0Z8Mdib0cGeH3xZjsNzr0+fBf\r\n" +
	"nhlrv2MhHltZ7OS4l7hUmwI5BVlxB1xdoZ9mGzuOj5QljiKdNwf4tztlJ5W0vwJS\r\n" +
	"jZ4UpeBmxNAqJ0jcJnaiMKLpYNrwaqRIabc6k2JeRoarmOirtw==\r\n" +
	"-----END CERTIFICATE-----\r\n";

	public static final String testCertWithNullKeyLength =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"MIIILjCCBxagAwIBAgIGV+vxwwAdMA0GCSqGSIb3DQEBCwUAMGAxCzAJBgNVBAYT\r\n" +
	"AkhVMREwDwYDVQQHDAhCdWRhcGVzdDEVMBMGA1UECgwMTkVUTE9DSyBMdGQuMScw\r\n" +
	"JQYDVQQDDB5ORVRMT0NLIFRydXN0IFF1YWxpZmllZCBTQ0QgQ0EwHhcNMTcwNjIw\r\n" +
	"MTE0NjIyWhcNMTkwNjIwMTE0NjIyWjCB1TELMAkGA1UEBhMCSFUxETAPBgNVBAcM\r\n" +
	"CEJ1ZGFwZXN0MRUwEwYDVQQKDAxORVRMT0NLIEx0ZC4xLzAtBgNVBAMMJk5FVExP\r\n" +
	"Q0sgUXVhbGlmaWVkIFByZXNlcnZhdGlvbiBTZXJ2aWNlMS0wKwYDVQQFEyQxLjMu\r\n" +
	"Ni4xLjQuMS4zNTU1LjUuMi4wNzc1MzEyOTc0NTkwODcxHjAcBgkqhkiG9w0BCQEW\r\n" +
	"D2luZm9AbmV0bG9jay5odTEcMBoGA1UEYQwTVkFUSFUtMTIyMDE1MjEtMi00MjCC\r\n" +
	"ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKg6dKFH2PRW4QsgcaCf1m68\r\n" +
	"Gk9an6mU8O6r8a/zd7wydknV98hvUn2Grh+umxDWtk3qivBsxOfvyuOEPkC0DkN3\r\n" +
	"VylgPL1Q47rIymsfIUl78f44UGe9PE7FC9/+9Iq+STeFWL1FHwJnvJEbfsPQofDh\r\n" +
	"e3v3Gblyv54umDj8eqDhuROsbl7Q4cECmJdw0mrqAEtgZo0og0DN1LXj2Ue69WVX\r\n" +
	"+6a1jI15kc7XNHpoNu9y9dJBBDhV5zLgdwS4737Ry+zTqYejmbo8XHVAANNypD4v\r\n" +
	"t8UqmCD51Ktm407pei4tI0sGsbMng4SRqYaAuOismiKx/Ep4zbYazS094LvG2LEC\r\n" +
	"AwDHEaOCBHYwggRyMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgZAMIGnBggr\r\n" +
	"BgEFBQcBAwSBmjCBlzAIBgYEAI5GAQEwFQYGBACORgECMAsTA0hVRgIBBQIBBjAL\r\n" +
	"BgYEAI5GAQMCAQowEwYGBACORgEGMAkGBwQAjkYBBgIwUgYGBACORgEFMEgwIhYc\r\n" +
	"aHR0cHM6Ly93d3cubmV0bG9jay5odS9kb2NzLxMCRU4wIhYcaHR0cHM6Ly93d3cu\r\n" +
	"bmV0bG9jay5odS9kb2NzLxMCSFUwggEgBgNVHSAEggEXMIIBEzAQBg4rBgEEAYL3\r\n" +
	"EAMCAgICAjCB/gYHBACL7EABATCB8jAnBggrBgEFBQcCARYbaHR0cDovL3d3dy5u\r\n" +
	"ZXRsb2NrLmh1L2RvY3MvMIHGBggrBgEFBQcCAjCBuQyBtkEgbWluxZFzw610ZXR0\r\n" +
	"IHRhbsO6c8OtdHbDoW55IG1hZ8Ohbmt1bGNzw6F2YWwgYXogZUlEQVMgc3plcmlu\r\n" +
	"dGkgZm9rb3pvdHQgYml6dG9uc8OhZ8O6IGLDqWx5ZWd6xZEgaG96aGF0w7MgbMOp\r\n" +
	"dHJlLiBNby4tbiB0ZWxqZXMgYml6b255w610w7MgZXJlasWxIG9raXJhdCBlbMWR\r\n" +
	"w6FsbMOtdMOhc8OhcmEgYWxrYWxtYXMuMB0GA1UdDgQWBBQtRsSRblIMUXoFNmAv\r\n" +
	"BHT1UQf4WjAfBgNVHSMEGDAWgBRUfuykHhhbu9ezeYP0+9dnuymV/DCCAVwGCCsG\r\n" +
	"AQUFBwEBBIIBTjCCAUowMQYIKwYBBQUHMAGGJWh0dHA6Ly9vY3NwMS5uZXRsb2Nr\r\n" +
	"Lmh1L3F0cnVzdHNjZC5jZ2kwMQYIKwYBBQUHMAGGJWh0dHA6Ly9vY3NwMi5uZXRs\r\n" +
	"b2NrLmh1L3F0cnVzdHNjZC5jZ2kwMQYIKwYBBQUHMAGGJWh0dHA6Ly9vY3NwMy5u\r\n" +
	"ZXRsb2NrLmh1L3F0cnVzdHNjZC5jZ2kwOQYIKwYBBQUHMAKGLWh0dHA6Ly9haWEx\r\n" +
	"Lm5ldGxvY2suaHUvaW5kZXguY2dpP2NhPXF0cnVzdHNjZDA5BggrBgEFBQcwAoYt\r\n" +
	"aHR0cDovL2FpYTIubmV0bG9jay5odS9pbmRleC5jZ2k/Y2E9cXRydXN0c2NkMDkG\r\n" +
	"CCsGAQUFBzAChi1odHRwOi8vYWlhMy5uZXRsb2NrLmh1L2luZGV4LmNnaT9jYT1x\r\n" +
	"dHJ1c3RzY2Qwga0GA1UdHwSBpTCBojA0oDKgMIYuaHR0cDovL2NybDEubmV0bG9j\r\n" +
	"ay5odS9pbmRleC5jZ2k/Y3JsPXF0cnVzdHNjZDA0oDKgMIYuaHR0cDovL2NybDIu\r\n" +
	"bmV0bG9jay5odS9pbmRleC5jZ2k/Y3JsPXF0cnVzdHNjZDA0oDKgMIYuaHR0cDov\r\n" +
	"L2NybDMubmV0bG9jay5odS9pbmRleC5jZ2k/Y3JsPXF0cnVzdHNjZDA0BgNVHREE\r\n" +
	"LTArgQ9pbmZvQG5ldGxvY2suaHWgGAYIKwYBBQUHCAOgDDAKBggrBgEEAZtjBTAN\r\n" +
	"BgkqhkiG9w0BAQsFAAOCAQEAAcKt8GFzuJD2/cstji4gU0AdbcdE0qHyWl0MZN+r\r\n" +
	"DXGTulBPNjRytlTho2hAE+AKUPebpWsgjks01ZyoSzqkJxSNdBYigz13T8MvwEQ3\r\n" +
	"S+wqp7tPGMvsd5pgd1W3CvYdbJ21Yfpv9Gx+j2n816QJEW6Y0kHeoiFLMiLaSspM\r\n" +
	"zd/qWZs8boSk6P+rbjirTRZQWsUzJDpCPhS2QOtLwdVhv9yXGYQZGTOM5WDl/AsU\r\n" +
	"VGiRfQOnDOMmG1WyA5nRj2wdiMBj2oawEqmZecKTE68ppIh4GHwbu6E0DCVwkhxH\r\n" +
	"dwkBZYrKyfdPeqAv70T4AgHTeGj6c9KMylD6EQ3Dl7CH8w==\r\n" +
	"-----END CERTIFICATE-----\r\n";

	public static final String testLocalhost =
	"-----BEGIN CERTIFICATE-----\r\n" +
	"MIIEJzCCAw+gAwIBAgITFQAADZ6N95LGYz1zLAABAAANnjANBgkqhkiG9w0BAQsF\r\n" +
	"ADAcMRowGAYDVQQDExFXUy0yMDE5LUlzc3VpbmdDQTAeFw0yMDA0MjcxMTUwMjNa\r\n" +
	"Fw0yMTA0MjYxMTAxMDdaMFwxCzAJBgNVBAYTAkRFMRYwFAYDVQQKEw10cnVzdGFi\r\n" +
	"bGUgTHRkMRswGQYDVQQLExJjYTNzIDE1ODc5ODg4MTk2MzAxGDAWBgNVBAMTD0RF\r\n" +
	"U0tUT1AtSjJDRjc0VjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnR\r\n" +
	"RZjN2Tr/CwlGW8tKnx47n1MTYNsa85h2jMVHUYRZOdpQMdrPRPlMn2joxW3tjgfo\r\n" +
	"l14DKIeaWoFxJJVqB006Ls4l6j899sbIstu9KR3ojXtjBs0s6qXBojCZrOmEeCU9\r\n" +
	"iWcL35qeSSlvmR6HL7f2GQ7xbWz4EEDrBQU7SZrkFsXJgowIrFR+nStHKkEqxcsz\r\n" +
	"+1y2hdv+6/OOBi3Yup6MiluYcmD3fqflEMHC7rQbUqyeWc8jboF0uL+W0DnxApwS\r\n" +
	"M+ofzzI28i/YZkafBGsLTeMTY7wYqneAqnzW4PTI7MSOyVB3mqJuH0tUBuqsN6Be\r\n" +
	"iJnSMBa61s9Q/Xj/mb0CAwEAAaOCASAwggEcMBMGA1UdJQQMMAoGCCsGAQUFBwMO\r\n" +
	"MB0GA1UdDgQWBBSUEqchB+jQT7/mf0aWt+BaaXcA7TAfBgNVHSMEGDAWgBTcYoRv\r\n" +
	"4Ad+klBDqCFzuWtK2ncZozBNBgNVHR8ERjBEMEKgQKA+hjxmaWxlOi8vLy9XSU4t\r\n" +
	"SjRFRkNTQVJFTjkvQ2VydEVucm9sbC9XUy0yMDE5LUlzc3VpbmdDQSgxKS5jcmww\r\n" +
	"aAYIKwYBBQUHAQEEXDBaMFgGCCsGAQUFBzAChkxmaWxlOi8vLy9XSU4tSjRFRkNT\r\n" +
	"QVJFTjkvQ2VydEVucm9sbC9XSU4tSjRFRkNTQVJFTjlfV1MtMjAxOS1Jc3N1aW5n\r\n" +
	"Q0EoMSkuY3J0MAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQELBQADggEBAJjEFIe4\r\n" +
	"CtRVc9vX8np4jZsqHE4K1TVkarj9a68RG2GTHUAdtNAVjK+ZFYeSdIg8YqkkLQmQ\r\n" +
	"Jh9BHPr5du1jBdgktznsm9wg7sljOUqm+v1XEk7wD4fsv6di1n52YlPX52+wNvpN\r\n" +
	"/vhZU15sHX/KqK6L7ZiXLEqBe3kRd1kfOf44+uPVqNFEs+OP1pCNKPZz9tjLBJrL\r\n" +
	"RuOzwmbh8xagJhXOokKwpRk4tFfL5Y6P2P4iZPNBRY1sWMDh8CIDl0ICrnl5KFo5\r\n" +
	"lPB7f85pmG1HvGAeQXdhkaVeTXUbC0CsjILEXcgFNyvQgHNT07TVT63zcEsm9wIC\r\n" +
	"5wvSWO94GjWtJT0=\r\n" +
	"-----END CERTIFICATE-----\r\n";

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

/*
	@Test
	public void testFileUrl() throws MalformedURLException {

		File dir = new File("c:/tmp");
		assertTrue( dir.exists() && dir.canRead());
	}
*/

	@Test
	public void testBuildCertificate() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCert, null, executionId);
		assertNotNull(cert);
		assertTrue(cert.getSans().isEmpty());
	}

	@Test
	public void testBuildCertificateWithSAN() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCertWithSAN, null, executionId);
		assertNotNull(cert);

		assertFalse(cert.getSans().isEmpty());
		assertEquals("ca@trustable.de", cert.getSans());

		boolean sanPresent = false;
		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_SAN.equals(certAtt.getName())) {
				assertEquals("ca@trustable.de", certAtt.getValue());
				System.out.println("::: " + certAtt.getValue());
				sanPresent = true;
			}
		}
		assertTrue(sanPresent);
	}


	@Test
	public void testBuildCertificateWithSAN2() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCertWithSAN2, null, executionId);
		assertNotNull(cert);

		assertFalse(cert.getSans().isEmpty());
		assertEquals("info@ramthun.net", cert.getSans());

		boolean sanPresent = false;
		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_SAN.equals(certAtt.getName())) {
//				assertEquals("ca@trustable.de", certAtt.getValue());
				System.out.println("::: " + certAtt.getValue());
				sanPresent = true;
			}
		}
		assertTrue(sanPresent);
	}


	@Test
	public void testBuildCertificatetestCertECCWithManySANs() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCertECCWithManySANs, null, executionId);
		assertNotNull(cert);

		assertFalse(cert.getSans().isEmpty());
		assertEquals("*.google.com;*.android.com;*.appengine.google.com;*.cloud.google.com;*.google-analytics.com;*.google.ca;*.google.cl;*.google.co.in;*.google.co.jp;*.google.co.uk;*.google.com.ar;*.google.com.au;*.google.com.br;*.google.com.co;*.google.com.mx;*.google.", cert.getSans());

		int sanCount = 0;
		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_SAN.equals(certAtt.getName())) {
				sanCount++;
			}
		}
		assertEquals(45, sanCount);

		assertEquals("ec", cert.getKeyAlgorithm());
		assertEquals("rsa", cert.getSigningAlgorithm());
		assertEquals("sha1", cert.getHashingAlgorithm());
		assertEquals("prime256v1", cert.getCurveName());
		assertEquals("256", cert.getKeyLength().toString());
		assertEquals("pkcs1", cert.getPaddingAlgorithm());

	}


	@Test
	public void testBuildCertificatetestCertWithLongSAN() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCertWithLongSAN, null, executionId);
		assertNotNull(cert);

		assertFalse(cert.getSans().isEmpty());
		assertEquals("2.16.724.1.3.5.1.2.1=sede electrónica,2.16.724.1.3.5.1.2.2=ministerio de industria energia y turismo,2.16.724.1.3.5.1.2.3=s2800214e,2.16.724.1.3.5.1.2.4=sede electronica del ministerio de industria energia y turismo,2.16.724.1.3.5.1.2.5=sede.minetur.", cert.getSans());

		int sanCount = 0;
		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_SAN.equals(certAtt.getName())) {
				sanCount++;
			}
			if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
				System.out.println("ATTRIBUTE_CRL_URL" + certAtt.getValue());
			}
		}
		assertEquals(2, sanCount);

		assertEquals("rsa", cert.getKeyAlgorithm());
		assertEquals("rsa", cert.getSigningAlgorithm());
		assertEquals("sha1", cert.getHashingAlgorithm());
		assertEquals("2048", cert.getKeyLength().toString());
		assertEquals("pkcs1", cert.getPaddingAlgorithm());

	}

	@Test
	public void testBuildCertificatetestCertWithNullKeyLength() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testCertWithNullKeyLength, null, executionId);
		assertNotNull(cert);


		int sanCount = 0;
		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_SAN.equals(certAtt.getName())) {
				sanCount++;
			}
			if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
				System.out.println("ATTRIBUTE_CRL_URL" + certAtt.getValue());
			}
		}
		assertEquals(2, sanCount);

		assertEquals("rsa", cert.getKeyAlgorithm());
		assertEquals("rsa", cert.getSigningAlgorithm());
		assertEquals("sha256", cert.getHashingAlgorithm());
		assertEquals("2048", cert.getKeyLength().toString());
		assertEquals("pkcs1", cert.getPaddingAlgorithm());

	}

	@Test
	public void testBuildCertificatestLocalhost() throws GeneralSecurityException, IOException{

		String executionId = "";
		Certificate cert = certificateUtil.createCertificate(testLocalhost, null, executionId);
		assertNotNull(cert);


		for(CertificateAttribute certAtt : cert.getCertificateAttributes()) {
			if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
				System.out.println("ATTRIBUTE_CRL_URL" + certAtt.getValue());
			}
		}

		assertEquals("rsa", cert.getKeyAlgorithm());
		assertEquals("rsa", cert.getSigningAlgorithm());
		assertEquals("sha256", cert.getHashingAlgorithm());
		assertEquals("2048", cert.getKeyLength().toString());
		assertEquals("pkcs1", cert.getPaddingAlgorithm());

	}

    @Test
	public void testNameNormalization() throws InvalidNameException {

        String a = certificateUtil.getNormalizedName("C=DE,O=T-Systems International GmbH,OU=T-Systems Trust Center,CN=TeleSec Business CA 1");
        String b = certificateUtil.getNormalizedName("CN=TeleSec Business CA 1,OU=T-Systems Trust Center,O=T-Systems International GmbH,C=DE");

//        System.out.println("Normalized name: " + a);
//        System.out.println("Normalized name: " + b);

        assertEquals("normalizing names expected to be identical ", a, b);
	}


    @Test
    public void testNameHandling() {

        GeneralName[] generalNames = certificateUtil.splitSANString(" foo.de, bar.de , baz.de", null);
        assertEquals(" expected to see 3 GeneralNames ", 3, generalNames.length);

    }
}
