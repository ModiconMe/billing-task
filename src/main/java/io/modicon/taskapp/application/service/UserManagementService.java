package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.UserDtoMapper;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.UserDataSource;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.infrastructure.security.jwt.JwtConfig;
import io.modicon.taskapp.infrastructure.security.jwt.JwtGeneration;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface UserManagementService {

    UserRegisterResponse register(UserRegisterRequest request);

    UserRegisterResponse registerAdmin(UserRegisterRequest request, String secret);

    UserLoginResponse login(UserLoginRequest request);

    @Slf4j
    @RequiredArgsConstructor
    @Service
    class Base implements UserManagementService {

        private final UserDataSource userDataSource;
        private final PasswordEncoder passwordEncoder;
        private final UserDtoMapper userDtoMapper;
        private final JwtGeneration jwtGeneration;

        private final JwtConfig jwtConfig;

        @Override
        public UserRegisterResponse register(UserRegisterRequest request) {
            String username = request.getUsername();
            userDataSource.validateNotExist(request.getUsername());

            UserEntity user = UserEntity.builder()
                    .username(username)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(ApplicationUserRole.USER)
                    .build();
             userDataSource.save(user);

            return new UserRegisterResponse();
        }

        @Override
        public UserRegisterResponse registerAdmin(UserRegisterRequest request, String secret) {
            if (secret.equals(jwtConfig.getSingKey())) {
                String username = request.getUsername();
                userDataSource.validateNotExist(request.getUsername());

                UserEntity user = UserEntity.builder()
                        .username(username)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(ApplicationUserRole.ADMIN)
                        .build();
                userDataSource.save(user);
                return new UserRegisterResponse();
            }
            throw ApiException.exception(HttpStatus.UNAUTHORIZED, "you are not allowed to do this operation");
        }

        @Override
        public UserLoginResponse login(UserLoginRequest request) {
            String username = request.getUsername();
            UserEntity user = userDataSource.findById(username);

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
                throw exception(HttpStatus.FORBIDDEN, "wrong password");

            String token = jwtGeneration.generateAccessToken(user);

            return new UserLoginResponse(userDtoMapper.apply(user), token);
        }
    }
}
