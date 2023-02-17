package io.modicon.taskapp.web.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Getter
public class TaskFileUploadRequest {
    private String taskName;
    private MultipartFile file;
}
