package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.UserDtoMapper;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.UserRepository;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.jwt.JwtGeneration;
import io.modicon.taskapp.web.dto.UserLoginRequest;
import io.modicon.taskapp.web.dto.UserLoginResponse;
import io.modicon.taskapp.web.dto.UserRegisterRequest;
import io.modicon.taskapp.web.dto.UserRegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface UserManagementService {

    UserRegisterResponse register(UserRegisterRequest request);

    UserLoginResponse login(UserLoginRequest request);

    @Slf4j
    @RequiredArgsConstructor
    @Service
    class Base implements UserManagementService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final UserDtoMapper userDtoMapper;
        private final JwtGeneration jwtGeneration;

        @Override
        public UserRegisterResponse register(UserRegisterRequest request) {
            String username = request.getUsername();
            if (userRepository.existsById(username))
                throw exception(HttpStatus.BAD_REQUEST, "user with username [%s] is already exist", username);

            UserEntity user = UserEntity.builder()
                    .username(username)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
             userRepository.save(user);

            return new UserRegisterResponse();
        }

        @Override
        public UserLoginResponse login(UserLoginRequest request) {
            String username = request.getUsername();
            UserEntity user = userRepository.findById(username)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "user with username [%s] not found", username));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
                throw exception(HttpStatus.FORBIDDEN, "wrong password");

            String token = jwtGeneration.generateAccessToken(user);

            return new UserLoginResponse(userDtoMapper.apply(user), token);
        }
    }
}
