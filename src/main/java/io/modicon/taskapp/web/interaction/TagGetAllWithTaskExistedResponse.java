package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.TagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TagGetAllWithTaskExistedResponse {
    List<TagDto> tags;
}
