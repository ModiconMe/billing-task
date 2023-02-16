package io.modicon.taskapp.infrastructure.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

public interface JwtAuthenticationProvider {
    Authentication getAuthentication(String username);

    @Component
    @RequiredArgsConstructor
    class Base implements JwtAuthenticationProvider {

        private final UserDetailsService userDetailsService;

        @Override
        public Authentication getAuthentication(String username) {
            return Optional.ofNullable(username)
                    .map(userDetailsService::loadUserByUsername)
                    .map(userDetails ->
                            new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities()))
                    .orElse(null);
        }
    }
}

