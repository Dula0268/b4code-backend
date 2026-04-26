package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.models.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmailAndDeletedFalse(String email);

    Optional<AdminUser> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT u FROM AdminUser u
            WHERE u.deleted = false
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(u.firstName)  LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.lastName)   LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email)      LIKE LOWER(CONCAT('%', :search, '%'))
                  )
              AND (:role   IS NULL OR u.role   = :role)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<AdminUser> findAllWithFilters(
            @Param("search") String search,
            @Param("role")   UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    boolean existsByEmailAndDeletedFalse(String email);
}
