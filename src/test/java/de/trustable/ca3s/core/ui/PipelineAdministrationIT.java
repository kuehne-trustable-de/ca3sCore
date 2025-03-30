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

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Config(
        browser = Browser.CHROME,
        url     = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class PipelineAdministrationIT extends WebTestBase{


    public static final By LOC_LNK_CONFIG_MENUE = By.xpath("//nav//a [.//span [text() = 'Config']]");
    public static final By LOC_LNK_PIPELINE_MENUE = By.xpath("//nav//a [.//span [text() = 'Pipeline']]");
    public static final By LOC_BTN_PIPELINE_NEW = By.xpath("//div//button [.//span [text() = 'Create a new Pipeline']]");
    public static final By LOC_INP_PIPELINE_NAME = By.xpath("//div/input [@type = 'text'][@id = 'pipeline-name']");
    public static final By LOC_SEL_PIPELINE_TYPE = By.xpath("//div//select [@id = 'pipeline-type']");
    public static final By LOC_SEL_PIPELINE_DESCRIPTION = By.xpath("//div/Textarea [@type = 'text'][@id = 'pipeline-description']");

    public static final By LOC_INP_PIPELINE_LIST_ORDER = By.xpath("//div/input [@type = 'number'][@id = 'pipeline-listOrder']");
    public static final By LOC_SEL_PIPELINE_CA_CONNECTOR = By.xpath("//div//select [@id = 'pipeline-caConnector']");
    public static final By LOC_SEL_PIPELINE_USAGE = By.xpath("//div//select [@id = 'pipeline-csrUsage']");


    public static final By LOC_INP_PIPELINE_URL_PART = By.xpath("//div/input [@type = 'text'][@id = 'pipeline-urlPart']");
    public static final By LOC_INP_CA_CONFIG_TLS_AUTH = By.xpath("//div/input [@type = 'number'][@id = 'ca-connector-config-tlsAuthentication']");
    public static final By LOC_INP_CA_CONFIG_MESSAGE_PROTECTION = By.xpath("//div/input [@type = 'number'][@id = 'ca-connector-config-messageProtection']");

    public static final By LOC_INP_CA_CONFIG_TRUST_SELFSIGNED = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-trustSelfsignedCertificates']");
    public static final By LOC_INP_CA_CONFIG_DEFAULT_CA = By.xpath("//div/input [@type = 'checkbox'][@id = 'ca-connector-config-defaultCA']");
    public static final By LOC_INP_PIPELINE_ACTIVE = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-active']");
    public static final By LOC_INP_PIPELINE_APPROVAL_REQUIRED = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-approvalRequired']");
    public static final By LOC_INP_PIPELINE_PENDING_ON_FAILURE = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-toPendingOnFailedRestrictions']");
    public static final By LOC_INP_PIPELINE_IP_AS_SUBJECT = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-ipAsSubjectAllowed']");
    public static final By LOC_INP_PIPELINE_IP_AS_SAN = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-ipAsSanAllowed']");
    public static final By LOC_INP_PIPELINE_HTTP01 = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-allowChallengeHTTP01']");
    public static final By LOC_INP_PIPELINE_ALPN = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-allowChallengeAlpn']");
    public static final By LOC_INP_PIPELINE_DNS = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-allowChallengeDNS']");
    public static final By LOC_INP_PIPELINE_WILDCARDS = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-allowWildcards']");
    public static final By LOC_INP_PIPELINE_CHECK_CAA = By.xpath("//div/input [@type = 'checkbox'][@id = 'pipeline-checkCAA']");


    public static final By LOC_INP_CA_CONFIG_PASSPHRASE = By.xpath("//div/input [@type = 'password'][@id = 'ca-connector-config-plainSecret']");
    public static final By LOC_INP_CA_ISSUER_NAME = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-issuerName']");
    public static final By LOC_INP_CA_MESSAGE_CONTENT_TYPE = By.xpath("//div/input [@type = 'text'][@id = 'ca-connector-config-msgContentType']");
    public static final By LOC_BTN_SAVE = By.xpath("//form//div/button [@type='submit'][span [text() = 'Save']]");
    public static final By LOC_TEXT_PIPELINES_LIST = By.xpath("//div/h2/span [text() = 'Pipelines']");
    private static final Logger LOG = LoggerFactory.getLogger(PipelineAdministrationIT.class);

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
	public void testCAConnectorCreateWeb() {

        String newPipelineName = "Pipeline_" + Math.random();
//        String newPipelineUrl = "http://acme.server/Url_" + Math.random();
        String newPipelineDesription = "Description_" + Math.random() + " text, lengthy ... lengthy ... very lengthy ";
        String newPipelineListOrder = "" + (int)(10 * Math.random());

		signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_PIPELINE_MENUE);
        click(LOC_LNK_PIPELINE_MENUE);

        validatePresent(LOC_BTN_PIPELINE_NEW);
        click(LOC_BTN_PIPELINE_NEW);

        // create new pipeline
        validatePresent(LOC_INP_PIPELINE_NAME);
        setText(LOC_INP_PIPELINE_NAME, newPipelineName);

        check(LOC_INP_PIPELINE_ACTIVE);

        validatePresent(LOC_SEL_PIPELINE_TYPE);
        click(LOC_SEL_PIPELINE_TYPE);
        selectOptionByText(LOC_SEL_PIPELINE_TYPE, "WEB");

        validatePresent(LOC_INP_PIPELINE_APPROVAL_REQUIRED);

        validatePresent(LOC_SEL_PIPELINE_DESCRIPTION);
        click(LOC_SEL_PIPELINE_DESCRIPTION);
        setText(LOC_SEL_PIPELINE_DESCRIPTION, newPipelineDesription);

        validatePresent(LOC_INP_PIPELINE_LIST_ORDER);
        click(LOC_INP_PIPELINE_LIST_ORDER);
        setText(LOC_INP_PIPELINE_LIST_ORDER, newPipelineListOrder);

        validateNotPresent(LOC_INP_PIPELINE_URL_PART);

        validatePresent(LOC_SEL_PIPELINE_CA_CONNECTOR);
        click(LOC_SEL_PIPELINE_CA_CONNECTOR);
        selectOptionByText(LOC_SEL_PIPELINE_CA_CONNECTOR, "InternalTestCA" );

        validateNotPresent(LOC_INP_PIPELINE_PENDING_ON_FAILURE);
        check(LOC_INP_PIPELINE_IP_AS_SUBJECT);
        check(LOC_INP_PIPELINE_IP_AS_SAN);

        validateNotPresent(LOC_INP_PIPELINE_HTTP01);
        validateNotPresent(LOC_INP_PIPELINE_ALPN);
        validateNotPresent(LOC_INP_PIPELINE_DNS);
        validateNotPresent(LOC_INP_PIPELINE_WILDCARDS);
        validateNotPresent(LOC_INP_PIPELINE_CHECK_CAA);

        validatePresent(LOC_SEL_PIPELINE_USAGE);
        click(LOC_SEL_PIPELINE_USAGE);
        selectOptionByText(LOC_SEL_PIPELINE_USAGE, "TLS Server" );

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_PIPELINES_LIST);

        By byPipelineName = By.xpath("//table//td [contains(text(), '" + newPipelineName + "')]");
        validatePresent(byPipelineName);

        click(byPipelineName);
        By byEditPipelineName = By.xpath("//table//tr [td [contains(text(), '" + newPipelineName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditPipelineName);
        click(byEditPipelineName);

        // back in the created connector
        validatePresent(LOC_INP_PIPELINE_NAME);
        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME), "Expect the name of the connector");

        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME));

        Assertions.assertEquals( newPipelineDesription, getText(LOC_SEL_PIPELINE_DESCRIPTION) );

        Assertions.assertTrue(isChecked(LOC_INP_PIPELINE_ACTIVE));

        Assertions.assertEquals( "WEB", getText(LOC_SEL_PIPELINE_TYPE));
        Assertions.assertEquals( "InternalTestCA", getText(LOC_SEL_PIPELINE_CA_CONNECTOR));
        Assertions.assertEquals( "TLS_SERVER", getText(LOC_SEL_PIPELINE_USAGE ));

    }

    @Test
    public void testCAConnectorCreateSCEP() {

        String newPipelineName = "Pipeline_" + Math.random();
        String newPipelineUrlPart = "scep_" + Math.random();
        String newPipelineDesription = "Description_" + Math.random() + " text, lengthy ... lengthy ... very lengthy ";
        String newPipelineListOrder = "" + (int)(10 * Math.random());

        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_PIPELINE_MENUE);
        click(LOC_LNK_PIPELINE_MENUE);

        validatePresent(LOC_BTN_PIPELINE_NEW);
        click(LOC_BTN_PIPELINE_NEW);

        // create new pipeline
        validatePresent(LOC_INP_PIPELINE_NAME);
        setText(LOC_INP_PIPELINE_NAME, newPipelineName);

        check(LOC_INP_PIPELINE_ACTIVE);

        validatePresent(LOC_SEL_PIPELINE_TYPE);
        click(LOC_SEL_PIPELINE_TYPE);
        selectOptionByText(LOC_SEL_PIPELINE_TYPE, "SCEP");

        validatePresent(LOC_INP_PIPELINE_APPROVAL_REQUIRED);

        validatePresent(LOC_INP_PIPELINE_URL_PART);
        click(LOC_INP_PIPELINE_URL_PART);
        setText(LOC_INP_PIPELINE_URL_PART, newPipelineUrlPart);

        validatePresent(LOC_SEL_PIPELINE_DESCRIPTION);
        click(LOC_SEL_PIPELINE_DESCRIPTION);
        setText(LOC_SEL_PIPELINE_DESCRIPTION, newPipelineDesription);

        validateNotPresent(LOC_INP_PIPELINE_LIST_ORDER);

        validatePresent(LOC_SEL_PIPELINE_CA_CONNECTOR);
        click(LOC_SEL_PIPELINE_CA_CONNECTOR);
        selectOptionByText(LOC_SEL_PIPELINE_CA_CONNECTOR, "InternalTestCA" );

        check(LOC_INP_PIPELINE_PENDING_ON_FAILURE);
        check(LOC_INP_PIPELINE_IP_AS_SUBJECT);
        check(LOC_INP_PIPELINE_IP_AS_SAN);

        validateNotPresent(LOC_INP_PIPELINE_HTTP01);
        validateNotPresent(LOC_INP_PIPELINE_ALPN);
        validateNotPresent(LOC_INP_PIPELINE_DNS);
        validateNotPresent(LOC_INP_PIPELINE_WILDCARDS);
        validateNotPresent(LOC_INP_PIPELINE_CHECK_CAA);

        validateNotPresent(LOC_SEL_PIPELINE_USAGE);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_PIPELINES_LIST);

        By byPipelineName = By.xpath("//table//td [contains(text(), '" + newPipelineName + "')]");
        validatePresent(byPipelineName);

        click(byPipelineName);
        By byEditPipelineName = By.xpath("//table//tr [td [contains(text(), '" + newPipelineName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditPipelineName);
        click(byEditPipelineName);

        // back in the created connector
        validatePresent(LOC_INP_PIPELINE_NAME);
        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME), "Expect the name of the connector");

        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME));

        Assertions.assertEquals( newPipelineDesription, getText(LOC_SEL_PIPELINE_DESCRIPTION) );

        Assertions.assertTrue(isChecked(LOC_INP_PIPELINE_ACTIVE));

        Assertions.assertEquals( "SCEP", getText(LOC_SEL_PIPELINE_TYPE));
        Assertions.assertEquals( "InternalTestCA", getText(LOC_SEL_PIPELINE_CA_CONNECTOR));
        Assertions.assertEquals( newPipelineUrlPart, getText(LOC_INP_PIPELINE_URL_PART));

    }

    @Test
    public void testCAConnectorCreateACME() {

        String newPipelineName = "Pipeline_" + Math.random();
        String newPipelineUrlPart = "acme_" + Math.random();
        String newPipelineDesription = "Description_" + Math.random() + " text, lengthy ... lengthy ... very lengthy ";
        String newPipelineListOrder = "" + (int)(10 * Math.random());

        signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);

        validatePresent(LOC_LNK_PIPELINE_MENUE);
        click(LOC_LNK_PIPELINE_MENUE);

        validatePresent(LOC_BTN_PIPELINE_NEW);
        click(LOC_BTN_PIPELINE_NEW);

        // create new pipeline
        validatePresent(LOC_INP_PIPELINE_NAME);
        setText(LOC_INP_PIPELINE_NAME, newPipelineName);

        check(LOC_INP_PIPELINE_ACTIVE);

        validatePresent(LOC_SEL_PIPELINE_TYPE);
        click(LOC_SEL_PIPELINE_TYPE);
        selectOptionByText(LOC_SEL_PIPELINE_TYPE, "ACME");

        validatePresent(LOC_INP_PIPELINE_APPROVAL_REQUIRED);

        validatePresent(LOC_SEL_PIPELINE_DESCRIPTION);
        click(LOC_SEL_PIPELINE_DESCRIPTION);
        setText(LOC_SEL_PIPELINE_DESCRIPTION, newPipelineDesription);

        validatePresent(LOC_INP_PIPELINE_URL_PART);
        click(LOC_INP_PIPELINE_URL_PART);
        setText(LOC_INP_PIPELINE_URL_PART, newPipelineUrlPart);

        validateNotPresent(LOC_INP_PIPELINE_LIST_ORDER);

        validatePresent(LOC_SEL_PIPELINE_CA_CONNECTOR);
        click(LOC_SEL_PIPELINE_CA_CONNECTOR);
        selectOptionByText(LOC_SEL_PIPELINE_CA_CONNECTOR, "InternalTestCA" );

        validatePresent(LOC_SEL_PIPELINE_CA_CONNECTOR);
        click(LOC_SEL_PIPELINE_CA_CONNECTOR);
        selectOptionByText(LOC_SEL_PIPELINE_CA_CONNECTOR, "InternalTestCA" );

        check(LOC_INP_PIPELINE_PENDING_ON_FAILURE);
        check(LOC_INP_PIPELINE_IP_AS_SUBJECT);
        check(LOC_INP_PIPELINE_IP_AS_SAN);

        check(LOC_INP_PIPELINE_HTTP01);
        check(LOC_INP_PIPELINE_ALPN);

        Assertions.assertFalse(isEnabled(LOC_INP_PIPELINE_WILDCARDS));
        check(LOC_INP_PIPELINE_DNS);
        Assertions.assertTrue(isEnabled(LOC_INP_PIPELINE_WILDCARDS));

        check(LOC_INP_PIPELINE_WILDCARDS);

        check(LOC_INP_PIPELINE_CHECK_CAA);

        validateNotPresent(LOC_SEL_PIPELINE_USAGE);

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_PIPELINES_LIST);

        By byPipelineName = By.xpath("//table//td [contains(text(), '" + newPipelineName + "')]");
        validatePresent(byPipelineName);

        click(byPipelineName);
        By byEditPipelineName = By.xpath("//table//tr [td [contains(text(), '" + newPipelineName + "')]]/td/div/button[span[contains(text(), 'Edit')]]");

        validatePresent(byEditPipelineName);
        click(byEditPipelineName);

        // back in the created connector
        validatePresent(LOC_INP_PIPELINE_NAME);
        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME), "Expect the name of the connector");

        Assertions.assertEquals(newPipelineName, getText(LOC_INP_PIPELINE_NAME));

        Assertions.assertEquals( newPipelineDesription, getText(LOC_SEL_PIPELINE_DESCRIPTION) );

        Assertions.assertTrue(isChecked(LOC_INP_PIPELINE_ACTIVE));

        Assertions.assertEquals( "ACME", getText(LOC_SEL_PIPELINE_TYPE));
        Assertions.assertEquals( "InternalTestCA", getText(LOC_SEL_PIPELINE_CA_CONNECTOR));
        Assertions.assertEquals( newPipelineUrlPart, getText(LOC_INP_PIPELINE_URL_PART));

    }

}
