package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskFileService;
import io.modicon.taskapp.web.dto.ApiExceptionDto;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadController {

    String BASE_URL_V1 = "api/v1/files";

    @Operation(summary = "list task files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "task not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping("/{taskName}")
    TaskFileListResponse listFiles(@PathVariable String taskName);

    @Operation(summary = "upload file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "task not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "500", description = "server upload error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @PostMapping("/{taskName}")
    TaskFileUploadResponse upload(@RequestParam(value = "file") MultipartFile file, @PathVariable String taskName);

    @Operation(summary = "upload file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskCreateResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "task or file not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "500", description = "server download error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping("/{taskName}/{fileName}")
    ResponseEntity<?> download(@PathVariable String taskName, @PathVariable String fileName);

    @SecurityRequirement(name = "Bearer Authentication")
    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class BaseFileUploadController implements FileUploadController {

        private final TaskFileService taskFileService;

        @Override
        public TaskFileListResponse listFiles(String taskName) {
            return taskFileService.listFiles(taskName);
        }

        @Override
        public TaskFileUploadResponse upload(MultipartFile file, String taskName) {
            return taskFileService.upload(new TaskFileUploadRequest(taskName, file));
        }

        @Override
        public ResponseEntity<?> download(String taskName, String fileName) {
            TaskFileDownloadResponse response = taskFileService.download(new TaskFileDownloadRequest(taskName, fileName));
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(response.getContentType()))
                    .body(response.getFile());
        }
    }
}
