package de.trustable.ca3s.core.service.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProtectedContentUtil {

	private @Value("${protectionSecret}") String protectionSecret;
	
	public String protectString(String content) {
		
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(protectionSecret);
	
		return textEncryptor.encrypt(content);
	}
	
	public String unprotectString(String protectedContent) {
		
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(protectionSecret);
		return textEncryptor.decrypt(protectedContent);

	}
}
