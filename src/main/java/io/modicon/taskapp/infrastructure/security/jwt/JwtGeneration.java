package io.modicon.taskapp.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

public interface JwtGeneration {

    String generateAccessToken(UserDetails userDetails);

    @RequiredArgsConstructor
    @Component
    class Base implements JwtGeneration {
        private final JwtConfig jwtConfig;

        @Override
        public String generateAccessToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();
            return Jwts.builder().setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuer(jwtConfig.getIssuer())
                    .claim("authorities", userDetails.getAuthorities())
                    .setIssuedAt(jwtConfig.getIssueAt())
                    .setExpiration(jwtConfig.getTokenExpiredTime())
                    .signWith(jwtConfig.getKey()).compact();
        }
    }
}

