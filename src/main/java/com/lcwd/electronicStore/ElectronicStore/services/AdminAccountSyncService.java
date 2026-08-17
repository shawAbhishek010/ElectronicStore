package com.lcwd.electronicStore.ElectronicStore.services;

import com.lcwd.electronicStore.ElectronicStore.entities.Admin;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.repositories.AdminRepository;
import org.springframework.stereotype.Service;

/*
Purpose:
Keeps the dedicated admins table in sync with user rows that have ROLE_ADMIN.
*/
@Service
public class AdminAccountSyncService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AdminRepository adminRepository;

    public AdminAccountSyncService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public void syncUser(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        if (!ROLE_ADMIN.equals(normalizeRole(user.getRole()))) {
            removeUser(user.getUserId());
            return;
        }

        Admin admin = adminRepository.findById(user.getUserId()).orElseGet(Admin::new);
        admin.setAdminId(user.getUserId());
        admin.setName(user.getName());
        admin.setEmail(user.getEmail());
        admin.setPassword(user.getPassword());
        admin.setGender(user.getGender());
        admin.setAbout(user.getAbout());
        adminRepository.save(admin);
    }

    public void removeUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        if (adminRepository.existsById(userId)) {
            adminRepository.deleteById(userId);
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole : "ROLE_" + cleanedRole;
    }
}
