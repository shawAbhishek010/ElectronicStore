package com.lcwd.electronicStore.ElectronicStore.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Purpose:
This DTO receives login data from the client.
Explanation:
It keeps only email and password because JWT login should not accept unnecessary user fields.
Flow:
Client sends this object to /auth/login, Spring validates it, and AuthController uses it for authentication.
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtRequest {

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
