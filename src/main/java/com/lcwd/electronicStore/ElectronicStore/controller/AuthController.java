package com.lcwd.electronicStore.ElectronicStore.controller;

import com.lcwd.electronicStore.ElectronicStore.dtos.JwtRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.JwtResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import com.lcwd.electronicStore.ElectronicStore.security.JwtHelper;
import com.lcwd.electronicStore.ElectronicStore.services.UserService;
import jakarta.validation.Valid;
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

    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtHelper jwtHelper, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtHelper = jwtHelper;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        // Step 1: Reuse existing UserService so registration follows the current user creation flow.
        UserDto createdUser = userService.createUser(userDto);

        // Step 2: Do not send password/hash back to the client.
        createdUser.setPassword(null);

        // Step 3: Return 201 because a new user resource was created.
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest) {
        // Step 1: Ask Spring Security to check email and password using UserDetailsService and BCrypt.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword())
        );

        // Step 2: Generate JWT only after authentication succeeds.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtHelper.generateToken(userDetails);

        // Step 3: Load existing user details for a helpful login response.
        UserDto user = userService.getUserByEmail(jwtRequest.getEmail());
        JwtResponse response = JwtResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }
}
