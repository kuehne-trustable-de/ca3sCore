package de.trustable.ca3s.core.ui;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sun.mail.imap.protocol.FLAGS;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.test.speech.SoundOutput;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.security.auth.x500.X500Principal;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.fail;

public class WebTestBase extends LocomotiveBase {

    private static final Logger LOG = LoggerFactory.getLogger(WebTestBase.class);

    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
	public static final String SESSION_COOKIE_DEFAULT_VALUE = "DummyCookieValue";

    public static final By LOC_TXT_WEBPACK_ERROR = By.xpath("//div//h1 [text() = 'An error has occurred :-(']");
    public static final By LOC_LNK_ACCOUNT_MENUE = By.xpath("//nav//a [span/@id='account-menu-span']");
    public static final By LOC_LNK_ACCOUNT_SIGN_IN_MENUE =  By.xpath("//nav//a [@id='login']");
    public static final String STRING_LNK_ACCOUNT_LANGUAGE_MENUE =  "//nav//a [contains(text(), '%s')]";
    public static final By LOC_LNK_ACCOUNT_SIGN_OUT_MENUE = By.xpath("//nav//a [@id='logout']");
    public static final By LOC_LNK_USER_SIGNED_IN = By.xpath("//nav//a [@logged_in = 'true']");

    public static final By LOC_LNK_SIGNIN_USERNAME = By.xpath("//form//input [@name = 'username']");
    public static final By LOC_LNK_SIGNIN_PASSWORD = By.xpath("//form//input [@name = 'password']");

    public static final By LOC_INP_SIGNIN_SECOND_FACTOR_TYPE = By.xpath("//div/select [@id = 'second-factor']");
    public static final By LOC_INP_SIGNIN_SECOND_FACTOR_VALUE = By.xpath("//* [@id = 'secondSecret']");

    public static final By LOC_BTN_SIGNIN_SUBMIT = By.xpath("//form//button [(@type='submit') and (@id = 'login.form.submit')]");
    public static final By LOC_TXT_SPOKEN_TEXT = By.xpath("//div[@name='spokenTextBox']");

    public static final By LOC_LOGIN_FAILED_TEXT = By.xpath("//div/strong [text() = 'Failed to sign in!']");

    public static final By LOC_HELP_TARGET_LIST = By.xpath("//a [starts-with(@href,'/helpTargetAdmin')]");

    public static final String USER_NAME_USER = "user";
    public static final String USER_PASSWORD_USER = "S3cr3t!S_user";
    public static final String USER_EMAIL_USER = "user@localhost";
    public static final String USER_NAME_RA = "ra";
    public static final String USER_PASSWORD_RA = "s3cr3t";
    public static final String USER_EMAIL_RA ="ra@localhost";

    public static int testPortHttp;
    public static int testPortHttps;

    static GreenMail greenMailSMTPIMAP;
    static String emailAddress;
    static String emailPassword;

    public boolean playSound = false;

    private String[] speechifyApiTokenArr = {};

    protected SoundOutput soundOutput = null;

    @Autowired
    UserRepository userRepository;


    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        if( "de".equalsIgnoreCase(locale)){
            this.locale = "de";
        }else{
            this.locale = "en";
        }
    }

    void setAllUserLocale(String locale){

        List<User> userList = userRepository.findAll();
        for(User user: userList){
            user.setLangKey(locale);
        }
        userRepository.saveAll(userList);
    }

    private String locale = "en";

    protected static Random rand = new SecureRandom();

    Document tutorialDocument;
    XPath xPath;


    public WebTestBase() {

        super();

        testPortHttp = super.port;
        testPortHttps = super.port;

        // assign the ports for this test to random values to avoid collisions to other instances
        System.setProperty(Ca3SApp.SERVER_TLS_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_RA_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_ACME_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_SCEP_PREFIX + "port", "" + testPortHttp);


        ClassPathResource explainationsResource =  new ClassPathResource("tutorial/explanations.xml");

        DocumentBuilderFactory builderFactory = newSecureDocumentBuilderFactory();
        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
            tutorialDocument = builder.parse(explainationsResource.getInputStream());
            xPath = XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }


    }

/*
    public WebTestBase(String[] speechifyApiTokenArr) {
        this();
        soundOutput = new SoundOutput(speechifyApiTokenArr);
    }
  */

    void setSpeechifyApiTokenArr(String[] speechifyApiTokenArr){
        soundOutput = new SoundOutput(speechifyApiTokenArr);
    }

    protected static void startEmailMock() throws IOException {
        ServerSocket ssSMTP = new ServerSocket(0);
        ServerSocket ssIMAP = new ServerSocket(0);
        int randomPortSMTP = ssSMTP.getLocalPort();
        int randomPortIMAP = ssIMAP.getLocalPort();
        ssSMTP.close();
        ssIMAP.close();

        ServerSetup smtpSetup = new ServerSetup(randomPortSMTP, null, ServerSetup.PROTOCOL_SMTP);
        ServerSetup imapSetup = new ServerSetup(randomPortIMAP, null, ServerSetup.PROTOCOL_IMAP);
        greenMailSMTPIMAP = new GreenMail(new ServerSetup[]{smtpSetup,imapSetup});

        greenMailSMTPIMAP.start();

        System.setProperty("spring.mail.host", "localhost");
        System.setProperty("spring.mail.port", "" + randomPortSMTP);

        System.setProperty("jhipster.mail.from", "ca3s@localhost");

        System.out.println("randomPortSMTP : " + randomPortSMTP);
        System.out.println("randomPortIMAP : " + randomPortIMAP);

        byte[] emailBytes = new byte[6];
        rand.nextBytes(emailBytes);
        emailAddress = "User_" + encodeBytesToText(emailBytes) + "@localhost.com";

        byte[] passwordBytes = new byte[6];
        rand.nextBytes(passwordBytes);
        emailPassword = "PasswordEMail_" + encodeBytesToText(passwordBytes);

        // Create user, as connect verifies pwd
        greenMailSMTPIMAP.setUser(emailAddress, emailAddress, emailPassword);
        System.out.println("create eMail account '" + emailAddress + "', identified by '" + emailPassword + "'");

        greenMailSMTPIMAP.setUser("admin@localhost", "admin@localhost", emailPassword);
        System.out.println("create eMail account 'admin@localhost', identified by '" + emailPassword + "'");


    }


    static String encodeBytesToText(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes).replace("+", "A").replace("=", "B").replace("/", "C");
    }

    protected static void dropMessagesFromInbox(Folder inbox) throws MessagingException {
        int msgCount = inbox.getMessageCount();
        System.out.println("inbox contains " + msgCount + " messages.");
        for( int i = 1 ; i <= msgCount; i++) {
            System.out.println("deleting message ...");
            inbox.getMessage(i).setFlag(FLAGS.Flag.DELETED, true);
        }
        System.out.println("inbox contains " + inbox.getMessageCount() + " messages after delete.");
    }

    protected static void waitForNewMessage(Folder inbox, int nMsgCurrent) throws MessagingException {

        waitForNewMessage(inbox, nMsgCurrent, 60);
    }
    protected static void waitForNewMessage(Folder inbox, int nMsgCurrent, int maxWaitSec) throws MessagingException {

        int waitCounter = 0;
        while( inbox.getMessageCount() == nMsgCurrent) {
            System.out.println( "#" + inbox.getMessageCount() + " messages present, waiting for new message ...");
            try {
                Thread.sleep(1000); // sleep for 1 second.
            } catch (Exception x) {
                fail("Failed due to an exception during Thread.sleep!");
                x.printStackTrace();
            }
            if( waitCounter ++ > maxWaitSec){
                throw new MessagingException("no message received");
            }
        }
    }

    private DocumentBuilderFactory newSecureDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        String[] featuresToDisable = {
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            //This feature has to be used together with the following one, otherwise it will not protect you from XXE for sure
            "http://xml.org/sax/features/external-general-entities",

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            //This feature has to be used together with the previous one, otherwise it will not protect you from XXE for sure
            "http://xml.org/sax/features/external-parameter-entities",

            // Disable external DTDs as well
            "http://apache.org/xml/features/nonvalidating/load-external-dtd"
        };

        for (String feature : featuresToDisable) {
            try {
                dbf.setFeature(feature, false);
            } catch (ParserConfigurationException e) {
                // This should catch a failed setFeature feature
                LOG.info("ParserConfigurationException was thrown. The feature '" + feature
                    + "' is probably not supported by your XML processor.");
            }
        }

        try {
            // Add these as per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            // As stated in the documentation, "Feature for Secure Processing (FSP)" is the central mechanism that will
            // help you safeguard XML processing. It instructs XML processors, such as parsers, validators,
            // and transformers, to try and process XML securely, and the FSP can be used as an alternative to
            // dbf.setExpandEntityReferences(false); to allow some safe level of Entity Expansion
            // Exists from JDK6.
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
            // ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
            // (http://cwe.mitre.org/data/definitions/918.html) and denial
            // of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."

            // remaining parser logic

        } catch (ParserConfigurationException e) {
            // This should catch a failed setFeature feature
            LOG.info("ParserConfigurationException was thrown. The feature 'XMLConstants.FEATURE_SECURE_PROCESSING'"
                + " is probably not supported by your XML processor.");
/*
        } catch (SAXException e) {
            // On Apache, this should be thrown when disallowing DOCTYPE
            LOG.warn("A DOCTYPE was passed into the XML document");

        } catch (IOException e) {
            // XXE that points to a file that doesn't exist
            LOG.error("IOException occurred, XXE may still possible: " + e.getMessage());
*/
        }

        return dbf;
    }


    public static void waitForUrl() {
        waitForUrl(configuration.url());
    }

    public static void waitForUrl(String target) {
		for (int i = 0; i < 30; i++) {
			try {
				System.out.println("connecting to : " + target);

				URL url = new URL(target);
				URLConnection connection = url.openConnection();

				connection.connect();

				// Cast to a HttpURLConnection
				if (connection instanceof HttpURLConnection) {
					HttpURLConnection httpConnection = (HttpURLConnection) connection;

					int code = httpConnection.getResponseCode();
					System.out.println("http response code: " + code);
					if (code >= 200 && code < 300) {
						break;
					}

				} else {
					System.err.println("error - not a http request!");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

    protected void explain(String s, int waitMs) {
        if (playSound) {
            explain(s);
            wait(waitMs);
        }
    }

    protected void explain(String s) {
        if( !playSound){
            return;
        }
        if( s == null || s.isEmpty()){
            return;
        }

        String message = getMessage(s);

        WebElement element = driver.findElement(LOC_TXT_SPOKEN_TEXT);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "arguments[0].style.display='inline';arguments[0].innerText = '"+
                message.replace("'","\\'")+"'",
            element);

        try {
            soundOutput.play(message, getLocale());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String getMessage(String s) {

        String message = s;

        String expression = getExplanationPath(s, getLocale());

        try {

            LOG.warn("Selecting in explanation document by path '{}'", expression);
            String text = (String) xPath.compile(expression).evaluate(tutorialDocument, XPathConstants.STRING);
            if (text == null || text.isEmpty()) {
                LOG.warn("No message in explanation document for path '{}'", expression);
                if (!"en".equalsIgnoreCase(getLocale())) {
                    text = (String) xPath.compile(getExplanationPath(s,"en")).evaluate(tutorialDocument, XPathConstants.STRING);
                }
            }
            if (text == null || text.isEmpty()) {
                LOG.warn("No message in bundle for '{}'", s);
            }else{
                message = text;
            }
        } catch (XPathExpressionException e) {
            LOG.warn("problem processing xpath expression '{}' : {}", expression, e.getMessage());
        }

        LOG.warn("key '{}' retrieves message '{}'", s, message);
        return message;
    }

    String getExplanationPath(String s, String locale){
//        return "/explanation/*[local-name()='" + s.replaceAll("'", "\\'") + "']/"+locale+"/text()[1]";
        return "/explanation/*[local-name()=\"" + s + "\"]/"+locale+"/text()";
    }

    protected void scrollToElement(final By loc) {
        scrollToElement(waitForElement(loc));
    }

    protected void scrollToElement(WebElement webElement){
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoViewIfNeeded()", webElement);
//        Thread.sleep(500);
    }

    protected void selectElementText(final By loc) {
        selectElementText(loc, null);
    }
    protected void selectElementText(final By loc, final String s) {
        selectElementText(waitForElement(loc), s);
    }
    protected void selectElementText(WebElement webElement, final String s) {

        ((JavascriptExecutor)driver).executeScript("arguments[0].setAttribute('style', 'background: lightblue;');", webElement);
        explain(s);
        ((JavascriptExecutor)driver).executeScript("arguments[0].setAttribute('style', 'background: white;');", webElement);
    }

    protected void setSessionCookieDefaultValue() {
		driver.manage().addCookie(new org.openqa.selenium.Cookie(SESSION_COOKIE_NAME, SESSION_COOKIE_DEFAULT_VALUE));
	}

	protected String getSessionCookieValue() {
		// driver.manage().getCookies().forEach(cookie->System.out.println("--------" +
		// cookie.getName() + " : " + cookie.getValue()));
		org.openqa.selenium.Cookie sessionCookie = driver.manage().getCookieNamed(SESSION_COOKIE_NAME);
		if (sessionCookie != null) {
			System.out.println("--------" + sessionCookie.getName() + " : " + sessionCookie.getValue());
			return (sessionCookie.getValue());
		} else {
			return "";
		}
	}

	public String waitForSessionCookieNotValue(String val) {

		int MAX_ATTEMPTS = 10;
		int attempts = 0;
		String sessionVal = getSessionCookieValue();

		while (val.equals(sessionVal)) {
			sessionVal = getSessionCookieValue();
			if (attempts == MAX_ATTEMPTS)
				fail(String.format("Session cookie has not the value '%s' (but '%s') after %d seconds", val, sessionVal,
						MAX_ATTEMPTS));
			attempts++;
			try {
				Thread.sleep(1000); // sleep for 1 second.
			} catch (Exception x) {
				fail("Failed due to an exception during Thread.sleep!");
				x.printStackTrace();
			}
		}

		return sessionVal;

	}

	public boolean equalsIgnoreOrdering(final X500Principal p1, final X500Principal p2) {

		if (p1.equals(p2)) {
			return true;
		}

		String[] p1Parts = p1.toString().split(",");
		String[] p2Parts = p2.toString().split(",");

		for (String part : p1Parts) {

			LOG.debug("checking for '" + part + "'");

			boolean match = false;
			for (String other : p2Parts) {
				LOG.debug("matching against '" + other + "'");
				if (other.trim().equalsIgnoreCase(part.trim())) {
					match = true;
					break;
				}
			}
			if (!match) {
				LOG.debug("did not find a match for '" + part + "'");
				return false;
			}

		}
		return true;
	}

	public void setLongText(final By loc, final String text) {

		WebElement we = waitForElement(loc);
		String javascript = "arguments[0].value = '"+text.replaceAll("(\\r\\n|\\r|\\n)", "\\\\n")+"';";
//		String javascript = "arguments[0].value = 'Foo\nBar\nBaz';";

		System.out.println("javascript: " + javascript);
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript(javascript, we);

		click(loc);
		driver.findElement(loc).sendKeys(Keys.SPACE);
		driver.findElement(loc).sendKeys(Keys.BACK_SPACE);
/*
		click(loc);
		driver.findElement(loc).sendKeys(Keys.CONTROL + "a");
		driver.findElement(loc).sendKeys(Keys.DELETE);

		ITestTextTransfer textTransfer = new ITestTextTransfer();
		textTransfer.setClipboardContents(text);
		driver.findElement(loc).sendKeys(Keys.CONTROL + "v");
*/

	}

    public boolean isEnabled(final By loc) {

        WebElement we = waitForElement(loc);
        return we.isEnabled();
    }
    public boolean isReadOnly(final By loc) {

        WebElement we = waitForElement(loc);
        String attVal = we.getAttribute("readonly");
        if(attVal == null) {
            return false;
        }
        return attVal.equalsIgnoreCase("true");
    }

    public void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    void selectLanguage(String locale){

        String langDescriptor = "English";
        if("de".equalsIgnoreCase(locale)){
            langDescriptor = "Deutsch";
        }else if("pl".equalsIgnoreCase(locale)) {
            langDescriptor = "Polski";
        }

        String langSelector = String.format(STRING_LNK_ACCOUNT_LANGUAGE_MENUE, langDescriptor);
        System.out.println("langSelector: " + langSelector);
        By byLang = By.xpath(langSelector);
        validatePresent(LOC_LNK_ACCOUNT_MENUE);
        click(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);

        validatePresent(byLang);
        click(byLang);
    }

    void signIn(final String user, final String password) {
         signIn ( user, password, null, 0);
    }

    void signIn(String user, String password, Totp totp) {
        signIn ( user, password, totp, null, 0, false);
    }

    void signIn(final String user, final String password, String s, int waitMillis) {
        signIn(user, password, null, s, waitMillis, false);
    }
    void signIn(final String user, final String password, Totp totp, String s, int waitMillis, boolean expectFailure) {

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
        validatePresent(LOC_INP_SIGNIN_SECOND_FACTOR_TYPE);
        validatePresent(LOC_BTN_SIGNIN_SUBMIT);

        setText(LOC_LNK_SIGNIN_USERNAME, user);
        setText(LOC_LNK_SIGNIN_PASSWORD, password);

        if( totp != null ) {
            selectOptionByValue(LOC_INP_SIGNIN_SECOND_FACTOR_TYPE, "TOTP");

            validatePresent(LOC_INP_SIGNIN_SECOND_FACTOR_VALUE);
            setText(LOC_INP_SIGNIN_SECOND_FACTOR_VALUE, totp.now());
        } else {
            selectOptionById(LOC_INP_SIGNIN_SECOND_FACTOR_TYPE, 0);
        }

        explain(s);
        wait(waitMillis);
        click(LOC_BTN_SIGNIN_SUBMIT);

        if( expectFailure ) {
            wait(1000);
            validatePresent(LOC_LOGIN_FAILED_TEXT);
            driver.findElement(LOC_LNK_SIGNIN_PASSWORD).sendKeys(Keys.ESCAPE);
        } else {
            validateNotPresent(LOC_LOGIN_FAILED_TEXT);
        }
        wait(500);

    }

    void logOut() {
        validatePresent(LOC_LNK_ACCOUNT_MENUE);

        if( isPresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE)) {
            LOG.debug("Logging out ...");
            validatePresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
            click(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
        }else {
            LOG.debug("Already logged out ...");
        }
    }

    void checkHelpTargets() {

        List<String> missingHelpRefs = new ArrayList<>();
        List<WebElement> webElementList = driver.findElements(LOC_HELP_TARGET_LIST);
        Assertions.assertFalse(webElementList.isEmpty(), "Expect some help targets on the page");
        for( WebElement we: webElementList){
            we.click();
            waitForWindow("ca3s Admin Help");
            switchToWindow("ca3s Admin Help");

            try {
                URL url = new URL(driver.getCurrentUrl());
//                System.out.println("CurrentUrl: " + url);
//                System.out.println("ref = " + url.getRef());

                By helpItem = By.xpath("//a [@id = '" + url.getRef() + "']");
                if (driver.findElements(helpItem).isEmpty()){
                    missingHelpRefs.add(url.getRef());
                }

            } catch (MalformedURLException e) {
                System.out.println( "MalformedURLException : " + e.getMessage());
            }

            switchToWindow("ca3s");
        }
        if( !missingHelpRefs.isEmpty()){
            System.out.println("\n######### Missing help targets found !");
            for( String missíngRef: missingHelpRefs){
                System.out.println("Missing help target: " + missíngRef);
            }
        }
        Assertions.assertEquals(0, missingHelpRefs.size(), "No missing help targets expected");
    }
}
