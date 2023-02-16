package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.TagRepository;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskService {

    TaskCreateResponse create(TaskCreateRequest request);

    TaskUpdateResponse update(String id, TaskUpdateRequest request);

    TaskDeleteResponse delete(String id);

    TaskGetByDateResponse getByDate(String date);

    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskService {

        private final TaskRepository taskRepository;
        private final TagRepository tagRepository;
        private final TaskDtoMapper taskDtoMapper;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            if (taskRepository.findByIdAndCreator(request.getId(), request.getUser()).isPresent())
                throw exception(HttpStatus.BAD_REQUEST, "task with that identifier already exist");

            List<TagEntity> tags = new ArrayList<>();
            if (request.getTags() != null) {
                tags = request.getTags().stream()
                        .map((t) -> tagRepository.findById(t).orElseGet(() -> new TagEntity(t)))
                        .collect(Collectors.toList());
            }

            PriorityType taskPriorityType;
            try {
                taskPriorityType = PriorityType.valueOf(PriorityType.class, request.getPriorityType());
            } catch (Exception e) {
                e.printStackTrace();
                throw exception(HttpStatus.BAD_REQUEST,
                        "wrong priority type for task, it only supports %s", Arrays.toString( PriorityType.values()));
            }

            TaskEntity task = TaskEntity.builder()
                    .id(request.getId())
                    .description(request.getDescription())
                    .createdAt(LocalDate.now())
                    .finishDate(request.getFinishDate())
                    .priorityType(taskPriorityType)
                    .tags(tags)
                    .creator(request.getUser())
                    .build();

            taskRepository.save(task);

            return new TaskCreateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskUpdateResponse update(String id, TaskUpdateRequest request) {
            TaskEntity task = taskRepository.findByIdAndCreator(id, request.getUser())
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "task not found..."));

            List<TagEntity> tags = new ArrayList<>();
            if (request.getTags() != null) {
                tags = request.getTags().stream()
                        .map((t) -> tagRepository.findById(t).orElseGet(() -> new TagEntity(t)))
                        .collect(Collectors.toList());
            }

            PriorityType taskPriorityType = null;
            if (request.getPriorityType() != null) {
                try {
                    taskPriorityType = PriorityType.valueOf(PriorityType.class, request.getPriorityType());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw exception(HttpStatus.BAD_REQUEST,
                            "wrong priority type for task, it only supports %s", Arrays.toString( PriorityType.values()));
                }
            }

            task = task.toBuilder()
                    .description(request.getDescription() != null ? request.getDescription() : task.getDescription())
                    .finishDate(request.getFinishDate() != null ? request.getFinishDate() : task.getFinishDate())
                    .priorityType(taskPriorityType != null ? taskPriorityType : task.getPriorityType())
                    .tags(!tags.isEmpty() ? tags : task.getTags())
                    .build();

            taskRepository.save(task);

            return new TaskUpdateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskDeleteResponse delete(String id) {
            return null;
        }

        @Transactional(readOnly = true)
        @Override
        public TaskGetByDateResponse getByDate(String date) {
            return null;
        }
    }
}
