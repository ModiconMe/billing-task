package io.modicon.taskapp.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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

}

