package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void shouldGetTagWhichTaskCounterGreaterThan0() {
        // given
        TagEntity tag1 = new TagEntity("tag1", 0L);
        TagEntity tag2 = new TagEntity("tag2", 2L);
        TagEntity tag3 = new TagEntity("tag3", 100L);
        TagEntity tag4 = new TagEntity("tag4", -100L);

        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4));

        // when
        List<TagEntity> result = tagRepository.findAllByTaskCountIsGreaterThan(0L);

        // then
        assertFalse(result.isEmpty());
        assertFalse(result.contains(tag1));
        assertTrue(result.contains(tag2));
        assertTrue(result.contains(tag3));
        assertFalse(result.contains(tag4));
    }
}