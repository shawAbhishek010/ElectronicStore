package com.lcwd.electronicStore.ElectronicStore.exceptions;

import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Purpose:
This class handles common application exceptions in one place.
Explanation:
Controllers stay clean because exception-to-response conversion is centralized here.
Flow:
When a controller/service throws a known exception, Spring calls the matching handler and returns a proper HTTP status.
*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse apiResponse = ApiResponse.builder().message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .successs(false).build();

        return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
    }

    //MethodArgumentNotValidException that deals with all Invalidated input......
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        Map<String, Object> response = new HashMap<>();
        allErrors.stream().forEach(objectError -> {
            String message = objectError.getDefaultMessage();
            String field = ((FieldError) objectError).getField();// type cast in field error
            response.put(field, message);
        }
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadApiRequestException.class)
    public ResponseEntity<ApiResponse> badApiRequestHandler(BadApiRequestException ex) {
        ApiResponse apiResponse = ApiResponse.builder().message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .successs(false).build();

        return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> authenticationExceptionHandler(AuthenticationException ex) {
        // Step 1: Build a clear login failure response for invalid email/password.
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Invalid email or password")
                .status(HttpStatus.UNAUTHORIZED)
                .successs(false)
                .build();

        // Step 2: Return 401 because authentication failed.
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> accessDeniedExceptionHandler(AccessDeniedException ex) {
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Forbidden: You do not have permission to access this resource")
                .status(HttpStatus.FORBIDDEN)
                .successs(false)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse> responseStatusExceptionHandler(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ApiResponse apiResponse = ApiResponse.builder()
                .message(ex.getReason() == null ? "Request failed" : ex.getReason())
                .status(status)
                .successs(false)
                .build();

        return new ResponseEntity<>(apiResponse, status);
    }

}
