package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagDataSourceTest {

    private TagDataSource.Write writeUnderTest;
    private TagDataSource.Read readUnderTest;

    @Mock
    private JpaTagRepository jpaTagRepository;

    @BeforeEach
    void setUp() {
        writeUnderTest = new TagDataSource.JpaWriteTagDataSource(jpaTagRepository);
        readUnderTest = new TagDataSource.JpaReadTagDataSource(jpaTagRepository);
    }

    private final UserEntity creator;
    private final TaskEntity urgentTask;
    private final TaskEntity importantTask;
    private final TaskEntity commonTask;
    private final TagEntity tag;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
        tag = new TagEntity("tag", 1L);
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
    }

    @Test
    void shouldFindTagsWithTasks() {
        // given
        Long taskCount = 1L;
        List<TagEntity> expected = List.of(tag);
        when(jpaTagRepository.findAllByTaskCountIsGreaterThan(taskCount)).thenReturn(expected);

        // when
        List<TagEntity> actual = readUnderTest.findTagWithTasks(taskCount);

        // then
        assertEquals(expected, actual);
    }
    @Test
    void shouldFindTagById() {
        // given
        when(jpaTagRepository.findById(tag.getTagName())).thenReturn(Optional.of(tag));

        // when
        TagEntity actual = readUnderTest.findById(tag.getTagName());

        // then
        assertEquals(tag, actual);
    }

    @Test
    void shouldNotFindTagById_whenTaskDoesNotExist() {
        // given
        when(jpaTagRepository.findById(tag.getTagName())).thenReturn(Optional.empty());

        // when
        ApiException actual = catchThrowableOfType(() -> readUnderTest.findById(tag.getTagName()), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.NOT_FOUND, "tag [%s] not found", tag.getTagName()), actual);
    }

    @Test
    void shouldValidateTaskNotExist_whenTaskNotExist() {
        // given
        when(jpaTagRepository.existsById(tag.getTagName())).thenReturn(false);

        // when
        // then
        assertDoesNotThrow(() -> readUnderTest.validateNotExist(tag.getTagName()));
    }

    @Test
    void shouldValidateTaskNotExist_whenTaskExist() {
        // given
        when(jpaTagRepository.existsById(tag.getTagName())).thenReturn(true);

        // when
        ApiException actual = catchThrowableOfType(() -> readUnderTest.validateNotExist(tag.getTagName()), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "tag [%s] already exist", tag.getTagName()), actual);
    }

    @Test
    void shouldSave() {
        // given
        // when
        writeUnderTest.save(tag);

        // then
        verify(jpaTagRepository, times(1)).save(tag);
    }

    @Test
    void shouldDelete() {
        // given
        // when
        writeUnderTest.delete(tag);

        // then
        verify(jpaTagRepository, times(1)).delete(tag);
    }
}