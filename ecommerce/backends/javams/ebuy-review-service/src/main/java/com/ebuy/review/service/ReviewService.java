package com.ebuy.review.service;

import com.ebuy.review.dto.request.CreateReviewRequest;
import com.ebuy.review.dto.request.UpdateReviewRequest;
import com.ebuy.review.dto.response.ReviewResponse;
import com.ebuy.review.dto.response.ReviewSummaryResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long userId, CreateReviewRequest request);
    ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request);
    void deleteReview(Long userId, Long reviewId);
    ReviewResponse getReview(Long reviewId);
    List<ReviewResponse> getReviewsByProduct(Long productId);
    List<ReviewResponse> getReviewsByUser(Long userId);
    ReviewSummaryResponse getReviewSummary(Long productId);
}
