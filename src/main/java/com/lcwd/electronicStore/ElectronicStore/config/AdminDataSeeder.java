package com.lcwd.electronicStore.ElectronicStore.config;

import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.AdminAccountSyncService;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
Purpose:
Backfills the admins table from existing ROLE_ADMIN users during application startup.
*/
@Component
public class AdminDataSeeder implements CommandLineRunner {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final AdminAccountSyncService adminAccountSyncService;

    public AdminDataSeeder(UserRepository userRepository, AdminAccountSyncService adminAccountSyncService) {
        this.userRepository = userRepository;
        this.adminAccountSyncService = adminAccountSyncService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.findAll().stream()
                .filter((user) -> ROLE_ADMIN.equals(normalizeRole(user.getRole())))
                .forEach(adminAccountSyncService::syncUser);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole : "ROLE_" + cleanedRole;
    }
}
