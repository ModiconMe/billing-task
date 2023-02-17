package io.modicon.taskapp.web.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaskFileDownloadResponse {
    private byte[] file;
    private String contentType;
}
