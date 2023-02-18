package io.modicon.taskapp.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.web.interaction.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String BASE_URL = "/api/v1/tasks";
    private final static String TOKEN_PREFIX = "Bearer ";
    private String token;

    {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void should_returnCorrectData_whenCreateTask() throws Exception {
        token = registerAndLogin();
        System.out.println("token" + token);

        TaskCreateRequest taskCreateRequest = new TaskCreateRequest(
                null, UUID.randomUUID().toString(), PriorityType.COMMON.name(), UUID.randomUUID().toString(), LocalDate.now(), UUID.randomUUID().toString());

        TaskCreateResponse response = sendRequestAndReturn(post(BASE_URL), taskCreateRequest, TaskCreateResponse.class, token);

        assertEquals(response.getTask().id(), taskCreateRequest.getId());
    }

    private String registerAndLogin() throws Exception {
        UserRegisterRequest registerRequest = registerCommand();
        sendRequest(post("/api/v1/users" + "/register"), registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest(registerRequest.getUsername(), registerRequest.getPassword());
        UserLoginResponse response = sendRequestAndReturn(post("/api/v1/users" + "/login"), loginRequest, UserLoginResponse.class);
        return response.getToken();
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

    private <E> E sendRequestAndReturn(MockHttpServletRequestBuilder requestBuilder, Object content, Class<E> clazz, String token) throws Exception {
        String contentAsString = mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                        .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString, clazz);
    }
}
