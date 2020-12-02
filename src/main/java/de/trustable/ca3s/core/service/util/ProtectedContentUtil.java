package de.trustable.ca3s.core.service.util;

import java.util.List;

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
			log.debug("using protection  secret '{}'", "******" + paddedSecret.substring(paddedSecret.length() - 6));
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
	 * @return
	 */
	public ProtectedContent createProtectedContent(final String plainText, ProtectedContentType pct, ContentRelationType crt, long connectionId) {

		ProtectedContent pc = new ProtectedContent();
		pc.setContentBase64(protectString(plainText));

		pc.setType(pct);
		pc.setRelationType(crt);
		pc.setRelatedId(connectionId);

		protContentRepository.save(pc);
		return pc;
	}

	/**
	 *
	 * @param type
	 * @param crt
	 * @param id
	 * @return
	 */
	public List<ProtectedContent> retrieveProtectedContent(ProtectedContentType type, ContentRelationType crt, long id) {
		return protContentRepository.findByTypeRelationId(type, crt, id);
	}

}
