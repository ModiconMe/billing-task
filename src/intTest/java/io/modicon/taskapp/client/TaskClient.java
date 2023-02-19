package io.modicon.taskapp.client;

import io.modicon.taskapp.config.LocalFeignConfig;
import io.modicon.taskapp.web.controller.TaskController;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "task-client", path = "/api/v1/tasks", configuration = LocalFeignConfig.class)
public interface TaskClient extends TaskController { }
