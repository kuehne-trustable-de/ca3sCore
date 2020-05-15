package de.trustable.ca3s.core.ui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

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
import de.trustable.ca3s.core.domain.Pipeline;
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

    public static final By LOC_LNK_ACCOUNT_MENUE = By.xpath("//nav//a [.//span [text() = 'Account']]");
    public static final By LOC_LNK_REQ_CERT_MENUE = By.xpath("//nav//a [.//span [text() = 'Request certificate']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_IN_MENUE = By.xpath("//nav//a [span [text() = 'Sign in']]");
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

    public static final By LOC_BTN_WITHDRAW_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Withdraw']]");

    private static final Logger LOG = LoggerFactory.getLogger(CSRSubmitIT.class);

    
	private static final String USER_NAME_USER = "user";
	private static final String USER_NAME_PASSWORD = "user";

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	@Autowired
	PipelineTestConfiguration ptc;
	
	
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		JCAManager.getInstance();
		
	}

	@BeforeEach
	void init() {
	    waitForUrl();
	    super.startWebDriver();
	    driver.manage().window().setSize(new Dimension(2000,768));
	}
	
	@Test
	public void testCSRSubmitDirect() throws GeneralSecurityException, IOException {

		Pipeline pipeline = ptc.getInternalWebDirectTestPipeline();
		
		signIn(USER_NAME_USER, USER_NAME_PASSWORD);
		
		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);
		
		validatePresent(LOC_TA_UPLOAD_CONTENT);
		
	    String subject = "CN=reqTest" + System.currentTimeMillis() + ", O=trustable Ltd, C=DE";
	    X500Principal subjectPrincipal = new X500Principal(subject);
	    String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);
        
	    validatePresent(LOC_TEXT_CONTENT_TYPE);
	    
	    validatePresent(LOC_SEL_PIPELINE);
	    
	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
	    
	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
	    click(LOC_BTN_REQUEST_CERTIFICATE);
	    
	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);
		
		validatePresent(LOC_SEL_REVOCATION_REASON);
	    selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

	    click(LOC_BTN_WITHDRAW_CERTIFICATE);
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void signIn(final String user, final String password) {
		
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
		    
		      String p10ReqPem = CryptoUtil.getCsrAsPEM(subjectPrincipal,
		          keyPair.getPublic(), 
		              keyPair.getPrivate(), 
		              "password".toCharArray());
		  
		      return( p10ReqPem);
		  }

}
