package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagRepository;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskService {

    TaskCreateResponse create(TaskCreateRequest request);

    TaskUpdateResponse update(String id, TaskUpdateRequest request);

    TaskDeleteResponse delete(String id, UserEntity user);

    TaskGetByDateResponse get(String date, String page, String limit);

    TaskGetGroupByPriorityType get(String page, String limit);

    TaskFileUploadResponse upload(TaskFileUploadRequest request);

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskService {

        private final TaskRepository taskRepository;
        private final TagRepository tagRepository;
        private final TaskDtoMapper taskDtoMapper;
        private final FileManagementService fileManagementService;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            if (taskRepository.findByIdAndCreator(request.getId(), request.getUser()).isPresent())
                throw exception(HttpStatus.BAD_REQUEST, "task with that identifier already exist");

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            List<TagEntity> tags = new ArrayList<>();
            if (request.getTags() != null) {
                tags = request.getTags().stream()
                        .map((t) -> tagRepository.findById(t).orElseGet(() -> new TagEntity(t, 0L)))
                        .collect(Collectors.toList());
                tags.forEach(TagEntity::addTask);
            }

            PriorityType taskPriorityType;
            try {
                taskPriorityType = PriorityType.valueOf(PriorityType.class, request.getPriorityType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw exception(HttpStatus.BAD_REQUEST,
                        "wrong priority type for task, it only supports %s", Arrays.toString(PriorityType.values()));
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

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            List<TagEntity> tags = new ArrayList<>();
            if (request.getTags() != null) {
                tags = request.getTags().stream()
                        .map((t) -> tagRepository.findById(t).orElseGet(() -> new TagEntity(t, 0L)))
                        .collect(Collectors.toList());
                tags.forEach(TagEntity::addTask);
            }

            PriorityType taskPriorityType = null;
            if (request.getPriorityType() != null) {
                try {
                    taskPriorityType = PriorityType.valueOf(PriorityType.class, request.getPriorityType());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw exception(HttpStatus.BAD_REQUEST,
                            "wrong priority type for task, it only supports %s", Arrays.toString(PriorityType.values()));
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
        public TaskDeleteResponse delete(String id, UserEntity user) {
            TaskEntity task = taskRepository.findByIdAndCreator(id, user)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "task not found..."));

            taskRepository.delete(task);

            return new TaskDeleteResponse(task.getId());
        }

        @Transactional(readOnly = true)
        @Override
        public TaskGetByDateResponse get(String date, String page, String limit) {
            LocalDate parsedDate = LocalDate.parse(date);

            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            List<TaskEntity> tasks;
            tasks = fieldToSort.map(field -> taskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(field.getName()))).getContent())
                    .orElseGet(() -> taskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit))
                    ).getContent());

            return new TaskGetByDateResponse(tasks.stream().map(taskDtoMapper).toList());
        }

        @Override
        public TaskGetGroupByPriorityType get(String page, String limit) {
            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            List<TaskEntity> tasks;
            tasks = fieldToSort.map(field -> taskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(field.getName()))).getContent())
                    .orElseGet(() -> taskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit))
                    ).getContent());

            Map<PriorityType, List<TaskDto>> priorityTaskMap = new LinkedHashMap<>();
            Arrays.stream(PriorityType.values()).forEach(p -> {
                List<TaskDto> sortedTask = tasks.stream()
                        .filter(t -> t.getPriorityType().equals(p))
                        .map(taskDtoMapper).toList();
                if (!sortedTask.isEmpty())
                    priorityTaskMap.put(p, sortedTask);
            });

            return new TaskGetGroupByPriorityType(priorityTaskMap);
        }

        @Override
        public TaskFileUploadResponse upload(TaskFileUploadRequest request) {
            TaskEntity task = taskRepository.findById(request.getTaskName()).orElseThrow(() ->
                    exception(HttpStatus.NOT_FOUND, "task not found..."));

            return new TaskFileUploadResponse(fileManagementService.store(task, request.getFile()));
        }
    }
}
