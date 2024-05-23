package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.test.speech.SoundOutput;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.fail;

public class WebTestBase extends LocomotiveBase {

	public static final String SESSION_COOKIE_NAME = "JSESSIONID";
	public static final String SESSION_COOKIE_DEFAULT_VALUE = "DummyCookieValue";

    public static final By LOC_TXT_WEBPACK_ERROR = By.xpath("//div//h1 [text() = 'An error has occured :-(']");
    public static final By LOC_LNK_ACCOUNT_MENUE =          By.xpath("//nav//a [.//span [text() = 'Account']]");

    public static final By LOC_LNK_ACCOUNT_SIGN_IN_MENUE =  By.xpath("//nav//a [span [text() = 'Sign in']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_OUT_MENUE = By.xpath("//nav//a [span [text() = 'Sign out']]");

    public static final By LOC_LNK_SIGNIN_USERNAME = By.xpath("//form//input [@name = 'username']");
    public static final By LOC_LNK_SIGNIN_PASSWORD = By.xpath("//form//input [@name = 'password']");
    public static final By LOC_BTN_SIGNIN_SUBMIT = By.xpath("//form//button [@type='submit'][text() = 'Sign in']");

    private static final Logger LOG = LoggerFactory.getLogger(WebTestBase.class);

    public static int testPortHttp;
    public static int testPortHttps;

    public boolean playSound = false;

/*-
    static{

        testPortHttp = SocketUtils.findAvailableTcpPort();
        testPortHttps = SocketUtils.findAvailableTcpPort();

        // assign the ports for this test to random values to avoid collisions to other instances
        System.setProperty(Ca3SApp.SERVER_TLS_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_RA_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_ACME_PREFIX + "port", "" + testPortHttps);
        System.setProperty(Ca3SApp.SERVER_SCEP_PREFIX + "port", "" + testPortHttp);

    }
*/


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
					if (code >= 200 && code < 299) {
						break;
					}

				} else {
					System.err.println("error - not a http request!");
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

        SoundOutput soundOutput = null;
        try {
            soundOutput = new SoundOutput(s);
            soundOutput.play();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void signIn(final String user, final String password) {
         signIn ( user, password, null, 0);
    }
    void signIn(final String user, final String password, String s, int waitMillis) {

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
        explain(s);
        wait(waitMillis);
        click(LOC_BTN_SIGNIN_SUBMIT);

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

}
