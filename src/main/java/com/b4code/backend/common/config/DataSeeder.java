package com.b4code.backend.common.config;

import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.modules.staff.entity.StaffProperty;
import com.b4code.backend.modules.staff.repository.StaffPropertyRepository;
import com.b4code.backend.modules.admin.dao.AdminPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final StaffPropertyRepository staffPropertyRepository;
    private final AdminPropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureUserStatusColumnExists();

        // Create default admin if not exists
        if (userRepository.findByEmail("admin@primestay.com").isEmpty()) {
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

        // Create default staff if not exists
        User staffUser;
        Optional<User> existingStaff = userRepository.findByEmail("staff@primestay.com");
        if (existingStaff.isEmpty()) {
            User staff = new User();
            staff.setEmail("staff@primestay.com");
            staff.setPasswordHash(passwordEncoder.encode("staff123"));
            staff.setFirstName("John");
            staff.setLastName("Staff");
            staff.setPhone("0712345678");
            staff.setRole(User.Role.STAFF);

            staffUser = userRepository.save(staff);
            System.out.println("✅ Default staff user created: staff@primestay.com / staff123");
        } else {
            staffUser = existingStaff.get();
        }

        // Create a default property if none exists and map to staff
        if (propertyRepository.count() == 0) {
            Property property = new Property();
            property.setName("PrimeStay Grand Hotel");
            property.setPvId("PV-12345");
            property.setOwnerId(1L);
            property.setStatus(com.b4code.backend.modules.admin.enums.PropertyStatus.APPROVED);
            property = propertyRepository.save(property);
            System.out.println("✅ Default property created: PrimeStay Grand Hotel");

            // Map staff to this property
            StaffProperty mapping = new StaffProperty();
            mapping.setStaffId(staffUser.getId());
            mapping.setPropertyId(property.getId());
            staffPropertyRepository.save(mapping);
            System.out.println("✅ Staff mapped to default property");
        } else if (staffPropertyRepository.findByStaffId(staffUser.getId()).isEmpty()) {
            // Map staff to first available property if no mapping exists
            Property firstProperty = propertyRepository.findAll().get(0);
            StaffProperty mapping = new StaffProperty();
            mapping.setStaffId(staffUser.getId());
            mapping.setPropertyId(firstProperty.getId());
            staffPropertyRepository.save(mapping);
            System.out.println("✅ Staff mapped to existing property: " + firstProperty.getName());
        }

        // Seed admin_users table
        if (adminUserRepository.count() == 0) {
            seedAdminUser("Sarah",  "Jenkins", "sarah.j@primestay.com",    UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Mike",   "Ross",    "mike.ross@primestay.com",  UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("John",   "Doe",     "john.d@gmail.com",         UserRole.STAFF, UserStatus.SUSPENDED);
            seedAdminUser("Emily",  "Chen",    "emily.chen@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Aisha",  "Kumar",   "aisha.k@primestay.com",    UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Nina",   "Patel",   "nina.patel@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Daniel", "Osei",    "daniel.o@primestay.com",   UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Priya",  "Sharma",  "priya.s@primestay.com",    UserRole.OWNER, UserStatus.SUSPENDED);
            System.out.println("✅ Sample admin_users seeded (8 records)");
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

    private void ensureUserStatusColumnExists() {
        jdbcTemplate.execute("""
                ALTER TABLE users
                ADD COLUMN IF NOT EXISTS status VARCHAR(255)
                """);
        jdbcTemplate.update("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN status SET DEFAULT 'ACTIVE'");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN status SET NOT NULL");
    }
}