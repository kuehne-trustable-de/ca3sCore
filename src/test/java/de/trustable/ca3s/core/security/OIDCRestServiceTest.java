package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.trustable.ca3s.core.domain.UserPreference.USER_PREFERENCE_KEYCLOAK_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("Disabled until a reliable OIDC server can be used")
public class OIDCRestServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCRestServiceTest.class);

    OIDCRestService OIDCRestService;

    static AuthorityRepository authorityRepository = mock(AuthorityRepository.class);

    String issuerUri;

    @BeforeEach
    public void init() {

        issuerUri = "http://localhost:50080/auth/realms/ca3sRealm";

        List<Authority> authorityList = new ArrayList<>();
        Authority authUser = new Authority();
        authUser.setName("ROLE_USER");
        Authority authRa = new Authority();
        authRa.setName("ROLE_RA");
        Authority authAdmin = new Authority();
        authAdmin.setName("ROLE_ADMIN");
        authorityList.add(authUser);
        authorityList.add(authRa);
        authorityList.add(authAdmin);
        when(authorityRepository.findAll()).thenReturn(authorityList);
    }

}
