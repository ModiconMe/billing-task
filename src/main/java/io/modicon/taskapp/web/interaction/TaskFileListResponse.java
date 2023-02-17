package io.modicon.taskapp.web.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class TaskFileListResponse {
    private Map<String, String> files;
}
