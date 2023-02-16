package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.UserManagementService;
import io.modicon.taskapp.web.dto.UserLoginRequest;
import io.modicon.taskapp.web.dto.UserLoginResponse;
import io.modicon.taskapp.web.dto.UserRegisterRequest;
import io.modicon.taskapp.web.dto.UserRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public interface AuthUserController {

    String BASE_URL_V1 = "/api/v1/users";

    @PostMapping("/register")
    ResponseEntity<UserRegisterResponse> register(@RequestBody UserRegisterRequest request);

    @PostMapping("/login")
    ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements AuthUserController {

        private final UserManagementService userManagementService;

        @Override
        public ResponseEntity<UserRegisterResponse> register(UserRegisterRequest request) {
            userManagementService.register(request);
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
