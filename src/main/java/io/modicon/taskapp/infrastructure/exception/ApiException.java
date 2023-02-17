package io.modicon.taskapp.infrastructure.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static java.lang.String.format;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiException exception(HttpStatus status, String message, Object... args) {
        return new ApiException(status, format(message, args));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiException exception = (ApiException) o;
        return status == exception.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}

