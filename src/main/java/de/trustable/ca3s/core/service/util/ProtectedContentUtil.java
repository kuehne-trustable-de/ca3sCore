package de.trustable.ca3s.core.service.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;

@Service
public class ProtectedContentUtil {

    private final Logger log = LoggerFactory.getLogger(ProtectedContentUtil.class);

	private BasicTextEncryptor textEncryptor;

	public ProtectedContentUtil(@Value("${protectionSecret:S3cr3t}") String protectionSecret) {
		textEncryptor = new BasicTextEncryptor();
		if( (protectionSecret == null) || (protectionSecret.trim().length() == 0)) {
			throw new UnsupportedOperationException("Configuration parameter 'protectionSecret' missing or invalid");
		}
		log.debug("using protection  secret '{}'", protectionSecret);
		
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
		
		return pc;
	}

}
