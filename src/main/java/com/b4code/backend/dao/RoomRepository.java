package com.b4code.backend.dao;

import com.b4code.backend.models.Room;
import com.b4code.backend.models.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByPropertyId(Long propertyId);

    @Query("SELECT r FROM Room r JOIN FETCH r.property LEFT JOIN FETCH r.image")
    List<Room> findAllWithRelations();

    // Paginated — no status filter (avoids null enum binding issue)
    @Query("""
            SELECT r FROM Room r
            WHERE r.property.ownerId = :ownerId
              AND (:search IS NULL OR :search = ''
                   OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CAST(r.roomType AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.id DESC
            """)
    Page<Room> findByOwnerAllStatuses(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            Pageable pageable);

    // Paginated — with status filter
    @Query("""
            SELECT r FROM Room r
            WHERE r.property.ownerId = :ownerId
              AND r.status = :status
              AND (:search IS NULL OR :search = ''
                   OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CAST(r.roomType AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.id DESC
            """)
    Page<Room> findByOwnerWithStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") RoomStatus status,
            @Param("search") String search,
            Pageable pageable);

    // Non-paginated fallback (used by propertyRoomInventry)
    @Query("""
            SELECT r FROM Room r
            WHERE r.property.ownerId = :ownerId
            ORDER BY r.id DESC
            """)
    List<Room> findAllByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.property.ownerId = :ownerId AND r.status = :status")
    long countByOwnerAndStatus(@Param("ownerId") Long ownerId, @Param("status") RoomStatus status);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.property.ownerId = :ownerId")
    long countByOwner(@Param("ownerId") Long ownerId);

    Optional<Room> findByIdAndPropertyOwnerId(Long id, Long ownerId);

    // Owner management — all rooms across the shared property table (no owner_id filter)
    @Query("""
            SELECT r FROM Room r
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CAST(r.roomType AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.id DESC
            """)
    Page<Room> findAllForOwner(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT r FROM Room r
            WHERE r.status = :status
              AND (:search IS NULL OR :search = ''
                   OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CAST(r.roomType AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.id DESC
            """)
    Page<Room> findAllForOwnerWithStatus(
            @Param("status") RoomStatus status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = :status")
    long countAllByStatus(@Param("status") RoomStatus status);
}
