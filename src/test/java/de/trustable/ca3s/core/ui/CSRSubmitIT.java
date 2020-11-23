package de.trustable.ca3s.core.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import io.ddavison.conductor.Browser;

// @ContextConfiguration(classes=PipelineTestConfiguration.class)

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@io.ddavison.conductor.Config(
        browser = Browser.CHROME,
        url     = "http://localhost:${local.server.port}/"
)
public class CSRSubmitIT extends WebTestBase{

    public static final By LOC_TXT_WEBPACK_ERROR = By.xpath("//div//h1 [text() = 'An error has occured :-(']");

    public static final By LOC_LNK_ACCOUNT_MENUE =          By.xpath("//nav//a [.//span [text() = 'Account']]");
    public static final By LOC_LNK_REQ_CERT_MENUE =         By.xpath("//nav//a [.//span [text() = 'Request certificate']]");
    public static final By LOC_LNK_REQUESTS_MENUE =         By.xpath("//nav//a [.//span [text() = 'Requests']]");
    public static final By LOC_LNK_CERTIFICATES_MENUE =     By.xpath("//nav//a [.//span [text() = 'Certificates']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_IN_MENUE =  By.xpath("//nav//a [span [text() = 'Sign in']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_OUT_MENUE = By.xpath("//nav//a [span [text() = 'Sign out']]");

    public static final By LOC_LNK_SIGNIN_USERNAME = By.xpath("//form//input [@name = 'username']");
    public static final By LOC_LNK_SIGNIN_PASSWORD = By.xpath("//form//input [@name = 'password']");
    public static final By LOC_BTN_SIGNIN_SUBMIT = By.xpath("//form//button [@type='submit'][text() = 'Sign in']");

    public static final By LOC_BTN_REQUEST_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Request certificate']]");

    public static final By LOC_TEXT_ACCOUNT_NAME = By.xpath("//nav//span [contains(text(), '\"user\"')]");
    public static final By LOC_TEXT_CONTENT_TYPE = By.xpath("//form//dl [dt[span [text() = 'Content type']]]/dd/span");

    public static final By LOC_TA_UPLOAD_CONTENT = By.xpath("//form//textarea [@name = 'content']");
    public static final By LOC_SEL_PIPELINE = By.xpath("//form//select [@name = 'pkcsxx-pipeline']");

    public static final By LOC_TEXT_CERT_HEADER = By.xpath("//div/h2/span [text() = 'Certificate']");
    public static final By LOC_TEXT_PKIX_LABEL = By.xpath("//div//dl [dt[span [text() = 'PKIX (DER encoded)']]]");
    public static final By LOC_SEL_REVOCATION_REASON = By.xpath("//form//select [@name = 'revocationReason']");

    public static final By LOC_TEXT_CSR_HEADER = By.xpath("//div/h2/span [text() = 'CSR']");

    public static final By LOC_TEXT_REQUEST_LIST = By.xpath("//div [text() = 'Request List']");
    public static final By LOC_TEXT_CERTIFICATE_LIST = By.xpath("//div [text() = 'Certificate list']");

    public static final By LOC_SEL_CSR_ATTRIBUTE = By.xpath("//div/select [@name = 'csrSelectionAttribute']");
    public static final By LOC_SEL_CSR_CHOICE = By.xpath("//div/select [@name = 'csrSelectionChoice']");
    public static final By LOC_INP_CSR_VALUE = By.xpath("//div/input [@name = 'csrSelectionValue']");
    public static final By LOC_SEL_CSR_VALUE_SET = By.xpath("//div/select [@name = 'csrSelectionSet']");
    public static final By LOC_INP_CSR_DATE = By.xpath("//div/input [@name = 'csrSelectionValueDate']");
    public static final By LOC_INP_CSR_BOOLEAN = By.xpath("//div/input [@name = 'csrSelectionValueBoolean']");

    public static final By LOC_SEL_CERT_ATTRIBUTE = By.xpath("//div/select [@name = 'certSelectionAttribute']");
    public static final By LOC_SEL_CERT_CHOICE = By.xpath("//div/select [@name = 'certSelectionChoice']");
    public static final By LOC_INP_CERT_VALUE = By.xpath("//div/input [@name = 'certSelectionValue']");
    public static final By LOC_SEL_CERT_VALUE_SET = By.xpath("//div/select [@name = 'certSelectionSet']");
    public static final By LOC_INP_CERT_DATE = By.xpath("//div/input [@name = 'certSelectionValueDate']");
    public static final By LOC_INP_CERT_BOOLEAN = By.xpath("//div/input [@name = 'certSelectionValueBoolean']");

    public static final By LOC_TD_CSR_ITEM_PENDING = By.xpath("//table//td [starts-with(text(), 'PENDING')]");

    public static final By LOC_TA_DOWNLOAD_CERT_CONTENT = By.xpath("//dd/span/textarea [@name = 'certContent']");
    public static final By LOC_TEXT_CERT_REVOCATION_REASON = By.xpath("//div//dd/span[@name = 'revocationReason']");


    public static final By LOC_BTN_WITHDRAW_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Withdraw']]");
    public static final By LOC_BTN_CONFIRM_REQUEST = By.xpath("//form/div/button [@type='button'][span [text() = 'Confirm Request']]");
    public static final By LOC_BTN_BACK = By.xpath("//form/div/button [@type='submit'][span [text() = 'Back']]");


    public static final By LOC_SEL_KEY_CREATION_CHOICE = By.xpath("//div/select [@name = 'pkcsxx-key-creation']");
    public static final By LOC_SEL_KEY_LENGTH_CHOICE = By.xpath("//div/select [@name = 'pkcsxx.upload.key-length']");

    public static final By LOC_INP_C_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.C']");
    public static final By LOC_INP_CN_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.CN']");
    public static final By LOC_INP_O_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.O']");
    public static final By LOC_INP_OU_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.OU']");
    public static final By LOC_INP_L_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.L']");
    public static final By LOC_INP_ST_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.ST']");
    public static final By LOC_INP_SAN_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.SAN']");

    public static final By LOC_INP_SECRET_VALUE = By.xpath("//div/input [@name = 'upload-secret']");
    public static final By LOC_INP_SECRET_REPEAT_VALUE = By.xpath("//div/input [@name = 'upload-secret-repeat']");



    private static final Logger LOG = LoggerFactory.getLogger(CSRSubmitIT.class);


	private static final String USER_NAME_USER = "user";
	private static final String USER_PASSWORD_USER = "user";

	private static final String USER_NAME_RA = "ra";
	private static final String USER_PASSWORD_RA = "s3cr3t";

	private static Random rand = new Random();

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	@Autowired
	PipelineTestConfiguration ptc;


	@BeforeAll
	public static void setUpBeforeClass() {
		JCAManager.getInstance();
	}

	@BeforeEach
	void init() {

	    waitForUrl();

		ptc.getInternalWebDirectTestPipeline();
		ptc.getInternalWebRACheckTestPipeline();

		if( driver == null) {
		    super.startWebDriver();
		    driver.manage().window().setSize(new Dimension(2000,768));
		}
	}

	@Test
	public void testCSRSubmitServersideDirect() throws GeneralSecurityException {

		String c = "GB";
		String cn = "reqTest" + System.currentTimeMillis();
		String o = "trustable Ltd";
		String ou = "nuclear research";
		String l = "Birmingham";
		String st = "West Midlands";
		String san = "wwww." + cn;

	    String subject = "CN=" + cn + ", O="+o+", OU="+ou+", C="+ c + ", L=" + l + ", ST=" + st;
	    X500Principal subjectPrincipal = new X500Principal(subject);

		byte[] secretBytes = new byte[6];
		rand.nextBytes(secretBytes);
		String secret = Base64.getEncoder().encodeToString(secretBytes);

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

		validatePresent(LOC_TA_UPLOAD_CONTENT);

	    validatePresent(LOC_SEL_PIPELINE);

	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

		validatePresent(LOC_SEL_KEY_CREATION_CHOICE);
	    selectOptionByText(LOC_SEL_KEY_CREATION_CHOICE, "Serverside key creation");

	    validatePresent(LOC_SEL_KEY_LENGTH_CHOICE);
	    selectOptionByText(LOC_SEL_KEY_LENGTH_CHOICE, "RSA_2048");

/*
		try {
			System.out.println("... waiting ...");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/

	    setText(LOC_INP_C_VALUE, c);
	    setText(LOC_INP_CN_VALUE, cn);
	    setText(LOC_INP_O_VALUE, o);
	    setText(LOC_INP_OU_VALUE, ou);
	    setText(LOC_INP_L_VALUE, l);
	    setText(LOC_INP_ST_VALUE, st);
	    setText(LOC_INP_SAN_VALUE, san);

	    setText(LOC_INP_SECRET_VALUE, secret);
	    setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);

	    assertTrue("Expecting request button enabled", isEnabled(LOC_BTN_REQUEST_CERTIFICATE));

	    setText(LOC_INP_SECRET_REPEAT_VALUE, "aa"+secret+"zz");
	    assertFalse("Expecting request button enabled", isEnabled(LOC_BTN_REQUEST_CERTIFICATE));


	    click(LOC_BTN_REQUEST_CERTIFICATE);


        waitForElement(LOC_TEXT_CERT_HEADER);
	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);


		String certAsPem = getText(LOC_TA_DOWNLOAD_CERT_CONTENT);
//		System.out.println("PEM cert = \n" + certAsPem );

	    X509Certificate newCert = CryptoUtil.convertPemToCertificate(certAsPem);

		System.out.println("subject = " + newCert.getSubjectDN().toString() );

	    boolean match = equalsIgnoreOrdering(subjectPrincipal, new X500Principal(newCert.getSubjectDN().toString()));
	    assertTrue("Expect cert's subject to be as requested (ignoring order)", match);

		validatePresent(LOC_SEL_REVOCATION_REASON);
	    selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

	    click(LOC_BTN_WITHDRAW_CERTIFICATE);

	    // waiting for the form to rebuild ...
		validatePresent(LOC_TA_UPLOAD_CONTENT);

		waitForElement(LOC_LNK_CERTIFICATES_MENUE);
		validatePresent(LOC_LNK_CERTIFICATES_MENUE);
		click(LOC_LNK_CERTIFICATES_MENUE);

	    // select the certificate in the cert list
	    validatePresent(LOC_SEL_CERT_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "subject");

	    validatePresent(LOC_SEL_CERT_CHOICE);

	    selectOptionByText(LOC_SEL_CERT_CHOICE, "EQUAL");

	    validatePresent(LOC_INP_CERT_VALUE);
	    setText(LOC_INP_CERT_VALUE, cn);

	    By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
	    validatePresent(byCertSubject);
	    click(byCertSubject);

	    validatePresent(LOC_TEXT_CERT_REVOCATION_REASON);

	}

	@Test
	public void testCSRSubmitDirect() throws GeneralSecurityException, IOException {

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

		validatePresent(LOC_TA_UPLOAD_CONTENT);

		String cn = "reqTest" + System.currentTimeMillis();
	    String subject = "CN=" + cn + ", O=trustable Ltd, C=DE";
	    X500Principal subjectPrincipal = new X500Principal(subject);
	    String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

	    validatePresent(LOC_TEXT_CONTENT_TYPE);

	    validatePresent(LOC_SEL_PIPELINE);

	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
	    click(LOC_BTN_REQUEST_CERTIFICATE);


        waitForElement(LOC_TEXT_CERT_HEADER);
	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);


		String certAsPem = getText(LOC_TA_DOWNLOAD_CERT_CONTENT);
		System.out.println("PEM cert = \n" + certAsPem );

	    X509Certificate newCert = CryptoUtil.convertPemToCertificate(certAsPem);

	    boolean match = equalsIgnoreOrdering(subjectPrincipal, new X500Principal(newCert.getSubjectDN().toString()));
	    assertTrue("Expect cert's subject to be as requested (ignoring order)", match);

		validatePresent(LOC_SEL_REVOCATION_REASON);
	    selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

	    click(LOC_BTN_WITHDRAW_CERTIFICATE);

	    // waiting for the form to rebuild ...
		validatePresent(LOC_TA_UPLOAD_CONTENT);

		waitForElement(LOC_LNK_CERTIFICATES_MENUE);
		validatePresent(LOC_LNK_CERTIFICATES_MENUE);
		click(LOC_LNK_CERTIFICATES_MENUE);

	    // select the certificate in the cert list
	    validatePresent(LOC_SEL_CERT_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "subject");

	    validatePresent(LOC_SEL_CERT_CHOICE);

	    selectOptionByText(LOC_SEL_CERT_CHOICE, "EQUAL");

	    validatePresent(LOC_INP_CERT_VALUE);
	    setText(LOC_INP_CERT_VALUE, cn);

	    By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
	    validatePresent(byCertSubject);
	    click(byCertSubject);

	    validatePresent(LOC_TEXT_CERT_REVOCATION_REASON);
	}

	@Test
	public void testCSRSubmitRACheck() throws GeneralSecurityException, IOException {

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

		validatePresent(LOC_TA_UPLOAD_CONTENT);

		String cn = "reqTest" + System.currentTimeMillis();
	    String subject = "CN=" + cn + ", O=trustable Ltd, C=DE";
	    X500Principal subjectPrincipal = new X500Principal(subject);
	    String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

	    validatePresent(LOC_TEXT_CONTENT_TYPE);

	    validatePresent(LOC_SEL_PIPELINE);

	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_RA_ISSUANCE);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
	    click(LOC_BTN_REQUEST_CERTIFICATE);

	    validatePresent(LOC_TEXT_CSR_HEADER);

	    // switch to RA officer role
		signIn(USER_NAME_RA, USER_PASSWORD_RA);

	    validatePresent(LOC_LNK_REQUESTS_MENUE);
	    click(LOC_LNK_REQUESTS_MENUE);

	    validatePresent(LOC_TEXT_REQUEST_LIST);

	    validatePresent(LOC_SEL_CSR_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CSR_ATTRIBUTE, "subject");

	    validatePresent(LOC_SEL_CSR_CHOICE);

	    selectOptionByText(LOC_SEL_CSR_CHOICE, "EQUAL");

	    validatePresent(LOC_INP_CSR_VALUE);
	    setText(LOC_INP_CSR_VALUE, cn);

	    validatePresent(LOC_TD_CSR_ITEM_PENDING);
	    click(LOC_TD_CSR_ITEM_PENDING);

	    validatePresent(LOC_TEXT_CSR_HEADER);

	    By bySubject = By.xpath("//div//dl/dd/span [contains(text(), '"+cn+"')]");
	    validatePresent(bySubject);

	    validatePresent(LOC_BTN_CONFIRM_REQUEST);
	    click(LOC_BTN_CONFIRM_REQUEST);

	    validatePresent(LOC_LNK_REQUESTS_MENUE);
	    click(LOC_LNK_REQUESTS_MENUE);

	    validatePresent(LOC_TEXT_REQUEST_LIST);

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		click(LOC_LNK_CERTIFICATES_MENUE);

	    validatePresent(LOC_TEXT_CERTIFICATE_LIST);


	    validatePresent(LOC_SEL_CERT_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "subject");

	    validatePresent(LOC_SEL_CERT_CHOICE);

	    selectOptionByText(LOC_SEL_CERT_CHOICE, "EQUAL");

	    validatePresent(LOC_INP_CERT_VALUE);
	    setText(LOC_INP_CERT_VALUE, cn);

	    By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
	    validatePresent(byCertSubject);
	    click(byCertSubject);

	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);

/*
		try {
			System.out.println("... waiting ...");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/

	}

	private void signIn(final String user, final String password) {

		if( isPresent(LOC_TXT_WEBPACK_ERROR) ) {
			System.err.println(
					"###########################################################\n"+
					"Startup failed, webpack missing. Please build full package.\n"+
			        "###########################################################\n");
		}

		validatePresent(LOC_LNK_ACCOUNT_MENUE);

		// log out, if logged in
		logOut();

		validatePresent(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);
		click(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);

		validatePresent(LOC_LNK_SIGNIN_USERNAME);
		validatePresent(LOC_LNK_SIGNIN_PASSWORD);
		validatePresent(LOC_BTN_SIGNIN_SUBMIT);

		setText(LOC_LNK_SIGNIN_USERNAME, user);
		setText(LOC_LNK_SIGNIN_PASSWORD, password);
		click(LOC_BTN_SIGNIN_SUBMIT);

	}

	private void logOut() {
		validatePresent(LOC_LNK_ACCOUNT_MENUE);

		if( isPresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE)) {
			LOG.debug("Logging out ...");
			validatePresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
			click(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
		}else {
			LOG.debug("Already logged out ...");
		}
	}

	  public String buildCSRAsPEM( final X500Principal subjectPrincipal ) throws GeneralSecurityException, IOException{
		    KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

          return CryptoUtil.getCsrAsPEM(subjectPrincipal,
		          keyPair.getPublic(),
		              keyPair.getPrivate(),
		              "password".toCharArray());
	  }

}
