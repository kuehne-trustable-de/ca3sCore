package de.trustable.ca3s.core.web.rest.util;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.UserNotFoundException;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class CurrentUserUtil {

    private final Logger LOG = LoggerFactory.getLogger(CurrentUserUtil.class);

    private final UserRepository userRepository;

    public CurrentUserUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        LOG.debug("getCurrentUser of a web session");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        if( userName == null) {
            String msg = "Current user == null!";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }

        Optional<User> optCurrentUser = userRepository.findOneByLogin(userName);
        if (!optCurrentUser.isPresent()) {
            String msg ="Name of ra officer '"+userName+ "' not found as user";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }
        return optCurrentUser.get();
    }

    public boolean isAdministrativeUser() {
        return isAdministrativeUser(getCurrentUser());
    }

    public static boolean isAdministrativeUser(final User user){
        for( Authority authority: user.getAuthorities()){
            String authorityName = authority.getName();
            if( authorityName.equals(AuthoritiesConstants.ADMIN) ||
                authorityName.equals(AuthoritiesConstants.RA_OFFICER) ||
                authorityName.equals(AuthoritiesConstants.DOMAIN_RA_OFFICER) ) {
                return true;
            }
        }
        return false;
    }

}
