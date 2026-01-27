package de.trustable.ca3s.core.web.rest;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.PasswordRestrictionMismatchException;
import de.trustable.ca3s.core.exception.UserNotFoundException;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.TotpService;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.dto.*;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.data.OTPDetailsResponse;
import de.trustable.ca3s.core.web.rest.errors.AccountResourceException;
import de.trustable.ca3s.core.web.rest.errors.EmailAlreadyUsedException;
import de.trustable.ca3s.core.web.rest.errors.InvalidPasswordException;
import de.trustable.ca3s.core.web.rest.errors.LoginAlreadyUsedException;
import de.trustable.ca3s.core.web.rest.vm.KeyAndPasswordVM;
import de.trustable.ca3s.core.web.rest.vm.ManagedUserVM;
import org.bouncycastle.util.encoders.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.security.RandomUtil;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.trustable.ca3s.core.domain.ProtectedContent.USER_CONTENT_RELATION_TYPES;
import static de.trustable.ca3s.core.domain.ProtectedContent.USER_TOKEN_RELATION_TYPES;
import static de.trustable.ca3s.core.service.dto.AccountCredentialsType.*;


/**
 * REST controller for managing the current user's account.
 */
@RestController
@Transactional
@RequestMapping("/api")
public class AccountResource {

    @Value("${ca3s.ui.languages:en,de,pl}")
    private String availableLanguages;

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final TenantRepository tenantRepository;
    private final ProtectedContentRepository protectedContentRepository;
    private final CertificateRepository certificateRepository;
    private final UserUtil userUtil;
    private final TotpService totpService;
    private final MailService mailService;
    private final de.trustable.ca3s.core.service.util.RandomUtil randomUtil;


    public AccountResource(UserRepository userRepository, UserService userService, TenantRepository tenantRepository,
                           ProtectedContentRepository protectedContentRepository, UserUtil userUtil,
                           MailService mailService,
                           CertificateRepository certificateRepository,
                           TotpService totpService, de.trustable.ca3s.core.service.util.RandomUtil randomUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tenantRepository = tenantRepository;
        this.protectedContentRepository = protectedContentRepository;
        this.userUtil = userUtil;
        this.mailService = mailService;
        this.certificateRepository = certificateRepository;
        this.totpService = totpService;
        this.randomUtil = randomUtil;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) throws MessagingException {
        checkPasswordLength(managedUserVM.getPassword());

        String activationKey = RandomUtil.generateActivationKey();
        User user = userService.registerUser(managedUserVM,
            managedUserVM.getPassword(),
            activationKey);
        mailService.sendActivationEmail(user, activationKey);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (user.isEmpty()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @Transactional
    @GetMapping("/account")
    public UserDTO getAccount() {

        Optional<User> optUser = userService.getUserWithAuthorities();

        if( optUser.isPresent()){

            // return available languages, only
            Languages languages = new Languages(availableLanguages);
            User user = optUser.get();
            if( user.getLangKey() == null){
                user.setLangKey(languages.getLanguageArr()[0]);
            }else {
                user.setLangKey(languages.alignLanguage(user.getLangKey()));
            }

            return new UserDTO(user, tenantRepository);
        }

        throw new AccountResourceException("User could not be found");
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @Transactional
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {

        String currentUser = findCurrentUser().getLogin();

        if( userUtil.isAdministrativeUser() ){
            // an administrator may update any user
            log.info("REST request to update an arbitrary user '{}' by an administrative user '{}'",
                userDTO.getLogin(), currentUser);
            userService.updateUser(userDTO);

        }else {

            Optional<User> existingUser = userRepository.findOneByLogin(userDTO.getLogin());
            if (existingUser.isEmpty()) {
                throw new AccountResourceException("User does not exist");
            }
            if (!existingUser.get().getLogin().equalsIgnoreCase(currentUser)) {
                throw new AccountResourceException("User mismatch");
            }

            userService.updateUser(userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                userDTO.getPhone(),
                userDTO.isSecondFactorRequired(),
                userDTO.getLangKey(),
                userDTO.getImageUrl(),
                userDTO.getTenantId());
        }
    }

    private User findCurrentUser() {
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> userOpt = userRepository.findOneByLogin(userLogin);
        if (userOpt.isEmpty()) {
            throw new AccountResourceException("User could not be found");
        }
        return userOpt.get();
    }



    /**
     * {@code GET  /account/tokens} : retrieve current user's credentials.
     *
     */
    @GetMapping(path = "/account/tokens")
    public List<AccountCredentialView> tokenList() {

        User currentUser = findCurrentUser();
        List<ProtectedContent> protectedContentList =
            protectedContentRepository.findByTypeRelationId(
                Arrays.asList(USER_TOKEN_RELATION_TYPES), currentUser.getId());

        List<AccountCredentialView> tokenViewList = new ArrayList<>();
        for(ProtectedContent protectedContent: protectedContentList){
            AccountCredentialView tokenView = new AccountCredentialView();
            tokenView.setId(protectedContent.getId());
            tokenView.setLeftUsages(protectedContent.getLeftUsages());
            tokenView.setCreatedOn(protectedContent.getCreatedOn());
            tokenView.setValidTo(protectedContent.getValidTo());
            tokenView.setStatus(protectedContent.getStatus());
            switch( protectedContent.getRelationType()){
                case API_TOKEN:
                    tokenView.setRelationType(AccountCredentialsType.API_TOKEN);
                    break;
                case SCEP_TOKEN:
                    tokenView.setRelationType(SCEP_TOKEN);
                    tokenView.setPipelineName(protectedContent.getPipeline().getName());
                    break;
                case EST_TOKEN:
                    tokenView.setRelationType(EST_TOKEN);
                    tokenView.setPipelineName(protectedContent.getPipeline().getName());
                    break;
                case EAB_PASSWORD:
                    tokenView.setRelationType(EAB_PASSWORD);
                    tokenView.setPipelineName(protectedContent.getPipeline().getName());
                    break;
                default:
                    log.warn("Unexpected relation type '{}' occurred.", protectedContent.getRelationType());
                    break;
            }
            tokenViewList.add(tokenView);
        }
        return tokenViewList;
    }

    /**
     * {@code GET  /account/credentials} : retrieve current user's credentials.
     *
     */
    @GetMapping(path = "/account/credentials")
    public List<AccountCredentialView> accountCredentialList() {

        User currentUser = findCurrentUser();
        List<ProtectedContent> protectedContentList =
        protectedContentRepository.findByTypeRelationId(
            Arrays.asList(USER_CONTENT_RELATION_TYPES), currentUser.getId());

        List<AccountCredentialView> accountCredentialViewList = new ArrayList<>();
        for(ProtectedContent protectedContent: protectedContentList){
            AccountCredentialView accountCredentialView = new AccountCredentialView();
            accountCredentialView.setId(protectedContent.getId());
            accountCredentialView.setLeftUsages(protectedContent.getLeftUsages());
            accountCredentialView.setCreatedOn(protectedContent.getCreatedOn());
            accountCredentialView.setValidTo(protectedContent.getValidTo());
            switch( protectedContent.getRelationType()){
                case ACCOUNT_TOKEN:
                    accountCredentialView.setRelationType(AccountCredentialsType.ACCOUNT_TOKEN);
                    break;
                case OTP_SECRET:
                    accountCredentialView.setRelationType(AccountCredentialsType.OTP_SECRET);
                    break;
                case SMS_PHONE:
                    accountCredentialView.setRelationType(AccountCredentialsType.SMS_ENABLED);
                    break;
                default:
                    log.warn("Unexpected relation type '{}' occurred.", protectedContent.getRelationType());
                    break;
            }
            accountCredentialViewList.add(accountCredentialView);
        }

        List<Certificate> certificateList =
            certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT,
                currentUser.getId().toString());

        for(Certificate certificate: certificateList) {
            AccountCredentialView accountCredentialView = new AccountCredentialView();
            accountCredentialView.setId(certificate.getId());
            accountCredentialView.setLeftUsages(Integer.MAX_VALUE);
            accountCredentialView.setCreatedOn(certificate.getValidFrom());
            accountCredentialView.setValidTo(certificate.getValidTo());
            accountCredentialView.setRelationType(AccountCredentialsType.CLIENT_CERTIFICATE);
            accountCredentialViewList.add(accountCredentialView);
        }

        return accountCredentialViewList;
    }

    /**
     * {@code POST  /account/credential} : delete user's credentials.
     *
     */
    @DeleteMapping(path = "/account/credentials/{type}/{id}")
    public void deleteAccountCredential(@PathVariable AccountCredentialsType type, @PathVariable Long id) {
        userService.deleteCredential(type, id);
    }


    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {

        if( passwordChangeDto.getCredentialUpdateType() == null) {
            passwordChangeDto.setCredentialUpdateType(CredentialUpdateType.PASSWORD);
        }

        if( CredentialUpdateType.PASSWORD.equals( passwordChangeDto.getCredentialUpdateType())) {
            checkPasswordLength(passwordChangeDto.getNewPassword());
        }

        userService.changePassword(passwordChangeDto);
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param username the login name of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String username) throws  MessagingException {

        String resetKey = RandomUtil.generateActivationKey();

        mailService.sendPasswordResetMail(
           userService.requestPasswordReset(username, resetKey)
               .orElseThrow(UserNotFoundException::new),
            resetKey );
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        checkPasswordLength(keyAndPassword.getNewPassword());

        Optional<User> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (user.isEmpty()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private void checkPasswordLength(String password) {
        try {
            userService.checkPassword(password);
        }catch(PasswordRestrictionMismatchException passwordRestrictionMismatchException){
            throw new InvalidPasswordException();
        }
    }

    /**
     * {@code POST  /account/initOTP} : changes the current user's password.
     *
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/initOTP")
    public OTPDetailsResponse initOTP() {

        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));

        byte[] seed = new byte[20];
        randomUtil.getSecureRandom().nextBytes(seed);

        OTPDetailsResponse otpDetailsResponse = new OTPDetailsResponse();

        otpDetailsResponse.setSeed(Base32.toBase32String(seed));
        String totpUrl = totpService.generateTotpUrlForUser(userLogin, otpDetailsResponse.getSeed());
        otpDetailsResponse.setTotpUrl(totpUrl);
        otpDetailsResponse.setQrCodeImg(generateQRasPNG(totpUrl));

        return otpDetailsResponse;
    }


    public byte[] generateQRasPNG(String code) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, 256, 256);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error while creating QR code temp file: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate QR Code!");
        } catch (WriterException e) {
            log.error("Error while generating QR code: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate QR Code!");
        }
    }

}
