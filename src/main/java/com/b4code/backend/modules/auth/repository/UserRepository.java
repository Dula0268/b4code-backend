package com.b4code.backend.modules.auth.repository;

import com.b4code.backend.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
        java.util.List<User> findByPropertyIdInAndRoleAndStatus(java.util.List<Long> propertyIds, User.Role role, User.UserStatus status);
}
