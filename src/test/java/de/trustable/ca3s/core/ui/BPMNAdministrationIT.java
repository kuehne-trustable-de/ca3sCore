package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import de.trustable.ca3s.core.web.rest.support.ContentUploadProcessor;
import de.trustable.util.JCAManager;
import org.apache.commons.io.FileUtils;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.Random;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Config(
        browser = Browser.CHROME,
        url     = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class BPMNAdministrationIT extends WebTestBase{


    public static final By LOC_LNK_CONFIG_MENUE = By.xpath("//nav//a [.//span [text() = 'Config']]");
    public static final By LOC_LNK_BPMN_PROCESS_INFO_MENUE = By.xpath("//nav//a [.//span [text() = 'BPMN Process Info']]");
    public static final By LOC_BTN_BPMN_PROCESS_INFO_NEW = By.xpath("//div//button [.//span [text() = 'Create a new BPMN Process Info']]");
    public static final By LOC_INP_BPMN_FILE = By.xpath("//div/input [@type = 'file'][@id = 'fileSelector']");
    public static final By LOC_INP_BPMN_NEW_NAME = By.xpath("//div/input [@type = 'text'][@id = 'bpmn.new.name']");
    public static final By LOC_INP_BPMN_NEW_VERSION = By.xpath("//div/input [@type = 'text'][@id = 'bpmn.new.version']");
    public static final By LOC_SEL_BPMN_NEW_TYPE = By.xpath("//div//select [@id = 'bpmn.new.type']");


    public static final By LOC_BTN_SAVE = By.xpath("//form//div/button [@type='button'][span [text() = 'Save']]");
    public static final By LOC_TEXT_PROCESS_LIST = By.xpath("//div/h2/span [text() = 'Process list']");
    private static final Logger LOG = LoggerFactory.getLogger(BPMNAdministrationIT.class);

    private static final String USER_NAME_USER = "user";
    private static final String USER_PASSWORD_USER = "user";

    private static final String USER_NAME_RA = "ra";
    private static final String USER_PASSWORD_RA = "s3cr3t";

    private static final String USER_NAME_ADMIN = "admin";
    private static final String USER_PASSWORD_ADMIN = "admin";


    private static Random rand = new Random();

//    static String bpmnTestFilePath;
    static File bpmnTestFile;

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
	public static void setUpBeforeClass() throws IOException {
        JCAManager.getInstance();

        ClassPathResource bpmnTestResource = new ClassPathResource("bpmn/CAInvocationProcess.bpmn");
        bpmnTestFile = File.createTempFile("TestProcess", ".bpmn");

        FileUtils.copyInputStreamToFile(bpmnTestResource.getInputStream(), bpmnTestFile);
    }

	@BeforeEach
	void init() {

	    waitForUrl();

		ptc.getInternalWebDirectTestPipeline();
		ptc.getInternalWebRACheckTestPipeline();
        prefTC.getTestUserPreference();

        /*
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
*/
        if( driver == null) {
		    super.startWebDriver();
		    driver.manage().window().setSize(new Dimension(2000,768));
		}
	}

	@Test
	public void testBPMNProcessInfoCreate() {

        String newBPNMName = "BPMN_" + Math.random();

        waitForElement(LOC_LNK_ACCOUNT_MENUE);

		signIn(USER_NAME_ADMIN, USER_PASSWORD_ADMIN);

        validatePresent(LOC_LNK_CONFIG_MENUE);
        click(LOC_LNK_CONFIG_MENUE);


        validatePresent(LOC_LNK_BPMN_PROCESS_INFO_MENUE);
        click(LOC_LNK_BPMN_PROCESS_INFO_MENUE);

        validatePresent(LOC_BTN_BPMN_PROCESS_INFO_NEW);
        click(LOC_BTN_BPMN_PROCESS_INFO_NEW);

        validatePresent(LOC_INP_BPMN_FILE);
        setText( LOC_INP_BPMN_FILE, bpmnTestFile.getAbsolutePath());

        validatePresent(LOC_INP_BPMN_NEW_NAME);
        Assertions.assertEquals(bpmnTestFile.getName().replace(".bpmn", ""), getText(LOC_INP_BPMN_NEW_NAME));

        setText(LOC_INP_BPMN_NEW_NAME, newBPNMName);

        validatePresent(LOC_INP_BPMN_NEW_VERSION);
        Assertions.assertEquals("0.0.1", getText(LOC_INP_BPMN_NEW_VERSION));

        validatePresent(LOC_SEL_BPMN_NEW_TYPE);
        Assertions.assertEquals("CERTIFICATE_NOTIFY", getText(LOC_SEL_BPMN_NEW_TYPE));

        validatePresent(LOC_BTN_SAVE);
        click(LOC_BTN_SAVE);

        validatePresent(LOC_TEXT_PROCESS_LIST);

        By byProcessName = By.xpath("//table//td [contains(text(), '" + newBPNMName + "')]");
        validatePresent(byProcessName);

        click(byProcessName);

        /*
        // back in the created process
        validatePresent(LOC_INP_PIPELINE_NAME);
        Assertions.assertEquals(newBPNMName, getText(LOC_INP_PIPELINE_NAME), "Expect the name of the connector");

        Assertions.assertEquals(newBPNMName, getText(LOC_INP_PIPELINE_NAME));

        Assertions.assertEquals( newPipelineDesription, getText(LOC_SEL_PIPELINE_DESCRIPTION) );

        Assertions.assertTrue(isChecked(LOC_INP_PIPELINE_ACTIVE));

        Assertions.assertEquals( "WEB", getText(LOC_SEL_PIPELINE_TYPE));
        Assertions.assertEquals( "InternalTestCA", getText(LOC_SEL_PIPELINE_CA_CONNECTOR));
        Assertions.assertEquals( "TLS_SERVER", getText(LOC_SEL_PIPELINE_USAGE ));
*/
    }

}
