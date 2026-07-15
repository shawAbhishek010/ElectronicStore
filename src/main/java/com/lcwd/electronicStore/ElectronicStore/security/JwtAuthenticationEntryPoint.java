package com.lcwd.electronicStore.ElectronicStore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
Purpose:
This class returns a clean 401 response when a secured API is called without a valid JWT.
Explanation:
Without this class, Spring Security may return a default error response that is less beginner-friendly.
Flow:
Security detects missing/invalid authentication, calls commence, and we write ApiResponse as JSON.
*/
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Step 1: Set HTTP status and JSON content type.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Step 2: Build the same ApiResponse style used in the existing project.
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Unauthorized: Please login and pass a valid JWT token")
                .status(HttpStatus.UNAUTHORIZED)
                .successs(false)
                .build();

        // Step 3: Convert the response object to JSON and write it to the client.
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
