package de.trustable.ca3s.core.schedule.spider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CrawlerTest {

    @Disabled("Takes very long ...!")
    @Test
    void search() {

        Crawler crawler = new Crawler();

        Set<String> linkList =  crawler.search("https://www.bundesdruckerei.de/de/2833-repository", ".*\\.(cer|cert|crt|pem)");

        Assertions.assertTrue(linkList.size() > 100, "ensure some certs are found");
    }
}
