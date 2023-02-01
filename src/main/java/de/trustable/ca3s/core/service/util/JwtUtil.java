package de.trustable.ca3s.core.service.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64.Encoder;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.trustable.ca3s.core.service.dto.acme.*;
import org.apache.commons.codec.binary.Base64;
// import org.apache.tomcat.util.codec.binary.Base64;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey.Factory;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;


@Service
public class JwtUtil {

	  public static final String KID = "kid";
	  public static final String NONCE = "nonce";
	  public static final String JWK = "jwk";
	  private static final String NO_EXPLICIT_JCA_PROVIDER = null;
	  private static final String EMPTY_PAYLOAD_REPLACEMENT_B64 = "e30";

	 private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

     private final ObjectMapper objectMapper = new ObjectMapper();


     public JwtUtil(){
         objectMapper.registerModule(new JavaTimeModule());
     }

//		  private final ObjectReader objectReader;

/*
		  @Override
		  protected final PAYLOAD parsePayloadFrom(final JwtClaims jwtClaims) throws InvalidAcmeMessageException {
		    try {
		      return objectReader.readValue(jwtClaims.toJson());
		    } catch (JsonMappingException e) {
		      final Throwable cause = e.getCause();
		      if (cause instanceof AcmeMessageException) {
		        throw (AcmeMessageException) cause;
		      }
		      throw new InvalidAcmeMessageException("Invalid payload", jwtClaims.toJson(), e);
		    } catch (IOException e) {
		      throw new InvalidAcmeMessageException("Invalid payload", jwtClaims.toJson(), e);
		    }
		  }

		}
*/

    private static final JwtConsumer NOT_VALIDATING_JWT_CONSUMER = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build();


    public JwtContext processFlattenedJWT(final String flattenedJwsJson) {

	    LOG.debug("Converting Flattened JWT: {}", flattenedJwsJson);
	    final String compactJwsSerialization;
	    try {
	      final JsonNode jsonRootNode = objectMapper.readTree(flattenedJwsJson);
	      final JsonNode jsonProtectedValue = jsonRootNode.get("protected");
	      if( jsonProtectedValue == null){
	    	  throw new IOException("JWT component 'protected' missing");
	      }
	      final String protectedValue = jsonProtectedValue.asText();
		  LOG.debug("protected JWT content decoded: {}", new String(Base64.decodeBase64(protectedValue)));

	      final JsonNode jsonPayloadValue = jsonRootNode.get("payload");
	      if( jsonPayloadValue == null){
	    	  throw new IOException("JWT component 'payload' missing");
	      }
	      String payload = jsonPayloadValue.asText();
	      if(payload.length() == 0 ) {
	    	  payload = EMPTY_PAYLOAD_REPLACEMENT_B64;
	      }
		  LOG.debug("JWT payload decoded: {}", new String(Base64.decodeBase64(payload)));

	      final JsonNode jsonSignatureValue = jsonRootNode.get("signature");
	      if( jsonSignatureValue == null){
	    	  throw new IOException("JWT component 'signature' missing");
	      }
	      final String signature = jsonSignatureValue.asText();
	      compactJwsSerialization = protectedValue + "." + payload + "." + signature;

	      return NOT_VALIDATING_JWT_CONSUMER.process(compactJwsSerialization);

	    } catch (IOException | InvalidJwtException e) {

		    LOG.debug("Problem processing JWT from flattenedJwsJson : " + flattenedJwsJson, e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JWT processing problem",
	                BAD_REQUEST, e.getMessage(), AcmeUtil.NO_INSTANCE);
	    	throw new AcmeProblemException(problem);

	    }

    }

    public JwtContext processCompactJWT(final String compactJwsSerialization) {

	    LOG.debug("Processing Compact JWT: {}", compactJwsSerialization);
	    try {
	      return NOT_VALIDATING_JWT_CONSUMER.process(compactJwsSerialization);

	    } catch (InvalidJwtException e) {

		    LOG.debug("Problem processing JWT from compactJwsSerialization : " + compactJwsSerialization, e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JWT processing problem",
	                BAD_REQUEST, e.getMessage(), AcmeUtil.NO_INSTANCE);
	    	throw new AcmeProblemException(problem);

	    }

    }

    public JwtContext convertCompact(final String compactJwsSerialization) throws InvalidJwtException {
    	return NOT_VALIDATING_JWT_CONSUMER.process(compactJwsSerialization);
    }

	public void validateSignature(JwtContext context, String publicKeyBase64, long accountId)
			throws InvalidJwtException, JoseException, IOException {

		try {
		    JsonWebStructure webStruct = getJsonWebStructure(context);

		    String algHeader = webStruct.getAlgorithmHeaderValue();
		    String keyAlgo = "RSA";
		    if( algHeader.toUpperCase().startsWith("E")) {
		    	keyAlgo = "EC";
		    }
			LOG.debug("jws key algo {} selected by JWT algorithm header {}", keyAlgo, algHeader);

			KeyFactory kf = KeyFactory.getInstance(keyAlgo);
			PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKeyBase64)));

			try {
				final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(publicKey).build();
				jwtConsumer.processContext(context);
				LOG.debug("JWT signature validation successful for account {}", accountId);

		    } catch (InvalidJwtSignatureException e) {

		    	// try to detect the certbot hack (inserting a dummy JSON payload '{}', base64 encoded 'e30').
		    	// An empty string is not accepted as a valid JWT payload.
		    	// if present, create an appropriate compact serialization of the JWT and verify it as a JWS.
		    	String jwt = context.getJwt();
				LOG.debug("JWT signature validation failed for content '" + jwt +"'");
	      	    String[] signatureSerializationParts = jwt.split("\\.");
	      	    if( signatureSerializationParts.length != 3) {
	      	    	LOG.debug("Unexpected number of parts in compact signature {} != 3", signatureSerializationParts.length);
	      	    }else {

			      	if( EMPTY_PAYLOAD_REPLACEMENT_B64.equals(signatureSerializationParts[1])) {

			      		// Create a new JsonWebSignature object
			      	    JsonWebSignature jws = new JsonWebSignature();

			      	    String compactSerialization = signatureSerializationParts[0] + ".." + signatureSerializationParts[2];

			      	    // Set the compact serialization on the JWS
			      	    jws.setCompactSerialization(compactSerialization);
			      	    jws.setKey(publicKey);

			      	    if( jws.verifySignature()){
			      	    	LOG.debug("JWT signature validation successful after settin payload to 'blank' for account {}", accountId);
			      	    	return;
			      	    }
		      	    }
		      	}
		      	throw e;

		    } catch (InvalidJwtException e) {
				LOG.error("Failed signature validation: " + e.getMessage());
				throw e;
		    }
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new JoseException("problem reading public key from database", e);
		}
	}

	public void verifyJWT(JwtContext context, PublicKey publicKey) {
		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(publicKey).build();
		try {
			jwtConsumer.processContext(context);
		} catch (InvalidJwtException e) {
		    LOG.info("Problem verifying JWT", e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Problem verifying JWT",
	                BAD_REQUEST, e.getMessage(), AcmeUtil.NO_INSTANCE);
	    	throw new AcmeProblemException(problem);
		}
	}


    public AccountRequest getAccountRequest(final JwtClaims jwtClaims)  {
    	try {
	    	ObjectReader objectReader = objectMapper.readerFor(AccountRequest.class);
	        return objectReader.readValue(jwtClaims.toJson());
	    } catch (IOException e) {
		    LOG.debug("Problem processing JWT payload for Account ", e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JWT processing problem",
	                BAD_REQUEST, e.getMessage(), AcmeUtil.NO_INSTANCE);
	    	throw new AcmeProblemException(problem);
	    }
    }

    public String getAccountResponseAsJSON(final AccountResponse accountResponse)  {
    	ObjectWriter objectWriter = objectMapper.writerFor(AccountResponse.class);
        try {
			return objectWriter.writeValueAsString(accountResponse);
		} catch (JsonProcessingException e) {
		    LOG.debug("Problem writing AccountRedponse ", e);
		    return e.getMessage();
		}
    }

	public String getOrderResponseAsJSON(NewOrderResponse newOrderResp) {
    	ObjectWriter objectWriter = objectMapper.writerFor(NewOrderResponse.class);
        try {
			return objectWriter.writeValueAsString(newOrderResp);
		} catch (JsonProcessingException e) {
		    LOG.debug("Problem writing NewOrderResponse ", e);
		    return e.getMessage();
		}
	}

	public String getOrderResponseAsJSON(OrderResponse orderResp) {
    	ObjectWriter objectWriter = objectMapper.writerFor(OrderResponse.class);
        try {
			return objectWriter.writeValueAsString(orderResp);
		} catch (JsonProcessingException e) {
		    LOG.debug("Problem writing OrderResponse ", e);
		    return e.getMessage();
		}
	}



    public ChangeKeyRequest getChangeKeyRequest(final JwtClaims jwtClaims)  {
    	try {

    		ObjectMapper mapper = new ObjectMapper();
    		SimpleModule module = new SimpleModule();
    		module.addDeserializer(JsonWebKey.class, new JWKJsonDeserializer());
    		mapper.registerModule(module);

	    	ObjectReader objectReader = mapper.readerFor(ChangeKeyRequest.class);
	        return objectReader.readValue(jwtClaims.toJson());
	    } catch (IOException e) {

		    LOG.debug("Problem processing JWT payload for ChangeKeyRequest", e);
	        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JWT processing problem",
	                BAD_REQUEST, e.getMessage(), AcmeUtil.NO_INSTANCE);
	    	throw new AcmeProblemException(problem);
	    }
    }



    public NewOrderRequest getNewOrderRequest(final JwtClaims jwtClaims){
        ObjectReader objectReader = objectMapper.readerFor(NewOrderRequest.class);
        try {
            return objectReader.readValue(jwtClaims.toJson());
        } catch (IOException e) {
            LOG.debug("Problem processing JWT payload for NewOrderRequest", e);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem parsing NewOrderRequest",
                BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }
    }

    public IdentifiersResponse getIdentifiers(final JwtClaims jwtClaims){
        ObjectReader objectReader = objectMapper.readerFor(IdentifiersResponse.class);
        try {
            return objectReader.readValue(jwtClaims.toJson());
        } catch (IOException e) {
            LOG.debug("Problem processing JWT payload for Identifier", e);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem parsing Identifier",
                BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }
    }

    public FinalizeRequest getFinalizeReq(final JwtClaims jwtClaims) {
    	ObjectReader objectReader = objectMapper.readerFor(FinalizeRequest.class);
        try {
			return objectReader.readValue(jwtClaims.toJson());
		} catch (IOException e) {
            LOG.debug("Problem processing JWT payload for FinalizeRequest", e);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem parsing FinalizeRequest",
					BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}
    }

    public RevokeRequest getRevokeReq(final JwtClaims jwtClaims) {

    	ObjectReader objectReader = objectMapper.readerFor(RevokeRequest.class);
        try {
			return objectReader.readValue(jwtClaims.toJson());
		} catch (IOException e) {
            LOG.debug("Problem processing JWT payload for RevokeRequest", e);
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem parsing RevokeRequest",
					BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}
    }


    public JsonWebStructure getJsonWebStructure(JwtContext context) {
	    if( context.getJoseObjects().isEmpty()) {
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JsonWebStructure missing",
					BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
			throw new AcmeProblemException(problem);
	    }
	    if( context.getJoseObjects().size() > 1) {
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "JsonWebStructure contains more than one (" + context.getJoseObjects().size() + ") elements",
					BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
			throw new AcmeProblemException(problem);
	    }

	    return context.getJoseObjects().get(0);
    }

    public long getAccountIdForKid(String kid) {
	    LOG.debug("No JWK, found kid: " + kid);
	    String[] kidParts = kid.split("/");
	    Long accountId = Long.MAX_VALUE;
	    if( kidParts.length > 0) {
	    	accountId = Long.parseLong(kidParts[kidParts.length - 1]);
	    }
	    LOG.debug("Looking for accountId: " + accountId);
	    return accountId;
    }

    /*
     *
    final Optional<URL> optJOSEkid = contextResult.has(JOSEkid.INSTANCE);

      final URL accountURL = optJOSEkid.get();
      final Optional<PublicKey> optPublicKey = accountDAO.getPublicKeyWith(accountURL);
      contextResult.setJsonWebKey(optPublicKey.orElseThrow(() -> new KidDoesNotExistException(accountURL)));
     */

	public PublicKey getPublicKey(final JsonWebStructure webStruct) {
		try {
			final PublicJsonWebKey jwk = webStruct.getHeaders().getPublicJwkHeaderValue(JWK, NO_EXPLICIT_JCA_PROVIDER);
			return (jwk.getPublicKey());
		} catch( NullPointerException | JoseException npe) {
			final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem reading / parsing JWK",
					BAD_REQUEST, "", AcmeUtil.NO_INSTANCE);
			throw new AcmeProblemException(problem);
		}
	}

	public String getKid(final JsonWebStructure webStruct) throws JoseException {
		if( webStruct.getHeader(KID) != null ) {
            return webStruct.getHeaders().getStringHeaderValue(KID);
		}else {
			return null;
		}
	}

	public String getNonce(final JsonWebStructure webStruct) throws JoseException {
		if( webStruct.getHeader(NONCE) != null ) {
            return webStruct.getHeaders().getStringHeaderValue(NONCE);
		}else {
			return null;
		}
	}

	/**
	 * @param newPK
	 * @return
	 * @throws JoseException
	 */
	public String getJWKThumbPrint(PublicKey newPK) throws JoseException {
		// follow all JWK conventions for the calculation of thumbprint
		PublicJsonWebKey jwk = Factory.newPublicJwk(newPK);
		Encoder URL_ENCODER = java.util.Base64.getUrlEncoder().withoutPadding();
        return URL_ENCODER.encodeToString(jwk.calculateThumbprint("SHA-256"));
	}


}
