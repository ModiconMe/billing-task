package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TaskDataSource {

    interface ReadAdmin {
        TaskEntity findById(String id);

        List<TaskEntity> findCurrentTask(LocalDate finishDate, String page, String limit);

        List<TaskEntity> findAllTasks(String page, String limit);

        List<TaskEntity> findByTag(TagEntity tag);

        List<TaskEntity> findByTag(TagEntity tag, String page, String limit);
    }

    interface ReadUser {
        void validateExistByIdAndCreator(String id, UserEntity creator);

        TaskEntity findByIdAndCreator(String id, UserEntity creator);

        List<TaskEntity> findByTag(TagEntity tag, UserEntity creator);

        List<TaskEntity> findByTag(TagEntity tag, String page, String limit, UserEntity creator);

        List<TaskEntity> findCurrentTask(LocalDate finishDate, String page, String limit, UserEntity creator);

        List<TaskEntity> findAllUserTasks(String page, String limit, UserEntity creator);
    }

    interface Write {
        TaskEntity save(TaskEntity task);

        void delete(TaskEntity task);

        void deleteAll(List<TaskEntity> tasks);
    }

    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    @Service
    class JpaReadUserTaskDataSource implements ReadUser {

        private final JpaTaskRepository jpaTaskRepository;
        private final TaskSortingDispatcher taskSortingDispatcher;

        @Override
        public TaskEntity findByIdAndCreator(String id, UserEntity creator) {
            return jpaTaskRepository.findByIdAndCreator(id, creator)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "task not found..."));
        }

        @Override
        public void validateExistByIdAndCreator(String id, UserEntity creator) {
            if (jpaTaskRepository.findByIdAndCreator(id, creator).isPresent())
                throw exception(HttpStatus.BAD_REQUEST, "task with that identifier already exist");
        }

        @Override
        public List<TaskEntity> findByTag(TagEntity tag, UserEntity creator) {
            return jpaTaskRepository.findByTagAndCreator(tag, creator);
        }

        @Override
        public List<TaskEntity> findByTag(TagEntity tag, String page, String limit, UserEntity creator) {
            return jpaTaskRepository.findByTagAndCreator(tag, creator, taskSortingDispatcher.getPage(page, limit));
        }

        @Override
        public List<TaskEntity> findCurrentTask(LocalDate finishDate, String page, String limit, UserEntity creator) {
            return jpaTaskRepository.findByFinishDateAndCreator(finishDate, creator,
                    taskSortingDispatcher.getPage(page, limit));
        }

        @Override
        public List<TaskEntity> findAllUserTasks(String page, String limit, UserEntity creator) {
            return jpaTaskRepository.findAllByCreator(creator, taskSortingDispatcher.getPage(page, limit));
        }
    }

    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    @Service
    class JpaReadAdminTaskDataSource implements ReadAdmin {

        private final JpaTaskRepository jpaTaskRepository;
        private final TaskSortingDispatcher taskSortingDispatcher;

        @Override
        public TaskEntity findById(String id) {
            return jpaTaskRepository.findById(id)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "task not found..."));
        }

        @Override
        public List<TaskEntity> findCurrentTask(LocalDate finishDate, String page, String limit) {
            return jpaTaskRepository.findByFinishDate(finishDate,
                    taskSortingDispatcher.getPage(page, limit));
        }

        @Override
        public List<TaskEntity> findAllTasks(String page, String limit) {
            return jpaTaskRepository.findAll(taskSortingDispatcher.getPage(page, limit)).getContent();
        }

        @Override
        public List<TaskEntity> findByTag(TagEntity tag) {
            return jpaTaskRepository.findByTag(tag);
        }

        @Override
        public List<TaskEntity> findByTag(TagEntity tag, String page, String limit) {
            return jpaTaskRepository.findByTag(tag, taskSortingDispatcher.getPage(page, limit));
        }
    }

    @Transactional
    @RequiredArgsConstructor
    @Service
    class JpaWriteTaskDataSource implements TaskDataSource.Write {
        private final JpaTaskRepository jpaTaskRepository;

        @Override
        public TaskEntity save(TaskEntity task) {
            return jpaTaskRepository.save(task);
        }

        @Override
        public void delete(TaskEntity task) {
            jpaTaskRepository.delete(task);
        }

        @Override
        public void deleteAll(List<TaskEntity> tasks) {
            jpaTaskRepository.deleteAll(tasks);
        }
    }
}
