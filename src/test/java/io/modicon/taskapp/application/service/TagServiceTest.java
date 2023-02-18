package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TagDtoMapper;
import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.web.dto.TagDto;
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
import java.util.List;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    private TagService underTest;

    @Mock
    private TagDataSource.Read readTagDataSource;
    @Mock
    private TagDataSource.Write writeTagDataSource;
    @Mock
    private TaskDataSource.ReadUser readUserTaskDataSource;
    @Mock
    private TaskDataSource.Write writeTaskDataSource;
    @Mock
    private TaskFileService taskFileService;
    @Mock
    private TaskDtoMapper taskDtoMapper;
    @Mock
    private TagDtoMapper tagDtoMapper;

    @BeforeEach
    void setUp() {
        underTest = new TagService.Base(readTagDataSource, writeTagDataSource, readUserTaskDataSource,
                writeTaskDataSource, taskFileService, taskDtoMapper, tagDtoMapper);
    }

    private final UserEntity creator;
    private final UserEntity admin;
    private final UserDto creatorDto;
    private final TaskEntity urgentTask;
    private final TaskEntity importantTask;
    private final TaskEntity commonTask;
    private final TagEntity tag;
    private final TagDto tagDto;
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
        tag = new TagEntity("tag", 1L);
        tag1 = new TagEntity("tag1", 1L);
        tagDto = new TagDto(tag.getTagName(), tag.getTaskCount());
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
    void shouldGetTagWithTasks() {
        // given
        when(readTagDataSource.findById(tag.getTagName())).thenReturn(tag);
        when(readUserTaskDataSource.findByTag(tag, "0", "1")).thenReturn(List.of(commonTask));
        when(tagDtoMapper.apply(tag)).thenReturn(tagDto);
        when(taskDtoMapper.apply(commonTask)).thenReturn(taskDto);

        // when
        TagGetByIdWithTaskResponse actual = underTest.getTagWithTasks(tag.getTagName(), "0", "1");

        // then
        TagGetByIdWithTaskResponse expected = new TagGetByIdWithTaskResponse(tagDto, List.of(taskDto));
        assertEquals(expected.getTasks(), actual.getTasks());
        assertEquals(expected.getTag(), actual.getTag());
    }

    @Test
    void shouldGetAllTagsWithExistedTasks() {
        // given
        when(readTagDataSource.findTagWithTasks(0L)).thenReturn(List.of(tag));
        when(tagDtoMapper.apply(tag)).thenReturn(tagDto);

        // when
        TagGetAllWithTaskExistedResponse actual = underTest.getAllTagsWithExistedTasks();

        // then
        TagGetAllWithTaskExistedResponse expected = new TagGetAllWithTaskExistedResponse(List.of(tagDto));
        assertEquals(expected.getTags(), actual.getTags());
    }

    @Test
    void shouldCreate() {
        // given
        when(tagDtoMapper.apply(tag)).thenReturn(tagDto);

        // when
        TagCreateResponse actual = underTest.create(new TagCreateRequest(tag.getTagName()));

        // then
        TagCreateResponse expected = new TagCreateResponse(tagDto);
        assertEquals(expected.getTag(), actual.getTag());
        verify(readTagDataSource).validateNotExist(tag.getTagName());
        verify(writeTagDataSource).save(tag);
    }

    @Test
    void shouldUpdateByAdmin() {
        // given
        when(tagDtoMapper.apply(tag)).thenReturn(tagDto);
        when(readTagDataSource.findById(tag.getTagName())).thenReturn(tag);

        // when
        TagUpdateResponse actual = underTest.update(new TagUpdateRequest(admin, tag.getTagName(), tag1.getTagName()));

        // then
        TagUpdateResponse expected = new TagUpdateResponse(tagDto);
        assertEquals(expected.getTag(), actual.getTag());
        verify(readTagDataSource).validateNotExist(tag.getTagName());
        verify(writeTagDataSource).save(tag);
    }

    @Test
    void shouldNotUpdateByUser() {
        // given
        // when
        ApiException actual = catchThrowableOfType(() -> underTest.update(new TagUpdateRequest(creator, tag.getTagName(), tag1.getTagName())),
                ApiException.class);

        // then
        assertEquals(exception(HttpStatus.UNAUTHORIZED, "you are not allow to do this operation"), actual);
    }

    @Test
    void shouldDeleteByAdmin() {
        // given
        when(tagDtoMapper.apply(tag)).thenReturn(tagDto);
        when(readTagDataSource.findById(tag.getTagName())).thenReturn(tag);
        when(readUserTaskDataSource.findByTag(tag)).thenReturn(List.of(commonTask));

        // when
        TagDeleteResponse actual = underTest.delete(tag.getTagName(), admin);

        // then
        TagDeleteResponse expected = new TagDeleteResponse(tagDto);
        assertEquals(expected.getTag(), actual.getTag());
        verify(taskFileService).deleteTaskFiles(commonTask.getId());
        verify(writeTagDataSource).delete(tag);
        verify(writeTaskDataSource).deleteAll(List.of(commonTask));
    }

    @Test
    void shouldNotDeleteByUser() {
        // given
        // when
        ApiException actual = catchThrowableOfType(() -> underTest.delete(tag.getTagName(), creator), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.UNAUTHORIZED, "you are not allow to do this operation"), actual);

    }
}