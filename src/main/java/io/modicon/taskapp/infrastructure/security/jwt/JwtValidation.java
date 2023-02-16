package io.modicon.taskapp.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

public interface JwtValidation {
    boolean isTokenValid(String token);

    String extractUsername(String token);

    @RequiredArgsConstructor
    @Component
    class Base implements JwtValidation {

        private final UserDetailsService userDetailsService;
        private final JwtConfig jwtConfig;

        @Override
        public boolean isTokenValid(String token) {
            try {
                Optional<UserDetails> userDetails = Optional.ofNullable(
                        userDetailsService.loadUserByUsername(extractUsername(token)));
                return (userDetails.isPresent());
            } catch (JwtException e) {
                return false;
            }
        }

        @Override
        public String extractUsername(String token) {
            Claims claims = Jwts.parserBuilder().setSigningKey(jwtConfig.getKey()).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        }
    }
}
