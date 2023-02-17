package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldFindByTagContaining() {
        // given
        TagEntity tag1 = new TagEntity("tag1", 0L);
        TagEntity tag2 = new TagEntity("tag2", 1L);
        TagEntity tag3 = new TagEntity("tag3", 0L);
        TaskEntity task = TaskEntity.builder()
                .id("taskid")
                .tag(tag1)
                .tag(tag2)
                .tag(tag3)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();

        taskRepository.save(task);

        // when
        List<TaskEntity> expected = taskRepository.findByTagsContaining(tag2);

        // then
        assertTrue(expected.contains(task));
    }
}
