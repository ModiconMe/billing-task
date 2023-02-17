package io.modicon.taskapp.web.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record TaskDto(
        String id,
        String priorityType,
        String description,
        LocalDate createdAt,
        LocalDate finishDate,
        String tag,
        UserDto creator
) {
}
