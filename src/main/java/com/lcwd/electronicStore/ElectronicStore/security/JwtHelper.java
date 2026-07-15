package com.lcwd.electronicStore.ElectronicStore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/*
Purpose:
This class creates and validates JWT tokens.
Explanation:
JWT lets the backend stay stateless because every request carries its own signed authentication token.
Flow:
After login we generate a token, and on every secured request JwtAuthenticationFilter validates that token.
*/
@Component
public class JwtHelper {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        // Step 1: Prepare current time and expiry time for the token.
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // Step 2: Put username in subject because Spring Security identifies users by username.
        // Step 3: Sign the token so clients cannot change its content without invalidating it.
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        // Step 1: Read the subject claim from the verified token.
        // Step 2: Return it as the username used by UserDetailsService.
        return getClaimFromToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // Step 1: Extract username from the token.
        String username = getUsernameFromToken(token);

        // Step 2: Compare it with database-loaded user details.
        // Step 3: Also confirm the token has not expired.
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        // Step 1: Parse the token using the same secret key used during token creation.
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Step 2: Return the requested claim like username or expiration.
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        // The secret must be at least 32 characters for HS256 so the token signature is strong enough.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
