package io.modicon.taskapp.web.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record TaskDto(
        String priorityType,
        String description,
        LocalDate createdAt,
        LocalDate finishDate,
        Set<String> tags,
        UserDto creator
) {
}
