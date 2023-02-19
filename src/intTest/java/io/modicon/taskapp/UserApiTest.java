package io.modicon.taskapp;

import feign.FeignException;
import io.modicon.taskapp.config.FeignBasedRestTest;
import io.modicon.taskapp.client.UserClient;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

public class UserApiTest extends FeignBasedRestTest {

    @Autowired
    private UserClient userClient;

    private final static String WRONG = "wrong";

    @Test
    void REGISTER_USER_should_returnCorrectData() {
        UserRegisterRequest request = registerCommand();

        UserRegisterResponse response = userClient.register(request);

        assertThat(response.getUser().username()).isEqualTo(request.getUsername());
    }

    @Test
    void REGISTER_USER_should_return400_whenProvideEmptyUsername() {
        UserRegisterRequest request = new UserRegisterRequest(UUID.randomUUID().toString(), "");

        FeignException exception = catchThrowableOfType(() -> userClient.register(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void REGISTER_USER_should_return400_whenProvideEmptyPassword() {
        UserRegisterRequest request = new UserRegisterRequest("", UUID.randomUUID().toString());

        FeignException exception = catchThrowableOfType(() -> userClient.register(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void REGISTER_USER_should_return400_whenProvideNull() {
        UserRegisterRequest request = new UserRegisterRequest(null, null);

        FeignException exception = catchThrowableOfType(() -> userClient.register(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void LOGIN_USER_should_returnCorrectData() {
        UserRegisterRequest request = registerCommand();
        userClient.register(request);

        UserLoginResponse response = userClient.login(new UserLoginRequest(request.getUsername(), request.getPassword()));

        assertThat(response.getUser().username()).isEqualTo(request.getUsername());
        assertThat(response.getToken()).isNotEmpty();
    }

    @Test
    void LOGIN_USER_should_return404_whenWrongUsernameProvided() {
        UserRegisterRequest request = registerCommand();
        userClient.register(request);

        FeignException exception = catchThrowableOfType(() -> userClient.login(new UserLoginRequest(WRONG, request.getPassword())), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void LOGIN_USER_should_return403_whenWrongPasswordProvided() {
        UserRegisterRequest request = registerCommand();
        userClient.register(request);

        FeignException exception = catchThrowableOfType(() -> userClient.login(new UserLoginRequest(request.getUsername(), WRONG)), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    private static UserRegisterRequest registerCommand() {
        return new UserRegisterRequest(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }
}
