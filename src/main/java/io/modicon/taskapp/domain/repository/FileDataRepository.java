package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDataRepository extends JpaRepository<FileData, String> {
}
