package io.modicon.taskapp.client;

import io.modicon.taskapp.config.LocalFeignConfig;
import io.modicon.taskapp.web.controller.AuthUserController;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-client", path = "/api/v1/users", configuration = LocalFeignConfig.class)
public interface UserClient extends AuthUserController { }
