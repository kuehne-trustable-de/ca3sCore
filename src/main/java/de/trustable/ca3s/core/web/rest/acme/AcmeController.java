package de.trustable.ca3s.core.web.rest.acme;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.trustable.ca3s.core.service.util.PipelineUtil;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.domain.AcmeNonce;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import de.trustable.ca3s.core.repository.AcmeContactRepository;
import de.trustable.ca3s.core.repository.AcmeNonceRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.DateUtil;
import de.trustable.ca3s.core.service.util.JwtUtil;
import de.trustable.util.CryptoUtil;

import javax.transaction.Transactional;

@Transactional(dontRollbackOn = AcmeProblemException.class)
@RestController
public class AcmeController {

	private static final Logger LOG = LoggerFactory.getLogger(AcmeController.class);

	public static final URI NO_INSTANCE = null;
	public static final String NO_DETAIL = null;


	public static final String APPLICATION_JWS_VALUE = "application/jws";
	public static final String APPLICATION_JOSE_JSON_VALUE = "application/jose+json";
	public static final String APPLICATION_PKIX_CERT_VALUE = "application/pkix-cert";
	public static final String APPLICATION_PEM_CERT_CHAIN_VALUE = "application/pem-certificate-chain";
    public static final String APPLICATION_X_PEM_CERT_CHAIN_VALUE = "application/x-pem-certificate-chain";
	public static final String APPLICATION_PEM_CERT_VALUE = "application/pem-certificate";
	public static final String APPLICATION_PEM_FILE_VALUE = "application/x-pem-file";
	public static final String APPLICATION_PKCS12_VALUE = "application/x-pkcs12";

	public static final MediaType APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");
	public static final MediaType APPLICATION_JOSE_JSON = MediaType.parseMediaType(APPLICATION_JOSE_JSON_VALUE);
	public static final MediaType APPLICATION_JWS = MediaType.parseMediaType(APPLICATION_JWS_VALUE);
	public static final MediaType APPLICATION_PKIX_CERT = MediaType.parseMediaType(APPLICATION_PKIX_CERT_VALUE);
    public static final MediaType APPLICATION_PEM_CERT_CHAIN = MediaType.parseMediaType(APPLICATION_PEM_CERT_CHAIN_VALUE);
    public static final MediaType APPLICATION_X_PEM_CERT_CHAIN = MediaType.parseMediaType(APPLICATION_X_PEM_CERT_CHAIN_VALUE);
	public static final MediaType APPLICATION_PEM_CERT = MediaType.parseMediaType(APPLICATION_PEM_CERT_VALUE);
	public static final MediaType APPLICATION_PEM_FILE = MediaType.parseMediaType(APPLICATION_PEM_FILE_VALUE);
	public static final MediaType APPLICATION_PKCS12 = MediaType.parseMediaType(APPLICATION_PKCS12_VALUE);

	public static int DEFAULT_NONCE_VALID_DAYS = 1;
	public static final String REPLAY_NONCE_HEADER = "Replay-Nonce";
    public static final String HEADER_X_CA3S_FORWARDED_HOST = "X-CA3S-Forwarded-Host";
    public static final String HEADER_X_CA3S_PROXY_ID = "X-CA3S-PROXY-ID";
    public static final String HEADER_X_JWS_SIGNATURE = "X-JWS-Signature";

    static final String GENERAL_URL_PREFIX = "/acme/{realm}";

	String DIRECTORY_RESOURCE_MAPPING = afterPrefix(
			DirectoryController.class.getAnnotation(RequestMapping.class).value()[0]);
	String NEW_AUTHORIZATION_RESOURCE_MAPPING = afterPrefix(
			NewOrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	String AUTHORIZATION_RESOURCE_MAPPING = afterPrefix(
			AuthorizationController.class.getAnnotation(RequestMapping.class).value()[0]);
	String ACCOUNT_RESOURCE_MAPPING = afterPrefix(
			AccountController.class.getAnnotation(RequestMapping.class).value()[0]);
	String NEW_NONCE_RESOURCE_MAPPING = afterPrefix(
			NewNonceController.class.getAnnotation(RequestMapping.class).value()[0]);
	String NEW_ACCOUNT_RESOURCE_MAPPING = afterPrefix(
			NewAccountController.class.getAnnotation(RequestMapping.class).value()[0]);
	String NEW_ORDER_RESOURCE_MAPPING = afterPrefix(
			NewOrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	String ORDER_RESOURCE_MAPPING = afterPrefix(OrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	String CHALLENGE_RESOURCE_MAPPING = afterPrefix(
			ChallengeController.class.getAnnotation(RequestMapping.class).value()[0]);
	String CERTIFICATE_RESOURCE_MAPPING = afterPrefix(
			AcmeCertificateController.class.getAnnotation(RequestMapping.class).value()[0]);

	static String afterPrefix(String url) {

		if (url.startsWith(GENERAL_URL_PREFIX)) {
			return url.replace(GENERAL_URL_PREFIX, "");
		}
		return url;
	}

	SecureRandom secRandom = new SecureRandom();

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	CryptoUtil cryptoUtil;

	@Autowired
	AcmeNonceRepository nonceRepository;

	@Autowired
	AcmeAccountRepository acctRepository;

	@Autowired
	AcmeContactRepository contactRepo;

	@Autowired
	PipelineRepository pipeRepo;

    @Autowired
    PipelineUtil pipelineUtil;

    UriComponentsBuilder getEffectiveUriComponentsBuilder(final String realm, final String forwardedHost){

        ServletUriComponentsBuilder builder = fromCurrentRequestUri();
        if( forwardedHost != null){
            try {
                URI forwardUri = new URI(forwardedHost);
                builder.scheme(forwardUri.getScheme());
                builder.host(forwardUri.getHost());
                builder.port(forwardUri.getPort());
                LOG.debug("ACME URI updated from proxy to {}://{}:{}", forwardUri.getScheme(), forwardUri.getHost(), forwardUri.getPort());
            } catch (URISyntaxException e) {
                LOG.warn("forwardedHost '"+forwardedHost+"' not valid URI", e);
            }
        }
        return builder;
    }

    public UriComponentsBuilder newAuthorizationResourceUriBuilderFrom(
			final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, NEW_AUTHORIZATION_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder authorizationResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, AUTHORIZATION_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder certificateResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, CERTIFICATE_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder challengeResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, CHALLENGE_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder newNonceResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, NEW_NONCE_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder newAccountResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, NEW_ACCOUNT_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder newOrderResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, NEW_ORDER_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder orderResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, ORDER_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder accountResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, ACCOUNT_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder directoryResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, DIRECTORY_RESOURCE_MAPPING);
	}

	public UriComponentsBuilder keyChangeResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, ACCOUNT_RESOURCE_MAPPING + "/changeKey");
	}

	public UriComponentsBuilder revokeResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
		return buildUrlFrom(uriComponentsBuilder, CERTIFICATE_RESOURCE_MAPPING + "/revoke");
	}

	public UriComponentsBuilder buildUrlFrom(final UriComponentsBuilder uriComponentsBuilder, final String path) {
		return uriComponentsBuilder.path("/..").path(path);
	}

	public URI locationUriOfOrder(final long orderId, final UriComponentsBuilder uriBuilder) {
		return orderResourceUriBuilderFrom(uriBuilder.path("..")).path("/").path(Long.toString(orderId)).build()
				.normalize().toUri();
	}

	public URI locationUriOfOrderFinalize(final long orderId, final UriComponentsBuilder uriBuilder) {
		return orderResourceUriBuilderFrom(uriBuilder.path("..")).path("/finalize/").path(Long.toString(orderId))
				.build().normalize().toUri();
	}

	public URI locationUriOfCertificate(final long certId, final UriComponentsBuilder uriBuilder) {
		return certificateResourceUriBuilderFrom(uriBuilder).path("/").path(Long.toString(certId)).build().normalize()
				.toUri();
	}

	public URI locationUriOfAuth(final long authId, final UriComponentsBuilder uriBuilder) {
		return authorizationResourceUriBuilderFrom(uriBuilder).path("/").path(Long.toString(authId)).build()
				.normalize().toUri();
	}

	/**
	 * get the pipeline for a given realm
	 *
	 * @param realm
	 * @return
	 */
	public Pipeline getPipelineForRealm(final String realm) {

		List<Pipeline> pipelineList = pipeRepo.findActiveByTypeUrl(PipelineType.ACME, realm);

		if(pipelineList.isEmpty()) {
			LOG.warn("realm {} is not known", realm);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.REALM_DOES_NOT_EXIST, "realm not found",
					BAD_REQUEST, "", AcmeController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}

		if(pipelineList.size() > 1) {
			LOG.warn("misconfiguration for realm '{}', multiple configurations handling this realm", realm);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.SERVER_INTERNAL, "Pipeline configuration broken",
					BAD_REQUEST, "", AcmeController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}

		return pipelineList.get(0);

	}
	/**
	 *
	 * @param acctDao
	 * @param updatedAcct
	 */
	public void contactsFromRequest(AcmeAccount acctDao, AccountRequest updatedAcct) {

		Set<AcmeContact> contactSet = acctDao.getContacts();
		if (contactSet == null) {
			contactSet = new HashSet<>();
		}

		contactSet.clear();

		if (updatedAcct.getContacts().isEmpty()) {
			// nothing to do
			LOG.error("No contact info present");
		} else {
			for (String contactUrl : updatedAcct.getContacts()) {

                if( acctDao.getContacts().stream().anyMatch(c -> c.getContactUrl().trim().equals(contactUrl.trim()))){
                    LOG.info("contact utl '{}' already known fo account {}", contactUrl, acctDao.getId());
                    continue;
                }

				AcmeContact contactDao = new AcmeContact();
				contactDao.setContactId(generateId());
				contactDao.setAccount(acctDao);
				contactDao.setContactUrl(contactUrl);
				contactSet.add(contactDao);
				LOG.info("contact info {} stored for account {}", contactDao.getContactUrl(),
						contactDao.getAccount().getAccountId());
			}
			contactRepo.saveAll(contactSet);
		}
		acctDao.setContacts(contactSet);

		if (updatedAcct.getExternalAccountBinding() != null) {
			LOG.info("Unsupported ExternalAccountBinding info present");
		}

		// don't allow to activate the account by a remote call
		if (AccountStatus.DEACTIVATED.equals(updatedAcct.getStatus())
				|| AccountStatus.REVOKED.equals(updatedAcct.getStatus())) {
			acctDao.setStatus(updatedAcct.getStatus());
		} else if (updatedAcct.getStatus() == null) {
			LOG.debug("No status transition of AccountStatus requested externally");
		} else {
			LOG.info("Unexpected transition of AccountStatus to '{}' requested", updatedAcct.getStatus());
		}

	}

//	AcmeAccount checkJWTSignatureForAccount(JwtContext context) {
//		return checkJWTSignatureForAccount(context, null, null);
//	}

	AcmeAccount checkJWTSignatureForAccount(JwtContext context, final String realm) {
		return checkJWTSignatureForAccount(context, realm, null);
	}

	/**
	 * retrieve Account and check given JWT
	 *
	 * @param context
	 * @return
     */
	AcmeAccount checkJWTSignatureForAccount(JwtContext context, final String realm, Long accountIdReq) {

		try {
			JsonWebStructure webStruct = jwtUtil.getJsonWebStructure(context);

			checkNonce(webStruct);

			String kid = jwtUtil.getKid(webStruct);
            if(kid == null){
                LOG.error("requested account {} does not match account for kid {}", accountIdReq, kid);
                final ProblemDetail problem = new ProblemDetail(AcmeUtil.ACCOUNT_DOES_NOT_EXIST, "No kid found in account jwt",
                    BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problem);
            }

			Long accountId = jwtUtil.getAccountIdForKid(kid);
			if (accountIdReq != null && (!accountId.equals(accountIdReq))) {
				LOG.error("requested account {} does not match account for kid {}", accountIdReq, kid);
				throw new AccountDoesNotExistException(accountId);
			}

			List<AcmeAccount> accListExisting = acctRepository.findByAccountId(accountId.longValue());
			if (accListExisting.isEmpty()) {
				LOG.error("Missing required key ID");
				throw new AccountDoesNotExistException(accountId);
			}

			AcmeAccount acctDao = accListExisting.get(0);
			LOG.debug("request signature identifies account id {} ", acctDao.getAccountId());

			if ((realm != null) && !realm.equals(acctDao.getRealm())) {
				LOG.warn("Account {} of {} does not match realm {}", acctDao.getAccountId(), acctDao.getRealm(), realm);
				final ProblemDetail problem = new ProblemDetail(AcmeUtil.ACCOUNT_DOES_NOT_EXIST, "Account not found",
						BAD_REQUEST, "", AcmeController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}

            if (!AccountStatus.VALID.equals(acctDao.getStatus())) {
                String title = "Account not activate";
                if (AccountStatus.PENDING.equals(acctDao.getStatus())){
                    LOG.warn("Account {} activation still pending", acctDao.getAccountId());
                    title = "Account not activate, yet";
                }else{
                    LOG.warn("Account {} is NOT activate (status {})", acctDao.getAccountId(), acctDao.getStatus());
                }

                final ProblemDetail problem = new ProblemDetail(AcmeUtil.ACCOUNT_DEACTIVATED, title,
                    BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problem);
            }

            Pipeline pipeline = getPipelineForRealm(realm);

            if(!pipeline.isActive()) {
                String msg = "Deactivated pipeline '"+pipeline.getName()+"' found for request realm '"+realm+"'" ;
                LOG.info(msg);
                final ProblemDetail problemDetail = new ProblemDetail(AcmeUtil.MALFORMED, "Realm unknown",
                    BAD_REQUEST, msg, AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problemDetail);
            }

            jwtUtil.validateSignature(context, acctDao.getPublicKey(), acctDao.getAccountId());

			return acctDao;

		} catch (IOException | JoseException | InvalidJwtException e) {

			LOG.debug("Problem processing JWT payload for Account ", e);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JWT validation problem", BAD_REQUEST,
					e.getMessage(), AcmeController.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}

	}

	/**
	 * @param webStruct
	 * @throws JoseException
	 * @throws AcmeProblemException
	 */
	protected void checkNonce(JsonWebStructure webStruct) throws JoseException, AcmeProblemException {
		String reqNonce = jwtUtil.getNonce(webStruct);
		List<AcmeNonce> nonceList = nonceRepository.findByNonceValue(reqNonce);
		if (nonceList.isEmpty()) {
			LOG.debug("Nonce {} not found in database", reqNonce);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.BAD_NONCE, "Nonce not known.", BAD_REQUEST,
					NO_DETAIL, NO_INSTANCE);
			throw new AcmeProblemException(problem);
		} else {
			nonceRepository.deleteAll(nonceList);
			LOG.debug("Nonce found ... and deleted");
		}
	}

	protected HttpHeaders buildNonceHeader() {
		final HttpHeaders additionalHeaders = new HttpHeaders();
		AcmeNonce nonce = getNewNonce();
		additionalHeaders.set(REPLAY_NONCE_HEADER, nonce.getNonceValue());
		return additionalHeaders;
	}

	/**
	 * @param e
	 * @return
	 */
	protected ResponseEntity<?> buildProblemResponseEntity(AcmeProblemException e) {

		LOG.debug("returning ACME problem ", e);
		final HttpHeaders problemHeaders = new HttpHeaders();
		problemHeaders.setContentType(ProblemDetail.APPLICATION_PROBLEM_JSON);
		return ResponseEntity.status(e.getProblem().getStatus()).headers(problemHeaders).body(e.getProblem());
	}

	protected AcmeNonce getNewNonce() {

		AcmeNonce nonce = new AcmeNonce();

		String nonceRaw = getBase64UrlEncodedRandom(16);
		nonce.setNonceValue(nonceRaw.split("=")[0]);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, DEFAULT_NONCE_VALID_DAYS); // minus number would decrement the days

		nonce.setExpiresAt(DateUtil.asInstant(new Date(cal.getTimeInMillis())));
		nonceRepository.save(nonce);

		LOG.debug("New Nonce {} created", nonce.getNonceValue());

		return nonce;
	}

	protected String getRandomChallenge() {
		String challengeToken = getBase64UrlEncodedRandom(16);
		return challengeToken.split("=")[0];
	}

	public String getBase64UrlEncodedRandom(int len) {
		final byte[] randomBytes = new byte[len];
		secRandom.nextBytes(randomBytes);
		return Base64Utils.encodeToUrlSafeString(randomBytes);
	}

	/**
	 * generate new random identifiers
	 *
	 * @return
	 */
	public long generateId() {

		long val = secRandom.nextLong();
		if (val < 0L) {
			return val * -1L;
		}
		return val;
	}

}
