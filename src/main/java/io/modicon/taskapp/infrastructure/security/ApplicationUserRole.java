package io.modicon.taskapp.infrastructure.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

public enum ApplicationUserRole {
    ADMIN, USER;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
