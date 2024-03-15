package de.trustable.ca3s.core.test;

import de.trustable.ca3s.core.ui.WebTestBase;
import org.junit.jupiter.api.Test;

public class ScreenRecorderTest extends WebTestBase {

    @Test
    public void simpleSoundRecording() throws Exception {
//        ScreenRecorderUtil.startRecord("main");
        try {

            explain("Navigate your browser to the start page of the application");

        } finally {
            ScreenRecorderUtil.stopRecord();
        }
    }
}
