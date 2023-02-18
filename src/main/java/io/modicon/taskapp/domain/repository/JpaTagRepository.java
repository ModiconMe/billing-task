package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaTagRepository extends JpaRepository<TagEntity, String> {
    List<TagEntity> findAllByTaskCountIsGreaterThan(Long taskCount);

    Optional<TagEntity> findByTagName(String tagName);

    boolean existsByTagName(String tagName);
}
