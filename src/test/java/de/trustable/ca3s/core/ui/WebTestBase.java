package de.trustable.ca3s.core.ui;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class WebTestBase extends LocomotiveBase {

	public static final String SESSION_COOKIE_NAME = "JSESSIONID";
	public static final String SESSION_COOKIE_DEFAULT_VALUE = "DummyCookieValue";

	public WebTestBase() {
		super();
	}

	public void waitForUrl() {
		for (int i = 0; i < 30; i++) {
			try {
				System.out.println("connecting to : " + configuration.url());

				URL url = new URL(configuration.url());
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

	void setLongText(final By loc, final String text) {

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

}
