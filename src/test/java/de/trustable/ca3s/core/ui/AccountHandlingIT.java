package de.trustable.ca3s.core.ui;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FLAGS;
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

    public static final By LOC_TEXT_REGISTRATION_SUCCESSFUL = By.xpath("//div/strong [text() = 'Registration saved!']");

    public static final By LOC_LNK_NEW_ACCOUNT_SIGN_IN = By.xpath("//div/a [text() = 'sign in']");

    private static Random rand = new Random();

    static GreenMail greenMailSMTPIMAP;
    static String emailAddress;
    static String emailPassword;

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    @Autowired
    PreferenceTestConfiguration prefTC;


    @BeforeAll
    public static void setUpBeforeClass() throws IOException {

        JCAManager.getInstance();

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

        IMAPStore imapStore = greenMailSMTPIMAP.getImap().createStore();
        imapStore.connect(emailAddress, emailPassword);
        Folder inbox = imapStore.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        System.out.println("inbox contains " + inbox.getMessageCount() + " messages.");
        while( inbox.getMessageCount() > 0) {
            System.out.println("deleting message ...");
            inbox.getMessage(1).setFlag(FLAGS.Flag.DELETED, true);
        }

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

        waitForElement(LOC_TEXT_REGISTRATION_SUCCESSFUL);

        while( inbox.getMessageCount() == 0) {
            System.out.println( "waiting for message ...");
            try {
                Thread.sleep(1000); // sleep for 1 second.
            } catch (Exception x) {
                fail("Failed due to an exception during Thread.sleep!");
                x.printStackTrace();
            }
        }

        Message msgReceived = inbox.getMessage(1);

        System.out.println( "msgReceived.getContentType() : " + msgReceived.getContentType() );
        System.out.println( "msgReceived.getContent() : " + msgReceived.getContent() );

        String emailContent = msgReceived.getContent().toString();

        Pattern p = Pattern.compile("<a href=\"http:\\/\\/.*:.*(\\/account\\/activate\\?key=.*)\">");
        Matcher m = p.matcher(emailContent);
        assertTrue(m.find());

        String activateUrl = m.group(1);
        System.out.println( "Confirming account at " + activateUrl);

        navigateTo(activateUrl);


        inbox.close();
        imapStore.close();

        validatePresent(LOC_LNK_NEW_ACCOUNT_SIGN_IN);
        click(LOC_LNK_NEW_ACCOUNT_SIGN_IN);

        signIn(loginName, loginPassword);


        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String encodeBytesToText(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes).replace("+", "A").replace("=", "B").replace("/", "C");
    }

}
