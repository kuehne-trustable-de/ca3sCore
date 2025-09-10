package de.trustable.ca3s.core.ui;

import com.google.common.base.Strings;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

// public class LocomotiveBase implements Conductor<LocomotiveBase>{
public class LocomotiveBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocomotiveBase.class);

    /**
     * All test configuration in here
     */
    static Config configuration;

    static WebDriver driver;

    // max seconds before failing a script.
    private final int MAX_ATTEMPTS = 5;

    private int attempts = 0;

    public Actions actions;

    private Map<String, String> vars = new HashMap<>();

    /**
     * The url that an automated test will be testing.
     */
    public String baseUrl;

    @Value("${local.server.port}")
    int port; //random port chosen by spring test


    File downloadDir;

    private Pattern p;
    private Matcher m;

    public LocomotiveBase() {
        final Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/default.properties"));
        } catch (Exception e) {
            logFatal("Couldn't load default properties");
        }
        try {
            File userDownloadDir = new File(FileUtils.getUserDirectory(), "Downloads");
            downloadDir = userDownloadDir;

//            File userDownloadDir = Files.createTempDirectory("tmpDirCa3s").toFile();
//            downloadDir = new File(userDownloadDir, "ca3s_test");
//            downloadDir = new File(userDownloadDir, "ca3s_test");
//            downloadDir.delete();
//            downloadDir.mkdirs();
            logInfo("downloadDir : " + downloadDir.getAbsolutePath());
//            downloadDir = Files.createTempDirectory("tmpDirPrefix").toFile();
        } catch (Exception e) {
            logFatal("Couldn't create downloadDir");
        }

        /**
         * Order of overrides:
         * <ul>
         *     <li>Test</li>
         *     <li>JVM Arguments</li>
         *     <li>Default properties</li>
         * </ul>
         */
        final Config testConfiguration = getClass().getAnnotation(Config.class);

        configuration = new Config() {
            @Override
            public String url() {
                String url = "";
                if (!StringUtils.isEmpty(getJvmProperty("CONDUCTOR_URL"))) url = getJvmProperty("CONDUCTOR_URL");
                if (!StringUtils.isEmpty(props.getProperty("url"))) url = props.getProperty("url");
                if (testConfiguration != null && (!StringUtils.isEmpty(testConfiguration.url()))) url = testConfiguration.url();

                if( url.contains("${local.server.port}") ){
                    url = url.replace("${local.server.port}", ""+port );
                }
                return url;
            }

            @Override
            public Browser browser() {
                Browser browser = Browser.NONE;
                if (!StringUtils.isEmpty(getJvmProperty("CONDUCTOR_BROWSER")))
                    browser = Browser.valueOf(getJvmProperty("CONDUCTOR_BROWSER").toUpperCase());
                if (testConfiguration != null && testConfiguration.browser() != Browser.NONE) return testConfiguration.browser();
                if (!StringUtils.isEmpty(props.getProperty("browser")))
                    browser = Browser.valueOf(props.getProperty("browser").toUpperCase());
                return browser;
            }

            @Override
            public String hub() {
                String hub = "";
                if (!StringUtils.isEmpty(getJvmProperty("CONDUCTOR_HUB"))) hub = getJvmProperty("CONDUCTOR_HUB");
                if (!StringUtils.isEmpty(props.getProperty("hub"))) hub = props.getProperty("hub");
                if (testConfiguration != null && (!StringUtils.isEmpty(testConfiguration.hub()))) hub = testConfiguration.hub();
                return hub;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String baseUrl() {

                String url = url();
                LOGGER.warn("returning url() for baseUrl() : '" + url + "'" );
                return url;
            }

            @Override
            public String path() {

                URL url;
                try {
                    url = new URL(url());
                } catch (MalformedURLException e) {
                    LOGGER.error("baseUrl() is not an URL", e );
                    return "";
                }

                String path = url.getPath();
                LOGGER.warn("returning path '"+ path +"' from baseUrl() : '" + url() + "'" );

                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    public void startWebDriver(){

        baseUrl = configuration.url();

        LOGGER.info(String.format("\n=== Configuration ===\n" +
            "\tURL:     %s\n" +
            "\tBrowser: %s\n" +
            "\tHub:     %s\n", configuration.url(), configuration.browser().name(), configuration.hub()));
        boolean isLocal = StringUtils.isEmpty(configuration.hub());

        switch (configuration.browser()) {
            case CHROME:
                String driverName = "chromedriver";
                if( SystemUtils.IS_OS_WINDOWS ){
                    driverName = "chromedriver.exe";
                }

                try{

                    URL resourceURL = ResourceUtils.getURL( "classpath:drivers/" + driverName);
                    File tmpFile = File.createTempFile("ca3sTest", driverName);

                    Files.copy( resourceURL.openStream(),
                        tmpFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                    if( SystemUtils.IS_OS_LINUX ) {
                        Set<PosixFilePermission> perms = new HashSet<>();
                        perms.add(PosixFilePermission.OWNER_READ);
                        perms.add(PosixFilePermission.OWNER_WRITE);
                        perms.add(PosixFilePermission.OWNER_EXECUTE);
                        Files.setPosixFilePermissions(tmpFile.toPath(), perms);
                    }

//                    System.setProperty("webdriver.chrome.driver", tmpFile.getAbsolutePath());
//                    System.err.println("starting local Chrome using driver at : " + System.getProperty("webdriver.chrome.driver"));

                    if (isLocal) {
                        try {

                            WebDriverManager.chromedriver().setup();

                            LOGGER.info("chrome driver at {}", System.getProperty("webdriver.chrome.driver"));

                            ChromeOptions options = new ChromeOptions();
                            options.addArguments("--remote-debugging-pipe");

                            options.addArguments("--no-sandbox");
                            options.addArguments("--disable-dev-shm-usage");
                            options.addArguments("--crash-dumps-dir=/tmp");

                            options.addArguments("safebrowsing.enabled=false");
                            options.addArguments("safebrowsing_for_trusted_sources_enabled=false");
                            options.addArguments("download.prompt_for_download=false");
                            options.addArguments("download.directory_upgrade=true");
                            options.addArguments("--remote-allow-origins=*");

                            options.addArguments("download.default_directory=" + downloadDir.getAbsolutePath() + File.separator);

 //                           options.setExperimentalOption("download.default_directory", downloadDir.getAbsolutePath());
 //                           options.setExperimentalOption("download.prompt_for_download", false);
 //                           options.setExperimentalOption("download.directory_upgrade", true);
 //                           options.setExperimentalOption("safebrowsing.enabled", false);

                            /*
                            options.addArguments("--download.default_directory --" +
                                downloadDir.getAbsolutePath());
                            options.addArguments("--safebrowsing-disable-download-protection");
                            options.addArguments("--safebrowsing-disable-extension-blacklist");
*/

                            options.addArguments("--no-sandbox");
                            options.addArguments("--disable-dev-shm-usage");

//                            driver = WebDriverManager.chromedriver().capabilities(options).create();

                            HashMap<String, Object>  chromePrefs = new HashMap<>();
                            chromePrefs.put("profile.default_content_settings.popups", 0);
                            chromePrefs.put("download.default_directory", downloadDir.getAbsolutePath());
                            options.setExperimentalOption("prefs", chromePrefs);

                            driver = new ChromeDriver(options);

                        } catch (Exception x) {
                            LOGGER.error("starting chrome driver, exiting ...", x);
                            logFatal("chromedriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                            System.exit(1);
                        }
                    }
                }catch(Exception ioe){
                    ioe.printStackTrace();
                    System.err.println("problem installing chrome driver, exiting ...");
                    System.exit(1);
                }
                break;
            case FIREFOX: {
                FirefoxOptions options = new FirefoxOptions();
                if (isLocal) driver = new FirefoxDriver(options);
                break;
            }
            case INTERNET_EXPLORER: {
                logFatal("iedriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                InternetExplorerOptions options = new InternetExplorerOptions();
                if (isLocal) driver = new InternetExplorerDriver(options);
                break;
            }
            case SAFARI: {
                logFatal("safaridriver not found. See https://github.com/ddavison/conductor/wiki/WebDriver-Executables for more information.");
                System.exit(1);
                SafariOptions options = new SafariOptions();
                if (isLocal) driver = new SafariDriver(options);
                break;
            }
            default:
                System.err.println("Unknown browser: " + configuration.browser());
                return;
        }

        if (!isLocal) {
            logFatal("RemoteWebDriver not implemented, sorry ...");
            System.exit(1);
            /*
            // they are using a hub.
            RemoteWebDriverOptions options = new RemoteWebDriverOptions();
            try {
                driver = new RemoteWebDriver(new URL(configuration.hub()), capabilities); // just override the driver.
            } catch (Exception x) {
                logFatal("Couldn't connect to hub: " + configuration.hub());
                x.printStackTrace();
                return;
            }
            */

        }
        actions = new Actions(driver);

        if (StringUtils.isNotEmpty(baseUrl)) driver.navigate().to(baseUrl);
    }

    /**
     * Get a Jvm property / environment variable
     * @param prop the property to get
     * @return the property value
     */
    private static String getJvmProperty(String prop) {
        return (System.getProperty(prop, System.getenv(prop)));
    }

    static {
        // Set the webdriver env vars.
        if (getJvmProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.mac"));
        } else if (getJvmProperty("os.name").toLowerCase().contains("nix") ||
            getJvmProperty("os.name").toLowerCase().contains("nux") ||
            getJvmProperty("os.name").toLowerCase().contains("aix")
        ) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.linux"));
        } else if (getJvmProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("webdriver.chrome.driver", findFile("chromedriver.exe"));
            System.setProperty("webdriver.ie.driver", findFile("iedriver.exe"));
        } else {
            System.err.println("Unexpected OS name '" + getJvmProperty("os.name") + "' !");
        }
    }

    static public String findFile(String filename) {
        String[] paths = {"", "bin/", "target/classes"}; // if you have chromedriver somewhere else on the path, then put it here.
        for (String path : paths) {
            if (new File(path + filename).exists())
                return path + filename;
        }
        return "";
    }

    @AfterAll
    public static void teardown() {
        System.err.println("--------------- teardown web driver !");
        if( driver != null) {
            driver.quit();
        }
    }

    public WebElement waitForElement(By by) {
        return waitForElement(by, MAX_ATTEMPTS);
    }
        /**
         * Private method that acts as an arbiter of implicit timeouts of sorts.. sort of like a Wait For Ajax method.
         */
    public WebElement waitForElement(By by, int maxAttempts) {
        int attempts = 0;
/*
        try {
            Thread.sleep(500); // sleep for 1 second.
        } catch (Exception x) {
            fail("Failed due to an exception during Thread.sleep!");
            x.printStackTrace();
        }
*/
        int size = driver.findElements(by).size();

        while (size == 0) {
            size = driver.findElements(by).size();
            if (attempts == maxAttempts) fail(String.format("Could not find %s after %d seconds",
                by.toString(),
                maxAttempts));
            attempts++;
            try {
                Thread.sleep(1000); // sleep for 1 second.
            } catch (Exception x) {
                fail("Failed due to an exception during Thread.sleep!");
                x.printStackTrace();
            }
        }

        if (size > 1) System.err.println("WARN: There are more than 1 " + by.toString() + " 's!");

        return driver.findElement(by);
    }

    public LocomotiveBase click(String css) {
        return click(By.cssSelector(css));
    }

    public LocomotiveBase click(By by) {
        WebElement we = waitForElement(by);

        try{
            we.click();
        }catch( ElementNotInteractableException enie){
            System.out.println("NotInteractable, trying to execute script");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript( "arguments[0].click();", we);
        }catch( WebDriverException wde){
            wde.printStackTrace();

//        	WebDriverWait wait = new WebDriverWait(driver, 10);
//        	wait.until(ExpectedConditions.elementToBeClickable(by));
//          we = waitForElement(by);
            try {
                try {Thread.sleep(200);}catch(Exception x) { x.printStackTrace(); }
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript( "arguments[0].scrollIntoView(true);", we);
                we = waitForElement(by);
            } catch (Exception e) { // just ignore
                System.out.println("Exception while retrying to set value on element '" + by.toString() + "'" + e.getMessage());
            }
            we.click();
        }
        return this;
    }

    public LocomotiveBase setText(String css, String text) {
        return setText(By.cssSelector(css), text);
    }

    public LocomotiveBase setText(By by, String text) {
        WebElement element = waitForElement(by);
        try{
            element.clear();
            element.sendKeys(text);
        }catch( ElementNotInteractableException enie){
            System.out.println("NotInteractable, trying to execute script");
            Duration duration30Sec = Duration.ofSeconds(30);
            new WebDriverWait(driver, duration30Sec).until((ExpectedCondition<Boolean>) wd ->
                ((org.openqa.selenium.JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

//            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript( "arguments[0].value = arguments[1];arguments[0].click()", element, text);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('value', '" + text +"')", element);
            System.out.println("setText for '" + by.toString() + "' to '" + text + "' by javascript.");
        }catch( WebDriverException wde){

            System.out.println("setText failed with Exception: " + wde.getMessage());

            for( int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
                element = waitForElement(by);
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                try {
                    try {Thread.sleep(1000);} catch (Exception x) {x.printStackTrace();}
                    element.clear();
                    element.sendKeys(text);
                } catch (Exception e) { // just ignore
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    public LocomotiveBase hoverOver(String css) {
        return hoverOver(By.cssSelector(css));
    }

    public LocomotiveBase hoverOver(By by) {
        actions.moveToElement(driver.findElement(by)).perform();
        return this;
    }

    public boolean isChecked(String css) {
        return isChecked(By.cssSelector(css));
    }

    public boolean isChecked(By by) {
        return waitForElement(by).isSelected();
    }

    public boolean isPresent(String css) {
        return isPresent(By.cssSelector(css));
    }

    public boolean isPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    public String getText(String css) {
        return getText(By.cssSelector(css));
    }

    public String getText(By by) {
        String text;
        WebElement e = waitForElement(by);

        if (e.getTagName().equalsIgnoreCase("input") || e.getTagName().equalsIgnoreCase("select") || e.getTagName().equalsIgnoreCase("textarea")) {
            text = e.getAttribute("value");
            //           System.out.println("reading attribute 'value' for " + e.getTagName() + " retrieves text '" + text +"'");
        }else {
            text = e.getText();
            //           System.out.println("reading text for " + e.getTagName() + " retrieves text '" + text +"'");
        }

        return text;
    }

    public String getAttribute(String css, String attribute) {
        return getAttribute(By.cssSelector(css), attribute);
    }

    public String getAttribute(By by, String attribute) {
        return waitForElement(by).getAttribute(attribute);
    }

    public LocomotiveBase check(String css) {
        return check(By.cssSelector(css));
    }

    public LocomotiveBase check(By by) {
        if (!isChecked(by)) {
            waitForElement(by).click();
            assertTrue(by.toString() + " did not check!", isChecked(by));
        }
        return this;
    }

    public LocomotiveBase uncheck(String css) {
        return uncheck(By.cssSelector(css));
    }

    public LocomotiveBase uncheck(By by) {
        if (isChecked(by)) {
            waitForElement(by).click();
            assertFalse(by.toString() + " did not uncheck!", isChecked(by));
        }
        return this;
    }

    public LocomotiveBase selectOptionByText(String css, String text) {
        return selectOptionByText(By.cssSelector(css), text);
    }

    public LocomotiveBase selectOptionByText(By by, String text) {

        WebElement selectElement = waitForElement(by);
        Select box = new Select(selectElement);
        try {
            box.selectByVisibleText(text);
        }catch (ElementNotInteractableException enie){

            ((JavascriptExecutor) driver).executeScript("arguments[0].style.cssText = {'overflow': 'visible', 'position': 'static'}", box);
            box.selectByVisibleText(text);
        }
        return this;
    }

    public LocomotiveBase selectOptionById(By by, int idx) {

        Select box = new Select(waitForElement(by));

        LOGGER.debug("selection box is visible: " + box.getWrappedElement().isDisplayed());

        for( WebElement we: box.getOptions()){
            LOGGER.debug("option: " + we + " is visible: " + we.isDisplayed());
        }

        waitForElement(by).click();

        try {
            box.selectByIndex(idx);
        }catch (ElementNotInteractableException enie){
            box.selectByIndex(idx);
        }
        return this;
    }

    public LocomotiveBase selectOptionByValue(String css, String value) {
        return selectOptionByValue(By.cssSelector(css), value);
    }

    public LocomotiveBase selectOptionByValue(By by, String value) {
        Select box = new Select(waitForElement(by));
        box.selectByValue(value);
        return this;
    }

    /* Window / Frame Switching */

    public LocomotiveBase waitForWindow(String regex) {
        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            try {
                driver.switchTo().window(window);

                p = Pattern.compile(regex);
                m = p.matcher(driver.getCurrentUrl());

                if (m.find()) {
                    attempts = 0;
                    return switchToWindow(regex);
                }
                else {
                    // try for title
                    m = p.matcher(driver.getTitle());

                    if (m.find()) {
                        attempts = 0;
                        return switchToWindow(regex);
                    }
                }
            } catch(NoSuchWindowException e) {
                if (attempts <= MAX_ATTEMPTS) {
                    attempts++;

                    try {Thread.sleep(1000);}catch(Exception x) { x.printStackTrace(); }

                    return waitForWindow(regex);
                } else {
                    fail("Window with url|title: " + regex + " did not appear after " + MAX_ATTEMPTS + " tries. Exiting.");
                }
            }
        }

        // when we reach this point, that means no window exists with that title..
        if (attempts == MAX_ATTEMPTS) {
            fail("Window with title: " + regex + " did not appear after 5 tries. Exiting.");
            return this;
        } else {
            System.out.println("#waitForWindow() : Window doesn't exist yet. [" + regex + "] Trying again. " + attempts + "/" + MAX_ATTEMPTS);
            attempts++;
            try {Thread.sleep(1000);}catch(Exception x) { x.printStackTrace(); }
            return waitForWindow(regex);
        }
    }

    public LocomotiveBase switchToWindow(String regex) {
        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            driver.switchTo().window(window);
            System.out.printf("#switchToWindow() : title=%s ; url=%s%n",
                driver.getTitle(),
                driver.getCurrentUrl());

            p = Pattern.compile(regex);
            m = p.matcher(driver.getTitle());

            if (m.find()) return this;
            else {
                m = p.matcher(driver.getCurrentUrl());
                if (m.find()) return this;
            }
        }

        fail("Could not switch to window with title / url: " + regex);
        return this;
    }

    public LocomotiveBase closeWindow(String regex) {
        if (regex == null) {
            driver.close();

            if (driver.getWindowHandles().size() == 1)
                driver.switchTo().window(driver.getWindowHandles().iterator().next());

            return this;
        }

        Set<String> windows = driver.getWindowHandles();

        for (String window : windows) {
            try {
                driver.switchTo().window(window);

                p = Pattern.compile(regex);
                m = p.matcher(driver.getTitle());

                if (m.find()) {
                    switchToWindow(regex); // switch to the window, then close it.
                    driver.close();

                    if (windows.size() == 2) // just default back to the first window.
                        driver.switchTo().window(windows.iterator().next());
                } else {
                    m = p.matcher(driver.getCurrentUrl());
                    if (m.find()) {
                        switchToWindow(regex);
                        driver.close();

                        if (windows.size() == 2) driver.switchTo().window(windows.iterator().next());
                    }
                }

            } catch(NoSuchWindowException e) {
                fail("Cannot close a window that doesn't exist. ["+regex+"]");
            }
        }
        return this;
    }

    public LocomotiveBase closeWindow() {
        return closeWindow(null);
    }

    public LocomotiveBase switchToFrame(String idOrName) {
        try {
            driver.switchTo().frame(idOrName);
        } catch (Exception x) {
            fail("Couldn't switch to frame with id or name [" + idOrName + "]");
        }
        return this;
    }

    public LocomotiveBase switchToFrame(int index) {
        try {
            driver.switchTo().frame(index);
        } catch (Exception x) {
            fail("Couldn't switch to frame with an index of [" + index + "]");
        }
        return this;
    }

    public LocomotiveBase switchToDefaultContent() {
        driver.switchTo().defaultContent();
        return this;
    }

    /* ************************ */

    /* Validation Functions for Testing */

    public LocomotiveBase validatePresent(String css) {
        return validatePresent(By.cssSelector(css));
    }

    public LocomotiveBase validatePresent(By by) {
        waitForElement(by);
        assertTrue("Element " + by.toString() + " does not exist!",
            isPresent(by));
        return this;
    }

    public LocomotiveBase validateNotPresent(String css) {
        return validateNotPresent(By.cssSelector(css));
    }

    public LocomotiveBase validateNotPresent(By by) {
        assertFalse("Element " + by.toString() + " exists!", isPresent(by));
        return this;
    }

    public LocomotiveBase validateText(String css, String text) {
        return validateText(By.cssSelector(css), text);
    }

    public LocomotiveBase validateText(By by, String text) {
        String actual = getText(by);

        assertEquals(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual), text, actual);
        return this;
    }

    public LocomotiveBase setAndValidateText(By by, String text) {
        return setText(by, text).validateText(by, text);
    }

    public LocomotiveBase setAndValidateText(String css, String text) {
        return setText(css, text).validateText(css, text);
    }

    public LocomotiveBase validateTextNot(String css, String text) {
        return validateTextNot(By.cssSelector(css), text);
    }

    public LocomotiveBase validateTextNot(By by, String text) {
        String actual = getText(by);

        assertNotEquals(String.format("Text matches! [expected: %s] [actual: %s]", text, actual), text, actual);
        return this;
    }

    public LocomotiveBase validateTextPresent(String text) {
        assertTrue(driver.getPageSource().contains(text));
        return this;
    }

    public LocomotiveBase validateTextNotPresent(String text) {
        assertFalse(driver.getPageSource().contains(text));
        return this;
    }

    public LocomotiveBase validateChecked(String css) {
        return validateChecked(By.cssSelector(css));
    }

    public LocomotiveBase validateChecked(By by) {
        assertTrue(by.toString() + " is not checked!", isChecked(by));
        return this;
    }

    public LocomotiveBase validateUnchecked(String css) {
        return validateUnchecked(By.cssSelector(css));
    }

    public LocomotiveBase validateUnchecked(By by) {
        assertFalse(by.toString() + " is not unchecked!", isChecked(by));
        return this;
    }

    public LocomotiveBase validateAttribute(String css, String attr, String regex) {
        return validateAttribute(By.cssSelector(css), attr, regex);
    }

    public LocomotiveBase validateAttribute(By by, String attr, String regex) {
        String actual = null;
        try {
            actual = driver.findElement(by).getAttribute(attr);
            if (actual.equals(regex)) return this; // test passes.
        } catch (NoSuchElementException e) {
            fail("No such element [" + by.toString() + "] exists.");
        } catch (Exception x) {
            fail("Cannot validate an attribute if an element doesn't have it!");
        }

        p = Pattern.compile(regex);
        m = p.matcher(actual);

        assertTrue(String.format("Attribute doesn't match! [Selector: %s] [Attribute: %s] [Desired value: %s] [Actual value: %s]",
            by.toString(),
            attr,
            regex,
            actual
        ), m.find());

        return this;
    }

    public LocomotiveBase validateUrl(String regex) {
        p = Pattern.compile(regex);
        m = p.matcher(driver.getCurrentUrl());

        assertTrue("Url does not match regex [" + regex + "] (actual is: \""+driver.getCurrentUrl()+"\")", m.find());
        return this;
    }

    public LocomotiveBase validateTrue(boolean condition) {
        assertTrue(condition);
        return this;
    }

    public LocomotiveBase validateFalse(boolean condition) {
        assertFalse(condition);
        return this;
    }

    /* ================================ */

    public LocomotiveBase goBack() {
        driver.navigate().back();
        return this;
    }

    public LocomotiveBase navigateTo(String url) {
        // absolute url
        if (url.contains("://"))      driver.navigate().to(url);
        else if (url.startsWith("/")) driver.navigate().to(baseUrl.concat(url.substring(1))); // drop the leading slash, already included in the base URL
        else                          driver.navigate().to(driver.getCurrentUrl().concat(url));

        return this;
    }

    public LocomotiveBase store(String key, String value) {
        vars.put(key, value);
        return this;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        if (Strings.isNullOrEmpty(vars.get(key))) {
            return defaultValue;
        } else
            return vars.get(key);
    }

    public LocomotiveBase log(Object object) {
        return logInfo(object);
    }

    public LocomotiveBase logInfo(Object msg) {
        LOGGER.info(msg.toString());
        return this;
    }

    public LocomotiveBase logWarn(Object msg) {
        LOGGER.warn(msg.toString());
        return this;
    }

    public LocomotiveBase logError(Object msg) {
        LOGGER.error(msg.toString());
        return this;
    }

    public LocomotiveBase logDebug(Object msg) {
        LOGGER.debug(msg.toString());
        return this;
    }

    public LocomotiveBase logFatal(Object msg) {
        LOGGER.error(msg.toString());
        return this;
    }


    public LocomotiveBase logInfo(String msg) {
        LOGGER.info(msg);
        return this;
    }

    public LocomotiveBase logWarn(String msg) {
        LOGGER.warn(msg);
        return this;
    }

    public LocomotiveBase logError(String msg) {
        LOGGER.error(msg);
        return this;
    }

    public LocomotiveBase logDebug(String msg) {
        LOGGER.debug(msg);
        return this;
    }

    public LocomotiveBase logFatal(String msg) {
        LOGGER.error(msg);
        return this;
    }

    public LocomotiveBase refresh() {
        logFatal("Not implemented: refresh ");
        return null;
    }

    public LocomotiveBase selectOptionByIndex(String arg0, Integer arg1) {
        logFatal("Not implemented: selectOptionByIndex ");
        return null;
    }

    public LocomotiveBase selectOptionByIndex(By arg0, Integer arg1) {
        logFatal("Not implemented: selectOptionByIndex ");
        return null;
    }

    public LocomotiveBase switchToFrame(WebElement arg0) {
        logFatal("Not implemented: switchToFrame ");
        return null;
    }

    public LocomotiveBase waitForCondition(ExpectedCondition<?> arg0, long arg1, long arg2) {
        logFatal("Not implemented: waitForCondition ");
        return null;
    }

}
