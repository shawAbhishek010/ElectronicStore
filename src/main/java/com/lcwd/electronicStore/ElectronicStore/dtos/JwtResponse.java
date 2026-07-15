package com.lcwd.electronicStore.ElectronicStore.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Purpose:
This DTO sends the generated JWT and basic user information back after login.
Explanation:
The client stores the token and sends it in the Authorization header for secured APIs.
Flow:
AuthController creates this response after Spring Security confirms that email and password are correct.
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;

    private String tokenType;

    private String userId;

    private String name;

    private String email;
}
