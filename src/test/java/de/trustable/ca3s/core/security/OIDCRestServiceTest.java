package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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


    @Test
    public void assertThatNoUserCanBeFoundByLogin() throws Exception {

        UserPreferenceRepository userPreferenceRepository = mock(UserPreferenceRepository.class);
        UserRepository userRepository  = mock(UserRepository.class);
        PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();

        OIDCRestService = new OIDCRestService(
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/token",
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/userinfo",
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/logout",
            "ca3s",
            "password",
            "197bc3b4-64b0-452f-9bdb-fcaea0988e90",
            "roles", userPreferenceRepository, userRepository, authorityRepository, passwordEncoder);

        // subject not found
        when(userPreferenceRepository.findByNameContent(eq(USER_PREFERENCE_KEYCLOAK_ID), anyString())).thenReturn(new ArrayList<>());

        // no user found
        when(userRepository.findById(anyLong())).thenReturn( Optional.empty());

        KeycloakUserId keycloakUserId = OIDCRestService.login("kcuser", "s3cr3t");
        assertNotNull(keycloakUserId);

        List<String> roleList = OIDCRestService.getRoles(keycloakUserId.getAccess_token());
        assertNotNull(roleList);
        assertFalse(roleList.isEmpty());

        verify(userRepository, Mockito.times(1)).save(any());
        verify(userPreferenceRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void assertThatExistingUserCanBeFoundByLogin() throws Exception {

        UserPreferenceRepository userPreferenceRepository = mock(UserPreferenceRepository.class);
        UserRepository userRepository  = mock(UserRepository.class);
        PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();

        OIDCRestService = new OIDCRestService(
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/token",
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/userinfo",
            "http://localhost:50080/auth/realms/ca3sRealm/protocol/openid-connect/logout",
            "ca3s",
            "password",
            "197bc3b4-64b0-452f-9bdb-fcaea0988e90",
            "roles", userPreferenceRepository, userRepository, authorityRepository, passwordEncoder);

        User user = new User();
        user.setId(1L);
        user.setLogin("Login");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setEmail("test@foo.de");

        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(1L);

        // subject not found
        when(userPreferenceRepository.findByNameContent(eq(USER_PREFERENCE_KEYCLOAK_ID), anyString())).
            thenReturn(Collections.singletonList(userPreference));

        // no user found
        when(userRepository.findById(anyLong())).thenReturn( Optional.of(user));

        KeycloakUserId keycloakUserId = OIDCRestService.login("kcuser", "s3cr3t");
        assertNotNull(keycloakUserId);

        List<String> roleList = OIDCRestService.getRoles(keycloakUserId.getAccess_token());
        assertNotNull(roleList);
        assertFalse(roleList.isEmpty());

        verify(userRepository, Mockito.times(1)).save(any());
        verify(userPreferenceRepository, Mockito.never()).save(any());
    }
}
