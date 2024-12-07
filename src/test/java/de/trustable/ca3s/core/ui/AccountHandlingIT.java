package de.trustable.ca3s.core.ui;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FLAGS;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import de.trustable.util.JCAManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Ca3SApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = { "mailservice.mock.enabled=false" })
@Config(
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

    public static final By LOC_INP_NEW_PASSWORD_VALUE = By.xpath("//div/input [@name = 'newPassword']");
    public static final By LOC_INP_CONFIRM_PASSWORD_VALUE = By.xpath("//div/input [@name = 'confirmPassword']");

    public static final By LOC_BTN_REGISTER = By.xpath("//form/button [@type='submit'][text() = 'Register']");

    public static final By LOC_TEXT_REGISTRATION_SUCCESSFUL = By.xpath("//div/strong [text() = 'Registration saved!']");

    public static final By LOC_LNK_NEW_ACCOUNT_SIGN_IN = By.xpath("//div/a [text() = 'sign in']");

    public static final By LOC_LNK_SIGNIN_RESET = By.xpath("//div/a [@href='/reset/request']");
    public static final By LOC_BTN_RESET = By.xpath("//form/button [@type='submit'][text() = 'Reset password']");
    public static final By LOC_BTN_SAVE = By.xpath("//form/button [@type='submit'][text() = 'Save']");


    @LocalServerPort
    int serverPort; // random port chosen by spring test

    @Autowired
    PreferenceTestConfiguration prefTC;


    @BeforeAll
    public static void setUpBeforeClass() throws IOException, MessagingException {

        JCAManager.getInstance();
        WebDriverManager.chromedriver().setup();

        startEmailMock();
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
    public void testCreateNewAccount() throws MessagingException, IOException {

        byte[] loginBytes = new byte[6];
        rand.nextBytes(loginBytes);
        String loginName = "User_" + encodeBytesToText(loginBytes);

        byte[] passwordBytes = new byte[6];
        rand.nextBytes(passwordBytes);
        String loginPassword = "S3cr3t!S_" + encodeBytesToText(passwordBytes);
        String newPassword = "New!S_" + encodeBytesToText(passwordBytes);

        IMAPStore imapStore;
        Folder inbox;

        imapStore = greenMailSMTPIMAP.getImap().createStore();
        imapStore.connect(emailAddress, emailPassword);
        inbox = imapStore.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        dropMessagesFromInbox(inbox);

        waitForElement(LOC_LNK_ACCOUNT_MENUE);
        validatePresent(LOC_LNK_ACCOUNT_MENUE);
        click(LOC_LNK_ACCOUNT_MENUE);

        validatePresent(LOC_LNK_ACCOUNT_REGISTER_MENUE);
        click(LOC_LNK_ACCOUNT_REGISTER_MENUE);

        waitForElement(LOC_TEXT_REGISTER_HEADER);

        validatePresent(LOC_INP_LOGIN_VALUE);
        setText(LOC_INP_LOGIN_VALUE, loginName);

        validatePresent(LOC_INP_EMAIL_VALUE);
        setText(LOC_INP_EMAIL_VALUE, emailAddress);

        validatePresent(LOC_INP_1_PASSWORD_VALUE);
        setText(LOC_INP_1_PASSWORD_VALUE, loginPassword);
        validatePresent(LOC_INP_2_PASSWORD_VALUE);
        setText(LOC_INP_2_PASSWORD_VALUE, loginPassword);

        validatePresent(LOC_BTN_REGISTER);
        click(LOC_BTN_REGISTER);

        waitForNewMessage(inbox, 0);
        waitForElement(LOC_TEXT_REGISTRATION_SUCCESSFUL);

        Message msgReceived = inbox.getMessage(1);

        System.out.println( "msgReceived.getContentType() : " + msgReceived.getContentType() );
        System.out.println( "msgReceived.getContent() : " + msgReceived.getContent() );

        String emailContent = msgReceived.getContent().toString();

/*
        <!DOCTYPE html>
<html>
    <head>
        <title>ca3s account activation</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="shortcut icon" href="http://127.0.0.1:8080/favicon.ico" />
    </head>
    <body>
        <patternActivateKey>Dear user_zi31rjdo</patternActivateKey>
        <patternActivateKey>Your ca3s account has been created, please click on the URL below to activate it:</patternActivateKey>
        <patternActivateKey>
            <a href="http://127.0.0.1:8080/account/activate?key=J1k3Q1DCw5xFStOVa9k7&amp;instantLogin=false">activation link</a>
        </patternActivateKey>
        <patternActivateKey>
            <span>Regards,</span>
            <br/>
            <em>ca3s Team.</em>
        </patternActivateKey>
    </body>
</html>

 */
        Pattern patternActivateKey = Pattern.compile("<a href=\"http:\\/\\/.*:.*(\\/account\\/activate\\?key=.*)\">");
        Matcher m = patternActivateKey.matcher(emailContent);
        assertTrue(m.find());

        String activateUrl = m.group(1);
        System.out.println( "Confirming account at " + activateUrl);

        navigateTo(activateUrl);

        click(LOC_LNK_NEW_ACCOUNT_SIGN_IN);

        signIn(loginName, loginPassword);

        logOut();


        imapStore = greenMailSMTPIMAP.getImap().createStore();
        imapStore.connect("admin@localhost", emailPassword);
        inbox = imapStore.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // reset password
        dropMessagesFromInbox(inbox);

        validatePresent(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);
        click(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);

        validatePresent(LOC_LNK_SIGNIN_USERNAME);
        validatePresent(LOC_LNK_NEW_ACCOUNT_SIGN_IN);

        validatePresent(LOC_LNK_SIGNIN_RESET);
        click(LOC_LNK_SIGNIN_RESET);

        validatePresent(LOC_LNK_SIGNIN_USERNAME);
        setText(LOC_LNK_SIGNIN_USERNAME, "admin");

        int nMsgCurrent = inbox.getMessageCount();
        Assertions.assertTrue(isEnabled(LOC_BTN_RESET), "Expecting reset button enabled");
        validatePresent(LOC_BTN_RESET);
        click(LOC_BTN_RESET);

        waitForNewMessage(inbox, nMsgCurrent);

        // waitForElement(LOC_TEXT_REGISTRATION_SUCCESSFUL);

        Message msgResetReceived = inbox.getMessage(nMsgCurrent + 1);
        System.out.println( "msgReceived.getContentType() : " + msgResetReceived.getContentType() );
        System.out.println( "msgReceived.getContent() : " + msgResetReceived.getContent() );

        /*
        <!DOCTYPE html>
<html>
    <head>
        <title>ca3s password reset</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="shortcut icon" href="http://localhost:8080/favicon.ico" />
    </head>
    <body>
        <p>Dear andreas_kuehne</p>
        <p>For your ca3s account a password reset was requested, please click on the URL below to reset it:</p>
        <p>
            <a href="http://localhost:8080/account/reset/finish?key=bSNPNpQEVuxiOHtTOpUH&amp;instantLogin=false">activation link</a>
        </p>
        <p>
            <span>Regards,</span>
            <br/>
            <em>ca3s Team.</em>
        </p>
    </body>
</html>
         */
        String emailReset = msgResetReceived.getContent().toString();
        Pattern patternResetKey = Pattern.compile("<a href=\"http:\\/\\/.*:.*(\\/account\\/reset\\/finish\\?key=.*)\">");
        Matcher matchReset = patternResetKey.matcher(emailReset);
        assertTrue(matchReset.find());

        activateUrl = matchReset.group(1);
        System.out.println( "Confirming account at " + activateUrl);

        navigateTo(activateUrl);

        validatePresent(LOC_INP_NEW_PASSWORD_VALUE);
        setText(LOC_INP_NEW_PASSWORD_VALUE, newPassword);
        validatePresent(LOC_INP_CONFIRM_PASSWORD_VALUE);
        setText(LOC_INP_CONFIRM_PASSWORD_VALUE, newPassword);

        Assertions.assertTrue(isEnabled(LOC_BTN_SAVE), "Expecting reset button enabled");
        click(LOC_BTN_SAVE);

        signIn("admin", newPassword);


        inbox.close();
        imapStore.close();

        /*
        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/


    }

}
