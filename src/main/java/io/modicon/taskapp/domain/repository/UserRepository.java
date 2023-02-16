package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
