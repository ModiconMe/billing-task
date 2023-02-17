package io.modicon.taskapp.web.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaskFileDownloadRequest {
    private String taskName;
    private String fileName;
}
