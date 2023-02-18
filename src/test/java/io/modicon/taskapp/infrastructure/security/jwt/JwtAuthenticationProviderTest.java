package io.modicon.taskapp.infrastructure.security.jwt;

import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        jwtAuthenticationProvider = new JwtAuthenticationProvider.Base(userDetailsService);
    }

    @Test
    void shouldAuthenticate() {
        UserEntity user = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
                .build();

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

        Authentication actual = jwtAuthenticationProvider.getAuthentication(user.getUsername());

        assertEquals(user.getPassword(), actual.getCredentials());
        assertEquals(user, actual.getPrincipal());
        assertFalse(actual.getAuthorities().isEmpty());
    }

    @Test
    void shouldNotAuthenticate_whenUserIsNotExist() {
        String username = "username";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        Authentication token = jwtAuthenticationProvider.getAuthentication(username);

        assertNull(token);
    }
}