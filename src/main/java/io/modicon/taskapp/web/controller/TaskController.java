package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskService;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

public interface TaskController {

    String BASE_URL_V1 = "api/v1/tasks";

    @PostMapping
    TaskCreateResponse create(@RequestBody TaskCreateRequest request);

    @PutMapping
    TaskUpdateResponse update(@RequestBody TaskUpdateRequest request);

    @DeleteMapping("/{id}")
    TaskDeleteResponse delete(@PathVariable String id);

    @GetMapping("/byDate")
    TaskGetByDateResponse getByDate(@RequestParam(value = "finish_date", required = false) String date);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements TaskController {

        private final TaskService taskService;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            return taskService.create(request);
        }

        @Override
        public TaskUpdateResponse update(TaskUpdateRequest request) {
            return taskService.update(request);
        }

        @Override
        public TaskDeleteResponse delete(String id) {
            return taskService.delete(id);
        }

        @Override
        public TaskGetByDateResponse getByDate(String date) {
            return taskService.getByDate(date);
        }
    }
}
