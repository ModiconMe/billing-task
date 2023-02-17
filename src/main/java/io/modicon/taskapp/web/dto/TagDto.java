package io.modicon.taskapp.web.dto;

public record TagDto(
        String tagName,
        Long taskCount
) {
}
