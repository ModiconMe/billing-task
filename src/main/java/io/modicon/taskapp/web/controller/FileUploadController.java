package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TaskFileService;
import io.modicon.taskapp.web.interaction.TaskFileDownloadRequest;
import io.modicon.taskapp.web.interaction.TaskFileUploadRequest;
import io.modicon.taskapp.web.interaction.TaskFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadController {

    String BASE_URL_V1 = "api/v1/files";

    @PostMapping("/{taskName}")
    TaskFileUploadResponse upload(@RequestParam(value = "file") MultipartFile file, @PathVariable String taskName);

    @GetMapping("/{taskName}/{fileName}")
    ResponseEntity<TaskFileUploadResponse> download(@PathVariable String taskName, @PathVariable String fileName);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements FileUploadController {

        private final TaskFileService taskFileService;

        @Override
        public TaskFileUploadResponse upload(MultipartFile file, String taskName) {
            return taskFileService.upload(new TaskFileUploadRequest(taskName, file));
        }

        @Override
        public ResponseEntity<TaskFileUploadResponse> download(String taskName, String fileName) {
            taskFileService.download(new TaskFileDownloadRequest(taskName, fileName));
            return ResponseEntity.ok()
                    .contentType();
        }
    }
}
