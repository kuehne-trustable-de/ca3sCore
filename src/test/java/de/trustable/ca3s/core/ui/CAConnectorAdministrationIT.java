package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.TypedValue;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import de.trustable.ca3s.core.web.rest.data.CreationMode;
import de.trustable.ca3s.core.service.dto.PkcsXXData;
import de.trustable.ca3s.core.web.rest.data.UploadPrecheckData;
import de.trustable.ca3s.core.web.rest.support.ContentUploadProcessor;
import de.trustable.util.JCAManager;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

import static org.junit.Assert.fail;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Config(
        browser = Browser.CHROME,
        url     = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class CAConnectorAdministrationIT extends WebTestBase{


    public static final By LOC_LNK_CONFIG_MENUE = By.xpath("//nav//a [.//span [text() = 'Config']]");
    public static final By LOC_LNK_CA_CONFIG_MENUE = By.xpath("//nav//a [.//span [text() = 'CA Connector Config']]");
    public static final By LOC_BTN_CA_CONFIG_NEW = By.xpath("//div//button [.//span [text() = 'Create a new CA Connector Config']]");
    public static final By LOC_INP_CA_CONFIG_NAME = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-name']");
    public static final By LOC_SEL_CA_CONFIG_TYPE = By.xpath("//div//select [@id = 'ca-connector-config-caConnectorType']");
    public static final By LOC_SEL_CA_CONFIG_URL = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-caUrl']");

    public static final By LOC_INP_CA_CONFIG_POLLING_OFFSET = By.xpath("//div/input [@type = 'number'][@id = 'ca-connector-config-pollingOffset']");
    public static final By LOC_SEL_CA_CONFIG_INTERVAL = By.xpath("//div//select [@id = 'ca-connector-config-interval']");

    public static final By LOC_INP_CA_CONFIG_SELECTOR = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-selector']");
    public static final By LOC_INP_CA_CONFIG_TLS_AUTH = By.xpath("//div/input [@type = 'number'][@id = 'ca-connector-config-tlsAuthentication']");
    public static final By LOC_INP_CA_CONFIG_MESSAGE_PROTECTION = By.xpath("//div/input [@type = 'number'][@id = 'ca-connector-config-messageProtection']");

    public static final By LOC_INP_CA_CONFIG_TRUST_SELFSIGNED = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-trustSelfsignedCertificates']");
    public static final By LOC_INP_CA_CONFIG_DEFAULT_CA = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-defaultCA']");
    public static final By LOC_INP_CA_CONFIG_ACTIVE = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-active']");
    public static final By LOC_INP_CA_CONFIG_PW_PROT = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-messageProtectionPassphrase']");
    public static final By LOC_INP_CA_CONFIG_PASSPHRASE = By.xpath("//div/input [@type = 'password'][@id = 'ca-connector-config-plainSecret']");
    public static final By LOC_INP_CA_ISSUER_NAME = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-issuerName']");
    public static final By LOC_INP_CA_MESSAGE_CONTENT_TYPE = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-msgContentType']");
    public static final By LOC_BTN_SAVE = By.xpath("//form//div/button [@type='submit'][span [text() = 'Save']]");
    public static final By LOC_TEXT_CONNECTOR_LIST = By.xpath("//div/h2/span [text() = 'CA Connector Configs']");
    private static final Logger LOG = LoggerFactory.getLogger(CAConnectorAdministrationIT.class);

    private static final String USER_NAME_USER = "user";
    private static final String USER_PASSWORD_USER = "user";

    private static final String USER_NAME_RA = "ra";
    private static final String USER_PASSWORD_RA = "s3cr3t";

    private static final String USER_NAME_ADMIN = "admin";
    private static final String USER_PASSWORD_ADMIN = "admin";


    private static Random rand = new Random();

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	@Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    private ContentUploadProcessor contentUploadProcessor;

    long createdCertificateId;

    @BeforeAll
	public static void setUpBeforeClass() {

        JCAManager.getInstance();
//        WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	void init() {

	    waitForUrl();

		ptc.getInternalWebDirectTestPipeline();
		ptc.getInternalWebRACheckTestPipeline();
        prefTC.getTestUserPreference();

        UploadPrecheckData uploaded = new UploadPrecheckData();
        uploaded.setPipelineId(1L);
        uploaded.setKeyAlgoLength("rsa-4096");

        NamedValues[] namedValues = new NamedValues[1];
        namedValues[0] = new NamedValues();
        namedValues[0].setName("CN");
        TypedValue[] typedValues = new TypedValue[1];
        typedValues[0] = new TypedValue();
        typedValues[0].setValue("test.host.dev");
        namedValues[0].setValues(typedValues);
        uploaded.setCertificateAttributes(namedValues);

        uploaded.setCreationMode(CreationMode.SERVERSIDE_KEY_CREATION);

        uploaded.setSecret("S3cr3t!S");

        ResponseEntity<PkcsXXData> responseEntity = contentUploadProcessor.buildServerSideKeyAndRequest(uploaded, "integrationTest");
        if(responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getCertsHolder() != null &&
             responseEntity.getBody().getCertsHolder().length > 0 ) {
            createdCertificateId = responseEntity.getBody().getCertsHolder()[0].getCertificateId();
        }else{
            Assert.fail("creation of certificate failed: " + responseEntity);
        }

        if( driver == null) {
		    super.startWebDriver();
		    driver.manage().window().setSize(new Dimension(2000,768));
		}
	}

	@Test
	public void testCAConnectorCreateCMP() {

        String newCAConnectorName = "CAConnector_" + Math.random();
        String newCAConnectorUrl = "http://cmp.server/Url_" + Math.random();
        String newCAConnectorSelector = "Selector_" + Math.random();
        String newCAConnectorIssuerName = "IssuerName_" + Math.random();
        String newCAConnectorMessageContenType = "MessageContenType_" + Math.random();
        String protectionPassphrase = "ProtectionPassphrase_" + Math.random();

		signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_CA_CONFIG_MENUE);
        click(LOC_LNK_CA_CONFIG_MENUE);

        validatePresent(LOC_BTN_CA_CONFIG_NEW);
        click(LOC_BTN_CA_CONFIG_NEW);

        // create new connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        setText(LOC_INP_CA_CONFIG_NAME, newCAConnectorName);

        validatePresent(LOC_SEL_CA_CONFIG_TYPE);
        click(LOC_SEL_CA_CONFIG_TYPE);
        selectOptionByText(LOC_SEL_CA_CONFIG_TYPE, "CMP");

        validatePresent(LOC_INP_CA_CONFIG_DEFAULT_CA);

        validatePresent(LOC_SEL_CA_CONFIG_URL);
        click(LOC_SEL_CA_CONFIG_URL);
        setText(LOC_SEL_CA_CONFIG_URL, newCAConnectorUrl);

        validatePresent(LOC_INP_CA_CONFIG_SELECTOR);
        click(LOC_INP_CA_CONFIG_SELECTOR);
        setText(LOC_INP_CA_CONFIG_SELECTOR, newCAConnectorSelector);

        validatePresent(LOC_INP_CA_CONFIG_TLS_AUTH);
        click(LOC_INP_CA_CONFIG_TLS_AUTH);
        setText(LOC_INP_CA_CONFIG_TLS_AUTH, "" + createdCertificateId);

        validatePresent(LOC_INP_CA_CONFIG_PW_PROT);
        click(LOC_INP_CA_CONFIG_PW_PROT);

        uncheck(LOC_INP_CA_CONFIG_PW_PROT);
        validateNotPresent(LOC_INP_CA_CONFIG_PASSPHRASE);
        validatePresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);

        check(LOC_INP_CA_CONFIG_PW_PROT);

        validatePresent(LOC_INP_CA_CONFIG_PASSPHRASE);
        validateNotPresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);
        click(LOC_INP_CA_CONFIG_PASSPHRASE);
        setText(LOC_INP_CA_CONFIG_PASSPHRASE, protectionPassphrase);

        validatePresent(LOC_INP_CA_ISSUER_NAME);
        click(LOC_INP_CA_ISSUER_NAME);
        setText(LOC_INP_CA_ISSUER_NAME, newCAConnectorIssuerName);

        validatePresent(LOC_INP_CA_MESSAGE_CONTENT_TYPE);
        click(LOC_INP_CA_MESSAGE_CONTENT_TYPE);
        setText(LOC_INP_CA_MESSAGE_CONTENT_TYPE, newCAConnectorMessageContenType);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        By byCAConnectorName = By.xpath("//table//td [contains(text(), '" + newCAConnectorName + "')]");
        validatePresent(byCAConnectorName);

        click(byCAConnectorName);
        By byEditCAConnectorName = By.xpath("//table//tr [td [contains(text(), '" + newCAConnectorName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        // back in the created connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        Assertions.assertEquals(newCAConnectorName, getText(LOC_INP_CA_CONFIG_NAME), "Expect the name of the connector");

        Assertions.assertEquals("CMP", getText(LOC_SEL_CA_CONFIG_TYPE));

        Assertions.assertEquals( newCAConnectorSelector, getText(LOC_INP_CA_CONFIG_SELECTOR) );

        Assertions.assertTrue(isChecked(LOC_INP_CA_CONFIG_PW_PROT));

        Assertions.assertEquals( newCAConnectorIssuerName, getText(LOC_INP_CA_ISSUER_NAME));
        Assertions.assertEquals( newCAConnectorMessageContenType, getText(LOC_INP_CA_MESSAGE_CONTENT_TYPE));
        Assertions.assertFalse(isChecked(LOC_INP_CA_CONFIG_DEFAULT_CA));

    }

    @Test
    public void testCAConnectorCreateADCS() {

        String newCAConnectorName = "CAConnector_" + Math.random();
        String newCAConnectorUrl = "http://adcs.server/Url_" + Math.random();
        String updatedCAConnectorUrl = "http://adcs.server/UpdatedUrl_" + Math.random();
        String newCAConnectorTemplate = "Template_" + Math.random();
        String protectionPassphrase = "ProtectionPassphrase_" + Math.random();


        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_CA_CONFIG_MENUE);
        click(LOC_LNK_CA_CONFIG_MENUE);

        validatePresent(LOC_BTN_CA_CONFIG_NEW);
        click(LOC_BTN_CA_CONFIG_NEW);

        // crete new connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        setText(LOC_INP_CA_CONFIG_NAME, newCAConnectorName);

        validatePresent(LOC_SEL_CA_CONFIG_TYPE);
        click(LOC_SEL_CA_CONFIG_TYPE);
        selectOptionByText(LOC_SEL_CA_CONFIG_TYPE, "ADCS");

        validatePresent(LOC_INP_CA_CONFIG_DEFAULT_CA);

        validatePresent(LOC_SEL_CA_CONFIG_URL);
        click(LOC_SEL_CA_CONFIG_URL);
        setText(LOC_SEL_CA_CONFIG_URL, newCAConnectorUrl);

        validatePresent(LOC_INP_CA_CONFIG_SELECTOR);
        click(LOC_INP_CA_CONFIG_SELECTOR);
        setText(LOC_INP_CA_CONFIG_SELECTOR, newCAConnectorTemplate);

        validateNotPresent(LOC_INP_CA_CONFIG_TLS_AUTH);
        validateNotPresent(LOC_INP_CA_CONFIG_PW_PROT);
        validateNotPresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);
        validateNotPresent(LOC_INP_CA_MESSAGE_CONTENT_TYPE);

        validatePresent(LOC_INP_CA_CONFIG_PASSPHRASE);
        click(LOC_INP_CA_CONFIG_PASSPHRASE);
        setText(LOC_INP_CA_CONFIG_PASSPHRASE, protectionPassphrase);


        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        By byCAConnectorName = By.xpath("//table//td [contains(text(), '" + newCAConnectorName + "')]");
        validatePresent(byCAConnectorName);

        click(byCAConnectorName);
        By byEditCAConnectorName = By.xpath("//table//tr [td [contains(text(), '" + newCAConnectorName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        // back in the created connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        Assertions.assertEquals(newCAConnectorName, getText(LOC_INP_CA_CONFIG_NAME), "Expect the name of the connector");
        Assertions.assertEquals("ADCS", getText(LOC_SEL_CA_CONFIG_TYPE));
        Assertions.assertEquals( newCAConnectorTemplate, getText(LOC_INP_CA_CONFIG_SELECTOR) );
        Assertions.assertFalse(isChecked(LOC_INP_CA_CONFIG_DEFAULT_CA));
        Assertions.assertEquals( newCAConnectorUrl, getText(LOC_SEL_CA_CONFIG_URL));

        click(LOC_SEL_CA_CONFIG_URL);
        setText(LOC_SEL_CA_CONFIG_URL, updatedCAConnectorUrl);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        Assertions.assertEquals( updatedCAConnectorUrl, getText(LOC_SEL_CA_CONFIG_URL));

    }

    @Test
    public void testCAConnectorCreateADCSInventory() {

        String newCAConnectorName = "CAConnector_" + Math.random();
        String newCAConnectorUrl = "http://adcs.server/Url_" + Math.random();
        String newCAConnectorPollingOffset = "" + (int)(1000 * Math.random());
        String protectionPassphrase = "ProtectionPassphrase_" + Math.random();


        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_CA_CONFIG_MENUE);
        click(LOC_LNK_CA_CONFIG_MENUE);

        validatePresent(LOC_BTN_CA_CONFIG_NEW);
        click(LOC_BTN_CA_CONFIG_NEW);

        // crete new connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        setText(LOC_INP_CA_CONFIG_NAME, newCAConnectorName);

        validatePresent(LOC_SEL_CA_CONFIG_TYPE);
        click(LOC_SEL_CA_CONFIG_TYPE);
        selectOptionByText(LOC_SEL_CA_CONFIG_TYPE, "ADCS_CERTIFICATE_INVENTORY");

        validatePresent(LOC_SEL_CA_CONFIG_URL);
        click(LOC_SEL_CA_CONFIG_URL);
        setText(LOC_SEL_CA_CONFIG_URL, newCAConnectorUrl);

        validatePresent(LOC_INP_CA_CONFIG_POLLING_OFFSET);
        click(LOC_INP_CA_CONFIG_POLLING_OFFSET);
        setText(LOC_INP_CA_CONFIG_POLLING_OFFSET, newCAConnectorPollingOffset);

        validatePresent(LOC_SEL_CA_CONFIG_INTERVAL);
        click(LOC_SEL_CA_CONFIG_INTERVAL);
        selectOptionByText(LOC_SEL_CA_CONFIG_INTERVAL, "HOUR");

        validateNotPresent(LOC_INP_CA_CONFIG_DEFAULT_CA);
        validateNotPresent(LOC_INP_CA_CONFIG_SELECTOR);
        validateNotPresent(LOC_INP_CA_CONFIG_TLS_AUTH);
        validateNotPresent(LOC_INP_CA_CONFIG_PW_PROT);
        validateNotPresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);
        validateNotPresent(LOC_INP_CA_MESSAGE_CONTENT_TYPE);

        validatePresent(LOC_INP_CA_CONFIG_PASSPHRASE);
        click(LOC_INP_CA_CONFIG_PASSPHRASE);
        setText(LOC_INP_CA_CONFIG_PASSPHRASE, protectionPassphrase);


        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        By byCAConnectorName = By.xpath("//table//td [contains(text(), '" + newCAConnectorName + "')]");
        validatePresent(byCAConnectorName);

        click(byCAConnectorName);
        By byEditCAConnectorName = By.xpath("//table//tr [td [contains(text(), '" + newCAConnectorName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        // back in the created connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        Assertions.assertEquals(newCAConnectorName, getText(LOC_INP_CA_CONFIG_NAME), "Expect the name of the connector");
        Assertions.assertEquals("ADCS_CERTIFICATE_INVENTORY", getText(LOC_SEL_CA_CONFIG_TYPE));
        Assertions.assertEquals(newCAConnectorUrl, getText(LOC_SEL_CA_CONFIG_URL));
        Assertions.assertEquals(newCAConnectorPollingOffset, getText(LOC_INP_CA_CONFIG_POLLING_OFFSET));
        Assertions.assertEquals("HOUR", getText(LOC_SEL_CA_CONFIG_INTERVAL));
        Assertions.assertEquals("*****", getText(LOC_INP_CA_CONFIG_PASSPHRASE));

    }

    @Test
    public void testCAConnectorCreateDirectory() {

        String newCAConnectorName = "CAConnector_" + Math.random();
        String newCAConnectorUrl = "http://adcs.server/Url_" + Math.random();

        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_CA_CONFIG_MENUE);
        click(LOC_LNK_CA_CONFIG_MENUE);

        validatePresent(LOC_BTN_CA_CONFIG_NEW);
        click(LOC_BTN_CA_CONFIG_NEW);

        // crete new connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        setText(LOC_INP_CA_CONFIG_NAME, newCAConnectorName);

        validatePresent(LOC_SEL_CA_CONFIG_TYPE);
        click(LOC_SEL_CA_CONFIG_TYPE);
        selectOptionByText(LOC_SEL_CA_CONFIG_TYPE, "DIRECTORY");

        validatePresent(LOC_SEL_CA_CONFIG_URL);
        click(LOC_SEL_CA_CONFIG_URL);
        setText(LOC_SEL_CA_CONFIG_URL, newCAConnectorUrl);

        check(LOC_INP_CA_CONFIG_TRUST_SELFSIGNED);

        validatePresent(LOC_SEL_CA_CONFIG_INTERVAL);
        click(LOC_SEL_CA_CONFIG_INTERVAL);
        selectOptionByText(LOC_SEL_CA_CONFIG_INTERVAL, "DAY");

        validateNotPresent(LOC_INP_CA_CONFIG_DEFAULT_CA);
        validateNotPresent(LOC_INP_CA_CONFIG_POLLING_OFFSET);
        validateNotPresent(LOC_INP_CA_CONFIG_SELECTOR);
        validateNotPresent(LOC_INP_CA_CONFIG_TLS_AUTH);
        validateNotPresent(LOC_INP_CA_CONFIG_PW_PROT);
        validateNotPresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);
        validateNotPresent(LOC_INP_CA_MESSAGE_CONTENT_TYPE);
        validateNotPresent(LOC_INP_CA_CONFIG_PASSPHRASE);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        By byCAConnectorName = By.xpath("//table//td [contains(text(), '" + newCAConnectorName + "')]");
        validatePresent(byCAConnectorName);

        click(byCAConnectorName);
        By byEditCAConnectorName = By.xpath("//table//tr [td [contains(text(), '" + newCAConnectorName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        // back in the created connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        Assertions.assertEquals(newCAConnectorName, getText(LOC_INP_CA_CONFIG_NAME), "Expect the name of the connector");
        Assertions.assertEquals("DIRECTORY", getText(LOC_SEL_CA_CONFIG_TYPE));
        Assertions.assertEquals(newCAConnectorUrl, getText(LOC_SEL_CA_CONFIG_URL));
        Assertions.assertEquals("DAY", getText(LOC_SEL_CA_CONFIG_INTERVAL));
        Assertions.assertTrue(isChecked(LOC_INP_CA_CONFIG_TRUST_SELFSIGNED));
    }

    @Test
    public void testCAConnectorCreateInternal() {

        String newCAConnectorName = "CAConnector_" + Math.random();
        String newCAConnectorUrl = "http://adcs.server/Url_" + Math.random();

        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_CA_CONFIG_MENUE);
        click(LOC_LNK_CA_CONFIG_MENUE);

        validatePresent(LOC_BTN_CA_CONFIG_NEW);
        click(LOC_BTN_CA_CONFIG_NEW);

        // crete new connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        setText(LOC_INP_CA_CONFIG_NAME, newCAConnectorName);

        validatePresent(LOC_SEL_CA_CONFIG_TYPE);
        click(LOC_SEL_CA_CONFIG_TYPE);
        selectOptionByText(LOC_SEL_CA_CONFIG_TYPE, "INTERNAL");

        validatePresent(LOC_INP_CA_CONFIG_DEFAULT_CA);

        validateNotPresent(LOC_INP_CA_CONFIG_TRUST_SELFSIGNED);
        validateNotPresent(LOC_SEL_CA_CONFIG_INTERVAL);
        validateNotPresent(LOC_SEL_CA_CONFIG_URL);
        validateNotPresent(LOC_INP_CA_CONFIG_POLLING_OFFSET);
        validateNotPresent(LOC_INP_CA_CONFIG_SELECTOR);
        validateNotPresent(LOC_INP_CA_CONFIG_TLS_AUTH);
        validateNotPresent(LOC_INP_CA_CONFIG_PW_PROT);
        validateNotPresent(LOC_INP_CA_CONFIG_MESSAGE_PROTECTION);
        validateNotPresent(LOC_INP_CA_MESSAGE_CONTENT_TYPE);
        validateNotPresent(LOC_INP_CA_CONFIG_PASSPHRASE);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_CONNECTOR_LIST);

        By byCAConnectorName = By.xpath("//table//td [contains(text(), '" + newCAConnectorName + "')]");
        validatePresent(byCAConnectorName);

        click(byCAConnectorName);
        By byEditCAConnectorName = By.xpath("//table//tr [td [contains(text(), '" + newCAConnectorName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditCAConnectorName);
        click(byEditCAConnectorName);

        // back in the created connector
        validatePresent(LOC_INP_CA_CONFIG_NAME);
        Assertions.assertEquals(newCAConnectorName, getText(LOC_INP_CA_CONFIG_NAME), "Expect the name of the connector");
        Assertions.assertEquals("INTERNAL", getText(LOC_SEL_CA_CONFIG_TYPE));
        Assertions.assertFalse(isChecked(LOC_INP_CA_CONFIG_DEFAULT_CA));
    }

}
