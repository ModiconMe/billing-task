package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
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

    @Test
    void shouldFindByTagContaining_andSortByPriorityType() {
        // given
        TagEntity tag1 = new TagEntity("tag1", 0L);
        TagEntity tag2 = new TagEntity("tag2", 1L);
        TagEntity tag3 = new TagEntity("tag3", 0L);
        TaskEntity importantTask = TaskEntity.builder()
                .id("taskid1")
                .tag(tag1)
                .tag(tag2)
                .tag(tag3)
                .description("description")
                .priorityType(PriorityType.IMPORTANT)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();
        TaskEntity commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(tag1)
                .tag(tag2)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();
        TaskEntity urgentTask = TaskEntity.builder()
                .id("taskid3")
                .tag(tag2)
                .description("description")
                .priorityType(PriorityType.URGENT)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();

        taskRepository.saveAll(List.of(importantTask, commonTask, urgentTask));

        Optional<Field> fieldToSort = Arrays
                .stream(TaskEntity.class.getDeclaredFields())
                .filter(f -> f.getType().equals(PriorityType.class))
                .findFirst();
        assertTrue(fieldToSort.isPresent());

        // when
        List<TaskEntity> expected = taskRepository.findByTagsContaining(tag2,
                PageRequest.of(0, 10, Sort.by(fieldToSort.get().getName())));

        // then
        assertEquals(expected.get(0), urgentTask);
        assertEquals(expected.get(1), importantTask);
        assertEquals(expected.get(2), commonTask);
    }
}
