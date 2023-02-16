package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.TagRepository;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface TaskService {

    TaskCreateResponse create(TaskCreateRequest request);

    TaskUpdateResponse update(TaskUpdateRequest request);

    TaskDeleteResponse delete(String id);

    TaskGetByDateResponse getByDate(String date);

    @RequiredArgsConstructor
    @Service
    class Base implements TaskService {

        private final TaskRepository taskRepository;
        private final TagRepository tagRepository;
        private final TaskDtoMapper taskDtoMapper;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            List<TagEntity> tags = new ArrayList<>();
            if (request.getTags() != null) {
                tags = request.getTags().stream()
                        .map((t) -> tagRepository.findById(t).orElseGet(() -> new TagEntity(t)))
                        .collect(Collectors.toList());
            }

            TaskEntity task = TaskEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .description(request.getDescription())
                    .createdAt(LocalDate.now())
                    .finishDate(request.getFinishDate())
                    .tags(tags)
                    .build();

            taskRepository.save(task);

            return new TaskCreateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskUpdateResponse update(TaskUpdateRequest request) {
            return null;
        }

        @Override
        public TaskDeleteResponse delete(String id) {
            return null;
        }

        @Override
        public TaskGetByDateResponse getByDate(String date) {
            return null;
        }
    }
}
