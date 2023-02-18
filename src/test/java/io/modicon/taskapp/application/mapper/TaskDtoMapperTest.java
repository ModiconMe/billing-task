package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskDtoMapperTest {

    private TaskDtoMapper underTest;

    @Mock
    private UserDtoMapper userDtoMapper;

    @BeforeEach
    void setUp() {
        underTest = new TaskDtoMapper(userDtoMapper);
    }

    @Test
    void shouldMap() {
        // given
        UserEntity creator = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
        UserDto userDto = new UserDto(creator.getUsername());

        when(userDtoMapper.apply(creator)).thenReturn(userDto);

        TaskEntity task = TaskEntity.builder()
                .id("taskid")
                .tag(new TagEntity("id", "name", 1L))
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .creator(creator)
                .build();

        // when
        TaskDto actual = underTest.apply(task);

        // then
        assertEquals(task.getId(), actual.id());
        assertEquals(task.getTag().getTagName(), actual.tag());
        assertEquals(userDto, actual.creator());
        assertEquals(task.getDescription(), actual.description());
        assertEquals(task.getPriorityType().name(), actual.priorityType());
        assertEquals(task.getFinishDate(), actual.finishDate());
        assertEquals(task.getCreatedAt(), actual.createdAt());
    }
}