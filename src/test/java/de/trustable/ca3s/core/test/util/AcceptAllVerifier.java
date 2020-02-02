package de.trustable.ca3s.core.test.util;

import java.security.cert.X509Certificate;

import org.jscep.client.verification.CertificateVerifier;

/**
 * NEVER use this in a production-like environment!!!
 * 
 * @author kuehn
 *
 */
public class AcceptAllVerifier implements CertificateVerifier{

	@Override
	public boolean verify(X509Certificate arg0) {
		return true;
	}

}
