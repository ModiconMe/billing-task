package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

public interface TaskController {

    String BASE_URL_V1 = "api/v1/tasks";

    @PostMapping
    TaskCreateResponse create(@Valid @RequestBody TaskCreateRequest request,
                              @AuthenticationPrincipal UserEntity user);

    @PutMapping("/{id}")
    TaskUpdateResponse update(@PathVariable String id,
                              @Valid @RequestBody TaskUpdateRequest request,
                              @AuthenticationPrincipal UserEntity user);

    @DeleteMapping("/{id}")
    TaskDeleteResponse delete(@PathVariable String id,
                              @AuthenticationPrincipal UserEntity user);

    @GetMapping("/byDate")
    TaskGetByDateResponse get(@RequestParam(value = "finish_date", required = false) String date,
                              @RequestParam(value = "page", defaultValue = "0") String page,
                              @RequestParam(value = "limit", defaultValue = "20") String limit,
                              @AuthenticationPrincipal UserEntity user);

    @GetMapping
    TaskGetGroupByPriorityType get(@RequestParam(value = "page", defaultValue = "0") String page,
                                   @RequestParam(value = "limit", defaultValue = "20") String limit,
                                   @AuthenticationPrincipal UserEntity user);

    @SecurityRequirement(name = "Bearer Authentication")
    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements TaskController {

        private final TaskService taskService;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request, UserEntity user) {
            return taskService.create(request.withUser(user));
        }

        @Override
        public TaskUpdateResponse update(String id, TaskUpdateRequest request, UserEntity user) {
            return taskService.update(id, request.withUser(user));
        }

        @Override
        public TaskDeleteResponse delete(String id, UserEntity user) {
            return taskService.delete(id, user);
        }

        @Override
        public TaskGetByDateResponse get(String date, String page, String limit, UserEntity user) {
            return taskService.get(date, page, limit, user);
        }

        @Cacheable(value = "tasks")
        @Override
        public TaskGetGroupByPriorityType get(String page, String limit, UserEntity user) {
            return taskService.get(page, limit, user);
        }
    }
}
