package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.SecurityContextHolderService;
import io.modicon.taskapp.application.service.TaskService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.dto.ApiExceptionDto;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

public interface TaskController {

    String BASE_URL_V1 = "api/v1/tasks";

    @Operation(summary = "create task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "task already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @PostMapping
    TaskCreateResponse create(@Valid @RequestBody TaskCreateRequest request);

    @Operation(summary = "update task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskUpdateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "task already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "404", description = "task not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @PutMapping("/{id}")
    TaskUpdateResponse update(@PathVariable String id,
                              @Valid @RequestBody TaskUpdateRequest request);

    @Operation(summary = "delete task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDeleteResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "task not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    TaskDeleteResponse delete(@PathVariable String id);

    @Operation(summary = "get tasks by date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskGetByDateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping("/byDate")
    TaskGetByDateResponse get(@RequestParam(value = "finish_date") String date,
                              @RequestParam(value = "page", defaultValue = "0") String page,
                              @RequestParam(value = "limit", defaultValue = "20") String limit);

    @Operation(summary = "get all tasks group by priority type, Cacheable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskGetGroupByPriorityType.class)) }),
            @ApiResponse(responseCode = "400", description = "invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping
    TaskGetGroupByPriorityType get(@RequestParam(value = "page", defaultValue = "0") String page,
                                   @RequestParam(value = "limit", defaultValue = "20") String limit);

    @SecurityRequirement(name = "Bearer Authentication")
    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class BaseTaskController implements TaskController {

        private final TaskService taskService;
        private final SecurityContextHolderService securityContextHolderService;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            return taskService.create(request.withUser(securityContextHolderService.getCurrentUser()));
        }

        @Override
        public TaskUpdateResponse update(String id, TaskUpdateRequest request) {
            return taskService.update(id, request.withUser(securityContextHolderService.getCurrentUser()));
        }

        @Override
        public TaskDeleteResponse delete(String id) {
            return taskService.delete(id, securityContextHolderService.getCurrentUser());
        }

        @Override
        public TaskGetByDateResponse get(String date, String page, String limit) {
            return taskService.get(date, page, limit, securityContextHolderService.getCurrentUser());
        }

        @Override
        public TaskGetGroupByPriorityType get(String page, String limit) {
            return taskService.get(page, limit, securityContextHolderService.getCurrentUser());
        }
    }
}
