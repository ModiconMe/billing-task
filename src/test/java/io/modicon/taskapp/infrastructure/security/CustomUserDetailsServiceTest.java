package io.modicon.taskapp.infrastructure.security;

import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.UserDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private UserDetailsService userDetailsService;

    @Mock
    private UserDataSource userDataSource;


    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userDataSource);
    }

    @Test
    void shouldReturnUserDetails() {
        String telephone = "telephone";

        UserEntity user = UserEntity.builder()
                .username("username")
                .password("password")
                .build();

        when(userDataSource.findById(telephone)).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername(telephone);

        assertEquals(user, result);
    }
}
