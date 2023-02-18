package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private TaskService underTest;

    @Mock
    private TaskDataSource.ReadUser readUserTaskDataSource;
    @Mock
    private TaskDataSource.ReadAdmin readAdminTaskDataSource;
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
        underTest = new TaskService.Base(readUserTaskDataSource, readAdminTaskDataSource, writeTaskDataSource,
                readTagDataSource, writeTagDataSource, taskFileService, taskDtoMapper);
    }

    private final UserEntity creator;
    private final UserEntity admin;
    private final UserDto creatorDto;
    private final TaskEntity urgentTask;
    private final TaskEntity importantTask;
    private final TaskEntity commonTask;
    private final TagEntity tag;
    private final TagEntity tag1;
    private final TaskDto taskDto;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
                .build();
        admin = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.ADMIN)
                .build();
        creatorDto = new UserDto(creator.getUsername());
        tag = new TagEntity("id", "tag", 1L);
        tag1 = new TagEntity("id1", "tag1", 1L);
        importantTask = TaskEntity.builder()
                .id("taskid1")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.IMPORTANT)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .creator(creator)
                .build();
        commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();
        urgentTask = TaskEntity.builder()
                .id("taskid3")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.URGENT)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .creator(creator)
                .build();
        taskDto = TaskDto.builder()
                .id(commonTask.getId())
                .description(commonTask.getDescription())
                .finishDate(commonTask.getFinishDate())
                .priorityType(commonTask.getPriorityType().name())
                .tag(commonTask.getTag().getTagName())
                .creator(creatorDto)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void shouldCreate() {
        // given
        when(writeTaskDataSource.save(commonTask)).thenReturn(commonTask);
        when(readTagDataSource.supplyTag(commonTask.getTag().getTagName())).thenReturn(tag);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskCreateResponse actual = underTest.create(new TaskCreateRequest(creator,
                commonTask.getId(),
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName()));

        // then
        assertEquals(taskDto, actual.getTask());
    }

    @Test
    void shouldNotCreate_whenUserAlreadyMaintainTaskWithThatId() {
        // given
        ApiException expected = exception(HttpStatus.BAD_REQUEST, "task with that identifier already exist");
        doThrow(expected).when(readUserTaskDataSource).validateExistByIdAndCreator(commonTask.getId(), creator);

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.create(new TaskCreateRequest(creator,
                commonTask.getId(),
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName())), ApiException.class);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotCreate_whenWrongFinishDateProvided() {
        // given
        // when
        ApiException actual = catchThrowableOfType(() -> underTest.create(new TaskCreateRequest(creator,
                commonTask.getId(),
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                LocalDate.now().minusDays(1),
                commonTask.getTag().getTagName())), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date"), actual);
    }

    @Test
    void shouldNotCreate_whenWrongPriorityTypeProvided() {
        // given
        when(readTagDataSource.supplyTag(commonTask.getTag().getTagName())).thenReturn(tag);

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.create(new TaskCreateRequest(creator,
                commonTask.getId(),
                "wrong priority type",
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName())), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date"), actual);
    }

    @Test
    void shouldUpdate() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);
        when(readTagDataSource.tryToFindTag(commonTask.getTag().getTagName())).thenReturn(Optional.of(tag));
        when(writeTaskDataSource.save(commonTask)).thenReturn(commonTask);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskUpdateResponse actual = underTest.update(commonTask.getId(), new TaskUpdateRequest(creator,
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName()));

        // then
        assertEquals(taskDto, actual.getTask());
    }

    @Test
    void shouldUpdate_whenDifferentTagProvided() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);
        when(readTagDataSource.tryToFindTag(commonTask.getTag().getTagName())).thenReturn(Optional.of(tag1));
        when(writeTaskDataSource.save(commonTask)).thenReturn(commonTask);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskUpdateResponse actual = underTest.update(commonTask.getId(), new TaskUpdateRequest(creator,
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName()));

        // then
        assertEquals(taskDto, actual.getTask());
    }

    @Test
    void shouldUpdate_whenDifferentNotExistedTagProvided() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);
        when(readTagDataSource.tryToFindTag(commonTask.getTag().getTagName())).thenReturn(Optional.empty());
        when(writeTaskDataSource.save(commonTask)).thenReturn(commonTask);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskUpdateResponse actual = underTest.update(commonTask.getId(), new TaskUpdateRequest(creator,
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName()));

        // then
        assertEquals(taskDto, actual.getTask());
    }

    @Test
    void shouldUpdateByAdmin() {
        // given
        when(readAdminTaskDataSource.findById(commonTask.getId())).thenReturn(commonTask);
        when(readTagDataSource.tryToFindTag(commonTask.getTag().getTagName())).thenReturn(Optional.of(tag));
        when(writeTaskDataSource.save(commonTask)).thenReturn(commonTask);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskUpdateResponse actual = underTest.update(commonTask.getId(), new TaskUpdateRequest(admin,
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName()));

        // then
        assertEquals(taskDto, actual.getTask());
    }

    @Test
    void shouldNotUpdate_whenWrongDateProvided() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.update(commonTask.getId(), new TaskUpdateRequest(creator,
                commonTask.getPriorityType().name(),
                commonTask.getDescription(),
                LocalDate.now().minusDays(1),
                commonTask.getTag().getTagName())), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "finish date cannot be earlier than today's date"), actual);
    }

    @Test
    void shouldNotUpdate_whenWrongPriorityTypeProvided() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);
        when(readTagDataSource.tryToFindTag(commonTask.getTag().getTagName())).thenReturn(Optional.of(tag));

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.update(commonTask.getId(), new TaskUpdateRequest(creator,
                "wrong priority type",
                commonTask.getDescription(),
                commonTask.getFinishDate(),
                commonTask.getTag().getTagName())), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST,
                "wrong priority type for task, it only supports %s", Arrays.toString(PriorityType.values())), actual);
    }


    @Test
    void shouldDelete() {
        // given
        when(readUserTaskDataSource.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(commonTask);

        // when
        underTest.delete(commonTask.getId(), creator);

        // then
        verify(writeTaskDataSource).delete(commonTask);
        verify(taskFileService).deleteTaskFiles(commonTask.getId());
    }

    @Test
    void shouldDeleteByAdmin() {
        // given
        when(readAdminTaskDataSource.findById(commonTask.getId())).thenReturn(commonTask);

        // when
        underTest.delete(commonTask.getId(), admin);

        // then
        verify(writeTaskDataSource).delete(commonTask);
        verify(taskFileService).deleteTaskFiles(commonTask.getId());
    }

    @Test
    void shouldGetUserCurrentTask() {
        // given
        when(readUserTaskDataSource.findCurrentTask(commonTask.getFinishDate(), "0", "1", creator)).thenReturn(List.of(commonTask));
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskGetByDateResponse actual = underTest.get(commonTask.getFinishDate().toString(), "0", "1", creator);

        // then
        assertEquals(List.of(taskDto), actual.getTasks());
    }

    @Test
    void shouldGetCurrentTask() {
        // given
        when(readAdminTaskDataSource.findCurrentTask(commonTask.getFinishDate(), "0", "1")).thenReturn(List.of(commonTask));
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskGetByDateResponse actual = underTest.get(commonTask.getFinishDate().toString(), "0", "1", admin);

        // then
        assertEquals(List.of(taskDto), actual.getTasks());
    }

    @Test
    void shouldNotGetCurrentTask_whenWrongDateFormatProvided() {
        // given
        String wrongDate = "2002-13-13";

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.get(wrongDate, "0", "1", admin), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "wrong date format, please provide date like [yyyy-mm-dd]"), actual);
    }

    @Test
    void shouldGetUserAllTasks() {
        // given
        when(readUserTaskDataSource.findAllUserTasks("0", "1", creator)).thenReturn(List.of(commonTask));
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskGetGroupByPriorityType actual = underTest.get("0", "1", creator);

        // then
        assertEquals(Map.of(commonTask.getPriorityType(), List.of(taskDto)), actual.getTasks());
    }

    @Test
    void shouldGetAllTasks() {
        // given
        when(readAdminTaskDataSource.findAllTasks("0", "1")).thenReturn(List.of(commonTask));
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TaskGetGroupByPriorityType actual = underTest.get("0", "1", admin);

        // then
        assertEquals(Map.of(commonTask.getPriorityType(), List.of(taskDto)), actual.getTasks());
    }
}