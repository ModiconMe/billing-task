package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.web.dto.TagDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagDtoMapperTest {

    private TagDtoMapper underTest = new TagDtoMapper();

    @Test
    void shouldMap() {
        // given
        TagEntity tag = new TagEntity("id", "name", 1L);

        // when
        TagDto actual = underTest.apply(tag);

        // then
        assertEquals(tag.getTagName(), actual.tagName());
        assertEquals(tag.getTaskCount(), actual.taskCount());
    }
}