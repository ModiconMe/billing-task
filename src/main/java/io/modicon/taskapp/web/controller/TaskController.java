package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.interaction.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    TaskGetByDateResponse getByDate(@RequestParam(value = "finish_date", required = false) String date,
                                    @AuthenticationPrincipal UserEntity user);

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
        public TaskGetByDateResponse getByDate(String date, UserEntity user) {
            return taskService.getByDate(date);
        }
    }
}
