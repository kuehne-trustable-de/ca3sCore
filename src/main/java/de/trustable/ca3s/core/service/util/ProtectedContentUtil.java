package de.trustable.ca3s.core.service.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;

@Service
public class ProtectedContentUtil {

    private final Logger log = LoggerFactory.getLogger(ProtectedContentUtil.class);

	private BasicTextEncryptor textEncryptor;

	// defining our own max instant, as Instant.MAX is out f the range hibernate supports :-(
	public static final Instant MAX_INSTANT = Instant.parse("9999-12-30T23:59:59Z");

	@Autowired
	private ProtectedContentRepository protContentRepository;

	public ProtectedContentUtil(@Value("${protectionSecret:S3cr3t}") String protectionSecret) {
		textEncryptor = new BasicTextEncryptor();
		if( (protectionSecret == null) || (protectionSecret.trim().length() == 0)) {
            System.err.println("Configuration parameter 'protectionSecret' missing or invalid!!");
			throw new UnsupportedOperationException("Configuration parameter 'protectionSecret' missing or invalid");
		}
		if( log.isDebugEnabled()) {
			String paddedSecret = "******" + protectionSecret;
			log.debug("using protection  secret '{}'", "******" + paddedSecret.substring(paddedSecret.length() - 4));
		}
		textEncryptor.setPassword(protectionSecret);
	}


	public String protectString(String content) {
		return textEncryptor.encrypt(content);
	}

	public String unprotectString(String protectedContent) {
		return textEncryptor.decrypt(protectedContent);

	}

    /**
     *
     * create a new ProtectedContent object and save the given content
     *
     * @param plainText the plain text to be protected
     * @param pct the content type of the plainText
     * @param crt the related entity
     * @param connectionId the related entity
     * @return the freshly created object
     */
    public ProtectedContent createProtectedContent(final String plainText, ProtectedContentType pct, ContentRelationType crt, long connectionId) {

        return createProtectedContent(plainText,
            pct,
            crt,
            connectionId,
            -1,
            MAX_INSTANT);
    }

    /**
     *
     * create a new ProtectedContent object and save the given content
     *
     * @param plainText the plain text to be protected
     * @param pct the content type of the plainText
     * @param crt the related entity
     * @param connectionId the related entity
     * @param leftUsages number of left usages for this element
     * @param validTo element usable until 'validTo'
     * @return the freshly created object
     */
    public ProtectedContent createProtectedContent(final String plainText,
                                                   ProtectedContentType pct,
                                                   ContentRelationType crt,
                                                   long connectionId,
                                                   int leftUsages,
                                                   Instant validTo) {

        ProtectedContent pc = new ProtectedContent();
        pc.setContentBase64(protectString(plainText));

        pc.setType(pct);
        pc.setRelationType(crt);
        pc.setRelatedId(connectionId);
        pc.setLeftUsages(leftUsages);
        pc.setValidTo(validTo);
        pc.setDeleteAfter(validTo.plus(1, ChronoUnit.DAYS));

        protContentRepository.save(pc);
        return pc;
    }

    /**
	 *
	 * @param type the type of object which is required
     * @param crt the related entity
	 * @param id the object id
	 * @return list of
	 */
	public List<ProtectedContent> retrieveProtectedContent(ProtectedContentType type, ContentRelationType crt, long id) {

        List<ProtectedContent> pcList = protContentRepository.findByTypeRelationId(type, crt, id);

        Instant now = Instant.now();
        Predicate<ProtectedContent> usableItem = pc -> ((pc.getLeftUsages() == -1) || (pc.getLeftUsages() > 0)) && pc.getValidTo().isAfter(now);
        return pcList.stream().filter(usableItem).collect(Collectors.toList());
	}

}
