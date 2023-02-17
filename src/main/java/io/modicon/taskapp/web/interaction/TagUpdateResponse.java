package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.TagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TagUpdateResponse {
    private TagDto tag;
}
