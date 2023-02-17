package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.web.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class TaskDtoMapper implements Function<TaskEntity, TaskDto> {

    private final UserDtoMapper userDtoMapper;

    @Override
    public TaskDto apply(TaskEntity task) {
        return TaskDto.builder()
                .id(task.getId())
                .description(task.getDescription())
                .priorityType(task.getPriorityType().name())
                .tag(task.getTag().getTagName())
                .createdAt(task.getCreatedAt())
                .finishDate(task.getFinishDate())
                .creator(userDtoMapper.apply(task.getCreator()))
                .build();
    }
}
