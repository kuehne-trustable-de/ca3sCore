package de.trustable.ca3s.core.service.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProtectedContentUtil {

//	private @Value("${protectionSecret}") String protectionSecret;

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
}
