package com.lcwd.electronicStore.ElectronicStore.security;

import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
Purpose:
This service tells Spring Security how to load our existing User entity during login.
Explanation:
We reuse the project's User table and do not create a duplicate security user class.
Flow:
Spring Security calls loadUserByUsername, we find the user by email, and return UserDetails for password checking.
*/
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Step 1: Treat username as email because this application stores unique emails.
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Step 2: Return Spring Security's built-in UserDetails object.
        // Step 3: Keep authorities empty for Feature 1; roles will be added in the RBAC feature.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                AuthorityUtils.NO_AUTHORITIES
        );
    }
}
