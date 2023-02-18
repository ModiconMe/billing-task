package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private JpaTagRepository tagRepository;

    @Test
    void shouldGetTagWhichTaskCounterGreaterThan0() {
        // given
        TagEntity tag1 = new TagEntity("id1","tag1", 0L);
        TagEntity tag2 = new TagEntity("id2","tag2", 2L);
        TagEntity tag3 = new TagEntity("id3","tag3", 100L);
        TagEntity tag4 = new TagEntity("id4","tag4", -100L);

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

    @Test
    void shouldFindByTagName() {
        // given
        TagEntity tag1 = new TagEntity("id1","tag1", 0L);
        TagEntity tag2 = new TagEntity("id2","tag2", 2L);
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        // when
        Optional<TagEntity> actual = tagRepository.findByTagName(tag1.getTagName());

        // then
        assertTrue(actual.isPresent());
        assertEquals(tag1, actual.get());
    }

    @Test
    void shouldNotFindByTagName() {
        // given
        TagEntity tag1 = new TagEntity("id1","tag1", 0L);
        tagRepository.save(tag1);

        // when
        Optional<TagEntity> actual = tagRepository.findByTagName("wrongName");

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldExistsByTagName() {
        // given
        TagEntity tag1 = new TagEntity("id1","tag1", 0L);
        TagEntity tag2 = new TagEntity("id2","tag2", 2L);
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        // when
        boolean actual = tagRepository.existsByTagName(tag1.getTagName());

        // then
        assertTrue(actual);
    }

    @Test
    void shouldNotExistsByTagName() {
        // given
        TagEntity tag1 = new TagEntity("id1","tag1", 0L);
        TagEntity tag2 = new TagEntity("id2","tag2", 2L);
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        // when
        boolean actual = tagRepository.existsByTagName("wrong name");

        // then
        assertFalse(actual);
    }
}