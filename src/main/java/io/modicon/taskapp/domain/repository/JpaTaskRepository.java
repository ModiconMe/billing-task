package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaTaskRepository extends JpaRepository<TaskEntity, String> {
    Optional<TaskEntity> findByIdAndCreator(String id, UserEntity creator);

    List<TaskEntity> findByTag(TagEntity tag);

    List<TaskEntity> findByTag(TagEntity tag, Pageable pageable);

    List<TaskEntity> findByFinishDateGreaterThanEqual(LocalDate finishDate, Pageable pageable);
}
