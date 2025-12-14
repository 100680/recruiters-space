package com.ebuy.review.service.Impl;

import com.ebuy.review.dto.request.CreateReviewRequest;
import com.ebuy.review.dto.request.UpdateReviewRequest;
import com.ebuy.review.dto.response.ReviewResponse;
import com.ebuy.review.dto.response.ReviewSummaryResponse;
import com.ebuy.review.entity.Review;
import com.ebuy.review.exception.DuplicateReviewException;
import com.ebuy.review.exception.ReviewNotFoundException;
import com.ebuy.review.mapper.ReviewMapper;
import com.ebuy.review.repository.ReviewRepository;
import com.ebuy.review.service.ReviewService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        logger.info("Creating review for user {} and product {}", userId, request.getProductId());

        // Check if user already has a review for this product
        Optional<Review> existingReview = reviewRepository
                .findActiveReviewByUserAndProduct(userId, request.getProductId());

        if (existingReview.isPresent()) {
            throw new DuplicateReviewException(
                    "User already has a review for this product",
                    userId,
                    request.getProductId()
            );
        }

        Review review = reviewMapper.toEntity(request, userId);
        Review savedReview = reviewRepository.save(review);

        logger.info("Successfully created review with id {}", savedReview.getReviewId());
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public ReviewResponse getReview(Long reviewId) {
        logger.debug("Fetching review with id {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        if (review.getIsDeleted()) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }

        return reviewMapper.toResponse(review);
    }

    @Override
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        logger.info("Updating review {} for user {}", reviewId, userId);

        // Find the existing review
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        // Check if the review is deleted
        if (existingReview.getIsDeleted()) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }

        // Verify that the user owns this review
        if (!existingReview.getUserId().equals(userId)) {
            throw new SecurityException("User is not authorized to update this review");
        }

        // Update the review using mapper
        reviewMapper.updateEntity(existingReview, request);

        // Save the updated review
        Review updatedReview = reviewRepository.save(existingReview);

        logger.info("Successfully updated review with id {}", reviewId);
        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        logger.info("Deleting review {} for user {}", reviewId, userId);

        // Find the existing review
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        // Check if the review is already deleted
        if (existingReview.getIsDeleted()) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }

        // Verify that the user owns this review
        if (!existingReview.getUserId().equals(userId)) {
            throw new SecurityException("User is not authorized to delete this review");
        }

        // Perform soft delete
        existingReview.setIsDeleted(true);
        existingReview.setDeletedAt(LocalDateTime.now());

        // Save the deleted review
        reviewRepository.save(existingReview);

        logger.info("Successfully deleted review with id {}", reviewId);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        logger.debug("Fetching reviews for product {}", productId);

        List<Review> reviews = reviewRepository.findActiveReviewsByProductId(productId);
        return reviewMapper.toResponseList(reviews);
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        logger.debug("Fetching reviews for user {}", userId);

        List<Review> reviews = reviewRepository.findActiveReviewsByUserId(userId);
        return reviewMapper.toResponseList(reviews);
    }

    @Override
    public ReviewSummaryResponse getReviewSummary(Long productId) {
        logger.debug("Generating review summary for product {}", productId);

        // Get total count and average rating
        Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);
        Optional<Double> averageRating = reviewRepository.findAverageRatingByProductId(productId);

        // Get rating counts for each star rating (1-5)
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            Long count = reviewRepository.countReviewsByProductIdAndRating(productId, rating);
            ratingCounts.put(rating, count != null ? count : 0L);
        }

        ReviewSummaryResponse summary = reviewMapper.toSummaryResponse(
                productId,
                totalReviews,
                averageRating.orElse(0.0),
                ratingCounts
        );

        logger.debug("Generated summary for product {}: {} reviews, avg rating {}",
                productId, totalReviews, averageRating.orElse(0.0));

        return summary;
    }
}