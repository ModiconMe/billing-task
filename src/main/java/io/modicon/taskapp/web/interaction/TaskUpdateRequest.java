package io.modicon.taskapp.web.interaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.modicon.taskapp.domain.model.UserEntity;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Set;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("task")
public class TaskUpdateRequest {

    @JsonIgnore
    @With
    private UserEntity user;

    private String priorityType;
    private String description;
    private LocalDate finishDate;
    private String tag;
}
