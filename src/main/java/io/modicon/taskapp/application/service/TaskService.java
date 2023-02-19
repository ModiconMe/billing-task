package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskService {

    TaskCreateResponse create(TaskCreateRequest request);

    TaskUpdateResponse update(String id, TaskUpdateRequest request);

    TaskDeleteResponse delete(String id, UserEntity user);

    TaskGetByDateResponse get(String date, String page, String limit, UserEntity user);

    TaskGetGroupByPriorityType get(String page, String limit, UserEntity user);

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TaskService {

        private final TaskDataSource.ReadUser readUserTaskDataSource;
        private final TaskDataSource.ReadAdmin readAdminTaskDataSource;
        private final TaskDataSource.Write writeTaskDataSource;
        private final TagDataSource.Read readTagDataSource;
        private final TagDataSource.Write writeTagDataSource;
        private final TaskFileService taskFileService;

        private final TaskDtoMapper taskDtoMapper;

        @Override
        public TaskCreateResponse create(TaskCreateRequest request) {
            readUserTaskDataSource.validateExistByIdAndCreator(request.getId(), request.getUser());

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            TagEntity tag = readTagDataSource.supplyTag(request.getTag());
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

            writeTaskDataSource.save(task);

            return new TaskCreateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskUpdateResponse update(String id, TaskUpdateRequest request) {
            TaskEntity task;
            if (request.getUser().getRole().equals(ApplicationUserRole.ADMIN)) // check admin role
                task = readAdminTaskDataSource.findById(id);
            else
                task = readUserTaskDataSource.findByIdAndCreator(id, request.getUser());

            if (request.getFinishDate().isBefore(LocalDate.now()))
                throw exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date");

            TagEntity tag = null;
            if (request.getTag() != null) {
                Optional<TagEntity> optionalTag = readTagDataSource.tryToFindTag(request.getTag());
                if (optionalTag.isPresent()) {
                    tag = optionalTag.get();
                    if (!task.getTag().equals(tag)) {
                        tag.addTask();
                        task.getTag().removeTask();
                        writeTagDataSource.save(task.getTag());
                    }
                } else {
                    tag = new TagEntity(UUID.randomUUID().toString(), request.getTag(), 0L);
                    tag.addTask();
                }
            }

            PriorityType taskPriorityType = null;
            if (request.getPriorityType() != null) {
                try {
                    taskPriorityType = PriorityType.valueOf(PriorityType.class, request.getPriorityType().toUpperCase());
                } catch (Exception e) {
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

            writeTaskDataSource.save(task);

            return new TaskUpdateResponse(taskDtoMapper.apply(task));
        }

        @Override
        public TaskDeleteResponse delete(String id, UserEntity user) {
            TaskEntity task;
            if (user.getRole().equals(ApplicationUserRole.ADMIN))
                task = readAdminTaskDataSource.findById(id);
            else
                task = readUserTaskDataSource.findByIdAndCreator(id, user);

            task.getTag().removeTask();

            writeTaskDataSource.delete(task);
            taskFileService.deleteTaskFiles(id);

            return new TaskDeleteResponse(task.getId());
        }

        @Transactional(readOnly = true)
        @Override
        public TaskGetByDateResponse get(String date, String page, String limit, UserEntity user) {
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (Exception e) {
                throw exception(HttpStatus.BAD_REQUEST, "wrong date format, please provide date like [yyyy-mm-dd]");
            }

            List<TaskEntity> tasks;
            if (user.getRole().equals(ApplicationUserRole.ADMIN))
                tasks = readAdminTaskDataSource.findCurrentTask(parsedDate, page, limit);
            else
                tasks = readUserTaskDataSource.findCurrentTask(parsedDate, page, limit, user);

            return new TaskGetByDateResponse(tasks.stream().map(taskDtoMapper).toList());
        }

        @Cacheable(value = "tasks", key = "#user")
        @Override
        public TaskGetGroupByPriorityType get(String page, String limit, UserEntity user) {
            List<TaskEntity> tasks;
            if (user.getRole().equals(ApplicationUserRole.ADMIN))
                tasks = readAdminTaskDataSource.findAllTasks(page, limit);
            else
                tasks = readUserTaskDataSource.findAllUserTasks(page, limit, user);

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
