package de.trustable.ca3s.core.schedule.spider;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerWorker
{

    Logger LOGGER = LoggerFactory.getLogger(CrawlerWorker.class);

    private static final String USER_AGENT = "ca3s certificate crawler";
    private List<String> links = new LinkedList<String>();


    public boolean crawl(String url, Pattern searchPattern, Set<String> certificateSet)
    {
        try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            if(connection.response().statusCode() >= 300){
                LOGGER.debug("problem ({}) reading page at {}", connection.response().statusCode(), url);
                return false;
            }

            if(!connection.response().contentType().contains("text/html"))
            {
                LOGGER.debug("unexpected content type  '{}' found at {}", connection.response().contentType(), url);
                return false;
            }

            Elements linksOnPage = htmlDocument.select("a[href]");
            LOGGER.debug("found #{} links at {}", linksOnPage.size(), url);
            for(Element link : linksOnPage)
            {

                String href = link.absUrl("href");
//                LOGGER.debug("checking {}", href);

                if (searchPattern.matcher(href).matches()) {
                    LOGGER.info("found certificate at {}", href);
                    certificateSet.add(href);
                }else {
                    this.links.add(link.absUrl("href"));
                }
            }
            return true;
        }catch(IOException ioe){
            LOGGER.debug("problem  '{}' occurred reading {}", ioe.getMessage(), url);
        }
        return false;
    }


    public List<String> getLinks()
    {
        return this.links;
    }

}
