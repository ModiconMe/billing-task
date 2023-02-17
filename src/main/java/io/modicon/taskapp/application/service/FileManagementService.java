package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.FileData;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.JpaTaskRepository;
import io.modicon.taskapp.infrastructure.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface FileManagementService {

    String store(TaskEntity task, MultipartFile file);

    byte[] getFileBytes(FileData file);

    void deleteFileDirectory(String dirName);

    @Slf4j
    @Service
    class Base implements FileManagementService {

        private final Path fileStorageLocation;
        private final JpaTaskRepository taskRepository;

        public Base(ApplicationConfig config, JpaTaskRepository taskRepository) {
            File homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
            this.fileStorageLocation = Paths.get(homeDirectory + config.getUploadDir());

            try {
                Files.createDirectories(this.fileStorageLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw exception(HttpStatus.INTERNAL_SERVER_ERROR, "could not create the directory where the uploaded files will be stored.");
            }
            this.taskRepository = taskRepository;
        }

        @Override
        public String store(TaskEntity task, MultipartFile file) {
            String separator = FileSystems.getDefault().getSeparator();
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            try {
                // Check if the file's name contains invalid characters
                if(fileName.contains("..")) {
                    throw exception(HttpStatus.BAD_REQUEST, "Sorry! Filename contains invalid path sequence " + fileName);
                }

                String taskName = task.getId();
                UUID fileId = UUID.randomUUID();
                Path dir = Paths.get(this.fileStorageLocation.toString() + separator + taskName);
                Files.createDirectories(dir);

                Path targetLocation = dir.resolve(fileId + "_" + fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                log.info("Stored {}", targetLocation);

                task = task.toBuilder()
                        .file(new FileData(fileId.toString(), fileName, file.getContentType(), targetLocation.toString()))
                        .build();
                taskRepository.save(task);

                return fileName;
            } catch (IOException ex) {
                throw exception(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + fileName + ". Please try again!");
            }
        }

        @Override
        public byte[] getFileBytes(FileData file) {
            String filePath = file.getFilePath();

            try {
                return Files.readAllBytes(new File(filePath).toPath());
            } catch (IOException e) {
                e.printStackTrace();
                throw exception(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        @Override
        public void deleteFileDirectory(String dirName) {
            try {
                String separator = FileSystems.getDefault().getSeparator();
                log.info("deleting dir");
                FileUtils.deleteDirectory(new File(this.fileStorageLocation.toString() + separator + dirName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
