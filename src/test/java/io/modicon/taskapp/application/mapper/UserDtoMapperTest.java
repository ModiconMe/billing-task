package io.modicon.taskapp.application.mapper;

import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.dto.UserDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoMapperTest {

    private UserDtoMapper underTest = new UserDtoMapper();

    @Test
    void shouldMap() {
        // given
        UserEntity user = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
        UserDto expected = new UserDto(user.getUsername());

        // when
        UserDto actual = underTest.apply(user);

        // then
        assertEquals(expected, actual);
    }
}