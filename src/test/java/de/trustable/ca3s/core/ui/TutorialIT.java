package de.trustable.ca3s.core.ui;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.test.obs.OBSClient;
import de.trustable.ca3s.core.test.obs.RunnableWithFilename;
import de.trustable.ca3s.core.test.obs.StartRecordConsumer;
import de.trustable.ca3s.core.test.obs.StopRecordConsumer;
import de.trustable.ca3s.core.ui.helper.Browser;
import de.trustable.ca3s.core.ui.helper.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Config(
    browser = Browser.CHROME,
    url = "http://localhost:${local.server.port}/core/info"
)
@ActiveProfiles("dev")
public class TutorialIT extends CSRSubmitIT {

    private static final Logger LOG = LoggerFactory.getLogger(TutorialIT.class);

    private static OBSClient obsClient;

    static String targetDirectoryPrefix = "/tmp/tutorial/";
    static File targetDirectory;

    @BeforeAll
    public static void setUpBeforeAll() throws IOException {
        obsClient = new OBSClient("localhost", 4455, "S3cr3t!S");

        targetDirectory = new File(targetDirectoryPrefix, "Run_" + System.currentTimeMillis());
        targetDirectory.mkdirs();

        CSRSubmitIT.setUpBeforeAll();
    }

    @AfterAll
    public static void cleanUpAfterAll() {
        if (obsClient != null) {
            obsClient.close();
        }
    }

    @BeforeEach
    public void init() throws InterruptedException {
        super.recordSession = true;
        super.playSound = true;

        obsClient.connect();

        super.init();

        CountDownLatch latchReadyToStart = new CountDownLatch(1);

        obsClient.startRecord( new StartRecordConsumer(() -> {
            LOG.info( "ready to start");
            latchReadyToStart.countDown();
        }));

        latchReadyToStart.await();

    }

    @RegisterExtension
    AfterTestExecutionCallback afterTestExecutionCallback = new AfterTestExecutionCallback() {
        @Override
        public void afterTestExecution(ExtensionContext context) {
            CountDownLatch latch = new CountDownLatch(1);

            RunnableWithFilename runner = new RunnableWithFilenameAndContext(context, latch);
            obsClient.stopRecord(new StopRecordConsumer(runner));

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    };

    @Test
    public void recordCSRSubmitServersideDirect() throws Exception {

        explain("tutorial.1");
        explain("tutorial.2");

        /*
        explain("Depending on your configuration you may be logged on, automatically. Not in this case, so select 'Account' and 'Sign In'");
        signIn(USER_NAME_USER, USER_PASSWORD_USER, "and login in as a simple user", 500);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);
*/
        //testSubmitServersideDirect();



        testCSRUploadDirect();

        explain("tutorial.3");
    }

    static class RunnableWithFilenameAndContext implements RunnableWithFilename {

        private final ExtensionContext context;
        private final CountDownLatch latch;

        RunnableWithFilenameAndContext(ExtensionContext context, CountDownLatch latch) {
            this.context = context;
            this.latch = latch;
        }

        @Override
        public void run(String filename) {

            String testName = context.getDisplayName().replace("(", "").replace(")", "");
            Optional<Throwable> exception = context.getExecutionException();
            File videoFile = null;

            try {
                Thread.sleep(1000);
                if (exception.isPresent()) { // has exception
                    LOG.info("recording stopped, test '{}' failed", testName);
                    if (filename != null) {
                        LOG.info("test failure, video written to '{}'", filename);

                        File recordedFile = new File(filename);
                        String ext = StringUtils.substringAfterLast(recordedFile.getName(), ".");
                        videoFile = new File(targetDirectory, "Failed_" + testName + "." + ext);

                        FileUtils.copyFile(recordedFile, videoFile);
                        LOG.info("video moved to '{}'", videoFile.getPath());
                    }
                } else {
                    System.out.println("recording stopped, test successful");
                    if (filename != null) {
                        LOG.info("successful test, video written to '{}'", filename);
                        File recordedFile = new File(filename);
                        String ext = StringUtils.substringAfterLast(recordedFile.getName(), ".");
                        videoFile = new File(targetDirectory, testName + "." + ext);
                        FileUtils.copyFile(recordedFile, videoFile);

                        LOG.info("video moved to '{}'", videoFile.getPath());
                    }
                }
            }catch (IOException ioException){
                ioException.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        }
    }
}
