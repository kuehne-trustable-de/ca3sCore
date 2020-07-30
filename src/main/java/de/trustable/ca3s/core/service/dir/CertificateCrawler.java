package de.trustable.ca3s.core.service.dir;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class crawls for certificates on the web and inserts it into the database.
 */
public class CertificateCrawler extends WebCrawler {

	Logger LOGGER = LoggerFactory.getLogger(CertificateCrawler.class);

	// Non-interesting artefacts
    private static final Pattern filters = Pattern.compile(
            ".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
            "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private final List<String> crawlDomains;
    private final Pattern certPatterns;
    private final CertificateUtil certUtil;
    private final ImportInfo importInfo;
    
    public CertificateCrawler(List<String> crawlDomains, String regEx, CertificateUtil certUtil, ImportInfo importInfo) {
        this.crawlDomains = ImmutableList.copyOf(crawlDomains);
        this.certPatterns = Pattern.compile(regEx);
        this.certUtil = certUtil;
        this.importInfo = importInfo;
        
    }

    /**
     * decide whether there may be interesting stuff or not
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
    	
        String href = url.getURL().toLowerCase();
    	
        if (filters.matcher(href).matches()) {
//        	LOGGER.debug("not visiting filtered page {}", href);
            return false;
        }

        if (certPatterns.matcher(href).matches()) {
        	LOGGER.debug("visiting {}", href);
            return true;
        }

        for (String domain : crawlDomains) {
            if (href.startsWith(domain)) {
            	LOGGER.debug("visiting {}", href);
                return true;
            }
        }
//    	LOGGER.debug("not visiting {}", href);
        return false;
    }

    /**
     * retrieve certificates
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();

        if (!certPatterns.matcher(url).matches()) {
            return;
        }

		try {
			
			LOGGER.debug("new certificate at '{}' found, importing ...", url);

			certUtil.createCertificate(page.getContentData(), null, null, false, url);
			importInfo.incImported();

		} catch (GeneralSecurityException | IOException e) {
			LOGGER.info("reading and importing certificate from '{}' causes {}",
					url, e.getLocalizedMessage());
		}
    }

}