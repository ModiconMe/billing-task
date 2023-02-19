package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

public interface SecurityContextHolderService {

    UserEntity getCurrentUser();

    @Service
    class Base implements SecurityContextHolderService {

        @Override
        public UserEntity getCurrentUser() {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
            Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
            return principal;
        }
    }
}
