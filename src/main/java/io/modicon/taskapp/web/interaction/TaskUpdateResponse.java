package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskUpdateResponse {
    private TaskDto task;
}
