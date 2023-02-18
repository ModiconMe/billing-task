package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.UserDtoMapper;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.UserDataSource;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.infrastructure.security.jwt.JwtConfig;
import io.modicon.taskapp.infrastructure.security.jwt.JwtGeneration;
import io.modicon.taskapp.web.dto.UserDto;
import io.modicon.taskapp.web.interaction.UserLoginRequest;
import io.modicon.taskapp.web.interaction.UserLoginResponse;
import io.modicon.taskapp.web.interaction.UserRegisterRequest;
import io.modicon.taskapp.web.interaction.UserRegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    private UserManagementService underTest;

    @Mock
    private UserDataSource userDataSource;
    @Mock
    private JwtGeneration jwtGeneration;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDtoMapper userDtoMapper;
    @Mock
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        underTest = new UserManagementService.Base(userDataSource, passwordEncoder, userDtoMapper, jwtGeneration, jwtConfig);
    }

    private UserEntity user;
    private UserDto userDto;

    {
        user = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
        userDto = new UserDto(user.getUsername());
    }

    @Test
    void shouldRegister() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(user.getUsername(), user.getPassword());

        // when
        UserRegisterResponse actual = underTest.register(request);

        // then
        assertEquals(actual, new UserRegisterResponse());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userDataSource, times(1)).validateNotExist(user.getUsername());
        verify(userDataSource, times(1)).save(user);
    }

    @Test
    void shouldLogin() {
        // given
        UserLoginRequest request = new UserLoginRequest(user.getUsername(), user.getPassword());
        when(userDtoMapper.apply(user)).thenReturn(userDto);
        when(userDataSource.findById(user.getUsername())).thenReturn(user);
        String token = "token";
        when(jwtGeneration.generateAccessToken(user)).thenReturn(token);
        when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);

        // when
        UserLoginResponse actual = underTest.login(request);

        // then
        assertEquals(new UserLoginResponse(userDto, token), actual);
    }

    @Test
    void shouldNotLogin_whenPasswordIsIncorrect() {
        // given
        UserLoginRequest request = new UserLoginRequest(user.getUsername(), user.getPassword());
        when(userDataSource.findById(user.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(false);

        // when
        ApiException actual = catchThrowableOfType(() -> underTest.login(request), ApiException.class);

        // then
        assertEquals(exception(HttpStatus.FORBIDDEN, "wrong password"), actual);
    }
}