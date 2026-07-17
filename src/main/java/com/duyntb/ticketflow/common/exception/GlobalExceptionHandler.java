package com.duyntb.ticketflow.common.exception;

import com.duyntb.ticketflow.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------------
    // 1. VALIDATION (@NotBlank, @Min,...)
    // ---------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");

        return ApiResponse.error(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleJsonParseException(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath()
                    .stream()
                    .findFirst()
                    .map(JacksonException.Reference::getPropertyName)
                    .orElse("field");

            return ApiResponse.error(fieldName + " has invalid format");
        }

        return ApiResponse.error("Invalid request body");
    }

    // ---------------------------------------------------------------------
    // 2. BUSINESS (business exception)
    // ---------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ApiResponse.error(exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleBadRequestException(BadRequestException exception) {
        return ApiResponse.error(exception.getMessage());
    }

    // ---------------------------------------------------------------------
    // 3. AUTHENTICATION / AUTHORIZATION
    // ---------------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleBadCredentialsException(BadCredentialsException exception) {
        return ApiResponse.error("Invalid email or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleAccessDeniedException(AccessDeniedException exception) {
        return ApiResponse.error("You do not have permission to perform this action");
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ApiResponse<Object> handleLockedException(LockedException exception) {
        return ApiResponse.error("Account has been locked");
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleDisabledException(DisabledException exception) {
        return ApiResponse.error("Account is disabled. Please verify your email before signing in.");
    }

    // ---------------------------------------------------------------------
    // 4. CATCH-ALL
    // ---------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception exception) {
        // Log fully on the server for debugging, but DO NOT return the raw message to the client
        log.error("Unhandled exception occurred", exception);
        return ApiResponse.error("Internal server error");
    }
}