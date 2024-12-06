package de.trustable.ca3s.core.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Test
    void addSplittedEMailAddress() {

        List<String> testList = new ArrayList<>();
        NotificationService.addSplittedEMailAddress(testList, "");
        NotificationService.addSplittedEMailAddress(testList, "    ");

        assertEquals(0, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "FOO@bar.com ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "bla foo 123456 root@localhost");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, ", foo@bar.com , ");
        assertEquals(1, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "foo@bar.com , test@ca3s.org");
        assertEquals(2, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "semi@bar.com ; semi@ca3s.org");
        assertEquals(4, testList.size());

        NotificationService.addSplittedEMailAddress(testList, "blank@bar.com blank@ca3s.org");
        assertEquals(6, testList.size());




        testList = new ArrayList<>();
        NotificationService.addSplittedEMailAddress(testList, "kuehne@trustable.de, kuehne@klup.de");
        assertEquals(2, testList.size());

    }
}
