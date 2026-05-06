package com.b4code.backend.common.config;


import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // ── Seed auth users table (for login) ──────────────────────────────
        userRepository.findByEmail("admin@primestay.com").ifPresentOrElse(
            admin -> {
                if (admin.getRole() != User.Role.ADMIN) {
                    admin.setRole(User.Role.ADMIN);
                    userRepository.save(admin);
                    System.out.println("✅ Forcefully updated admin@primestay.com to ADMIN role");
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
                System.out.println("✅ Default admin user created: admin@primestay.com");
            }
        );
    }
}