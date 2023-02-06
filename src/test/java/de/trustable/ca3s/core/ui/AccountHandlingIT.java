package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import io.ddavison.conductor.Browser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Base64;
import java.util.Random;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@io.ddavison.conductor.Config(
    browser = Browser.CHROME,
    url     = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class AccountHandlingIT extends WebTestBase{

    public static final By LOC_LNK_ACCOUNT_REGISTER_MENUE = By.xpath("//nav//a [span [text() = 'Register']]");

    public static final By LOC_TEXT_REGISTER_HEADER = By.xpath("//div/h1 [text() = 'Registration']");

    public static final By LOC_INP_LOGIN_VALUE = By.xpath("//div/input [@name = 'login']");
    public static final By LOC_INP_EMAIL_VALUE = By.xpath("//div/input [@name = 'email']");
    public static final By LOC_INP_1_PASSWORD_VALUE = By.xpath("//div/input [@name = 'password']");
    public static final By LOC_INP_2_PASSWORD_VALUE = By.xpath("//div/input [@name = 'confirmPasswordInput']");

    public static final By LOC_BTN_REGISTER = By.xpath("//form/button [@type='submit'][text() = 'Register']");

    public static final By LOC_LNK_SIGNIN_USERNAME = By.xpath("//form//input [@name = 'username']");
    public static final By LOC_LNK_SIGNIN_PASSWORD = By.xpath("//form//input [@name = 'password']");
    public static final By LOC_BTN_SIGNIN_SUBMIT = By.xpath("//form//button [@type='submit'][text() = 'Sign in']");

    public static final By LOC_BTN_REQUEST_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Request certificate']]");

    public static final By LOC_TEXT_ACCOUNT_NAME = By.xpath("//nav//span [contains(text(), '\"user\"')]");

    private static final String USER_NAME_USER = "user";
    private static final String USER_PASSWORD_USER = "user";

    private static final String USER_NAME_ADMIN = "admin";
    private static final String USER_PASSWORD_ADMIN = "admin";

    private static Random rand = new Random();


    @LocalServerPort
    int serverPort; // random port chosen by spring test

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeAll
    public static void setUpBeforeClass() {
        JCAManager.getInstance();
    }

    @BeforeEach
    void init() {

        waitForUrl();

        prefTC.getTestUserPreference();

        if( driver == null) {
            super.startWebDriver();
            driver.manage().window().setSize(new Dimension(2000,768));
        }
    }

    @Test
    public void testCreateNewAccount()  {

        byte[] loginBytes = new byte[6];
        rand.nextBytes(loginBytes);
        String loginName = "User_" + Base64.getEncoder().encodeToString(loginBytes);

        byte[] emailBytes = new byte[6];
        rand.nextBytes(emailBytes);
        String email = "User_" + Base64.getEncoder().encodeToString(emailBytes) + "@localhost";

        byte[] passwordBytes = new byte[6];
        rand.nextBytes(passwordBytes);
        String password = "Password_" + Base64.getEncoder().encodeToString(passwordBytes);

//        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        waitForElement(LOC_LNK_ACCOUNT_MENUE);
        validatePresent(LOC_LNK_ACCOUNT_MENUE);
        click(LOC_LNK_ACCOUNT_MENUE);

        validatePresent(LOC_LNK_ACCOUNT_REGISTER_MENUE);
        click(LOC_LNK_ACCOUNT_REGISTER_MENUE);

        waitForElement(LOC_TEXT_REGISTER_HEADER);

        validatePresent(LOC_INP_LOGIN_VALUE);
        setText(LOC_INP_LOGIN_VALUE, loginName);

        validatePresent(LOC_INP_EMAIL_VALUE);
        setText(LOC_INP_EMAIL_VALUE, email);

        validatePresent(LOC_INP_1_PASSWORD_VALUE);
        setText(LOC_INP_1_PASSWORD_VALUE, password);
        validatePresent(LOC_INP_2_PASSWORD_VALUE);
        setText(LOC_INP_2_PASSWORD_VALUE, password);

        validatePresent(LOC_BTN_REGISTER);
        click(LOC_BTN_REGISTER);



        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
