package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.PasswordRestrictionMismatch;
import de.trustable.ca3s.core.exception.UserNotFoundException;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.dto.Languages;
import de.trustable.ca3s.core.service.dto.PasswordChangeDTO;
import de.trustable.ca3s.core.service.dto.UserDTO;
import de.trustable.ca3s.core.web.rest.errors.EmailAlreadyUsedException;
import de.trustable.ca3s.core.web.rest.errors.InvalidPasswordException;
import de.trustable.ca3s.core.web.rest.errors.LoginAlreadyUsedException;
import de.trustable.ca3s.core.web.rest.vm.KeyAndPasswordVM;
import de.trustable.ca3s.core.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.security.RandomUtil;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    @Value("${ca3s.ui.languages:en,de,pl}")
    private String availableLanguages;

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final TenantRepository tenantRepository;

    private final MailService mailService;

    public AccountResource(UserRepository userRepository, UserService userService, TenantRepository tenantRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tenantRepository = tenantRepository;
        this.mailService = mailService;
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
        if (!user.isPresent()) {
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

            UserDTO userDTO = new UserDTO(user, tenantRepository);

            return userDTO;
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
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(userDTO.getFirstName(),
            userDTO.getLastName(),
            userDTO.getEmail(),
            userDTO.getLangKey(),
            userDTO.getImageUrl(),
            userDTO.getTenantId());
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        checkPasswordLength(passwordChangeDto.getNewPassword());

        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String username) throws  MessagingException {
       mailService.sendPasswordResetMail(
           userService.requestPasswordReset(username)
               .orElseThrow(UserNotFoundException::new)
       );
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

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private void  checkPasswordLength(String password) {
        try {
            userService.checkPassword(password);
        }catch(PasswordRestrictionMismatch passwordRestrictionMismatch){
            throw new InvalidPasswordException();
        }

    }
}
