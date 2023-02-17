package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.web.interaction.TaskFileDownloadRequest;
import io.modicon.taskapp.web.interaction.TaskFileUploadRequest;
import io.modicon.taskapp.web.interaction.TaskFileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskFileService {

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
        public TaskFileUploadResponse upload(TaskFileUploadRequest request) {
            TaskEntity task = taskRepository.findById(request.getTaskName()).orElseThrow(() ->
                    exception(HttpStatus.NOT_FOUND, "task not found..."));

            return new TaskFileUploadResponse(fileManagementService.store(task, request.getFile()));
        }

        @Override
        public TaskFileDownloadResponse download(TaskFileDownloadRequest request) {
            return null;
        }
    }
}
