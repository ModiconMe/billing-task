package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.*;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.infrastructure.config.ApplicationConfig;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.web.dto.UserDto;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {

    private FileManagementService underTest;

    @Mock
    private TaskDataSource.Write taskDataSource;
    @Mock
    private ApplicationConfig applicationConfig;

    private String fileDir = "/taskapp/uploads1";

    @BeforeEach
    void setUp() {
        when(applicationConfig.getUploadDir()).thenReturn(fileDir);
        underTest = new FileManagementService.Base(applicationConfig, taskDataSource);

    }

    private final UserEntity creator;
    private final UserDto creatorDto;
    private final TaskEntity commonTask;
    private final TagEntity tag;
    private final FileData fileData;

    private final MultipartFile file;
    private final MultipartFile wrongFile;
    private Path dir;
    private String separator;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
                .build();
        creatorDto = new UserDto(creator.getUsername());
        tag = new TagEntity("id", "name", 1L);
        fileData = new FileData("id", "name", "type", "path");
        commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .file(fileData)
                .build();

        Path path = Paths.get("src/test/java/io/modicon/taskapp/application/service/FileManagementServiceTest.java");
        String name = "FileManagementServiceTest.java";
        String originalFileName = "FileManagementServiceTest.java";
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }
        file = new MockMultipartFile(name,
                originalFileName, contentType, content);

        wrongFile = new MockMultipartFile("..hello", "..hello", contentType, content);
        dir = Paths.get(FileSystemView.getFileSystemView().getHomeDirectory() + fileDir);
        separator = FileSystems.getDefault().getSeparator();
    }

    @Test
    void shouldStoreFile() throws IOException {
        // given
        // when
        String actual = underTest.store(commonTask, file);

        // then
        verify(taskDataSource, times(1)).save(commonTask);
        Path path = Paths.get(dir + separator + commonTask.getId());
        assertTrue(Files.exists(path));
        List<Path> paths = Files.walk(path).toList();
        assertEquals(paths.size(), 2);
    }

    @Test
    void shouldNOtStoreFile_whenWrongNameProvided() {
        // given
        // when
        ApiException actual = catchThrowableOfType(() -> underTest.store(commonTask, wrongFile), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "Sorry! Filename contains invalid path sequence " + wrongFile.getName()), actual);
    }

    @Test
    void getFileBytes() throws IOException {
        // given
        String path = "src/test/java/io/modicon/taskapp/application/service/FileManagementServiceTest.java";
        byte[] bytes = Files.readAllBytes(new File(path).toPath());

        // when
        byte[] fileBytes = underTest.getFileBytes(new FileData(fileData.getFilePath(), fileData.getName(), fileData.getType(), path));

        // then
        assertEquals(bytes[100], fileBytes[100]);
    }

    @Test
    void deleteFileDirectory() throws IOException {
        // given
        String testDir = "test";
        Path testPath = Path.of(dir + separator + testDir);
        Files.createDirectories(testPath);
        assertTrue(Files.exists(testPath));
        List<Path> paths = Files.walk(testPath).toList();
        assertEquals(paths.size(), 1);

        // when
        underTest.deleteFileDirectory(testDir);

        // then
        assertFalse(Files.exists(testPath));
    }

    @AfterEach
    void tearDown() {
        try {
            FileUtils.deleteDirectory(dir.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}