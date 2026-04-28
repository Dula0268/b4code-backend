package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    long countByPropertyIdInAndReadByOwner(List<Long> propertyIds, boolean readByOwner);
}
