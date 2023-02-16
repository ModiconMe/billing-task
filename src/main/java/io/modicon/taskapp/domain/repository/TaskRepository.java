package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
}
