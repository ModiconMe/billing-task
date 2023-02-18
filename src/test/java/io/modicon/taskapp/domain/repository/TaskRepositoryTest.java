package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private JpaTaskRepository taskRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private final UserEntity creator;
    private final TaskEntity urgentTask;
    private final TaskEntity importantTask;
    private final TaskEntity commonTask;
    private final TagEntity tag;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .role(ApplicationUserRole.USER)
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
        taskRepository.save(importantTask);
        userRepository.save(creator);

        Optional<TaskEntity> expected = taskRepository.findByIdAndCreator(importantTask.getId(), creator);

        assertTrue(expected.isPresent());
        assertEquals(expected.get(), importantTask);
    }

    @Test
    void shouldNotFindByIdAndCreator() {
        taskRepository.save(importantTask);
        userRepository.save(creator);

        Optional<TaskEntity> expected = taskRepository.findByIdAndCreator(commonTask.getId(), creator);

        assertTrue(expected.isEmpty());
    }

    @Test
    void shouldFindByTag_andSortByPriorityType() {
        // given
        taskRepository.saveAll(List.of(importantTask, commonTask, urgentTask));

        Optional<Field> fieldToSort = Arrays
                .stream(TaskEntity.class.getDeclaredFields())
                .filter(f -> f.getType().equals(PriorityType.class))
                .findFirst();
        assertTrue(fieldToSort.isPresent());

        // when
        List<TaskEntity> expected = taskRepository.findByTag(tag,
                PageRequest.of(0, 10, Sort.by(fieldToSort.get().getName())));

        // then
        assertEquals(expected.get(0), urgentTask);
        assertEquals(expected.get(1), importantTask);
        assertEquals(expected.get(2), commonTask);
    }

    @Test
    void shouldNotFindByTag_sorted() {
        // given
        Optional<Field> fieldToSort = Arrays
                .stream(TaskEntity.class.getDeclaredFields())
                .filter(f -> f.getType().equals(PriorityType.class))
                .findFirst();
        assertTrue(fieldToSort.isPresent());

        // when
        List<TaskEntity> expected = taskRepository.findByTag(tag,
                PageRequest.of(0, 10, Sort.by(fieldToSort.get().getName())));

        // then
        assertTrue(expected.isEmpty());
    }

    @Test
    void shouldFindByTag() {
        // given
        taskRepository.saveAll(List.of(importantTask, commonTask, urgentTask));

        // when
        List<TaskEntity> expected = taskRepository.findByTag(tag);

        // then
        assertEquals(expected.get(0), importantTask);
        assertEquals(expected.get(1), commonTask);
        assertEquals(expected.get(2), urgentTask);
    }

    @Test
    void shouldNotFindByTag() {
        // given
        // when
        List<TaskEntity> expected = taskRepository.findByTag(tag);

        // then
        assertTrue(expected.isEmpty());
    }
}
