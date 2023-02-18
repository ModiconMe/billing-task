package io.modicon.taskapp.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String BASE_URL = "/api/v1/users";

    @Test
    void should_returnCorrectData_whenRegisterUser() throws Exception {
        UserRegisterRequest request = registerCommand();
        sendRequest(post(BASE_URL + "/register"), request);
    }

    @Test
    void should_returnCorrectData_whenLoginUser() throws Exception {
        UserRegisterRequest registerRequest = registerCommand();
        sendRequest(post(BASE_URL + "/register"), registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest(registerRequest.getUsername(), registerRequest.getPassword());
        UserLoginResponse response = sendRequestAndReturn(post(BASE_URL + "/login"), loginRequest, UserLoginResponse.class);

        assertEquals(response.getUser().username(), loginRequest.getUsername());
        assertFalse(response.getToken().isEmpty());
    }

    @Test
    void should_return401_whenLoginUser() throws Exception {
        UserRegisterRequest registerRequest = registerCommand();
        sendRequest(post(BASE_URL + "/register"), registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest(
                registerRequest.getUsername(),
                registerRequest.getPassword() + "wrong");

        sendRequestAndExpect(post(BASE_URL + "/login"), loginRequest, status().isUnauthorized());
    }

    @Test
    void should_return404_whenLoginUser() throws Exception {
        UserRegisterRequest registerRequest = registerCommand();
        sendRequest(post(BASE_URL + "/register"), registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest(
                registerRequest.getUsername() + "wrong",
                registerRequest.getPassword());

        sendRequestAndExpect(post(BASE_URL + "/login"), loginRequest, status().isNotFound());
    }

    private static UserRegisterRequest registerCommand() {
        return new UserRegisterRequest(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    private void sendRequest(MockHttpServletRequestBuilder requestBuilder, Object content) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content)))
                .andExpect(status().isOk());
    }

    private void sendRequestAndExpect(MockHttpServletRequestBuilder requestBuilder, Object content, ResultMatcher status) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content)))
                .andExpect(status);
    }

    private <E> E sendRequestAndReturn(MockHttpServletRequestBuilder requestBuilder, Object content, Class<E> clazz) throws Exception {
        String contentAsString = mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString, clazz);
    }
}
