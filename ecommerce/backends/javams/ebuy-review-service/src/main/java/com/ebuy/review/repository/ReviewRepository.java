package com.ebuy.review.repository;

import com.ebuy.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.isDeleted = false")
    List<Review> findActiveReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.isDeleted = false")
    List<Review> findActiveReviewsByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.productId = :productId AND r.isDeleted = false")
    Optional<Review> findActiveReviewByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isDeleted = false")
    Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isDeleted = false")
    Long countActiveReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isDeleted = false GROUP BY r.rating")
    List<Object[]> findRatingCountsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.rating = :rating AND r.isDeleted = false")
    Long countReviewsByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);
}
