package com.lcwd.electronicStore.ElectronicStore.security;

/*
Purpose:
Provides reusable ownership checks for method-level security expressions.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityGuard")
public class SecurityGuard {

    private final UserRepository userRepository;

    public SecurityGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isCurrentUserId(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return true;
        }

        return userRepository.findByEmail(authentication.getName())
                .map(User::getUserId)
                .filter(userId::equals)
                .isPresent();
    }
}
