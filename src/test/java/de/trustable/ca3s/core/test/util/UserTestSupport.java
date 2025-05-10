package de.trustable.ca3s.core.test.util;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserTestSupport {

    private final UserRepository userRepository;

    public UserTestSupport(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createTestUser(final String userName) {

        Optional<User> optionalUser = userRepository.findOneByLogin(userName);

        if( optionalUser.isPresent()){
            return optionalUser.get();
        }

        User user = new User();
        user.setLogin(userName);
        user.setEmail(userName + "@example.com");
        user.setLangKey("en");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setFirstName("");
        user.setLastName(userName);

        userRepository.save(user);

        return user;
    }


    public void setCurrentUser(final User user) {

        Authentication auth = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return user.getLogin();
            }
        };

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
