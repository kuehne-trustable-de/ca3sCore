package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.config.Constants;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.dto.AccountCredentialsType;
import de.trustable.ca3s.core.service.dto.PasswordChangeDTO;
import de.trustable.ca3s.core.service.dto.UserDTO;

import de.trustable.ca3s.core.service.exception.InvalidCredentialException;
import de.trustable.ca3s.core.service.exception.InvalidPasswordException;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.PasswordUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import tech.jhipster.security.RandomUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProtectedContentUtil protectedContentUtil;
    private final ProtectedContentRepository protContentRepository;
    private final AuthorityRepository authorityRepository;
    private final TenantRepository tenantRepository;
    private final CacheManager cacheManager;
    private final PasswordUtil passwordUtil;
    private final CertificateUtil certificateUtil;
    private final SMSService smsService;

    final private EntityManager entityManager;
    private final int activationKeyValidity;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ProtectedContentUtil protectedContentUtil,
                       ProtectedContentRepository protContentRepository,
                       AuthorityRepository authorityRepository,
                       TenantRepository tenantRepository,
                       CacheManager cacheManager,
                       @Value("${ca3s.ui.password.check.regexp:^(?=.*\\d)(?=.*[a-z]).{6,100}$}") String passwordCheckRegExp,
                       CertificateUtil certificateUtil,
                       SMSService smsService,
                       EntityManager entityManager,
                       @Value("${ca3s.ui.password.activation.keyValidity:7}") int activationKeyValidity) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.protectedContentUtil = protectedContentUtil;
        this.protContentRepository = protContentRepository;
        this.authorityRepository = authorityRepository;
        this.tenantRepository = tenantRepository;
        this.cacheManager = cacheManager;

        this.passwordUtil = new PasswordUtil(passwordCheckRegExp);
        this.certificateUtil = certificateUtil;
        this.smsService = smsService;
        this.entityManager = entityManager;
        this.activationKeyValidity = activationKeyValidity;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);

        List<ProtectedContent> protectedContents = findActivationKeys(key);
        if( protectedContents.isEmpty()){
            log.info("No User found for activation key: {}", key);
        }else{

            for( ProtectedContent protectedContent: protectedContents){
                Optional<User> optUser = userRepository.findById(protectedContent.getRelatedId());
                if(optUser.isPresent()) {
                    User user = optUser.get();
                    user.setActivated(true);
                    this.clearUserCaches(user);
                    log.debug("Activated user: {}", user);
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String username) {
        return userRepository.findOneByLogin(username)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password,
                             final String activationKey) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        if (userDTO.getPhone() != null) {
            newUser.setPhone(userDTO.getPhone().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        newUser.setSecondFactorRequired(false);

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        updateTenant(newUser, userDTO);

        newUser = userRepository.save(newUser);
        this.clearUserCaches(newUser);
        log.debug("Created Information for User: {}", newUser);

        protectedContentUtil.createDerivedProtectedContent(activationKey,
            ProtectedContentType.DERIVED_SECRET,
            ContentRelationType.ACTIVATION_KEY,
            newUser.getId(),
            -1,
            Instant.now().plus(activationKeyValidity, ChronoUnit.DAYS));

        return newUser;
    }

    private List<ProtectedContent> findActivationKeys(final String plainText) {

        return protectedContentUtil.findProtectedContentBySecret(plainText,
            ProtectedContentType.DERIVED_SECRET,
            ContentRelationType.ACTIVATION_KEY);
    }

    private boolean removeNonActivatedUser(User existingUser){
        if (existingUser.getActivated()) {
             return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        user.setSecondFactorRequired(false);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }

        updateTenant(user, userDTO);

        userRepository.save(user);
        this.clearUserCaches(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param phone     phone number of user.
     * @param secondFactorRequired  required to use a second authentication factor.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     * @param tenantId
     */
    public void updateUser(String firstName, String lastName, String email, String phone, boolean secondFactorRequired, String langKey, String imageUrl, Long tenantId) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                if (phone != null) {
                    user.setPhone(phone.toLowerCase());
                }
                user.setSecondFactorRequired(secondFactorRequired);
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                updateTenant(user, tenantId);

                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                if (userDTO.getPhone() != null) {
                    user.setPhone(userDTO.getPhone().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setSecondFactorRequired(userDTO.isSecondFactorRequired());

                user.setLangKey(userDTO.getLangKey());

                updateTenant(user, userDTO);

                user.setBlockedUntilDate(userDTO.getBlockedUntilDate());
                user.setFailedLogins(userDTO.getFailedLogins());

                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);

                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    private void updateTenant(User user, UserDTO userDTO) {
        updateTenant( user, userDTO.getTenantId());
    }
    private void updateTenant( User user, Long tenantId) {
        Tenant tenant = null;
        if( tenantId != null && tenantId != 0L) {
            Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
            if(optionalTenant.isPresent()){
                tenant = optionalTenant.get();
            }
        }
        user.setTenant(tenant);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            this.clearUserCaches(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword( final PasswordChangeDTO passwordChangeDto){
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {

                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }

                switch( passwordChangeDto.getCredentialUpdateType()){
                    case PASSWORD:
                        String encryptedPassword = passwordEncoder.encode(passwordChangeDto.getNewPassword());
                        user.setPassword(encryptedPassword);
                        break;

                    case SMS:
                        if( smsService.verifySMS(user, passwordChangeDto.getOtpTestValue())) {
                            protectedContentUtil.createProtectedContent(user.getPhone(),
                                ProtectedContentType.SECRET,
                                ContentRelationType.SMS_PHONE,
                                user.getId());
                        }else{
                            throw new InvalidCredentialException("SMS does not verify");
                        }
                        break;

                    case TOTP:
                        // verify given TOTP
                        if( TotpService.verifyOTP(passwordChangeDto.getSeed(),
                            passwordChangeDto.getOtpTestValue())) {

                            protectedContentUtil.createProtectedContent(passwordChangeDto.getSeed(),
                                ProtectedContentType.SECRET,
                                ContentRelationType.OTP_SECRET,
                                user.getId());
                        }else{
                            throw new InvalidCredentialException("TOTP does not verify");
                        }
                        break;

                    case CLIENT_CERT:
                        Optional<Certificate> optCert =
                            certificateUtil.findCertificateById(passwordChangeDto.getClientAuthCertId());

                        if( optCert.isPresent()) {
                            certificateUtil.setCertAttribute(optCert.get(),
                                CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT,
                                user.getId().toString(), false);
                            log.debug("new user certificate assigned cert id : {}", passwordChangeDto.getClientAuthCertId());
                        }else{
                            log.warn("found no user certificate with cert id : {}", passwordChangeDto.getClientAuthCertId());
                        }
                        break;

                    default:
                        log.warn("unexpected CredentialUpdateType: {}", passwordChangeDto.getCredentialUpdateType());
                        break;
                }

                updateSecondFactorRequirement(user,false);

                this.clearUserCaches(user);
                log.debug("Changed password for User: {}", user);
            }
        );
    }

    public void updateSecondFactorRequirement(User user, boolean downgradeOnly) {
        boolean oldState = user.isSecondFactorRequired();
        List<ProtectedContent> protectedContentList = protectedContentUtil.getActiveCredentials(user);
        if( protectedContentList.size() >= 2){
            if( !downgradeOnly) {
                user.setSecondFactorRequired(true);
            }
        }else{
            user.setSecondFactorRequired(false);
        }
        userRepository.save(user);
        if( oldState != user.isSecondFactorRequired() ) {
            log.info("Second factor requirement for user: {} changed to {}", user, user.isSecondFactorRequired());
        }
    }

    public void deleteCredential(AccountCredentialsType type, Long id) {

        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {

                switch (type) {
                    case OTP_SECRET:
                        Optional<ProtectedContent> pcOpt = protContentRepository.findById(id);
                        if (pcOpt.isPresent()) {
                            ProtectedContent pc = pcOpt.get();
                            if (pc.getRelatedId().equals(user.getId())) {
                                protContentRepository.delete(pc);
                            } else {
                                log.warn("Current user '{}' not matching user id '{}' for credentials deletion",
                                    user.getId(), pc.getId());
                            }
                        } else {
                            log.warn("Unknown ProtectedContent id '{}' for credentials deletion", id);
                        }
                        break;
                    case CLIENT_CERTIFICATE:
                        Optional<Certificate> certificateOptional = certificateUtil.findCertificateById(id);
                        if (certificateOptional.isPresent()) {
                            if (user.getId().toString().equals(
                                certificateUtil.getCertAttribute(certificateOptional.get(), CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT))) {
                                certificateUtil.setCertAttribute(certificateOptional.get(),
                                    CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT,
                                    null, false);
                            } else {
                                log.warn("Certificate id '{}' not related to current user", id);
                            }
                        } else {
                            log.warn("Unknown certificate id '{}' for credentials deletion", id);
                        }
                        break;
                    default:
                        log.warn("Unexpected credential type: {}", id);
                }
                updateSecondFactorRequirement(user, false);
            }
        );
    }


/*
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                log.debug("Changed password for User: {}", user);
            });
    }
*/

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(final String role) {
        return userRepository.findActiveByRole(role);
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }

    public void checkPassword(String password) {
        passwordUtil.checkPassword(password, "user password");
    }

    public Page<UserDTO> findSelection(Map<String, String[]> parameterMap) {

        Page<UserDTO> userDTOPage = UserSpecifications.handleQueryParamsUser(entityManager,
            entityManager.getCriteriaBuilder(),
            parameterMap );

        for( UserDTO userDTO: userDTOPage.getContent()){
            userRepository.findById(userDTO.getId()).ifPresent(
                user -> {
                    userDTO.setAuthorities(user.getAuthorities().stream().map(
                        authority -> {
                            return authority.getName();
                        }
                    ).collect(Collectors.toSet()));
                    Tenant tenant = user.getTenant();
                    if( tenant != null) {
                        userDTO.setTenantId(tenant.getId());
                        userDTO.setTenantName(tenant.getLongname());
                    }
                }
            );
        }

        return userDTOPage;
    }
}
