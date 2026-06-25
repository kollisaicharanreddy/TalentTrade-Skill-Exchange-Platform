package com.talenttrade.repository;

import com.talenttrade.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);

    Page<Review> findByRevieweeEmail(String email, Pageable pageable);

    boolean existsBySessionIdAndReviewerId(Long sessionId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.email = :email")
    Double getAverageRatingForUser(@Param("email") String email);

    long countByRevieweeEmail(String email);
}
