package io.modicon.taskapp.web.interaction;

import io.modicon.taskapp.web.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserLoginResponse {
    private UserDto user;
    private String token;
}
