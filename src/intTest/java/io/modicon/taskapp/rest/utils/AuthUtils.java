package io.modicon.taskapp.rest.utils;

import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.rest.client.UserClient;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthUtils {

    @Autowired(required = false)
    private UserClient userClient;

    @Value("${jwt.sign-key}")
    private String secret;

    public RegisteredUser register() {
        String uuid = UUID.randomUUID().toString();
        return register(uuid, uuid);
    }

    public RegisteredUser registerAdmin() {
        String uuid = UUID.randomUUID().toString();
        return registerAdmin(uuid, uuid);
    }

    public RegisteredUser register(String username, String password) {
        userClient.register(new UserRegisterRequest(username, password));
        return new RegisteredUser(username, password);
    }

    public RegisteredUser registerAdmin(String username, String password) {
        userClient.register(new UserRegisterRequest(username, password), secret);
        return new RegisteredUser(username, password);
    }

    public void login(String username, String password) {
        UserLoginResponse response = userClient.login(new UserLoginRequest(username, password));
        TokenHolder.token = response.getToken();
    }

    public void logout() {
        TokenHolder.token = null;
    }

    public static class TokenHolder {
        public static String token;
    }

    @AllArgsConstructor
    @Getter
    public class RegisteredUser {
        private String username;
        private String password;

        public RegisteredUser login() {
            AuthUtils.this.login(username, password);
            return this;
        }
    }

}
