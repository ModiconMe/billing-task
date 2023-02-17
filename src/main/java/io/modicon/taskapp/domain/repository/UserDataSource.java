package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface UserDataSource {
    UserEntity findById(String id);
    boolean existById(String id);
    UserEntity save(UserEntity user);

    @RequiredArgsConstructor
    @Repository
    class JpaUserDataSource implements UserDataSource {
        private final JpaUserRepository repository;

        @Override
        public UserEntity findById(String id) {
            return repository.findById(id)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "user with username [%s] not found", id));
        }

        @Override
        public boolean existById(String id) {
            if (repository.existsById(id))
                throw exception(HttpStatus.BAD_REQUEST, "user with username [%s] is already exist", id);
            return true;
        }

        @Override
        public UserEntity save(UserEntity user) {
            return repository.save(user);
        }
    }
}
