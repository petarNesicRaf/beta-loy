package com.beta.loyalty.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    record ApiError(String error, int status, OffsetDateTime timestamp) {}
    record ValidationError(String error, int status, OffsetDateTime timestamp, Map<String, String> fields) {}

    // 400 — invalid input (manual throws)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException ex) {
        return new ApiError(ex.getMessage(), 400, OffsetDateTime.now());
    }

    // 400 — @Valid on @RequestBody failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationError validationFailed(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        return new ValidationError("Validation failed", 400, OffsetDateTime.now(), fields);
    }

    // 400 — @Validated on path/query params failed
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError constraintViolation(ConstraintViolationException ex) {
        return new ApiError(ex.getMessage(), 400, OffsetDateTime.now());
    }

    // 401 — unauthenticated
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError unauthorized(UnauthorizedException ex) {
        return new ApiError(ex.getMessage(), 401, OffsetDateTime.now());
    }

    // 403 — authenticated but not permitted (e.g. staff not assigned to venue)
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(ForbiddenException ex) {
        return new ApiError(ex.getMessage(), 403, OffsetDateTime.now());
    }

    // 404 — resource not found
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NotFoundException ex) {
        return new ApiError(ex.getMessage(), 404, OffsetDateTime.now());
    }

    // 409 — business rule conflict (insufficient points, reward inactive, friendship blocked, etc.)
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(ConflictException ex) {
        return new ApiError(ex.getMessage(), 409, OffsetDateTime.now());
    }

    // 500 — unexpected error; log full trace internally, return generic message to client
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError unexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ApiError("An unexpected error occurred", 500, OffsetDateTime.now());
    }
}
