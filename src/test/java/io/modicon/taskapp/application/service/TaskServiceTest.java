package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.web.interaction.TaskCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private TaskService underTest;

    @Mock
    private TaskDataSource.Read readTaskDataSource;
    @Mock
    private TaskDataSource.Write writeTaskDataSource;
    @Mock
    private TagDataSource.Read readTagDataSource;
    @Mock
    private TagDataSource.Write writeTagDataSource;
    @Mock
    private TaskFileService taskFileService;
    @Mock
    private TaskDtoMapper taskDtoMapper;

    @BeforeEach
    void setUp() {
        underTest = new TaskService.Base(readTaskDataSource, writeTaskDataSource,
                readTagDataSource, writeTagDataSource, taskFileService, taskDtoMapper);
    }

    private UserEntity user;

    {
        user = UserEntity.builder()
                .build();
    }

    @Test
    void shouldCreate() {
        // given

        // when
        underTest.create(new TaskCreateRequest())
        // then
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void get() {
    }

    @Test
    void testGet() {
    }
}