package de.trustable.ca3s.core.schedule.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Crawler {

    Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private static final int MAX_PAGES_TO_SEARCH = 100;

    public Set<String> search(String url, String regEx) {

        Set<String> pagesVisited = new HashSet<String>();
        Set<String> certificateSet = new HashSet<String>();
        List<String> pagesToVisit = new LinkedList<String>();

        Pattern searchPattern = Pattern.compile(regEx);

        while (pagesVisited.size() < MAX_PAGES_TO_SEARCH) {
            String currentUrl;
            CrawlerWorker leg = new CrawlerWorker();
            if (pagesToVisit.isEmpty()) {
                currentUrl = url;
                pagesVisited.add(url);
            } else {

                String nextUrl;
                do {
                    nextUrl = pagesToVisit.remove(0);
                } while (pagesVisited.contains(nextUrl));
                pagesVisited.add(nextUrl);
                currentUrl = nextUrl;
            }

            try {
                leg.crawl(currentUrl, searchPattern, certificateSet); // Lots of stuff happening here. Look at the crawl method in
            }catch(IllegalArgumentException mue){
                LOGGER.debug("unexpected URL found at url '" +  url + "'", mue );
            }

            pagesToVisit.addAll(leg.getLinks());
        }
        LOGGER.debug("Visited " + pagesVisited.size() + " web page(s), found #"+certificateSet.size()+" different certificates");

        return certificateSet;
    }

}
