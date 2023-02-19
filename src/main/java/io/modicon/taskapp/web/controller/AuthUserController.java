package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.UserManagementService;
import io.modicon.taskapp.web.dto.ApiExceptionDto;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AuthUserController {

    String BASE_URL_V1 = "/api/v1/users";

    @Operation(summary = "register user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully register",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "user already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) })
    })
    @PostMapping("/register")
    UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest request);

    @Operation(summary = "register admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully register",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "user already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) })
    })
    @PostMapping("/register/{secret}")
    UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest request, @PathVariable String secret);

    @Operation(summary = "login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully login",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserLoginResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "404", description = "user not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) })
    })
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
            return userManagementService.registerAdmin(request, secret);
        }

        @Override
        public UserLoginResponse login(UserLoginRequest request) {
            return userManagementService.login(request);
        }
    }

}
