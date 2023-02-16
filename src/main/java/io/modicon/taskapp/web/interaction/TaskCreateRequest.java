package io.modicon.taskapp.web.interaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("task")
public class TaskCreateRequest {

    @With
    @JsonIgnore
    private UserEntity user;

    @NotEmpty(message = "task identifier must be not empty")
    private String id;
    @NotEmpty(message = "priority type must be not empty")
    private String priorityType;
    @NotEmpty(message = "description must be not empty")
    private String description;
    @NotEmpty(message = "finish date type must be not empty")
    private LocalDate finishDate;
    private Set<String> tags;
}
