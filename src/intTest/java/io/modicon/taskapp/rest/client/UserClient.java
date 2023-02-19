package io.modicon.taskapp.rest.client;

import io.modicon.taskapp.rest.config.LocalFeignConfig;
import io.modicon.taskapp.web.controller.AuthUserController;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-client", path = "/api/v1/users", configuration = LocalFeignConfig.class)
public interface UserClient extends AuthUserController { }
