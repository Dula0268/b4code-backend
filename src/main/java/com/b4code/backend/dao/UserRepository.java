package com.b4code.backend.dao;

import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndDeletedFalse(String email);

  Optional<User> findByIdAndDeletedFalse(Long id);

  List<User> findByPropertyIdInAndRoleAndStatusAndDeletedFalse(List<Long> propertyIds, UserRole role,
      UserStatus status);

  @Query("""
      SELECT u FROM User u
      LEFT JOIN Property p ON p.id = u.propertyId
      WHERE u.deleted = false
        AND (
              :search IS NULL OR :search = ''
              OR LOWER(u.firstName)  LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(u.lastName)   LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(u.email)      LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(p.name)       LIKE LOWER(CONCAT('%', :search, '%'))
            )
        AND (:role   IS NULL OR u.role   = :role)
        AND (:status IS NULL OR u.status = :status)
      """)
  Page<User> findAllWithFilters(
      @Param("search") String search,
      @Param("role") UserRole role,
      @Param("status") UserStatus status,
      Pageable pageable);

  boolean existsByEmailAndDeletedFalse(String email);

  default boolean existsByEmail(String email) {
    return existsByEmailAndDeletedFalse(email);
  }
}
