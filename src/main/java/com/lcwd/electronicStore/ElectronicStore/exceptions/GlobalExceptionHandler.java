package com.lcwd.electronicStore.ElectronicStore.exceptions;

import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
