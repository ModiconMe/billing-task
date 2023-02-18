package io.modicon.taskapp.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidationTest {

    private JwtValidation jwtValidation;

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtValidation = new JwtValidation.Base(userDetailsService, jwtConfig);
    }

    private UserEntity user;

    {
        user = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
                .build();
    }


    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        String issuer = "issuer";
        Date issueAt = new Date();
        Date accessExpired = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        String signKey = "secretsecretsecretsecretsecretsecret";

        Map<String, Object> claims = new HashMap<>();
        SecretKey key = Keys.hmacShaKeyFor(signKey.getBytes());
        String token = Jwts.builder().setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .claim("authorities", user.getAuthorities())
                .setIssuedAt(issueAt)
                .setExpiration(accessExpired)
                .signWith(key).compact();

        when(jwtConfig.getKey()).thenReturn(key);
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

        boolean result = jwtValidation.isTokenValid(token);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenTokenIsExpired() {
        String issuer = "issuer";
        Date issueAt = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        Date accessExpired = new Date(System.currentTimeMillis());
        String signKey = "secretsecretsecretsecretsecretsecret";

        Map<String, Object> claims = new HashMap<>();
        SecretKey key = Keys.hmacShaKeyFor(signKey.getBytes());
        String token = Jwts.builder().setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .claim("authorities", user.getAuthorities())
                .setIssuedAt(issueAt)
                .setExpiration(accessExpired)
                .signWith(key).compact();

        when(jwtConfig.getKey()).thenReturn(key);

        boolean result = jwtValidation.isTokenValid(token);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenUserIsNotExist() {
        String issuer = "issuer";
        Date issueAt = new Date(System.currentTimeMillis());
        Date accessExpired = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        String signKey = "secretsecretsecretsecretsecretsecret";

        Map<String, Object> claims = new HashMap<>();
        SecretKey key = Keys.hmacShaKeyFor(signKey.getBytes());
        String token = Jwts.builder().setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .claim("authorities", user.getAuthorities())
                .setIssuedAt(issueAt)
                .setExpiration(accessExpired)
                .signWith(key).compact();

        when(jwtConfig.getKey()).thenReturn(key);
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(null);

        boolean result = jwtValidation.isTokenValid(token);

        assertFalse(result);
    }
}