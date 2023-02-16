package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, String> {
}
