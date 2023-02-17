package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataSourceTest {

    private UserDataSource underTest;

    @Mock
    private JpaUserRepository jpaUserRepository;

    @BeforeEach
    void setUp() {
        underTest = new UserDataSource.JpaUserDataSource(jpaUserRepository);
    }

    UserEntity user;

    {
        user = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
    }

    @Test
    void shouldFindById() {
        // given
        when(jpaUserRepository.findById(user.getUsername())).thenReturn(Optional.of(user));

        // when
        UserEntity expected = underTest.findById(user.getUsername());

        // then
        assertEquals(expected, user);
    }

    @Test
    void shouldNotFindById_whenDoesNotExist() {
        // given
        when(jpaUserRepository.findById(user.getUsername())).thenReturn(Optional.empty());

        // when
        ApiException exception = catchThrowableOfType(() -> underTest.findById(user.getUsername()), ApiException.class);

        // then
        assertEquals(exception, exception(HttpStatus.NOT_FOUND, "user with username [%s] not found", user.getUsername()));
    }

    @Test
    void shouldValidate_ifUserNotExist() {
        // given
        when(jpaUserRepository.existsById(user.getUsername())).thenReturn(false);

        // when
        // then
        assertDoesNotThrow(() -> underTest.validateNotExist(user.getUsername()));
    }

    @Test
    void shouldValidate_ifUserExist() {
        // given
        when(jpaUserRepository.existsById(user.getUsername())).thenReturn(true);

        // when
        ApiException exception = catchThrowableOfType(() -> underTest.validateNotExist(user.getUsername()), ApiException.class);

        // then
        assertEquals(exception, exception(HttpStatus.BAD_REQUEST, "user with username [%s] is already exist", user.getUsername()));
    }

    @Test
    void shouldSave() {
        // given
        // when
        underTest.save(user);

        // then
        verify(jpaUserRepository, times(1)).save(user);
    }
}