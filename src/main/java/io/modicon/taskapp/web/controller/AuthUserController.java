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
    ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request);

    @PostMapping("/register/{secret}")
    ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request, @PathVariable String secret);

    @PostMapping("/login")
    ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class BaseAuthController implements AuthUserController {

        private final UserManagementService userManagementService;

        @Override
        public ResponseEntity<UserRegisterResponse> register(UserRegisterRequest request) {
            userManagementService.register(request);
            return ResponseEntity.ok().build();
        }

        @Override
        public ResponseEntity<UserRegisterResponse> register(UserRegisterRequest request, String secret) {
            userManagementService.registerAdmin(request, secret);
            return ResponseEntity.ok().build();
        }

        @Override
        public ResponseEntity<UserLoginResponse> login(UserLoginRequest request) {
            UserLoginResponse response = userManagementService.login(request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, response.getToken())
                    .body(response);
        }
    }

}
