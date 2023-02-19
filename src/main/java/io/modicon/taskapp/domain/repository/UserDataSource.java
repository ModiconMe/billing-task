package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface UserDataSource {
    UserEntity findById(String id);
    void validateNotExist(String id);
    UserEntity save(UserEntity user);

    @Transactional
    @RequiredArgsConstructor
    @Repository
    class JpaUserDataSource implements UserDataSource {
        private final JpaUserRepository repository;

        @Transactional(readOnly = true)
        @Override
        public UserEntity findById(String id) {
            return repository.findById(id)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "user with username [%s] not found", id));
        }

        @Transactional(readOnly = true)
        @Override
        public void validateNotExist(String id) {
            if (repository.existsById(id))
                throw exception(HttpStatus.BAD_REQUEST, "user with username [%s] is already exist", id);
        }

        @Override
        public UserEntity save(UserEntity user) {
            return repository.save(user);
        }

    }
}
