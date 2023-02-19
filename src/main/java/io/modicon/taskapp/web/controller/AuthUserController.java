package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.UserManagementService;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AuthUserController {

    String BASE_URL_V1 = "/api/v1/users";

    @PostMapping("/register")
    UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest request);

    @PostMapping("/register/{secret}")
    UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest request, @PathVariable String secret);

    @PostMapping("/login")
    UserLoginResponse login(@Valid @RequestBody UserLoginRequest request);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class BaseAuthController implements AuthUserController {

        private final UserManagementService userManagementService;

        @Override
        public UserRegisterResponse register(UserRegisterRequest request) {
            return userManagementService.register(request);
        }

        @Override
        public UserRegisterResponse register(UserRegisterRequest request, String secret) {
            return userManagementService.register(request);
        }

        @Override
        public UserLoginResponse login(UserLoginRequest request) {
            return userManagementService.login(request);
        }
    }

}
