package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.web.dto.TaskDto;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TaskDtoMapper implements Function<TaskEntity, TaskDto> {

    @Override
    public TaskDto apply(TaskEntity task) {
        return TaskDto.builder()
                .description(task.getDescription())
                .priorityType(task.getPriorityType().name())
                .tags(task.getTags().stream().map(TagEntity::getTagName).collect(Collectors.toSet()))
                .createdAt(task.getCreatedAt())
                .finishDate(task.getFinishDate())
                .build();
    }
}
