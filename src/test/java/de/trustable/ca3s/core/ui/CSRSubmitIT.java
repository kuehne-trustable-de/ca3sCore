package de.trustable.ca3s.core.ui;

import com.sun.mail.imap.IMAPStore;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jetbrains.annotations.NotNull;
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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Ca3SApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = { "mailservice.mock.enabled=false" })
@Config(
    browser = Browser.CHROME,
    url = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class CSRSubmitIT extends WebTestBase {
    public boolean recordSession = true;


    public static final By LOC_LNK_ACCOUNT_MENUE = By.xpath("//nav//a [@href='#account-menu']");
    public static final By LOC_LNK_REQ_CERT_MENUE = By.xpath("//nav//a [@href='/pkcsxx']");
    public static final By LOC_LNK_REQUESTS_MENUE = By.xpath( "//nav//a [@href='/csr-list']");
    public static final By LOC_LNK_CERTIFICATES_MENUE = By.xpath("//nav//a [@href='/cert-list']");

    public static final By LOC_BTN_REQUEST_CERTIFICATE = By.xpath("//form/div/button [@id='uploadContent']");

    public static final By LOC_TEXT_ACCOUNT_NAME = By.xpath("//nav//span [contains(text(), '\"user\"')]");
    public static final By LOC_TEXT_CONTENT_TYPE = By.xpath("//form//dl [dt[span [@value = 'content-type']]]/dd/span");
    public static final By LOC_TEXT_WARNING_LABEL = By.xpath("//form//dl [dt[span [@value = 'warning-label']]]/dd/span");

    public static final By LOC_TA_UPLOAD_CONTENT = By.xpath("//form//textarea [@name = 'content']");
    public static final By LOC_TA_COMMENT = By.xpath("//form//textarea [@id = 'comment']");
    public static final By LOC_SEL_PIPELINE = By.xpath("//form//select [@id = 'pkcsxx-pipeline']");


    public static final By LOC_TEXT_CERT_HEADER = By.xpath("//div/h2 [@id='certificateHeader']");
    public static final By LOC_TEXT_PKIX_LABEL = By.xpath("//div//dl [dt[span [text() = 'Certificate']]]");
    public static final By LOC_SEL_CERT_FORMAT = By.xpath("//div//select [@name = 'download-format']");
    public static final By LOC_LNK_DOWNLOAD_CERT_ANCHOR = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_SEL_REVOCATION_REASON = By.xpath("//form//select [@name = 'revocationReason']");

    public static final By LOC_TEXT_CSR_HEADER = By.xpath("//div/h2/span [text() = 'CSR']");

    public static final By LOC_TEXT_REQUEST_LIST = By.xpath("//div/h2/span [@id='request-list-header']");
    public static final By LOC_TEXT_CERTIFICATE_LIST = By.xpath("//div/h2/span [@id='certificate-list-header']");

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
    public static final By LOC_TEXT_MESSAGE_CSR_SIGNATURE_INVALID = By.xpath("//form//dl [dt[span [text() = 'Message']]]/dd/div[ul/li [contains(text(), 'CSR signature invalid')]]");
    public static final By LOC_TEXT_MESSAGE_CSR_ALGO_LENGTH_INVALID =
        By.xpath("//form//dl [dt[span [text() = 'Message']]]/dd/div[ul/li [contains(text(), 'restriction mismatch: signature algo / length')]]");


    public static final By LOC_TD_CSR_ITEM_PENDING = By.xpath("//table//td [@value='PENDING']");

    public static final By LOC_TA_DOWNLOAD_CERT_CONTENT = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_SELECT_FILE = By.xpath("//div/input [@type = 'file']");

    public static final By LOC_INP_PKCS12_ALIAS = By.xpath("//div/input [@name = 'p12Alias']");
    public static final By LOC_LNK_DOWNLOAD_PKCS12 = By.xpath("//dd/div/div/a [@id = 'pkcs12-download']");

    public static final By LOC_TEXT_CERT_REVOCATION_REASON = By.xpath("//div//dd/span[@name = 'revocationReason']");

    public static final By LOC_SHOW_HIDE_AUDIT = By.xpath("//button[@id='showHideAudit']");

    public static final By LOC_TABLE_AUDIT_REVOCATION_PRESENT = By.xpath("//div/table/tbody/tr [td/@value = 'CERTIFICATE_REVOKED']");

    public static final By LOC_INP_TOS_AGREED = By.xpath("//div/input [@name = 'tosAgreed']");

    public static final By LOC_A_TOS_LINK = By.xpath("//div/a [@href = 'http://trustable.eu/tos.html']");

    public static final By LOC_BTN_EDIT = By.xpath("//form/div/button [@type='button'][@id = 'edit']");

    //    public static final By LOC_BTN_WITHDRAW_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Withdraw']]");
    public static final By LOC_BTN_CONFIRM_REQUEST = By.xpath("//form/div/button [(@type='button') and (@id='confirm')]");
    public static final By LOC_BTN_REVOKE = By.xpath("//form/div/button [(@type='button') and (@id='revoke')]");

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
    public static final By LOC_INP_E_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.E']");
    public static final By LOC_INP_SAN_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.SAN']");

    public static final By LOC_INP_SECRET_VALUE = By.xpath("//div/input [@name = 'upload-secret']");
    public static final By LOC_INP_SECRET_REPEAT_VALUE = By.xpath("//div/input [@name = 'upload-secret-repeat']");

    public static final By LOC_LNK_CERTIFICATE_ISSUER = By.xpath("//div/dl/dd/a [@href = 'issuer']");
    public static final By LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT = By.xpath("//div//dd//div/select [@name = 'download-format']");
    public static final By LOC_INP_CERTIFICATE_PKCS12_ALIAS = By.xpath("//div//dd//div/input [@name = 'p12Alias']");

    public static final By LOC_SMALL_ERROR_CN_SAN_RESTRICTION = By.xpath("//div//form//div/small [@id = 'pkcsxx.upload.cn-san.restriction.required']");
    public static final By LOC_SMALL_WARNING_CN_SAN_RESTRICTION = By.xpath("//div//form//div/small [@id = 'pkcsxx.upload.cn-san.restriction.recommended']");


    private static final Logger LOG = LoggerFactory.getLogger(CSRSubmitIT.class);

    private static final Random rand = new Random();

    String randomComment;

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    @Autowired
    PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    PreferenceUtil preferenceUtil;

    @Autowired
    UserUtil userUtil;

    public CSRSubmitIT() {
        super();
    }

    /*
    public CSRSubmitIT(String[] speechifyApiTokenArr) {
        super(speechifyApiTokenArr);
    }
*/

    @BeforeAll
    public static void setUpBeforeAll() throws IOException, MessagingException {
        JCAManager.getInstance();
        WebDriverManager.chromedriver().setup();
        startEmailMock();
    }

    @BeforeEach
    public void init() throws InterruptedException {

        waitForUrl();


        ptc.getInternalWebDirectTestPipeline();
        ptc.getInternalWebDirectKeyReuseTestPipeline();
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
        randomComment = "Neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit " +
            Base64.getEncoder().encodeToString(commentBytes);

        userUtil.updateUserByLogin(USER_NAME_USER, USER_PASSWORD_USER, USER_EMAIL_USER);
        userUtil.updateUserByLogin(USER_NAME_RA, USER_PASSWORD_RA, USER_EMAIL_RA);
    }


    @Test
    public void testSubmitServersideDirect() throws Exception {

        String c = "DE";
        String cn = "reqTest" + System.currentTimeMillis();
        String o = "trustable solutions";
        String ou = "crypto research";
        String l = "Hannover";
        String st = "Lower Saxony";
        String san = "wwww." + cn;

        byte[] secretBytes = new byte[6];
        rand.nextBytes(secretBytes);
        String secret = "1Aa" + Base64.getEncoder().encodeToString(secretBytes)
            .replace("/", "_")
            .replace("-", "_")
            .replace("+", "_")
            .replace("#", "_");

        explain("csr.submit.1");
        explain("csr.submit.2");

        selectElementText(LOC_LNK_ACCOUNT_MENUE, "csr.submit.login.3");

        signIn(USER_NAME_USER, USER_PASSWORD_USER, "csr.submit.login.4", 500);

        wait(1000);

        waitForElement(LOC_LNK_REQ_CERT_MENUE);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        selectElementText(LOC_LNK_REQ_CERT_MENUE, "csr.submit.navigate.1");

        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE);
        validatePresent(LOC_SEL_PIPELINE);
        click(LOC_SEL_PIPELINE);
        explain("csr.submit.3");
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);
        click(LOC_SEL_KEY_CREATION_CHOICE);
        validatePresent(LOC_SEL_KEY_CREATION_CHOICE);
        explain("csr.submit.3.1");

        click(LOC_SEL_KEY_CREATION_CHOICE);
        selectOptionByValue(LOC_SEL_KEY_CREATION_CHOICE, "CSR_AVAILABLE");
        explain("csr.submit.4");

        scrollToElement(LOC_TA_UPLOAD_CONTENT);
        click(LOC_TA_UPLOAD_CONTENT);
        explain("csr.submit.5");

        selectOptionByValue(LOC_SEL_KEY_CREATION_CHOICE, "SERVERSIDE_KEY_CREATION");
        explain("csr.submit.6");

        validatePresent(LOC_SEL_KEY_LENGTH_CHOICE);
        selectOptionByText(LOC_SEL_KEY_LENGTH_CHOICE, "rsa-2048");
        explain("csr.submit.7");

        setText(LOC_INP_C_VALUE, c);
        setText(LOC_INP_CN_VALUE, cn);
        setText(LOC_INP_O_VALUE, o);
        setText(LOC_INP_OU_VALUE, ou);
        setText(LOC_INP_L_VALUE, l);
        setText(LOC_INP_ST_VALUE, st);
        explain("csr.submit.8");
        setText(LOC_INP_SAN_VALUE, san);
        explain("csr.submit.8.1");

        String prefilledEMail = getText(LOC_INP_E_VALUE);
        assertEquals(USER_EMAIL_USER, prefilledEMail);
        assertTrue(isReadOnly(LOC_INP_E_VALUE));

        validatePresent(LOC_SMALL_WARNING_CN_SAN_RESTRICTION);
        setText(LOC_INP_SAN_VALUE, cn);

        validateNotPresent(LOC_SMALL_WARNING_CN_SAN_RESTRICTION);

        scrollToElement(LOC_BTN_REQUEST_CERTIFICATE);

        setText(LOC_INP_SECRET_VALUE, "1234");
        explain("csr.submit.9");
        explain("csr.submit.10");

        setText(LOC_INP_SECRET_VALUE, secret);
        explain("csr.submit.11");
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

        // mismatch of secret
        setText(LOC_INP_SECRET_REPEAT_VALUE, "aa" + secret + "zz");
        assertFalse(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button disabled");
        explain("csr.submit.12");
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);
        explain("csr.submit.12.0");

        validatePresent(LOC_INP_TOS_AGREED);
        scrollToElement(LOC_INP_TOS_AGREED);

        validatePresent(LOC_A_TOS_LINK);
        selectElementText(LOC_A_TOS_LINK, "csr.submit.12.1");
        explain("csr.submit.12.2");
        check(LOC_INP_TOS_AGREED);

        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");
        explain("csr.submit.13");

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        explain("csr.submit.14");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER, 20);
        validatePresent(LOC_TEXT_CERT_HEADER);
//        validatePresent(LOC_TEXT_PKIX_LABEL);

        explain("csr.submit.15");
        explain("csr.submit.16");
        explain("csr.submit.17");

        selectElementText(LOC_LNK_CERTIFICATE_ISSUER, "csr.submit.17.1");

        explain("csr.submit.18");

        scrollToElement(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT);

        explain("csr.submit.19");
        explain("csr.submit.20");

        selectElementText(LOC_INP_CERTIFICATE_PKCS12_ALIAS, "csr.submit.20.1" );
        checkPKCS12Download(cn, secret);

        selectElementText(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT, "csr.submit.20.2");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pkix");
        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        System.out.println("certTypeName: " + certTypeName);
        Assertions.assertEquals(cn + ".crt", certTypeName, "Expect a informing name of the link");
        //Disabled due to browser restrictions on cert download
        //checkPEMDownload(cn, "pkix");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pem");
        explain("csr.submit.21");
        explain("csr.submit.21.1");
        X509Certificate newCert = checkPEMDownload(cn, "pem");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemPart");
        explain("csr.submit.22");
        checkPEMDownload(cn, "pemPart");

        explain("csr.submit.23");
        checkPEMDownload(cn, "pemFull");

        validatePresent(LOC_SEL_REVOCATION_REASON);
        scrollToElement(LOC_SEL_REVOCATION_REASON);

        explain("csr.submit.24");
        explain("csr.submit.25");
        explain("csr.submit.26");
        selectOptionByValue(LOC_SEL_REVOCATION_REASON, "keyCompromise");
        explain("csr.submit.27");

        explain("csr.submit.28");
        click(LOC_BTN_REVOKE);

        explain("csr.submit.28.1");
        validatePresent(LOC_BTN_CONFIRM_REVOKE);
        click(LOC_BTN_CONFIRM_REVOKE);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);

        selectElementText(LOC_LNK_CERTIFICATES_MENUE, "csr.submit.28.2");
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "subject");
        explain("csr.submit.29");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "EQUAL");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);
        explain("csr.submit.30");

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        explain("csr.submit.31");
        click(byCertSubject);

        // already revoked
        validateNotPresent(LOC_TEXT_CERT_REVOCATION_REASON);
        selectElementText(LOC_TEXT_CERT_REVOCATION_REASON, "csr.submit.31.1");

        scrollToElement(LOC_BTN_EDIT);

        validatePresent(LOC_SHOW_HIDE_AUDIT);
        selectElementText(LOC_SHOW_HIDE_AUDIT, "csr.submit.31.2");
        click(LOC_SHOW_HIDE_AUDIT);
        scrollToElement(LOC_BTN_EDIT);

        // ensure a revocation item regarding revocation is present
        validatePresent(LOC_TABLE_AUDIT_REVOCATION_PRESENT);
        selectElementText(LOC_TABLE_AUDIT_REVOCATION_PRESENT, "csr.submit.31.3");


        waitForElement(LOC_TA_COMMENT);
        validatePresent(LOC_TA_COMMENT);
        explain("csr.submit.32");
        setText(LOC_TA_COMMENT, randomComment);

        validatePresent(LOC_BTN_EDIT);
        selectElementText(LOC_BTN_EDIT, "csr.submit.32.1");
        click(LOC_BTN_EDIT);

        // search by serial no
        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        selectElementText(LOC_LNK_CERTIFICATES_MENUE, "csr.submit.32.2");
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "serial");
        explain("csr.submit.33");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "DECIMAL");
        explain("csr.submit.34");

        // set the serial number, decimal
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        // set the serial number, decimal with leading zeros
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, "00" + newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        selectOptionByValue(LOC_SEL_CERT_CHOICE, "HEX");

        // set the serial number, hex
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, "0x" + newCert.getSerialNumber().toString(16));

        validatePresent(byCertSubject);

        click(byCertSubject);
        waitForElement(LOC_TA_COMMENT);
        String certComment = getText(LOC_TA_COMMENT);
        Assertions.assertEquals(randomComment, certComment, "Expecting the certificate comment to contain ´the expected content");

    }


//    @Test
    public void testSubmitAllAlgosServersideDirect() throws Exception {

        for( String algoName: preferenceUtil.getKeyAlgos()){
            testSubmitAllAlgosServersideDirect(algoName);
        }
    }

    void testSubmitAllAlgosServersideDirect(final String algoName) throws Exception {

        String c = "DE";
        String cn = "reqTest" + System.currentTimeMillis();
        String o = "trustable solutions";
        String san = "wwww." + cn;

        byte[] secretBytes = new byte[6];
        rand.nextBytes(secretBytes);
        String secret = "1Aa" + Base64.getEncoder().encodeToString(secretBytes)
            .replace("/", "_")
            .replace("-", "_")
            .replace("+", "_")
            .replace("#", "_");

       selectElementText(LOC_LNK_ACCOUNT_MENUE);

        signIn(USER_NAME_USER, USER_PASSWORD_USER, null, 500);

        wait(1000);

        waitForElement(LOC_LNK_REQ_CERT_MENUE);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        selectElementText(LOC_LNK_REQ_CERT_MENUE);

        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE);
        validatePresent(LOC_SEL_PIPELINE);
        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);
        click(LOC_SEL_KEY_CREATION_CHOICE);
        validatePresent(LOC_SEL_KEY_CREATION_CHOICE);

        selectOptionByValue(LOC_SEL_KEY_CREATION_CHOICE, "SERVERSIDE_KEY_CREATION");

        validatePresent(LOC_SEL_KEY_LENGTH_CHOICE);
        selectOptionByText(LOC_SEL_KEY_LENGTH_CHOICE, algoName);

        setText(LOC_INP_C_VALUE, c);
        setText(LOC_INP_CN_VALUE, cn);
        setText(LOC_INP_O_VALUE, o);
        setText(LOC_INP_SAN_VALUE, san);

        validatePresent(LOC_SMALL_WARNING_CN_SAN_RESTRICTION);
        setText(LOC_INP_SAN_VALUE, cn);

        validateNotPresent(LOC_SMALL_WARNING_CN_SAN_RESTRICTION);

        scrollToElement(LOC_BTN_REQUEST_CERTIFICATE);

        setText(LOC_INP_SECRET_VALUE, secret);
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

        // mismatch of secret
        setText(LOC_INP_SECRET_REPEAT_VALUE, "aa" + secret + "zz");
        assertFalse(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button disabled");
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

        validatePresent(LOC_INP_TOS_AGREED);
        scrollToElement(LOC_INP_TOS_AGREED);

        validatePresent(LOC_A_TOS_LINK);
        selectElementText(LOC_A_TOS_LINK);
        check(LOC_INP_TOS_AGREED);

        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER, 20);
        validatePresent(LOC_TEXT_CERT_HEADER);

        selectElementText(LOC_LNK_CERTIFICATE_ISSUER);

        scrollToElement(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT);

        selectElementText(LOC_INP_CERTIFICATE_PKCS12_ALIAS );
        checkPKCS12Download(cn, secret);

        selectElementText(LOC_SEL_CERTIFICATE_DOWNLOAD_FORMAT);

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pkix");
        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        System.out.println("certTypeName: " + certTypeName);
        Assertions.assertEquals(cn + ".crt", certTypeName, "Expect a informing name of the link");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pem");
        X509Certificate newCert = checkPEMDownload(cn, "pem");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemPart");
        checkPEMDownload(cn, "pemPart");

        checkPEMDownload(cn, "pemFull");

        validatePresent(LOC_SEL_REVOCATION_REASON);
        scrollToElement(LOC_SEL_REVOCATION_REASON);

        selectOptionByValue(LOC_SEL_REVOCATION_REASON, "keyCompromise");
        click(LOC_BTN_REVOKE);

        validatePresent(LOC_BTN_CONFIRM_REVOKE);
        click(LOC_BTN_CONFIRM_REVOKE);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);

        selectElementText(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "subject");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "EQUAL");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        click(byCertSubject);

        // already revoked
        validateNotPresent(LOC_TEXT_CERT_REVOCATION_REASON);
        selectElementText(LOC_TEXT_CERT_REVOCATION_REASON);

        scrollToElement(LOC_BTN_EDIT);

        validatePresent(LOC_SHOW_HIDE_AUDIT);
        selectElementText(LOC_SHOW_HIDE_AUDIT);
        click(LOC_SHOW_HIDE_AUDIT);
        scrollToElement(LOC_BTN_EDIT);

        // ensure a revocation item regarding revocation is present
        validatePresent(LOC_TABLE_AUDIT_REVOCATION_PRESENT);
        selectElementText(LOC_TABLE_AUDIT_REVOCATION_PRESENT);


        waitForElement(LOC_TA_COMMENT);
        validatePresent(LOC_TA_COMMENT);
        setText(LOC_TA_COMMENT, randomComment);

        validatePresent(LOC_BTN_EDIT);
        selectElementText(LOC_BTN_EDIT);
        click(LOC_BTN_EDIT);

        // search by serial no
        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        selectElementText(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "serial");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "DECIMAL");

        // set the serial number, decimal
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        // set the serial number, decimal with leading zeros
        validatePresent(LOC_INP_CERT_SERIAL_VALUE);
        setText(LOC_INP_CERT_SERIAL_VALUE, "00" + newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "HEX");

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

        explain("csr.submit.35");
        explain("csr.submit.4");
        selectElementText(LOC_LNK_ACCOUNT_MENUE, "csr.submit.login.3");

        signIn(USER_NAME_USER, USER_PASSWORD_USER, "csr.submit.login.2", 500);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        selectElementText(LOC_LNK_REQ_CERT_MENUE, "csr.submit.navigate.1");
        wait(1000);
        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE, 20);
        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        explain("csr.submit.37");

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqtest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);


        // build an invalid CSR
        KeyPair keyPair1 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair keyPair2 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String csrFilePath = buildCSRAsDERFile(subjectPrincipal,
            null,
            keyPair1.getPublic(),
            keyPair2.getPrivate() );

        LOG.info("upload csrFilePath '{}'", csrFilePath);

        validatePresent(LOC_SELECT_FILE);
        setText(LOC_SELECT_FILE, csrFilePath);

        explain("csr.submit.38");

        waitForElement(LOC_TEXT_CONTENT_TYPE);
        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_INP_TOS_AGREED);
        scrollToElement(LOC_INP_TOS_AGREED);
        validatePresent(LOC_A_TOS_LINK);
        check(LOC_INP_TOS_AGREED);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        selectElementText(LOC_BTN_REQUEST_CERTIFICATE, "csr.submit.49");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        validatePresent(LOC_TEXT_CONTENT_TYPE);
        String invalidContent = getText(LOC_TEXT_CONTENT_TYPE);
        Assertions.assertTrue(invalidContent.contains("invalid"));
        validatePresent(LOC_TEXT_MESSAGE_CSR_SIGNATURE_INVALID);

        // build an invalid CSR
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, new SecureRandom());
        KeyPair keyPair3 = keyPairGenerator.generateKeyPair();
        csrFilePath = buildCSRAsDERFile(subjectPrincipal,
            null,
            keyPair3.getPublic(),
            keyPair3.getPrivate() );

        validatePresent(LOC_SELECT_FILE);
        setText(LOC_SELECT_FILE, csrFilePath);
//        explain("csr.submit.38");

        waitForElement(LOC_TEXT_CONTENT_TYPE);
        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_INP_TOS_AGREED);
        scrollToElement(LOC_INP_TOS_AGREED);
        validatePresent(LOC_A_TOS_LINK);
        check(LOC_INP_TOS_AGREED);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        // @ToDo
        selectElementText(LOC_BTN_REQUEST_CERTIFICATE, "csr.submit.49");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        validatePresent(LOC_TEXT_MESSAGE_CSR_ALGO_LENGTH_INVALID);

        validatePresent(LOC_TEXT_CONTENT_TYPE);
        Assertions.assertEquals("CSR", getText(LOC_TEXT_CONTENT_TYPE));

        // use a valid CSR
        csrFilePath = buildCSRAsDERFile(subjectPrincipal,
            null,
            keyPair1.getPublic(),
            keyPair1.getPrivate() );

        validatePresent(LOC_SELECT_FILE);
        setText(LOC_SELECT_FILE, csrFilePath);
        explain("csr.submit.38");

        waitForElement(LOC_TEXT_CONTENT_TYPE);
        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_INP_TOS_AGREED);
        scrollToElement(LOC_INP_TOS_AGREED);
        validatePresent(LOC_A_TOS_LINK);
        check(LOC_INP_TOS_AGREED);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        selectElementText(LOC_BTN_REQUEST_CERTIFICATE, "csr.submit.49");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        validateNotPresent(LOC_TEXT_MESSAGE_CSR_SIGNATURE_INVALID);

        waitForElement(LOC_TEXT_CERT_HEADER, 20);
        validatePresent(LOC_TEXT_CERT_HEADER);
//        validatePresent(LOC_TEXT_PKIX_LABEL);

        selectElementText(LOC_SEL_CERT_FORMAT, "csr.submit.50");
        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");

        // retry to submit same csr
        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE, 20);
        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        // explain("csr.submit.37");

        setText(LOC_SELECT_FILE, csrFilePath);
        // explain("csr.submit.38");

        waitForElement(LOC_TEXT_WARNING_LABEL);
        // explain("csr.submit.38");

        /*
        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

    @Test
    public void testCSRSubmitDirect() throws GeneralSecurityException, IOException, InterruptedException {


        if( !isPresent(LOC_LNK_USER_SIGNED_IN)) {

            explain("csr.submit.35");
            explain("csr.submit.4");
            selectElementText(LOC_LNK_ACCOUNT_MENUE, "csr.submit.login.1");

            signIn(USER_NAME_USER, USER_PASSWORD_USER, "csr.submit.login.2", 500);
        }

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        selectElementText(LOC_LNK_REQ_CERT_MENUE, "csr.submit.navigate.1");
        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE, 20);
        validatePresent(LOC_SEL_PIPELINE);

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        explain("csr.submit.37");

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqtest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

        explain("csr.submit.60");
        String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

        validatePresent(LOC_TEXT_CONTENT_TYPE);
        selectElementText(LOC_TEXT_CONTENT_TYPE, "csr.submit.61");

        scrollToElement(LOC_BTN_REQUEST_CERTIFICATE);

        validatePresent(LOC_INP_TOS_AGREED);
        validatePresent(LOC_A_TOS_LINK);
        selectElementText(LOC_INP_TOS_AGREED, "csr.submit.62");
        check(LOC_INP_TOS_AGREED);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        selectElementText(LOC_BTN_REQUEST_CERTIFICATE, "csr.submit.49");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER, 10);
        validatePresent(LOC_TEXT_CERT_HEADER);
//        validatePresent(LOC_TEXT_PKIX_LABEL);

        selectElementText(LOC_SEL_CERT_FORMAT, "csr.submit.50");
        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");
/*
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
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "subject");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "LIKE");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        By byCertSubject = By.xpath("//table//td [contains(text(), '" + cn + "')]");
        validatePresent(byCertSubject);
        click(byCertSubject);

        // already revoked
        validateNotPresent(LOC_TEXT_CERT_REVOCATION_REASON);
 */
    }

    @Test
    public void testCSRUploadDirect() throws GeneralSecurityException, IOException, InterruptedException {

        /*
        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        explain("csr.submit.35");
        explain("csr.submit.4");

        */
        selectElementText(LOC_LNK_ACCOUNT_MENUE, "As this is not the case here, select 'Account' and then 'Sign In'.");

        signIn(USER_NAME_USER, USER_PASSWORD_USER, "Once you have entered your credentials into the respective fields, you can proceed.", 500);

        wait(1000);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        System.out.println("LOC_LNK_REQ_CERT_MENUE");
        wait(1000);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        System.out.println("LOC_LNK_REQ_CERT_MENUE");
        wait(1000);
        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        System.out.println("LOC_LNK_REQ_CERT_MENUE");

        selectElementText(LOC_LNK_REQ_CERT_MENUE, "Press Request certificate on the top right menu ");
        click(LOC_LNK_REQ_CERT_MENUE);

        waitForElement(LOC_SEL_PIPELINE, 20);
        validatePresent(LOC_SEL_PIPELINE);
        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        explain("csr.submit.37");

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        explain("csr.submit.45");

        String cn = "reqtest" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

        String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);
        explain("csr.submit.46");
        By bySubject = By.xpath("//div//dl/dd/span [contains(text(), '" + cn + "')]");
        selectElementText(bySubject, "Before you request the certificate, you can check the given information here.");

        By byBits = By.xpath("//div//dl/dd/span [contains(text(), 'Bits')]");
        selectElementText(byBits, "The key size is shown here. ");

        setLongText(LOC_TA_UPLOAD_CONTENT, "");

        explain("csr.submit.47");

        String csrFilePath = buildCSRAsDERFile(subjectPrincipal, null);
        validatePresent(LOC_SELECT_FILE);
        setText(LOC_SELECT_FILE, csrFilePath);
        explain("csr.submit.48");
        selectElementText(bySubject, "the same information as before");
        selectElementText(byBits, "are shown below.");

        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_INP_TOS_AGREED);
        validatePresent(LOC_A_TOS_LINK);
        check(LOC_INP_TOS_AGREED);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        selectElementText(LOC_BTN_REQUEST_CERTIFICATE, "Now you can press the Request Certificate button.");
        click(LOC_BTN_REQUEST_CERTIFICATE);

        wait(1000);
        waitForElement(LOC_TEXT_CERT_HEADER);
        validatePresent(LOC_TEXT_CERT_HEADER);
//        validatePresent(LOC_TEXT_PKIX_LABEL);

        selectElementText(LOC_SEL_CERT_FORMAT, "By pressing this button, you can download the your certificate as a pem file.");
        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");

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
        } else if ("pkix".equals(format)) {
            fileEx = "crt";
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
        LOG.info("waiting for certFile: {}", certFile.getAbsolutePath());
        Assertions.assertTrue(certFile.exists());

    }

    private void checkPKCS12Download(String cn, String alias) {

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
*/
        click(LOC_LNK_DOWNLOAD_PKCS12);

/*
        wait(500);

        File pkcs12File = new File(downloadDir, pkcs12FileName);
        LOG.info("downloaded pkcs12 file name: {}", pkcs12File.getAbsolutePath());

        Assertions.assertTrue(pkcs12File.exists());



        X509Certificate x509Cert = CryptoService.convertPemToCertificate(new String(Files.readAllBytes(pkcs12File.toPath())));

        assertTrue( "Expecting the requested Common Name included in the subject", x509Cert.getSubjectDN().getName().contains(cn) );
*/
    }

    @Test
    public void testCSRSubmitDirectRestrictionViolated() throws GeneralSecurityException, IOException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER, null, 1000);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

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
    public void testCSRSubmitRACheck() throws GeneralSecurityException, IOException, MessagingException {

        EMailInfo userEmailInfo = getInboxForUser(USER_NAME_USER, USER_PASSWORD_USER);
        EMailInfo raEmailInfo = getInboxForUser(USER_NAME_RA, USER_PASSWORD_RA);


        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

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

        // wait for incoming notification
        System.out.println( "------ wait for incoming ra notification regarding new request");
        waitForNewMessage(raEmailInfo.getUserFolder(), 0);
        Message msgReceived = raEmailInfo.getUserFolder().getMessage(1);
        System.out.println( "msgReceived.getContentType() : " + msgReceived.getContentType() );

        String emailContent = msgReceived.getContent().toString();
        System.out.println( "msgReceived.getContent() : " + emailContent);

        // switch to RA officer role
        signIn(USER_NAME_RA, USER_PASSWORD_RA);

        wait(1000);

        validatePresent(LOC_LNK_REQUESTS_MENUE);
        click(LOC_LNK_REQUESTS_MENUE);

        validatePresent(LOC_TEXT_REQUEST_LIST);

        validatePresent(LOC_SEL_CSR_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CSR_ATTRIBUTE, "subject");

        validatePresent(LOC_SEL_CSR_CHOICE);

        selectOptionByValue(LOC_SEL_CSR_CHOICE, "EQUAL");

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

//        validatePresent(LOC_LNK_REQUESTS_MENUE);
//        click(LOC_LNK_REQUESTS_MENUE);

//        validatePresent(LOC_LNK_REQUESTS_MENUE);
//        click(LOC_LNK_REQUESTS_MENUE);

//        waitForElement(LOC_TEXT_REQUEST_LIST);
//        validatePresent(LOC_TEXT_REQUEST_LIST);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);
/*
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "cn");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "EQUAL");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);
*/
/*
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "id");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "GREATERTHAN");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, "0");
*/

        // locate the created certificate
        By byCertSubject = By.xpath("//table//td [contains(text(), 'CN=" + cn + "')]");
//        waitForElement(byCertSubject);
//        validatePresent(byCertSubject);
//        click(byCertSubject);


        // wait for incoming notification
        System.out.println( "------ wait for incoming user notification regarding issuance");
        waitForNewMessage(userEmailInfo.getUserFolder(), 0);
        Message userMsgReceived = userEmailInfo.getUserFolder().getMessage(1);
        System.out.println( "userMsgReceived.getContentType() : " + userMsgReceived.getContentType() );

        String userEmailContent = userMsgReceived.getContent().toString();
        System.out.println( "userMsgReceived.getContent() : " +userEmailContent);

        System.out.println( "userEmailInfo.getUserFolder().getMessageCount() : " + userEmailInfo.getUserFolder().getMessageCount());
        waitForNewMessage(userEmailInfo.getUserFolder(), 1);
        Message userDownloadMsgReceived = userEmailInfo.getUserFolder().getMessage(2);
        String userDownloadContent = userDownloadMsgReceived.getContent().toString();
        System.out.println( "userDownloadContent : " +userDownloadContent);

        Pattern patternSKI = Pattern.compile("<a href=\"http:\\/\\/.*:.*\\/publicapi\\/certPKIX\\/.*\\/ski\\/(.*)\\/.*\">");
        Matcher m = patternSKI.matcher(userDownloadContent);
        assertTrue(m.find());

        String ski = m.group(1);
        System.out.println("Download link include ski " + ski);
        assertFalse(ski.contains("%"));

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        click(LOC_LNK_CERTIFICATES_MENUE);

        wait(1000);

        waitForElement(LOC_TEXT_CERTIFICATE_LIST);
        validatePresent(LOC_TEXT_CERTIFICATE_LIST);

        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "subject");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "EQUAL");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        waitForElement(byCertSubject);
        validatePresent(byCertSubject);
        click(byCertSubject);

        validatePresent(LOC_TEXT_CERT_HEADER);
//        validatePresent(LOC_TEXT_PKIX_LABEL);

        dropMessagesFromInbox(raEmailInfo.getUserFolder());

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

        // wait for incoming notification
        System.out.println( "------ wait for incoming ra notification regarding revocation");
        waitForNewMessage(raEmailInfo.getUserFolder(), 0);
        Message msgRaReceived = raEmailInfo.getUserFolder().getMessage(1);
        System.out.println( "msgRaReceived.getContentType() : " + msgRaReceived.getContentType() );

        String raEmailContent = msgRaReceived.getContent().toString();
        System.out.println( "msgRaReceived.getContent() : " +raEmailContent);

        validatePresent(LOC_TEXT_CERTIFICATE_LIST);

        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByValue(LOC_SEL_CERT_ATTRIBUTE, "subject");

        validatePresent(LOC_SEL_CERT_CHOICE);
        selectOptionByValue(LOC_SEL_CERT_CHOICE, "EQUAL");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        validatePresent(byCertSubject);
        click(byCertSubject);

        waitForElement(LOC_TA_COMMENT);
        Assertions.assertEquals(commentUpdated, getText(LOC_TA_COMMENT), "Expecting the certificate comment to contain the expected, updated content");

        setText(LOC_TA_COMMENT, randomComment + ", revoked by user");


    }

    private @NotNull EMailInfo getInboxForUser(String username, String accountPassword) throws MessagingException {
        IMAPStore imapStore;
        Folder inbox;
        byte[] emailBytes = new byte[6];
        rand.nextBytes(emailBytes);
        emailAddress = username + "_" + encodeBytesToText(emailBytes) + "@localhost.com";

        userUtil.updateUserByLogin( username, accountPassword, emailAddress);

        byte[] passwordBytes = new byte[6];
        rand.nextBytes(passwordBytes);
        emailPassword = "PasswordEMail_" + encodeBytesToText(passwordBytes);

        greenMailSMTPIMAP.setUser(emailAddress, emailAddress, emailPassword);
        System.out.println("create eMail account '" + emailAddress + "', identified by '" + emailPassword + "'");

        imapStore = greenMailSMTPIMAP.getImap().createStore();
        imapStore.connect(emailAddress, emailPassword);
        inbox = imapStore.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        dropMessagesFromInbox(inbox);

        Optional<User>  optUser = userRepository.findOneByLogin(username);
        if(optUser.isPresent()){
            optUser.get().setEmail(emailAddress);
            userRepository.save(optUser.get());
        }

        return new EMailInfo(username, emailPassword, inbox);
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

    public String buildCSRAsDERFile(final X500Principal subjectPrincipal,
                                    GeneralName[] sanArray) throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        return buildCSRAsDERFile(subjectPrincipal,
        sanArray,
            keyPair.getPublic(),
            keyPair.getPrivate() );
    }

    public String buildCSRAsDERFile(final X500Principal subjectPrincipal,
                                    GeneralName[] sanArray,
                                    PublicKey pubKey,
                                    PrivateKey privKey) throws GeneralSecurityException, IOException {

        PKCS10CertificationRequest req = CryptoUtil.getCsr(subjectPrincipal,
            pubKey,
            privKey,
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

                LOG.debug("PemParser returned: " + parsedObj);
                if (!(parsedObj instanceof X509CertificateHolder)) {
                    throw new GeneralSecurityException("Unexpected parsing result: " + parsedObj.getClass().getName());
                }

                X509Certificate cert = (new JcaX509CertificateConverter()).setProvider("BC").getCertificate((X509CertificateHolder) parsedObj);
                x509CertificateList.add(cert);
            }
        } catch (IOException var13) {
            LOG.error("IOException, convertPemToCertificate", var13);
            throw new GeneralSecurityException("Parsing of certificate failed! Not PEM encoded?");
        } finally {
            try {
                pemParser.close();
            } catch (IOException var12) {
                LOG.debug("IOException on close()", var12);
            }
        }

        return x509CertificateList;
    }

/*
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        KeyPair keyPair1 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair keyPair2 = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        X500Principal subjectPrincipal = new X500Principal("CN=subjectPrincipal");
        PKCS10CertificationRequest req = CryptoUtil.getCsr(subjectPrincipal,
            keyPair1.getPublic(),
            keyPair2.getPrivate(),
            "password".toCharArray(),
            null,
            null);

        System.out.println("broken CSR:\n"+ CryptoUtil.pkcs10RequestToPem(req));
    }

 */
}

class EMailInfo{

    private final String username;
    private final String emailAccountPassword;
    private final Folder userFolder;

    public EMailInfo(String username, String emailAccountPassword, Folder userFolder){
        this.username = username;
        this.emailAccountPassword = emailAccountPassword;
        this.userFolder = userFolder;
    }

    public String getUserName() {
        return username;
    }

    public String getEmailAccountPassword() {
        return emailAccountPassword;
    }

    public Folder getUserFolder() {
        return userFolder;
    }
}
