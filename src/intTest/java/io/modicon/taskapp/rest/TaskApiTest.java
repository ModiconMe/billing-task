package io.modicon.taskapp.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.rest.client.TaskClient;
import io.modicon.taskapp.rest.client.UserClient;
import io.modicon.taskapp.rest.config.FeignBasedRestTest;
import io.modicon.taskapp.rest.utils.AuthUtils;
import io.modicon.taskapp.web.interaction.TaskCreateRequest;
import io.modicon.taskapp.web.interaction.TaskCreateResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

public class TaskApiTest extends FeignBasedRestTest {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private TaskClient taskClient;

    private static final String WRONG = "wrong";

    @Test
    void CREATE_TASK_should_returnCorrectData() {
        auth.register().login();

        TaskCreateRequest request = createTask();
        TaskCreateResponse response = taskClient.create(request);

        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(request.getDescription());
        assertThat(response.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(response.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(response.getTask().tag()).isEqualTo(request.getTag());
    }

    @Test
    void CREATE_TASK_should_return401_whenNotAuthorized() {
        auth.logout();

        TaskCreateRequest request = createTask();
        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void CREATE_TASK_should_return400_whenWrongDateProvided() {
        auth.register().login();

        TaskCreateRequest request = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now().minusDays(1), UUID.randomUUID().toString());

        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        AssertionsForClassTypes.assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void CREATE_TASK_should_return400_whenWrongPriorityType() {
        auth.register().login();

        TaskCreateRequest request = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name() + WRONG, UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());

        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        AssertionsForClassTypes.assertThat(exception.contentUTF8()).isNotEmpty();
    }

    private TaskCreateRequest createTask() {
        return new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString()
        );
    }

//
//    private String registerAndLogin() throws Exception {
//        UserRegisterRequest registerRequest = registerCommand();
//        sendRequest(post("/api/v1/users" + "/register"), registerRequest);
//
//        UserLoginRequest loginRequest = new UserLoginRequest(registerRequest.getUsername(), registerRequest.getPassword());
//        UserLoginResponse response = sendRequestAndReturn(post("/api/v1/users" + "/login"), loginRequest, UserLoginResponse.class);
//        return response.getToken();
//    }
//
//    private static UserRegisterRequest registerCommand() {
//        return new UserRegisterRequest(UUID.randomUUID().toString(),
//                UUID.randomUUID().toString());
//    }
//
//    private void sendRequest(MockHttpServletRequestBuilder requestBuilder, Object content) throws Exception {
//        mockMvc.perform(requestBuilder
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(content)))
//                .andExpect(status().isOk());
//    }
//
//    private void sendRequestAndExpect(MockHttpServletRequestBuilder requestBuilder, Object content, ResultMatcher status) throws Exception {
//        mockMvc.perform(requestBuilder
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(content)))
//                .andExpect(status);
//    }
//
//    private <E> E sendRequestAndReturn(MockHttpServletRequestBuilder requestBuilder, Object content, Class<E> clazz) throws Exception {
//        String contentAsString = mockMvc.perform(requestBuilder
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(content)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        return objectMapper.readValue(contentAsString, clazz);
//    }
//
//    private <E> E sendRequestAndReturn(MockHttpServletRequestBuilder requestBuilder, Object content, Class<E> clazz, String token) throws Exception {
//        String contentAsString = mockMvc.perform(requestBuilder
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(content))
//                        .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        return objectMapper.readValue(contentAsString, clazz);
//    }
}
