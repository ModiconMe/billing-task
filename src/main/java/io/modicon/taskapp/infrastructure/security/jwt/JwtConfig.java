package io.modicon.taskapp.infrastructure.security.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Getter
@Component
public class JwtConfig {

    @Value("${jwt.sign-key}")
    private String singKey;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Value("${jwt.access-valid-time}")
    private Integer accessValidTime;

    @Value("${jwt.issuer}")
    private String issuer;

    public Key getKey() {
        if (singKey.length() < 32)
            throw new RuntimeException("signKey must have length at least 32");
        return Keys.hmacShaKeyFor(singKey.getBytes(StandardCharsets.UTF_8));
    }

    public Date getTokenExpiredTime() {
        return new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(accessValidTime));
    }

    public Date getIssueAt() {
        return new Date(System.currentTimeMillis());
    }

}
