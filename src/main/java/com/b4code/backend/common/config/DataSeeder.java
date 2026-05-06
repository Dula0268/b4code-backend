package com.b4code.backend.common.config;

import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ✅ Ensure admin always exists (dev logic)
        userRepository.findByEmail("admin@primestay.com").ifPresentOrElse(
                admin -> {
                    if (admin.getRole() != User.Role.ADMIN) {
                        admin.setRole(User.Role.ADMIN);
                        userRepository.save(admin);
                        System.out.println("✅ Updated admin role");
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setEmail("admin@primestay.com");
                    admin.setPasswordHash(passwordEncoder.encode("admin123"));
                    admin.setFirstName("System");
                    admin.setLastName("Admin");
                    admin.setRole(User.Role.ADMIN);
                    admin.setStatus(User.UserStatus.ACTIVE);
                    userRepository.save(admin);
                    System.out.println("✅ Admin user created");
                });

        // ✅ Your additional seed users
        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", User.Role.GUEST);
        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", User.Role.OWNER);
        seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", User.Role.STAFF);

        // ✅ Admin users table
        if (adminUserRepository.count() == 0) {
            seedAdminUser("Sarah", "Jenkins", "sarah.j@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Mike", "Ross", "mike.ross@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            System.out.println("✅ Admin users seeded");
        }
    }

    private void seedUserIfMissing(String email, String password, String first, String last, User.Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFirstName(first);
            user.setLastName(last);
            user.setRole(role);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }

    private void seedAdminUser(String first, String last, String email,
            UserRole role, UserStatus status) {
        AdminUser u = new AdminUser();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setStatus(status);
        adminUserRepository.save(u);
    }
}