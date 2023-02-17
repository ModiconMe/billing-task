package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.FileData;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.FileDataRepository;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskFileService {

    TaskFileListResponse listFiles(String taskName);

    TaskFileUploadResponse upload(TaskFileUploadRequest request);

    TaskFileDownloadResponse download(TaskFileDownloadRequest request);

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskFileService {

        private final FileManagementService fileManagementService;
        private final TaskRepository taskRepository;

        @Override
        public TaskFileListResponse listFiles(String taskName) {
            TaskEntity task = taskRepository.findById(taskName).orElseThrow(() ->
                    exception(HttpStatus.NOT_FOUND, "task not found..."));

            Map<String, String> files = new HashMap<>();
            task.getFiles().forEach(f -> files.put(f.getId(), f.getName()));

            return new TaskFileListResponse(files);
        }

        @Override
        public TaskFileUploadResponse upload(TaskFileUploadRequest request) {
            TaskEntity task = taskRepository.findById(request.getTaskName()).orElseThrow(() ->
                    exception(HttpStatus.NOT_FOUND, "task not found..."));

            return new TaskFileUploadResponse(fileManagementService.store(task, request.getFile()));
        }

        @Override
        public TaskFileDownloadResponse download(TaskFileDownloadRequest request) {
            TaskEntity task = taskRepository.findById(request.getTaskName()).orElseThrow(() ->
                    exception(HttpStatus.NOT_FOUND, "task not found..."));

            FileData file = task.getFiles().stream().filter(f -> f.getId().equals(request.getFileId())).findFirst()
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "file not found..."));

            return new TaskFileDownloadResponse(fileManagementService.getFileBytes(file), file.getType());
        }
    }
}
