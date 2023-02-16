package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserDtoMapper implements Function<UserEntity, UserDto> {

    @Override
    public UserDto apply(UserEntity user) {
        return new UserDto(user.getUsername());
    }
}
