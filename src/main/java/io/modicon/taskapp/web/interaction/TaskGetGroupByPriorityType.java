package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.web.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskGetGroupByPriorityType {
    Map<PriorityType, List<TaskDto>> tasks;
}
