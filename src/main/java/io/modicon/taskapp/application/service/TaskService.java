package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.JpaTagRepository;
import io.modicon.taskapp.domain.repository.JpaTaskRepository;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskService {

    TaskCreateResponse create(TaskCreateRequest request);

    TaskUpdateResponse update(String id, TaskUpdateRequest request);

    TaskDeleteResponse delete(String id, UserEntity user);

    TaskGetByDateResponse get(String date, String page, String limit);

    TaskGetGroupByPriorityType get(String page, String limit);

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskService {

        private final TaskDataSource taskDataSource;
        private final JpaTagRepository tagRepository;
        private final TaskDtoMapper taskDtoMapper;
        private final TaskFileService taskFileService;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            taskDataSource.validateExistByIdAndCreator(request.getId(), request.getUser());

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            TagEntity tag = tagRepository
                    .findById(request.getTag()).orElseGet(() -> new TagEntity(request.getTag(), 0L));
            tag.addTask();

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
                    .tag(tag)
                    .creator(request.getUser())
                    .build();

            taskDataSource.save(task);

            return new TaskCreateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskUpdateResponse update(String id, TaskUpdateRequest request) {
            TaskEntity task = taskDataSource.findByIdAndCreator(id, request.getUser());

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            TagEntity tag = null;
            if (request.getTag() != null) {
                Optional<TagEntity> optionalTag = tagRepository.findById(request.getTag());
                if (optionalTag.isPresent()) {
                     tag = optionalTag.get();
                     if (!task.getTag().equals(tag)) {
                         tag.addTask();
                         task.getTag().removeTask();
                         tagRepository.save(task.getTag());
                     }
                } else {
                    tag = new TagEntity(request.getTag(), 0L);
                    tag.addTask();
                }
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
                    .tag(tag != null ? tag : task.getTag())
                    .build();

            taskDataSource.save(task);

            return new TaskUpdateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskDeleteResponse delete(String id, UserEntity user) {
            TaskEntity task = taskDataSource.findByIdAndCreator(id, user);

            task.getTag().removeTask();
            tagRepository.delete(task.getTag());

            taskDataSource.delete(task);
            taskFileService.deleteTaskFiles(id);

            return new TaskDeleteResponse(task.getId());
        }

        @Transactional(readOnly = true)
        @Override
        public TaskGetByDateResponse get(String date, String page, String limit) {
            LocalDate parsedDate = LocalDate.parse(date);

            List<TaskEntity> tasks = taskDataSource.findByFinishDateGreaterThanEqual(parsedDate, page, limit);

            return new TaskGetByDateResponse(tasks.stream().map(taskDtoMapper).toList());
        }

        @Override
        public TaskGetGroupByPriorityType get(String page, String limit) {
            List<TaskEntity> tasks = taskDataSource.findAll(page, limit);

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

    }
}
