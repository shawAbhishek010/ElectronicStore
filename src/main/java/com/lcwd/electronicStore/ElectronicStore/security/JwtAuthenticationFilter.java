package com.lcwd.electronicStore.ElectronicStore.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
Purpose:
This filter checks JWT tokens before Spring reaches controller methods.
Explanation:
It reads the Authorization header, validates the token, and sets the logged-in user in SecurityContext.
Flow:
Every request passes here once, valid Bearer tokens create authentication, and invalid tokens remain unauthenticated.
*/
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtHelper jwtHelper, CustomUserDetailsService customUserDetailsService) {
        this.jwtHelper = jwtHelper;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Step 1: Read Authorization header. JWT should come as: Bearer <token>
        String requestHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                username = jwtHelper.getUsernameFromToken(token);
            } catch (JwtException | IllegalArgumentException ex) {
                // Invalid token should not crash the API; Spring Security will return 401 for secured endpoints.
                username = null;
            }
        }

        // Step 2: Authenticate only when username is present and no user is already authenticated.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Step 3: Validate token and set authentication in Spring Security context.
            if (jwtHelper.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
