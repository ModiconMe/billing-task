package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private TaskService underTest;

    @Mock
    private TaskDataSource.ReadAdmin readTaskDataSource;
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
//        underTest = new TaskService.Base(readTaskDataSource, writeTaskDataSource,
//                readTagDataSource, writeTagDataSource, taskFileService, taskDtoMapper);
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
//        underTest.create(new TaskCreateRequest())
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