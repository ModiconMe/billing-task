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

    interface Read {
        TaskEntity findByIdAndCreator(String id, UserEntity creator);

        void validateExistByIdAndCreator(String id, UserEntity creator);

        List<TaskEntity> findByTag(TagEntity tag);

        List<TaskEntity> findByTag(TagEntity tag, String page, String limit);

        List<TaskEntity> findByFinishDateGreaterThanEqual(LocalDate finishDate, String page, String limit);

        List<TaskEntity> findAll(String page, String limit);
    }

    interface Write {
        TaskEntity save(TaskEntity task);

        void delete(TaskEntity task);

        void deleteAll(List<TaskEntity> tasks);
    }

    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    @Service
    class JpaReadTaskDataSource implements TaskDataSource.Read {

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
        public List<TaskEntity> findByTag(TagEntity tag) {
            return jpaTaskRepository.findByTag(tag);
        }

        @Override
        public List<TaskEntity> findByTag(TagEntity tag, String page, String limit) {
            return jpaTaskRepository.findByTag(tag, taskSortingDispatcher.getPage(page, limit));
        }

        @Override
        public List<TaskEntity> findByFinishDateGreaterThanEqual(LocalDate finishDate, String page, String limit) {
            return jpaTaskRepository.findByFinishDateGreaterThanEqual(finishDate,
                    taskSortingDispatcher.getPage(page, limit));
        }

        @Override
        public List<TaskEntity> findAll(String page, String limit) {
            return jpaTaskRepository.findAll(taskSortingDispatcher.getPage(page, limit)).getContent();
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
