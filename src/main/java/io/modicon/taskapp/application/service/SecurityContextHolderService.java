package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

public interface SecurityContextHolderService {

    UserEntity getCurrentUser();

    @Service
    class Base implements SecurityContextHolderService {

        @Override
        public UserEntity getCurrentUser() {
            return (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
    }
}
