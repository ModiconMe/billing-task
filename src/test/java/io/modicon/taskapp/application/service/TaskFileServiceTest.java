package io.modicon.taskapp.application.service;

import io.modicon.taskapp.domain.model.*;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.dto.UserDto;
import io.modicon.taskapp.web.interaction.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Map;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskFileServiceTest {

    private TaskFileService underTest;

    @Mock
    private FileManagementService fileManagementService;
    @Mock
    private TaskDataSource.ReadAdmin taskDataSource;

    @BeforeEach
    void setUp() {
        underTest = new TaskFileService.Base(fileManagementService, taskDataSource);
    }

    private final UserEntity creator;
    private final UserDto creatorDto;
    private final TaskEntity commonTask;
    private final TagEntity tag;
    private final FileData file;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
                .build();
        creatorDto = new UserDto(creator.getUsername());
        tag = new TagEntity("id", "tag", 1L);
        file = new FileData("id", "name", "type", "path");
        commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .file(file)
                .build();
    }

    @Test
    void shouldListFiles() {
        // given
        when(taskDataSource.findById(commonTask.getId())).thenReturn(commonTask);

        // when
        TaskFileListResponse actual = underTest.listFiles(commonTask.getId());

        // then
        assertEquals(Map.of(file.getId(), file.getName()), actual.getFiles());
    }

    @Test
    void shouldUpload() {
        // given
        when(taskDataSource.findById(commonTask.getId())).thenReturn(commonTask);
        MockMultipartFile fileToStore = new MockMultipartFile(file.getName(), new byte[1]);
        when(fileManagementService.store(commonTask, fileToStore)).thenReturn(file.getName());

        // when
        TaskFileUploadResponse actual = underTest.upload(new TaskFileUploadRequest(commonTask.getId(), fileToStore));

        // then
        assertEquals(file.getName(), actual.getMessage());
    }

    @Test
    void shouldDownload() {
        // given
        when(taskDataSource.findById(commonTask.getId())).thenReturn(commonTask);
        byte[] fileBytes = new byte[1];
        when(fileManagementService.getFileBytes(file)).thenReturn(fileBytes);

        // when
        TaskFileDownloadResponse actual = underTest.download(new TaskFileDownloadRequest(commonTask.getId(), file.getId()));

        // then
        assertEquals(fileBytes, actual.getFile());
        assertEquals(file.getType(), actual.getContentType());
    }

    @Test
    void shouldNotDownload_whenFileDoesNotExist() {
        // given
        when(taskDataSource.findById(commonTask.getId())).thenReturn(commonTask);

        // when
        ApiException actual = catchThrowableOfType(() ->
                underTest.download(new TaskFileDownloadRequest(commonTask.getId(), "not existed")), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.NOT_FOUND, "file not found..."), actual);
    }

    @Test
    void deleteTaskFiles() {
        // given
        // when
        underTest.deleteTaskFiles(commonTask.getId());

        // then
        verify(fileManagementService, times(1)).deleteFileDirectory(commonTask.getId());
    }
}