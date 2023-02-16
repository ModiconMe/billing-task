package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskGetByDateResponse {
    private List<TaskDto> tasks;
}
