package de.trustable.ca3sjh.service.util;

import java.net.URI;

public class ACMEUtil {

	  public static final URI NO_INSTANCE = null;

	public static final String ACME_ERROR_URI_NAMESPACE = "urn:ietf:params:acme:error";

	public static final URI BAD_CSR = URI.create(ACME_ERROR_URI_NAMESPACE + ":badCSR");
	public static final URI BAD_NONCE = URI.create(ACME_ERROR_URI_NAMESPACE + ":badNonce");
	public static final URI BAD_SIGNATURE_ALGORITHM = URI.create(ACME_ERROR_URI_NAMESPACE + ":badSignatureAlgorithm");
	public static final URI BAD_JWK = URI.create(ACME_ERROR_URI_NAMESPACE + ":badJWK");
	public static final URI INVALID_CONTACT = URI.create(ACME_ERROR_URI_NAMESPACE + ":invalidContact");
	public static final URI UNSUPPORTED_CONTACT = URI.create(ACME_ERROR_URI_NAMESPACE + ":unsupportedContact");
	public static final URI EXTERNAL_ACCOUNT_REQUIRED = URI.create(ACME_ERROR_URI_NAMESPACE + ":externalAccountRequired");
	public static final URI ACCOUNT_DOES_NOT_EXIST = URI.create(ACME_ERROR_URI_NAMESPACE + ":accountDoesNotExist");
	public static final URI ACCOUNT_DEACTIVATED = URI.create(ACME_ERROR_URI_NAMESPACE + ":accountDeactivated");
	public static final URI MALFORMED = URI.create(ACME_ERROR_URI_NAMESPACE + ":malformed");
	public static final URI RATE_LIMITED = URI.create(ACME_ERROR_URI_NAMESPACE + ":rateLimited");
	public static final URI REJECTED_IDENTIFIER = URI.create(ACME_ERROR_URI_NAMESPACE + ":rejectedIdentifier");
	public static final URI SERVER_INTERNAL = URI.create(ACME_ERROR_URI_NAMESPACE + ":serverInternal");
	public static final URI UNAUTHORIZED = URI.create(ACME_ERROR_URI_NAMESPACE + ":unauthorized");
	public static final URI UNSUPPORTED_IDENTIFIER = URI.create(ACME_ERROR_URI_NAMESPACE + ":unsupportedIdentifier");
	public static final URI USER_ACTION_REQUIRED = URI.create(ACME_ERROR_URI_NAMESPACE + ":userActionRequired");
	public static final URI BAD_REVOCATION_REASON = URI.create(ACME_ERROR_URI_NAMESPACE + ":badRevocationReason");
	public static final URI CAA = URI.create(ACME_ERROR_URI_NAMESPACE + ":caa"); // NOPMD
	public static final URI DNS = URI.create(ACME_ERROR_URI_NAMESPACE + ":dns");
	public static final URI CONNECTION = URI.create(ACME_ERROR_URI_NAMESPACE + ":connection");
	public static final URI TLS = URI.create(ACME_ERROR_URI_NAMESPACE + ":tls");
	public static final URI INCORRECT_RESPONSE = URI.create(ACME_ERROR_URI_NAMESPACE + ":incorrectResponse");

}
