package com.lcwd.electronicStore.ElectronicStore.controller;

import com.lcwd.electronicStore.ElectronicStore.dtos.JwtRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.JwtResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import com.lcwd.electronicStore.ElectronicStore.exceptions.BadApiRequestException;
import com.lcwd.electronicStore.ElectronicStore.security.JwtHelper;
import com.lcwd.electronicStore.ElectronicStore.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
Purpose:
This controller exposes authentication APIs for register and login.
Explanation:
Register creates a user with BCrypt password, and login returns a JWT for secured APIs.
Flow:
Client registers, then logs in, then sends Authorization: Bearer <token> for protected endpoints.
*/
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;
    private final UserService userService;

    @Value("${admin.portal.password:Admin@123}")
    private String adminPortalPassword;

    public AuthController(AuthenticationManager authenticationManager, JwtHelper jwtHelper, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtHelper = jwtHelper;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        String requestedRole = normalizeRole(userDto.getRole());
        validateAdminPortalPassword(requestedRole, userDto.getAdminPortalPassword());
        userDto.setRole(requestedRole);

        // Step 1: Reuse existing UserService so registration follows the current user creation flow.
        UserDto createdUser = userService.createUser(userDto);

        // Step 2: Do not send password/hash back to the client.
        createdUser.setPassword(null);
        createdUser.setAdminPortalPassword(null);

        // Step 3: Return 201 because a new user resource was created.
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest) {
        String requestedRole = normalizeRole(jwtRequest.getRole());

        // Step 1: Ask Spring Security to check email and password using UserDetailsService and BCrypt.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword())
        );

        // Step 2: Generate JWT only after authentication succeeds.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtHelper.generateToken(userDetails);

        // Step 3: Load existing user details for a helpful login response.
        UserDto user = userService.getUserByEmail(jwtRequest.getEmail());
        String storedRole = normalizeRole(user.getRole());
        if (!storedRole.equals(requestedRole)) {
            throw new BadApiRequestException("Selected portal does not match this account role.");
        }
        validateAdminPortalPassword(storedRole, jwtRequest.getAdminPortalPassword());

        JwtResponse response = JwtResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(storedRole)
                .build();

        return ResponseEntity.ok(response);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }

        String cleanedRole = role.trim().toUpperCase();
        if (!cleanedRole.startsWith("ROLE_")) {
            cleanedRole = "ROLE_" + cleanedRole;
        }

        if (!ROLE_ADMIN.equals(cleanedRole) && !ROLE_USER.equals(cleanedRole)) {
            throw new BadApiRequestException("Invalid role selected.");
        }

        return cleanedRole;
    }

    private void validateAdminPortalPassword(String role, String providedPassword) {
        if (!ROLE_ADMIN.equals(role)) {
            return;
        }

        if (providedPassword == null || providedPassword.isBlank() || !providedPassword.equals(adminPortalPassword)) {
            throw new BadApiRequestException("Invalid admin portal password.");
        }
    }
}
