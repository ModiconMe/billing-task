package io.modicon.taskapp.web.interaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.modicon.taskapp.domain.model.UserEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@AllArgsConstructor
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("tag")
public class TagUpdateRequest {

    @JsonIgnore
    @With
    private UserEntity user;
    @JsonIgnore
    @With
    private String updatedTag;

    @NotEmpty(message = "tag name must be not empty")
    private String tag;
}
