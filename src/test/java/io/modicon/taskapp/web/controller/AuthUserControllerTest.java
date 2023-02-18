package io.modicon.taskapp.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.application.service.UserManagementService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.security.jwt.JwtAuthFilter;
import io.modicon.taskapp.web.dto.UserDto;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final static String BASE_URL = "/api/v1/users";

    private UserEntity user;

    {
        user = UserEntity.builder()
                .username("username")
                .password("password")
                .build();

    }


    @Test
    void shouldRegisterUser() throws Exception {
        // given
        UserRegisterResponse expected = new UserRegisterResponse();
        UserRegisterRequest request = new UserRegisterRequest(user.getUsername(), user.getPassword());
        when(userManagementService.register(request))
                .thenReturn(expected);

        // when
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(userManagementService, times(1)).register(request);
    }

    @Test
    void shouldLoginUser() throws Exception {
        // given
        String token = "token";
        UserLoginResponse expected = new UserLoginResponse(new UserDto(user.getUsername()), token);
        UserLoginRequest request = new UserLoginRequest(user.getUsername(), user.getPassword());
        when(userManagementService.login(request))
                .thenReturn(expected);

        // when
        var json = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        verify(userManagementService, times(1)).login(request);
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }
}