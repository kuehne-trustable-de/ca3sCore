package de.trustable.ca3s.core.web.rest.acme;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.domain.Nonce;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.ACMEAccountRepository;
import de.trustable.ca3s.core.repository.AcmeContactRepository;
import de.trustable.ca3s.core.repository.NonceRepository;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.DateUtil;
import de.trustable.ca3s.core.service.util.JwtUtil;
import de.trustable.util.CryptoUtil;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Controller
public class ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(ACMEController.class);
    

	  public static final URI NO_INSTANCE = null;
	  public static final String NO_DETAIL = null;

	  
	  public static final String APPLICATION_JWS_VALUE = "application/jws";
	  public static final String APPLICATION_JOSE_JSON_VALUE = "application/jose+json";
	  public static final String APPLICATION_PKIX_CERT_VALUE = "application/pkix-cert";
	  public static final String APPLICATION_PEM_CERT_CHAIN_VALUE = "application/pem-certificate-chain";
	  public static final String APPLICATION_PEM_CERT_VALUE = "application/pem-certificate";
	  public static final String APPLICATION_PEM_FILE_VALUE = "application/x-pem-file";
	  public static final MediaType APPLICATION_JOSE_JSON = MediaType.parseMediaType(APPLICATION_JOSE_JSON_VALUE);
	  public static final MediaType APPLICATION_JWS = MediaType.parseMediaType(APPLICATION_JWS_VALUE);
	  public static final MediaType APPLICATION_PKIX_CERT = MediaType.parseMediaType(APPLICATION_PKIX_CERT_VALUE);
	  public static final MediaType APPLICATION_PEM_CERT_CHAIN = MediaType.parseMediaType(APPLICATION_PEM_CERT_CHAIN_VALUE);
	  public static final MediaType APPLICATION_PEM_CERT = MediaType.parseMediaType(APPLICATION_PEM_CERT_VALUE);
	  public static final MediaType APPLICATION_PEM_FILE = MediaType.parseMediaType(APPLICATION_PEM_FILE_VALUE);
	  
	  public static int DEFAULT_NONCE_VALID_DAYS = 1;
	  public static final String REPLAY_NONCE_HEADER = "Replay-Nonce";

	  static final String GENERAL_URL_PREFIX = "/acme/{realm}";
	  
	  String DIRECTORY_RESOURCE_MAPPING = afterPrefix(DirectoryController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String NEW_AUTHORIZATION_RESOURCE_MAPPING = afterPrefix(NewOrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String AUTHORIZATION_RESOURCE_MAPPING = afterPrefix(AuthorizationController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String ACCOUNT_RESOURCE_MAPPING = afterPrefix(AccountController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String NEW_NONCE_RESOURCE_MAPPING = afterPrefix(NewNonceController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String NEW_ACCOUNT_RESOURCE_MAPPING = afterPrefix(NewAccountController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String NEW_ORDER_RESOURCE_MAPPING = afterPrefix(NewOrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String ORDER_RESOURCE_MAPPING = afterPrefix(OrderController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String CHALLENGE_RESOURCE_MAPPING = afterPrefix(ChallengeController.class.getAnnotation(RequestMapping.class).value()[0]);
	  String CERTIFICATE_RESOURCE_MAPPING = afterPrefix(ACMECertificateController.class.getAnnotation(RequestMapping.class).value()[0]);

	  
	  static String afterPrefix(String url) {
		
		  if( url.startsWith(GENERAL_URL_PREFIX)) {
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
	  NonceRepository nonceRepository;

	  @Autowired
      ACMEAccountRepository acctRepository;

	  @Autowired
	  AcmeContactRepository contactRepo;


	  public UriComponentsBuilder newAuthorizationResourceUriBuilderFrom(final UriComponentsBuilder uriComponentsBuilder) {
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
		    return orderResourceUriBuilderFrom(uriBuilder.path("..")).path("/").path(Long.toString(orderId)).build().normalize().toUri();
		  }

	  public URI locationUriOfOrderFinalize(final long orderId, final UriComponentsBuilder uriBuilder) {
		    return orderResourceUriBuilderFrom(uriBuilder.path("..")).path("/finalize/").path(Long.toString(orderId)).build().normalize().toUri();
		  }

	  public URI locationUriOfCertificate(final long certId, final UriComponentsBuilder uriBuilder) {
		    return certificateResourceUriBuilderFrom(uriBuilder).path("/").path(Long.toString(certId)).build().normalize().toUri();
		  }

	  public URI locationUriOfAuth(final long authId, final UriComponentsBuilder uriBuilder) {
		    return authorizationResourceUriBuilderFrom(uriBuilder.path("..")).path("/").path(Long.toString(authId)).build().normalize().toUri();
		  }

	  
	public void contactsFromRequest(ACMEAccount acctDao, AccountRequest updatedAcct) {
		
		Set<AcmeContact> contactSet = acctDao.getContacts();
		if(contactSet == null){
			contactSet = new HashSet<AcmeContact>();
		}
		
		contactSet.clear();
		
		if( updatedAcct.getContacts().isEmpty()) {
			// nothing to do
			LOG.error("No contact info present");
		}else {
			for(String contactUrl: updatedAcct.getContacts()) {
				AcmeContact contactDao = new AcmeContact();
				contactDao.setContactId(generateId());
				contactDao.setAccount(acctDao);
				contactDao.setContactUrl(contactUrl);
				contactSet.add(contactDao);
				LOG.info("contact info {} stored for account {}", contactDao.getContactUrl(), contactDao.getAccount().getAccountId());
			}
			contactRepo.saveAll(contactSet);
		}
		acctDao.setContacts(contactSet);
		
		if( updatedAcct.getExternalAccountBinding() != null) {
			LOG.info("Unsupported ExternalAccountBinding info present");
		}
		
		// don't allow to activate the account by a remote call
		if( AccountStatus.DEACTIVATED.equals( updatedAcct.getStatus() ) || AccountStatus.REVOKED.equals( updatedAcct.getStatus() ) ) {
			acctDao.setStatus(updatedAcct.getStatus());
		}else {
			LOG.info("Unexpected transition of AccountStatus to '{}' requested", updatedAcct.getStatus());
		}
		

	}

	ACMEAccount checkJWTSignatureForAccount(JwtContext context)  {
		  return checkJWTSignatureForAccount(context, null, null); 
	  }
	  
	ACMEAccount checkJWTSignatureForAccount(JwtContext context, final String realm)  {
		  return checkJWTSignatureForAccount(context, realm, null); 
	  }
	  /**
	   * retrieve Account and check given JWT
	   * 
	   * @param context
	   * @return
	   * @throws IOException
	   * @throws JoseException
	   * @throws InvalidJwtException
	   */
	ACMEAccount checkJWTSignatureForAccount(JwtContext context, final String realm, Long accountIdReq)  {
		  
		try {
			JsonWebStructure webStruct = jwtUtil.getJsonWebStructure(context);

			checkNonce(webStruct);

			String kid = jwtUtil.getKid(webStruct);
			Long accountId = jwtUtil.getAccountIdForKid(kid);
			if( accountIdReq != null && (!accountId.equals(accountIdReq))) {
				LOG.error("requested account {} does not match account for kid {}", accountIdReq, kid);
				throw new AccountDoesNotExistException(accountId);
			}
			
			List<ACMEAccount> accListExisting = acctRepository.findByAccountId(accountId.longValue());
			if (accListExisting.isEmpty()) {
				LOG.error("Missing required key ID");
				throw new AccountDoesNotExistException(accountId);
			}

			ACMEAccount acctDao = accListExisting.get(0);
			LOG.info("request signature identifies account id {} ", acctDao.getAccountId());

			
			if( ( realm != null ) && !realm.equals( acctDao.getRealm())){
				LOG.warn("Account {} of {} does not match realm {}", acctDao.getAccountId(), acctDao.getRealm(), realm);
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.ACCOUNT_DOES_NOT_EXIST, "Account not found",
						BAD_REQUEST, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}
			
			if( AccountStatus.DEACTIVATED.equals( acctDao.getStatus())){
				LOG.warn("Account {} is deactivated", acctDao.getAccountId());
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.ACCOUNT_DEACTIVATED, "Account deactivated",
						BAD_REQUEST, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}
			
			jwtUtil.validateSignature(context, acctDao.getPublicKey(), acctDao.getAccountId());

			return acctDao;
			
		} catch (IOException | JoseException | InvalidJwtException e) {

			LOG.debug("Problem processing JWT payload for Account ", e);
			final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "JWT validation problem",
					BAD_REQUEST, e.getMessage(), ACMEController.NO_INSTANCE);
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
	    List<Nonce> nonceList = nonceRepository.findByNonceValue(reqNonce);
	    if( nonceList.isEmpty()) {
			LOG.debug("Nonce {} not found in database", reqNonce);
	        final ProblemDetail problem = new ProblemDetail(ACMEUtil.BAD_NONCE, "Nonce not known.",
	                BAD_REQUEST, NO_DETAIL, NO_INSTANCE);
	    	throw new AcmeProblemException(problem);
	    }else {
	    	nonceRepository.deleteAll(nonceList);
			LOG.debug("Nonce found ... and deleted");
	    }
	}

	protected HttpHeaders buildNonceHeader() {
		final HttpHeaders additionalHeaders = new HttpHeaders();
		Nonce nonce = getNewNonce();
	    additionalHeaders.set(REPLAY_NONCE_HEADER, nonce.getNonceValue());
		return additionalHeaders;
	}


	
	/**
	 * @param e
	 * @return
	 */
	protected ResponseEntity<?> buildProblemResponseEntity(AcmeProblemException e) {
		
		LOG.debug("returning ACME problem ", e );
		final HttpHeaders problemHeaders = new HttpHeaders();
		problemHeaders.setContentType(ProblemDetail.APPLICATION_PROBLEM_JSON);
		return ResponseEntity.status(e.getProblem().getStatus()).headers(problemHeaders).body(e.getProblem());
	}
	
	 

	protected Nonce getNewNonce() {
		
		Nonce nonce = new Nonce();
		
		String nonceRaw = getBase64UrlEncodedRandom(16);
		nonce.setNonceValue( nonceRaw.split("=")[0]);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, DEFAULT_NONCE_VALID_DAYS); // minus number would decrement the days

		nonce.setExpiresAt(DateUtil.asLocalDate(new Date(cal.getTimeInMillis())));
		nonceRepository.save(nonce);

		LOG.info("New Nonce {} created", nonce.getNonceValue());

		return nonce;
	}


	protected String getNewChallenge() {
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
		if( val < 0L) {
			return val * -1L;
		}
		return val;
	}

}
