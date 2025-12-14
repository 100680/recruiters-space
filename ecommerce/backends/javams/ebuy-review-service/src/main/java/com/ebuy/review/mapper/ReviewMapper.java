package com.ebuy.review.mapper;

import com.ebuy.review.dto.request.CreateReviewRequest;
import com.ebuy.review.dto.request.UpdateReviewRequest;
import com.ebuy.review.dto.response.ReviewResponse;
import com.ebuy.review.dto.response.ReviewSummaryResponse;
import com.ebuy.review.entity.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    /**
     * Convert Review entity to ReviewResponse DTO
     */
    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }

        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setUserId(review.getUserId());
        response.setProductId(review.getProductId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setModifiedAt(review.getModifiedAt());

        return response;
    }

    /**
     * Convert list of Review entities to list of ReviewResponse DTOs
     */
    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        if (reviews == null) {
            return null;
        }

        return reviews.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert CreateReviewRequest to Review entity
     */
    public Review toEntity(CreateReviewRequest request, Long userId) {
        if (request == null) {
            return null;
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return review;
    }

    /**
     * Update existing Review entity with UpdateReviewRequest data
     */
    public void updateEntity(Review review, UpdateReviewRequest request) {
        if (review == null || request == null) {
            return;
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
    }

    /**
     * Create ReviewSummaryResponse from aggregated data
     */
    public ReviewSummaryResponse toSummaryResponse(Long productId,
                                                   Long totalReviews,
                                                   Double averageRating,
                                                   Map<Integer, Long> ratingCounts) {
        ReviewSummaryResponse summary = new ReviewSummaryResponse();
        summary.setProductId(productId);
        summary.setTotalReviews(totalReviews != null ? totalReviews : 0L);
        summary.setAverageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0);

        // Set rating counts (default to 0 if not present)
        summary.setOneStarCount(ratingCounts.getOrDefault(1, 0L));
        summary.setTwoStarCount(ratingCounts.getOrDefault(2, 0L));
        summary.setThreeStarCount(ratingCounts.getOrDefault(3, 0L));
        summary.setFourStarCount(ratingCounts.getOrDefault(4, 0L));
        summary.setFiveStarCount(ratingCounts.getOrDefault(5, 0L));

        return summary;
    }

    /**
     * Create ReviewSummaryResponse with individual parameters
     */
    public ReviewSummaryResponse toSummaryResponse(Long productId,
                                                   Long totalReviews,
                                                   Double averageRating,
                                                   Long oneStarCount,
                                                   Long twoStarCount,
                                                   Long threeStarCount,
                                                   Long fourStarCount,
                                                   Long fiveStarCount) {
        return new ReviewSummaryResponse(
                productId,
                totalReviews != null ? totalReviews : 0L,
                averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0,
                fiveStarCount != null ? fiveStarCount : 0L,
                fourStarCount != null ? fourStarCount : 0L,
                threeStarCount != null ? threeStarCount : 0L,
                twoStarCount != null ? twoStarCount : 0L,
                oneStarCount != null ? oneStarCount : 0L
        );
    }
}
