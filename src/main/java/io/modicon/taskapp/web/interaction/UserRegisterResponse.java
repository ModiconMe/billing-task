package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {
    private UserDto user;
}
