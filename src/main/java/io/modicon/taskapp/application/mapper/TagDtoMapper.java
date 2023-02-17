package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.web.dto.TagDto;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class TagDtoMapper implements Function<TagEntity, TagDto> {

    @Override
    public TagDto apply(TagEntity tag) {
        return new TagDto(tag.getTagName(), tag.getTaskCount());
    }
}
