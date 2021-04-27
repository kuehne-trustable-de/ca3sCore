package de.trustable.ca3s.core.schedule.spider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpiderTest {

    @Test
    void search() {

        Spider spider = new Spider();

        spider.search("https://www.bundesdruckerei.de/de/2833-repository", ".*\\.(cer|cert|crt|pem)");
    }
}
