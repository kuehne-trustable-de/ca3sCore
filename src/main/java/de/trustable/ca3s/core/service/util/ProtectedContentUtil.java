package de.trustable.ca3s.core.service.util;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static de.trustable.ca3s.core.domain.ProtectedContent.USER_CONTENT_RELATION_TYPE_LIST;

@Service
public class ProtectedContentUtil {

    public static final String PLAIN_SECRET_PLACEHOLDER = "******";
    private final Logger log = LoggerFactory.getLogger(ProtectedContentUtil.class);

    private final BasicTextEncryptor textEncryptor;

	// defining our own max instant, as Instant.MAX is out of the range hibernate supports :-(
    // the following version fails:
    // public static final Instant MAX_INSTANT = Instant.parse("9999-12-30T23:59:59Z");

    public static final Instant MAX_INSTANT = Instant.parse("9990-12-30T23:59:59Z");

    private final ProtectedContentRepository protContentRepository;

    private final CertificateRepository certificateRepository;

    private final String salt;
    private final int iterations;
    private final String pbeAlgo;

	public ProtectedContentUtil(ProtectedContentRepository protContentRepository,
                                CertificateRepository certificateRepository,
                                @Value("${protectionSecret:mJvR25yt4NHTIqe5Hz7nUHhQNUuM}") String protectionSecretFallback,
                                @Value("${ca3s.protectionSecret:#{null}}") String protectionSecret,
                                @Value("${ca3s.connection.salt:ca3sSalt}") String salt,
                                @Value("${ca3s.connection.iterations:4567}") int iterations,
                                @Value("${ca3s.connection.pbeAlgo:PBKDF2WithHmacSHA256}") String pbeAlgo) {

        this.protContentRepository = protContentRepository;
        this.certificateRepository = certificateRepository;

        this.salt = salt;
        this.iterations = iterations;
        this.pbeAlgo = pbeAlgo;

        PasswordUtil passwordUtil = new PasswordUtil("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{16,100}$");

        if( (protectionSecret == null) || (protectionSecret.trim().length() == 0)) {
            if ((protectionSecretFallback != null) && !protectionSecretFallback.trim().isEmpty()) {
                if("mJvR25yt4NHTIqe5Hz7nUHhQNUuM".equals(protectionSecretFallback)){
                    log.warn("Please provide a secure value for 'ca3s.protectionSecret'!");
                }else{
                    log.warn("The configuration parameter 'protectionSecret' is deprecated! Use 'ca3s.protectionSecret'.");
                }
                protectionSecret = protectionSecretFallback;
            }
        }

		if( (protectionSecret == null) || (protectionSecret.trim().length() == 0)) {
            log.warn("Configuration parameter 'protectionSecret' missing or invalid!!");
            throw new UnsupportedOperationException("Configuration parameter 'protectionSecret' missing or invalid");
		}

        passwordUtil.checkPassword(protectionSecret, "Value of 'ca3s.protectionSecret'");

		if( log.isDebugEnabled()) {
			log.debug("using protection secret '{}'", PasswordUtil.maskPassword(protectionSecret) );
		}

        textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(protectionSecret);
	}


    public boolean hasMinimumOrLessLeftUsages(ProtectedContent protectedContent, int minNumber){
        if (protectedContent.getLeftUsages() < 0){
            return false;
        }
        return (protectedContent.getLeftUsages() <= minNumber);

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
            MAX_INSTANT,
            null,
            ProtectedContentStatus.ACTIVE);
    }

    /**
     * create a new ProtectedContent object and save the given content
     *
     * @param plainText    the plain text to be protected
     * @param pct          the content type of the plainText
     * @param crt          the related entity
     * @param connectionId the related entity
     * @param validTo      element usable until 'validTo'
     * @return the freshly created object
     */
    public ProtectedContent createProtectedContent(final String plainText, ProtectedContentType pct,
                                                   ContentRelationType crt, long connectionId,
                                                   Instant validTo) {

        return createProtectedContent(plainText,
            pct,
            crt,
            connectionId,
            -1,
            validTo,
            null,
            ProtectedContentStatus.ACTIVE);
    }

    /**
     * create a new ProtectedContent object and save the given content
     *
     * @param plainText    the plain text to be protected
     * @param pct          the content type of the plainText
     * @param crt          the related entity
     * @param connectionId the related entity
     * @param leftUsages   number of left usages for this element
     * @param validTo      element usable until 'validTo'
     * @return the freshly created object
     */
    public ProtectedContent createProtectedContent(final String plainText,
                                                   ProtectedContentType pct,
                                                   ContentRelationType crt,
                                                   long connectionId,
                                                   int leftUsages,
                                                   Instant validTo) {
        return createProtectedContent(plainText,
            pct,
            crt,
            connectionId,
            leftUsages,
            validTo,
            null,
            ProtectedContentStatus.ACTIVE);
    }
    /**
     * create a new ProtectedContent object and save the given content
     *
     * @param plainText    the plain text to be protected
     * @param pct          the content type of the plainText
     * @param crt          the related entity
     * @param connectionId the related entity
     * @param leftUsages   number of left usages for this element
     * @param validTo      element usable until 'validTo'
     * @param pipeline     the related pipeline
     * @return the freshly created object
     */
    public ProtectedContent createProtectedContent(final String plainText,
                                                   ProtectedContentType pct,
                                                   ContentRelationType crt,
                                                   long connectionId,
                                                   int leftUsages,
                                                   Instant validTo,
                                                   Pipeline pipeline,
                                                   ProtectedContentStatus protectedContentStatus) {
        return createPlainProtectedContent(protectString(plainText),
            pct,
            crt,
            connectionId,
            leftUsages,
            validTo,
            pipeline,
            protectedContentStatus);
    }


    public ProtectedContent createDerivedProtectedContent(final String plainText,
                                                          ProtectedContentType pct,
                                                          ContentRelationType crt,
                                                          long connectionId,
                                                          int leftUsages,
                                                          Instant validTo) {

        try {
            return createPlainProtectedContent(Base64.getEncoder().encodeToString(deriveSecret(plainText)),
                pct, crt, connectionId, leftUsages, validTo);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    public ProtectedContent createDerivedProtectedContent(final String plainText,
                                                          ProtectedContentType pct,
                                                          ContentRelationType crt,
                                                          long connectionId,
                                                          int leftUsages,
                                                          Instant validTo,
                                                          Pipeline pipeline,
                                                          ProtectedContentStatus protectedContentStatus) {

        try {
            return createPlainProtectedContent(Base64.getEncoder().encodeToString(deriveSecret(plainText)),
                pct, crt, connectionId, leftUsages, validTo, pipeline, protectedContentStatus);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    private ProtectedContent createPlainProtectedContent(final String contentBase64,
                                                         ProtectedContentType pct,
                                                         ContentRelationType crt,
                                                         long connectionId,
                                                         int leftUsages,
                                                         Instant validTo) {
        return createPlainProtectedContent(contentBase64,
            pct,
            crt,
            connectionId,
            leftUsages,
            validTo,
            null,
            ProtectedContentStatus.ACTIVE);
    }

    private ProtectedContent createPlainProtectedContent(final String contentBase64,
                                                         ProtectedContentType pct,
                                                         ContentRelationType crt,
                                                         long connectionId,
                                                         int leftUsages,
                                                         Instant validTo,
                                                         Pipeline pipeline,
                                                         ProtectedContentStatus protectedContentStatus) {

        ProtectedContent pc = new ProtectedContent();

        pc.setContentBase64(contentBase64);
        pc.setType(pct);
        pc.setRelationType(crt);
        pc.setRelatedId(connectionId);
        pc.setCreatedOn(Instant.now());
        pc.setLeftUsages(leftUsages);
        pc.setValidTo(validTo);
        pc.setDeleteAfter(validTo.plus(1, ChronoUnit.DAYS));
        pc.setPipeline(pipeline);
        pc.setStatus(protectedContentStatus);

        protContentRepository.save(pc);
        return pc;
    }

    public List<ProtectedContent> findProtectedContentBySecret(final String plainText,
                                                                  final ProtectedContentType type,
                                                                  final ContentRelationType crt) {

        String protectedString = null;
        try {
            protectedString = Base64.getEncoder().encodeToString( deriveSecret(plainText));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        log.debug("searching for protectedString '{}'", protectedString );

        List<ProtectedContent> pcList = protContentRepository.findByTypeRelationContentB64(type, crt, protectedString);

        Instant now = Instant.now();
        Predicate<ProtectedContent> usableItem = pc -> ((pc.getLeftUsages() == -1) || (pc.getLeftUsages() > 0)) && pc.getValidTo().isAfter(now);
        return pcList.stream().filter(usableItem).collect(Collectors.toList());
    }

    public List<ProtectedContent> findProtectedContentByRelatedIdAndSecret(final String plainText,
                                                               final ProtectedContentType type,
                                                               final ContentRelationType crt,
                                                               Long id) {

        log.debug("searching for protectedString '{}'", plainText );

        List<ProtectedContent> pcList = protContentRepository.findByTypeRelationId(type, crt, id);

        List<ProtectedContent> matchingItemList = pcList.stream().filter(
            pc -> unprotectString(pc.getContentBase64()).equals(plainText))
            .collect(Collectors.toList());

        Instant now = Instant.now();
        Predicate<ProtectedContent> usableItem = pc -> ((pc.getLeftUsages() == -1) || (pc.getLeftUsages() > 0)) && pc.getValidTo().isAfter(now);
        return matchingItemList.stream().filter(usableItem).collect(Collectors.toList());
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

    public void deleteProtectedContent(ProtectedContent pc) {
        protContentRepository.delete(pc);
    }

    public void deleteProtectedContent(ProtectedContentType type, ContentRelationType crt, long id) {
        List<ProtectedContent> pcList = protContentRepository.findByTypeRelationId(type, crt, id);
        protContentRepository.deleteAll(pcList);
    }

    boolean isActive(ProtectedContent pc, Instant now){
        return ((pc.getLeftUsages() == -1) || (pc.getLeftUsages() > 0)) && pc.getValidTo().isAfter(now);
    }

    /**
     * Get all active second factor credentials for a given user.
     *
     * @param user the user holding the credentials.
     */
    public List<ProtectedContent> getActiveCredentials(User user) {
        log.debug("getActiveCredentials for  : {}", user);

        Instant now = Instant.now();
        List<ProtectedContent> activeCredentialsList = new ArrayList<>();

        for( ProtectedContent protectedContent:
            protContentRepository.findByTypeRelationId( USER_CONTENT_RELATION_TYPE_LIST, user.getId())){

            if( isActive(protectedContent, now) ){
                switch( protectedContent.getRelationType()){
                    case CERTIFICATE:
                        Optional<Certificate> certificateOptional =
                            certificateRepository.findById(protectedContent.getRelatedId());
                        if( certificateOptional.isPresent()) {
                            Certificate userCert = certificateOptional.get();
                            if (userCert.isActive()) {
                                activeCredentialsList.add(protectedContent);
                            }
                        }
                        break;

                    case ACCOUNT_TOKEN:
                        // not relevant, here
                        break;
                    case OTP_SECRET:
                    case SMS_PHONE:
                        activeCredentialsList.add(protectedContent);
                        break;
                    default:
                        log.warn("Unexpected relation type '{}' occurred.", protectedContent.getRelationType());
                        break;
                }

            }
        }

        return activeCredentialsList;
    }

    public void updateServersideKeyRetentionSettings(long csrId, Instant validTo, int usages){

        log.info("Update the retention settings for csr #{} to validTo {}, left usages {} ", csrId, validTo, usages);

        List<ProtectedContent> protectedKeys = retrieveProtectedContent(
            ProtectedContentType.KEY,
            ContentRelationType.CSR,
            csrId);

        for(ProtectedContent protectedContent: protectedKeys) {
            setRetentionSettings(protectedContent, validTo, usages);
        }
        protContentRepository.saveAll(protectedKeys);

        List<ProtectedContent> protectedPasswords = retrieveProtectedContent(
            ProtectedContentType.PASSWORD,
            ContentRelationType.CSR,
            csrId);

        for(ProtectedContent protectedContent: protectedPasswords) {
            setRetentionSettings(protectedContent, validTo, usages);
        }
        protContentRepository.saveAll(protectedPasswords);
    }

    private void setRetentionSettings(ProtectedContent protectedContent, Instant validTo, int usages) {
        protectedContent.setValidTo(validTo);
        protectedContent.setDeleteAfter(validTo.plus(5, ChronoUnit.DAYS));
        protectedContent.setLeftUsages(usages);
    }

    public byte[] deriveSecret(String secret) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return deriveSecret(secret.toCharArray());
    }

    public byte[] deriveSecret(char[] secret) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(this.pbeAlgo);
        PBEKeySpec specSecKey = new PBEKeySpec(secret, salt.getBytes(), iterations, 256);
        return skf.generateSecret(specSecKey).getEncoded();

    }

}
