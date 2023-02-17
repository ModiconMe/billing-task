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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDataSourceTest {

    private TaskDataSource underTest;

    @Mock
    private JpaTaskRepository jpaTaskRepository;

    @BeforeEach
    void setUp() {
        underTest = new TaskDataSource.JpaTaskDataSource(jpaTaskRepository);
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
    void shouldFindByIdAndCreator() {
        // given
        when(jpaTaskRepository.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(Optional.of(commonTask));

        // when
        TaskEntity actual = underTest.findByIdAndCreator(commonTask.getId(), creator);

        // then
        assertEquals(commonTask, actual);
    }

    @Test
    void shouldNotFindByIdAndCreator() {
        // given
        when(jpaTaskRepository.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(Optional.empty());

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.findByIdAndCreator(commonTask.getId(), creator), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.NOT_FOUND, "task not found..."), actual);
    }

    @Test
    void shouldValidateExistByIdAndCreator_ifTaskExist() {
        // given
        when(jpaTaskRepository.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(Optional.of(commonTask));

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.validateExistByIdAndCreator(commonTask.getId(), creator), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.BAD_REQUEST, "user with username [%s] is already exist", creator.getUsername()), actual);
    }

    @Test
    void shouldValidateExistByIdAndCreator_ifTaskIsNotExist() {
        // given
        when(jpaTaskRepository.findByIdAndCreator(commonTask.getId(), creator)).thenReturn(Optional.empty());

        // when
        // then
        assertDoesNotThrow(() -> underTest.validateExistByIdAndCreator(commonTask.getId(), creator));
    }

    @Test
    void shouldFindByTag() {
        // given
        when(jpaTaskRepository.findByTag(tag)).thenReturn(List.of(commonTask));

        // when
        List<TaskEntity> actual = underTest.findByTag(tag);

        // then
        assertEquals(List.of(commonTask), actual);
    }

    @Test
    void shouldFindByTag_sorted() {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        when(jpaTaskRepository.findByTag(tag, pageable)).thenReturn(List.of(commonTask));

        // when
        List<TaskEntity> actual = underTest.findByTag(tag, "0", "1");

        // then
        assertEquals(List.of(commonTask), actual);
    }

    @Test
    void findByFinishDateGreaterThanEqual() {
        // given
        Optional<Field> fieldToSort = Arrays
                .stream(TaskEntity.class.getDeclaredFields())
                .filter(f -> f.getType().equals(PriorityType.class))
                .findFirst();

        LocalDate date = LocalDate.now();
        PageRequest pageable = PageRequest.of(0, 1, Sort.by(fieldToSort.get().getName()));
        when(jpaTaskRepository.findByFinishDateGreaterThanEqual(date, pageable)).thenReturn(List.of(commonTask));

        // when
        List<TaskEntity> actual = underTest.findByFinishDateGreaterThanEqual(date, "0", "1");

        // then
        assertEquals(List.of(commonTask), actual);
    }

    @Test
    void save() {
        // given
        // when
        underTest.save(commonTask);

        // then
        verify(jpaTaskRepository, times(1)).save(commonTask);
    }

    @Test
    void delete() {
        // given
        // when
        underTest.delete(commonTask);

        // then
        verify(jpaTaskRepository, times(1)).delete(commonTask);
    }

    @Test
    void findAll() {
        // given
        Optional<Field> fieldToSort = Arrays
                .stream(TaskEntity.class.getDeclaredFields())
                .filter(f -> f.getType().equals(PriorityType.class))
                .findFirst();

        PageRequest pageable = PageRequest.of(0, 1, Sort.by(fieldToSort.get().getName()));
        when(jpaTaskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(commonTask)));

        // when
        List<TaskEntity> actual = underTest.findAll("0", "1");

        // then
        assertEquals(List.of(commonTask), actual);
    }
}