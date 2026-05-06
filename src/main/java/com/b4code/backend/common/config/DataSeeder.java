package com.b4code.backend.common.config;

import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual constructor to avoid Lombok @RequiredArgsConstructor issues
    public DataSeeder(UserRepository userRepository, 
                      AdminUserRepository adminUserRepository, 
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ── Seed auth users table (for login) ──────────────────────────────
        seedUserIfMissing("admin@primestay.com", "admin123", "System", "Admin", User.Role.ADMIN);
        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", User.Role.GUEST);
        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", User.Role.OWNER);
        seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", User.Role.STAFF);

        // ── Seed admin_users table (for admin user management module) ───────
        if (adminUserRepository.count() == 0) {
            seedAdminUser("Sarah", "Jenkins", "sarah.j@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Mike", "Ross", "mike.ross@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("John", "Doe", "john.d@gmail.com", UserRole.STAFF, UserStatus.SUSPENDED);
            seedAdminUser("Emily", "Chen", "emily.chen@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Aisha", "Kumar", "aisha.k@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Nina", "Patel", "nina.patel@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Daniel", "Osei", "daniel.o@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Priya", "Sharma", "priya.s@primestay.com", UserRole.OWNER, UserStatus.SUSPENDED);
            System.out.println("✅ Sample admin_users seeded (8 records)");
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
            System.out.println("✅ Default " + role + " user created: " + email);
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