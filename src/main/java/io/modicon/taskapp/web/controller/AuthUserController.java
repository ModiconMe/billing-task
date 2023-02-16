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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public interface AuthUserController {

    String BASE_URL_V1 = "/api/v1/users";

    @PostMapping("/register")
    UserRegisterResponse register(UserRegisterRequest request);

    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/login")
    UserLoginResponse login(UserLoginRequest request);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements AuthUserController {

        private final UserManagementService userManagementService;

        @Override
        public UserRegisterResponse register(UserRegisterRequest request) {
            return null;
        }

        @Override
        public UserLoginResponse login(UserLoginRequest request) {
            return null;
        }
    }

}
