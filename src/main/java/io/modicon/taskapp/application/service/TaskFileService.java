package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.FileData;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.JpaTaskRepository;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskFileService {

    TaskFileListResponse listFiles(String taskName);

    TaskFileUploadResponse upload(TaskFileUploadRequest request);

    TaskFileDownloadResponse download(TaskFileDownloadRequest request);

    void deleteTaskFiles(String taskName);

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskFileService {

        private final FileManagementService fileManagementService;
        private final TaskDataSource.ReadAdmin taskDataService;

        @Override
        public TaskFileListResponse listFiles(String taskName) {
            TaskEntity task = taskDataService.findById(taskName);

            Map<String, String> files = new HashMap<>();
            task.getFiles().forEach(f -> files.put(f.getId(), f.getName()));

            return new TaskFileListResponse(files);
        }

        @Override
        public TaskFileUploadResponse upload(TaskFileUploadRequest request) {
            TaskEntity task = taskDataService.findById(request.getTaskName());

            return new TaskFileUploadResponse(fileManagementService.store(task, request.getFile()));
        }

        @Override
        public TaskFileDownloadResponse download(TaskFileDownloadRequest request) {
            TaskEntity task = taskDataService.findById(request.getTaskName());;

            FileData file = task.getFiles().stream().filter(f -> f.getId().equals(request.getFileId())).findFirst()
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "file not found..."));

            return new TaskFileDownloadResponse(fileManagementService.getFileBytes(file), file.getType());
        }

        @Override
        public void deleteTaskFiles(String taskName) {
            fileManagementService.deleteFileDirectory(taskName);
        }
    }
}
