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
        pagesToVisit.add(url);

        Pattern searchPattern = Pattern.compile(regEx);

        while(!pagesToVisit.isEmpty() && (pagesVisited.size() < MAX_PAGES_TO_SEARCH)) {
            CrawlerWorker leg = new CrawlerWorker();
            String nextUrl = url;
            if (!pagesToVisit.isEmpty()) {
                do {
                    nextUrl = pagesToVisit.remove(0);
                } while (pagesVisited.contains(nextUrl) && !pagesToVisit.isEmpty());
                pagesVisited.add(nextUrl);
            }

            try {
                if(leg.crawl(nextUrl, searchPattern, certificateSet)) { // Lots of stuff happening here. Look at the crawl method in
                    pagesToVisit.addAll(leg.getLinks());
                }
            }catch(IllegalArgumentException mue){
                LOGGER.debug("unexpected URL found at url '" +  url + "'", mue );
            }
        }
        LOGGER.debug("Visited " + pagesVisited.size() + " web page(s), found #"+certificateSet.size()+" different certificates");

        return certificateSet;
    }

}
