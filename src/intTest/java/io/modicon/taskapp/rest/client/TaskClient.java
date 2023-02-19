package io.modicon.taskapp.rest.client;

import io.modicon.taskapp.rest.config.LocalFeignConfig;
import io.modicon.taskapp.web.controller.AuthUserController;
import io.modicon.taskapp.web.controller.TaskController;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "task-client", path = "/api/v1/tasks", configuration = LocalFeignConfig.class)
public interface TaskClient extends TaskController { }
