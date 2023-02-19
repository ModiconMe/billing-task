package io.modicon.taskapp.client;

import io.modicon.taskapp.config.LocalFeignConfig;
import io.modicon.taskapp.web.controller.TagController;
import io.modicon.taskapp.web.controller.TaskController;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "tag-client", path = "/api/v1/tags", configuration = LocalFeignConfig.class)
public interface TagClient extends TagController { }
