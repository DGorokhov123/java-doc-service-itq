package ru.dgorokhov.docservice.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<FieldValidationError> fieldErrors = new ArrayList<>();

    public static ApiError of(HttpStatus status, String message) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(LocalDateTime.now());
        apiError.setStatus(status.value());
        apiError.setError(status.getReasonPhrase());
        apiError.setMessage(message);
        return apiError;
    }

    public static ApiError of(HttpStatus status, String message, List<FieldError> fieldErrors) {
        List<FieldValidationError> fieldValidationErrors = fieldErrors.stream()
                .map(fe -> {
                    FieldValidationError fve = new FieldValidationError();
                    fve.setField(fe.getField());
                    fve.setMessage(fe.getDefaultMessage());
                    return fve;
                })
                .toList();

        ApiError apiError = new ApiError();
        apiError.setTimestamp(LocalDateTime.now());
        apiError.setStatus(status.value());
        apiError.setError(status.getReasonPhrase());
        apiError.setMessage(message);
        apiError.setFieldErrors(fieldValidationErrors);
        return apiError;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FieldValidationError {
        private String field;
        private String message;
    }

}
