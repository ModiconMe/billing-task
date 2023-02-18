package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskFileService;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadController {

    String BASE_URL_V1 = "api/v1/files";

    @GetMapping("/{taskName}")
    TaskFileListResponse listFiles(@PathVariable String taskName);

    @PostMapping("/{taskName}")
    TaskFileUploadResponse upload(@RequestParam(value = "file") MultipartFile file, @PathVariable String taskName);

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
