package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskDataSource {
    TaskEntity findByIdAndCreator(String id, UserEntity creator);

    void validateExistByIdAndCreator(String id, UserEntity creator);

    List<TaskEntity> findByTag(TagEntity tag);

    List<TaskEntity> findByTag(TagEntity tag, Pageable pageable);

    List<TaskEntity> findByFinishDateGreaterThanEqual(LocalDate finishDate, String page, String limit);

    TaskEntity save(TaskEntity task);

    void delete(TaskEntity task);

    List<TaskEntity> findAll(String page, String limit);

    @Transactional
    @RequiredArgsConstructor
    @Service
    class JpaTaskDataSource implements TaskDataSource {

        private final JpaTaskRepository jpaTaskRepository;

        @Transactional(readOnly = true)
        @Override
        public TaskEntity findByIdAndCreator(String id, UserEntity creator) {
            return jpaTaskRepository.findByIdAndCreator(id, creator)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "task not found..."));
        }

        @Transactional(readOnly = true)
        @Override
        public void validateExistByIdAndCreator(String id, UserEntity creator) {
            if (jpaTaskRepository.findByIdAndCreator(id, creator).isPresent())
                throw exception(HttpStatus.BAD_REQUEST, "task with that identifier already exist");
        }

        @Transactional(readOnly = true)
        @Override
        public List<TaskEntity> findByTag(TagEntity tag) {
            return jpaTaskRepository.findByTag(tag);
        }

        @Transactional(readOnly = true)
        @Override
        public List<TaskEntity> findByTag(TagEntity tag, Pageable pageable) {
            return jpaTaskRepository.findByTag(tag, pageable);
        }

        @Transactional(readOnly = true)
        @Override
        public List<TaskEntity> findByFinishDateGreaterThanEqual(LocalDate finishDate, String page, String limit) {
            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            List<TaskEntity> tasks;
            tasks = fieldToSort.map(field -> jpaTaskRepository.findByFinishDateGreaterThanEqual(finishDate, PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(field.getName()))))
                    .orElseGet(() -> jpaTaskRepository.findByFinishDateGreaterThanEqual(finishDate, PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit))
                    ));

            return tasks;
        }

        @Override
        public TaskEntity save(TaskEntity task) {
            return jpaTaskRepository.save(task);
        }

        @Override
        public void delete(TaskEntity task) {
            jpaTaskRepository.delete(task);
        }

        @Transactional(readOnly = true)
        @Override
        public List<TaskEntity> findAll(String page, String limit) {
            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            List<TaskEntity> tasks;
            tasks = fieldToSort.map(field -> jpaTaskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(field.getName()))))
                    .orElseGet(() -> jpaTaskRepository.findAll(PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit))
                    )).getContent();

            return tasks;
        }
    }

}
