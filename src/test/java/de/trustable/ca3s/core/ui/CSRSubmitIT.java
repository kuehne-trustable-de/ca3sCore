package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.test.ScreenRecorderUtil;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import io.ddavison.conductor.Browser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.fail;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@io.ddavison.conductor.Config(
    browser = Browser.CHROME,
    url = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class CSRSubmitIT extends WebTestBase {
    public boolean recordSession = true;


    public static final By LOC_LNK_REQ_CERT_MENUE = By.xpath("//nav//a [.//span [text() = 'Request certificate']]");
    public static final By LOC_LNK_REQUESTS_MENUE = By.xpath("//nav//a [.//span [text() = 'Requests']]");
    public static final By LOC_LNK_CERTIFICATES_MENUE = By.xpath("//nav//a [.//span [text() = 'Certificates']]");

    public static final By LOC_BTN_REQUEST_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Request certificate']]");

    public static final By LOC_TEXT_ACCOUNT_NAME = By.xpath("//nav//span [contains(text(), '\"user\"')]");
    public static final By LOC_TEXT_CONTENT_TYPE = By.xpath("//form//dl [dt[span [text() = 'Content type']]]/dd/span");

    public static final By LOC_TA_UPLOAD_CONTENT = By.xpath("//form//textarea [@name = 'content']");
    public static final By LOC_TA_COMMENT = By.xpath("//form//textarea [@id = 'comment']");
    public static final By LOC_SEL_PIPELINE = By.xpath("//form//select [@id = 'pkcsxx-pipeline']");


    public static final By LOC_TEXT_CERT_HEADER = By.xpath("//div/h2/span [text() = 'Certificate']");
    public static final By LOC_TEXT_PKIX_LABEL = By.xpath("//div//dl [dt[span [text() = 'Certificate']]]");
    public static final By LOC_SEL_CERT_FORMAT = By.xpath("//div//select [@name = 'download-format']");
    public static final By LOC_LNK_DOWNLOAD_CERT_ANCHOR = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_SEL_REVOCATION_REASON = By.xpath("//form//select [@name = 'revocationReason']");

    public static final By LOC_TEXT_CSR_HEADER = By.xpath("//div/h2/span [text() = 'CSR']");

    public static final By LOC_TEXT_REQUEST_LIST = By.xpath("//div/h2/span [text() = 'Request List']");
    public static final By LOC_TEXT_CERTIFICATE_LIST = By.xpath("//div/h2/span [text() = 'Certificate List']");

    public static final By LOC_SEL_CSR_ATTRIBUTE = By.xpath("//div/select [@name = 'csrSelectionAttribute']");
    public static final By LOC_SEL_CSR_CHOICE = By.xpath("//div/select [@name = 'csrSelectionChoice']");
    public static final By LOC_INP_CSR_VALUE = By.xpath("//div/input [@name = 'csrSelectionValue']");
    public static final By LOC_SEL_CSR_VALUE_SET = By.xpath("//div/select [@name = 'csrSelectionSet']");
    public static final By LOC_INP_CSR_DATE = By.xpath("//div/input [@name = 'csrSelectionValueDate']");
    public static final By LOC_INP_CSR_BOOLEAN = By.xpath("//div/input [@name = 'csrSelectionValueBoolean']");

    public static final By LOC_SEL_CERT_ATTRIBUTE = By.xpath("//div/select [@name = 'certSelectionAttribute']");
    public static final By LOC_SEL_CERT_CHOICE = By.xpath("//div/select [@name = 'certSelectionChoice']");
    public static final By LOC_INP_CERT_VALUE = By.xpath("//div/input [@name = 'certSelectionValue']");
    public static final By LOC_INP_CERT_SERIAL_VALUE = By.xpath("//div/input [@name = 'certSelectionValueSerial']");
    public static final By LOC_SEL_CERT_VALUE_SET = By.xpath("//div/select [@name = 'certSelectionSet']");
    public static final By LOC_INP_CERT_DATE = By.xpath("//div/input [@name = 'certSelectionValueDate']");
    public static final By LOC_INP_CERT_BOOLEAN = By.xpath("//div/input [@name = 'certSelectionValueBoolean']");

    public static final By LOC_TEXT_MESSAGE_NO_IP = By.xpath("//form//dl [dt[span [text() = 'Message']]]/dd/div[ul/li [contains(text(), 'is an IP address, not allowed')]]");

    public static final By LOC_TD_CSR_ITEM_PENDING = By.xpath("//table//td [starts-with(text(), 'Pending')]");

    public static final By LOC_TA_DOWNLOAD_CERT_CONTENT = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_SELECT_FILE = By.xpath("//div/input [@type = 'file']");

    public static final By LOC_INP_PKCS12_ALIAS = By.xpath("//div/input [@name = 'p12Alias']");
    public static final By LOC_LNK_DOWNLOAD_PKCS12 = By.xpath("//dd/div/div/a [@id = 'pkcs12-download']");

    public static final By LOC_TEXT_CERT_REVOCATION_REASON = By.xpath("//div//dd/span[@name = 'revocationReason']");

    public static final By LOC_SHOW_HIDE_AUDIT = By.xpath("//button[@id='showHideAudit']");
    //    public static final By LOC_TABLE_AUDIT = By.xpath("//div/table [thead/tr/th/span[text() = 'Role']] [tbody/tr]");
    public static final By LOC_TABLE_AUDIT = By.xpath("//div//div [.//tr/th/span [contains(text(), 'Role')] ] [tbody/tr]");

    public static final By LOC_TABLE_AUDIT_REVOCATION_PRESENT = By.xpath("//div/table [thead/tr/th/span [contains(text(), 'Role')] ] [tbody/tr/td [contains(text(), 'Certificate revoked')]]");

    public static final By LOC_BTN_EDIT = By.xpath("//form/div/button [@type='button'][@id = 'edit']");

    //    public static final By LOC_BTN_WITHDRAW_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Withdraw']]");
    public static final By LOC_BTN_CONFIRM_REQUEST = By.xpath("//form/div/button [@type='button'][span [text() = 'Confirm Request']]");
    public static final By LOC_BTN_REVOKE = By.xpath("//form/div/button [@type='button'][span [text() = 'Revoke']]");

    public static final By LOC_SPAN_REVOKE_QUESTION = By.xpath("//body/div//span[@id='ca3SApp.certificate.revoke.question']");
    public static final By LOC_BTN_CONFIRM_REVOKE = By.xpath("//body/div//button [@type='button'][@id = 'confirm-revoke-certificate']");

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

    public static final By LOC_LNK_CERTIFICATE_ISSUER = By.xpath("//div/dl/dd/a [@href = 'issuer']");
    public static final By LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT = By.xpath("//div//dd//div/select [@name = 'download-format']");
    public static final By LOC_INP_CERTIFICATE_PKCS12_ALIAS = By.xpath("//div//dd//div/input [@name = 'p12Alias']");

    private static final Logger LOG = LoggerFactory.getLogger(CSRSubmitIT.class);

    protected static final String USER_NAME_USER = "user";
    protected static final String USER_PASSWORD_USER = "user";

    protected static final String USER_NAME_RA = "ra";
    protected static final String USER_PASSWORD_RA = "s3cr3t";

    private static Random rand = new Random();

    String randomComment;

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeAll
    public static void setUpBeforeAll() throws IOException {
        JCAManager.getInstance();
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void init() throws InterruptedException {

        waitForUrl();

        ptc.getInternalWebDirectTestPipeline();
        ptc.getInternalWebRACheckTestPipeline();
        prefTC.getTestUserPreference();

        if (driver == null) {
            super.startWebDriver();
            if (recordSession) {
                driver.manage().window().maximize();
            } else {
                driver.manage().window().setSize(new Dimension(2000, 768));
            }
        }

        byte[] commentBytes = new byte[16];
        rand.nextBytes(commentBytes);
        randomComment = Base64.getEncoder().encodeToString(commentBytes);
    }


    @Test
    public void testCSRSubmitServersideDirect() throws Exception {

        String c = "DE";
        String cn = "reqTest" + System.currentTimeMillis();
        String o = "trustable solutions";
        String ou = "nuclear research";
        String l = "Hannover";
        String st = "Lower Saxony";
        String san = "wwww." + cn;

        String subject = "CN=" + cn + ", O=" + o + ", OU=" + ou + ", C=" + c + ", L=" + l + ", ST=" + st;
        X500Principal subjectPrincipal = new X500Principal(subject);

        byte[] secretBytes = new byte[6];
        rand.nextBytes(secretBytes);
        String secret = "1Aa" + Base64.getEncoder().encodeToString(secretBytes);

        explain("Navigate your browser to the start page of the application");
        explain("Depending on your configuration you may be logged on, automatically. Not in this case, so select 'Account' and 'Sign In'");
        signIn(USER_NAME_USER, USER_PASSWORD_USER, "and login in as a simple user", 500);

        waitForElement(LOC_LNK_REQ_CERT_MENUE);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        selectElementText(LOC_LNK_REQ_CERT_MENUE, "select the certificate request menue");

        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        explain("select a certificate processing pipeline matching your requirements");

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        validatePresent(LOC_SEL_KEY_CREATION_CHOICE);

        selectOptionByText(LOC_SEL_KEY_CREATION_CHOICE, "CSR available");
        explain("if you have certificate signing request available, select this option");

        scrollToElement(LOC_TA_UPLOAD_CONTENT);
        click(LOC_TA_UPLOAD_CONTENT);
        explain("This text field is the where you can drop or paste your certificate signing request. We will return here, later.");

        selectOptionByText(LOC_SEL_KEY_CREATION_CHOICE, "Serverside key creation");
        explain("The easiest option is to avoid the certificate signing request creation and just let the server do the handle the request and the private key. But keep in mind this option has security drawbacks");

        validatePresent(LOC_SEL_KEY_LENGTH_CHOICE);
        selectOptionByText(LOC_SEL_KEY_LENGTH_CHOICE, "rsa-2048");
        explain("Select a key length. For a test a 2048 bit key length will do.");

        setText(LOC_INP_C_VALUE, c);
        setText(LOC_INP_CN_VALUE, cn);
        setText(LOC_INP_O_VALUE, o);
        setText(LOC_INP_OU_VALUE, ou);
        setText(LOC_INP_L_VALUE, l);
        setText(LOC_INP_ST_VALUE, st);
        explain("provide the details of the certificate's subject");
        setText(LOC_INP_SAN_VALUE, san);
        explain("if required, provide one or more subject alternative names");

        scrollToElement(LOC_BTN_REQUEST_CERTIFICATE);

        setText(LOC_INP_SECRET_VALUE, "1234");
        explain("provide a secret passphrase that will protect the private key in the certificate container");
        explain("Some restrictions apply to the length and character set of the passphrase. If these requirements are not met, a hint is shown.");

        setText(LOC_INP_SECRET_VALUE, secret);
        explain("This passphrase matches the requirements. Store it in a save place.  It cannot be retrieved! It unlocks the certificate container which will be created later on in this process.");
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

        // mismatch of secret
        setText(LOC_INP_SECRET_REPEAT_VALUE, "aa" + secret + "zz");
        Assertions.assertFalse(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button disabled");
        explain("repeat the secret passphrase. A mismatch between the values will be reported");

        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);
        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");
        explain("Once certificate details are provided and the passphrase was entered the request button will be enabled.");

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        explain("submit the certificate by clicking the request button");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_PKIX_LABEL);

        explain("depending on the configuration a certificate request may need an explicit approval by an registration officer. ");
        explain("For demonstration reasons we take the alternative path of immediate issuance.");
        explain("This is the certificate page providing some details about the certificate.");

        selectElementText(LOC_LNK_CERTIFICATE_ISSUER, "a link enables navigation to the issuing certificate");

        explain("Several other certificate aspects may be reviewed.");

        scrollToElement(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT);

        explain("Depending on the key creation mode the may be the option to download a protected container containing certificates and private key.");
        explain("In the previous form we selected server side key creation ");

        selectElementText(LOC_INP_CERTIFICATE_PKCS12_ALIAS, "provide a name of the key entry in the container." );
        checkPKCS12Download(cn, secret);

        selectElementText(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT, "choose a certificate download format");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pkix");
        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".crt", certTypeName, "Expect a informing name of the link");
        checkPEMDownload(cn, "pkix");

        selectOptionByText(LOC_SEL_CERT_FORMAT, "pem");
        explain("The PEM format is a well recognized format in the unix world. It's the default format for the openssl tool.");
        explain("A click on the link starts the download.");
        X509Certificate newCert = checkPEMDownload(cn, "pem");

        selectOptionByText(LOC_SEL_CERT_FORMAT, "pemPart");
        explain("The PEM format including issuing certificates is useful for example for the apache webserver");
        checkPEMDownload(cn, "pemPart");

        explain("The PEM format including the complete certificate chain is another option");
        checkPEMDownload(cn, "pemFull");

        explain("That's all about certificate download. Another important topic of the certificate usage is revocation.");
        explain("Even if there is just a minor doubt about a possible compromise of your private it is good security practise to revoke the certificate");
        explain("The owner of the certificate can always revoke a certificate by selecting a reason");
        validatePresent(LOC_SEL_REVOCATION_REASON);
        selectOptionByText(LOC_SEL_REVOCATION_REASON, "keyCompromised");
        explain("Let's assume the key was disclosed somehow. So select 'key compromise'");

        explain("and click the 'revoke' button");
        click(LOC_BTN_REVOKE);

        explain("a revocation of a certificate cannot be undone. Therefore an additional approval is required.");
        validatePresent(LOC_BTN_CONFIRM_REVOKE);
        click(LOC_BTN_CONFIRM_REVOKE);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);

        selectElementText(LOC_LNK_CERTIFICATES_MENUE, "To find certificates, select the certificate menue");
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");
        explain("search a certificate by its common name.");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);
        explain("enter the common name we provided in the creation form.");

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        explain("the list of matching certificates is pretty short. It's easy to identify or freshly created certificate.");
        click(byCertSubject);

        // already revoked
        validateNotPresent(LOC_TEXT_CERT_REVOCATION_REASON);
        selectElementText(LOC_TEXT_CERT_REVOCATION_REASON, "The certificate is revoked as expected.");

        validatePresent(LOC_SHOW_HIDE_AUDIT);
        selectElementText(LOC_SHOW_HIDE_AUDIT, "the application tracks all relevant actions related to the certificate.");
        click(LOC_SHOW_HIDE_AUDIT);

        // ensure a revocation item regarding revocation is present
        validatePresent(LOC_TABLE_AUDIT_REVOCATION_PRESENT);
        selectElementText(LOC_TABLE_AUDIT_REVOCATION_PRESENT, "The revocation is logged, too.");


        waitForElement(LOC_TA_COMMENT);
        validatePresent(LOC_TA_COMMENT);
        explain("to provide some additional information there is a comment field.");
        setText(LOC_TA_COMMENT, randomComment);

        validatePresent(LOC_BTN_EDIT);
        selectElementText(LOC_BTN_EDIT, "Clicking the 'edit' button persists the new comment");
        click(LOC_BTN_EDIT);

        // search by serial no
        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        selectElementText(LOC_LNK_CERTIFICATES_MENUE, "select the certificate menue to retrieve the certificate list");
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Serial");
        explain("search a certificate by its serial number.");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "decimal");
        explain("select the number representation.");

        // set the serial number, decimal
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        // set the serial number, decimal with leading zeros
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, "00" + newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "hex");

        // set the serial number, hex
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, "0x" + newCert.getSerialNumber().toString(16));

        validatePresent(byCertSubject);

        click(byCertSubject);
        waitForElement(LOC_TA_COMMENT);
        String certComment = getText(LOC_TA_COMMENT);
        Assertions.assertEquals(randomComment, certComment, "Expecting the certificate comment to contain ´the expected content");

    }

    @Test
    public void testCSRDERSubmitDirect() throws GeneralSecurityException, IOException, InterruptedException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqtest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

        String csrFilePath = buildCSRAsDERFile(subjectPrincipal, null);
        validatePresent(LOC_SELECT_FILE);
        setText(LOC_SELECT_FILE, csrFilePath);

        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_PKIX_LABEL);

        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");

    }

    @Test
    public void testCSRSubmitDirect() throws GeneralSecurityException, IOException, InterruptedException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqtest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

        String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_PKIX_LABEL);

        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pkix");
        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".crt", certTypeName, "Expect a informing name of the link");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemPart");
        certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".part.pem", certTypeName, "Expect a informing name of the link");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemFull");
        certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".full.pem", certTypeName, "Expect a informing name of the link");


        validatePresent(LOC_SEL_REVOCATION_REASON);
        selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

        click(LOC_BTN_REVOKE);

        validatePresent(LOC_BTN_CONFIRM_REVOKE);
        click(LOC_BTN_CONFIRM_REVOKE);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "like");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        click(byCertSubject);

        // already revoked
        validateNotPresent(LOC_TEXT_CERT_REVOCATION_REASON);
    }

    private X509Certificate checkPEMDownload(String cn, String format) throws InterruptedException, GeneralSecurityException, IOException {

        selectOptionByValue(LOC_SEL_CERT_FORMAT, format);

        int expectedChainLength = 1;
        String fileEx = format;
        if ("pemPart".equals(format)) {
            expectedChainLength = 2;
            fileEx = "part.pem";
        } else if ("pemFull".equals(format)) {
            expectedChainLength = 3;
            fileEx = "full.pem";
        }

        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + "." + fileEx, certTypeName, "Expect an informing name of the link");

        click(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Thread.sleep(500);

        File certFile = new File(downloadDir, certTypeName);
        LOG.info("downloaded certFile: {}", certFile.getAbsolutePath());

        waitForFileExists(certFile);
        List<X509Certificate> certificateList = convertPemToCertificateChain(new String(Files.readAllBytes(certFile.toPath())));

        Assertions.assertEquals(expectedChainLength, certificateList.size(), "Expected chain length");

//        assertTrue( "Expecting the requested Common Name included in the subject", x509Cert.getSubjectDN().getName().contains(cn) );

        return certificateList.get(0);
    }

    void waitForFileExists(final File certFile) {
        for (int i = 0; i < 10; i++) {

            if (certFile.exists()) {
                return;
            }

            try {
                Thread.sleep(500); // sleep for 1 second.
            } catch (Exception x) {
                fail("Failed due to an exception during Thread.sleep!");
                x.printStackTrace();
            }
        }
        Assertions.assertTrue(certFile.exists());

    }

    private void checkPKCS12Download(String cn, String secret) throws InterruptedException, GeneralSecurityException, IOException {

        byte[] secretBytes = new byte[6];
        String alias = Base64.getEncoder().encodeToString(secretBytes).replaceAll("/", "X").replaceAll("\\+", "x");

        setText(LOC_INP_PKCS12_ALIAS, alias);

        String pkcs12FileName = getText(LOC_LNK_DOWNLOAD_PKCS12);
        Assertions.assertEquals(cn + ".p12", pkcs12FileName, "Expect an informing name of the link");

/*
        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        click(LOC_LNK_DOWNLOAD_PKCS12);
        Thread.sleep(500);

        File pkcs12File = new File(downloadDir, pkcs12FileName);
        LOG.info("downloaded pkcs12 file name: {}", pkcs12File.getAbsolutePath());

        Assertions.assertTrue(pkcs12File.exists());

*/
/*
        X509Certificate x509Cert = CryptoService.convertPemToCertificate(new String(Files.readAllBytes(pkcs12File.toPath())));

        assertTrue( "Expecting the requested Common Name included in the subject", x509Cert.getSubjectDN().getName().contains(cn) );
*/
    }

    @Test
    public void testCSRSubmitDirectRestrictionViolated() throws GeneralSecurityException, IOException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqTestRestrictionViolated" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

//        GeneralName gn = new GeneralName(GeneralName.dNSName, "foo.bar.com");
        GeneralName gn = new GeneralName(GeneralName.iPAddress, "8.8.8.8");
        String csr = buildCSRAsPEM(subjectPrincipal, new GeneralName[]{gn});

        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_SEL_PIPELINE);

        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        click(LOC_BTN_REQUEST_CERTIFICATE);


        validatePresent(LOC_TEXT_MESSAGE_NO_IP);
    }


    @Test
    public void testCSRSubmitRACheck() throws GeneralSecurityException, IOException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_RA_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqTest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
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
        selectOptionByText(LOC_SEL_CSR_ATTRIBUTE, "Subject");

        validatePresent(LOC_SEL_CSR_CHOICE);

        selectOptionByText(LOC_SEL_CSR_CHOICE, "equals");

        validatePresent(LOC_INP_CSR_VALUE);
        setText(LOC_INP_CSR_VALUE, cn);

        validatePresent(LOC_TD_CSR_ITEM_PENDING);
        click(LOC_TD_CSR_ITEM_PENDING);

        validatePresent(LOC_TEXT_CSR_HEADER);

        By bySubject = By.xpath("//div//dl/dd/span [contains(text(), '" + cn + "')]");
        validatePresent(bySubject);

        waitForElement(LOC_TA_COMMENT);
        validatePresent(LOC_TA_COMMENT);


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(LOC_TA_COMMENT));
        setLongText(LOC_TA_COMMENT, randomComment);

        validatePresent(LOC_BTN_CONFIRM_REQUEST);
        click(LOC_BTN_CONFIRM_REQUEST);

        validatePresent(LOC_LNK_REQUESTS_MENUE);
        click(LOC_LNK_REQUESTS_MENUE);

        validatePresent(LOC_LNK_REQUESTS_MENUE);
        click(LOC_LNK_REQUESTS_MENUE);

        waitForElement(LOC_TEXT_REQUEST_LIST);

        validatePresent(LOC_TEXT_REQUEST_LIST);

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        click(LOC_LNK_CERTIFICATES_MENUE);

        validatePresent(LOC_TEXT_CERTIFICATE_LIST);

        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        click(byCertSubject);

        validatePresent(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_PKIX_LABEL);

        waitForElement(LOC_TA_COMMENT);
        String certComment = getText(LOC_TA_COMMENT);
        Assertions.assertEquals(randomComment, certComment, "Expecting the certificate comment to contain ´the expected content");

        String commentUpdated = randomComment + ", revoked by user";
        setText(LOC_TA_COMMENT, commentUpdated);

        validatePresent(LOC_SEL_REVOCATION_REASON);
        selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

        validatePresent(LOC_BTN_REVOKE);
        click(LOC_BTN_REVOKE);

        validatePresent(LOC_SPAN_REVOKE_QUESTION);
        validatePresent(LOC_BTN_CONFIRM_REVOKE);

        click(LOC_BTN_CONFIRM_REVOKE);

        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        validatePresent(LOC_TEXT_CERTIFICATE_LIST);

        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        validatePresent(byCertSubject);
        click(byCertSubject);

        waitForElement(LOC_TA_COMMENT);
        Assertions.assertEquals(commentUpdated, getText(LOC_TA_COMMENT), "Expecting the certificate comment to contain the expected, updated content");

        setText(LOC_TA_COMMENT, randomComment + ", revoked by user");


    }

    public String buildCSRAsPEM(final X500Principal subjectPrincipal) throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        return CryptoUtil.getCsrAsPEM(subjectPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            "password".toCharArray());
    }

    public String buildCSRAsPEM(final X500Principal subjectPrincipal, GeneralName[] sanArray) throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        PKCS10CertificationRequest req = CryptoUtil.getCsr(subjectPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            "password".toCharArray(),
            null,
            sanArray);
        return CryptoUtil.pkcs10RequestToPem(req);

    }

    public String buildCSRAsDERFile(final X500Principal subjectPrincipal, GeneralName[] sanArray) throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        PKCS10CertificationRequest req = CryptoUtil.getCsr(subjectPrincipal,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            "password".toCharArray(),
            null,
            sanArray);
        File fileDerCSR = File.createTempFile("testCSR", ".der");
//        fileDerCSR.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(fileDerCSR)) {
            fos.write(req.getEncoded());
        }

        return fileDerCSR.getAbsolutePath();

    }

    public static List<X509Certificate> convertPemToCertificateChain(String pem) throws GeneralSecurityException {
        ByteArrayInputStream pemStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
        Reader pemReader = new InputStreamReader(pemStream);
        PEMParser pemParser = new PEMParser(pemReader);

        List<X509Certificate> x509CertificateList = new ArrayList<>();

        try {
            Object parsedObj;
            while ((parsedObj = pemParser.readObject()) != null) {

                if (parsedObj == null) {
                    throw new GeneralSecurityException("Parsing of certificate failed! Not PEM encoded?");
                }

                LOG.debug("PemParser returned: " + parsedObj);
                if (!(parsedObj instanceof X509CertificateHolder)) {
                    throw new GeneralSecurityException("Unexpected parsing result: " + parsedObj.getClass().getName());
                }

                X509Certificate cert = (new JcaX509CertificateConverter()).setProvider("BC").getCertificate((X509CertificateHolder) parsedObj);
                x509CertificateList.add(cert);
            }
            ;
        } catch (IOException var13) {
            LOG.error("IOException, convertPemToCertificate", var13);
            throw new GeneralSecurityException("Parsing of certificate failed! Not PEM encoded?");
        } finally {
            try {
                if (pemParser != null) {
                    pemParser.close();
                }
            } catch (IOException var12) {
                LOG.debug("IOException on close()", var12);
            }
        }

        return x509CertificateList;
    }

}
