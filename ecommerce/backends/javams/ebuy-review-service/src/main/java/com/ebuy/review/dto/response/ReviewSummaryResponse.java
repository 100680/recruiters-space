package com.ebuy.review.dto.response;


public class ReviewSummaryResponse {
    private Long productId;
    private Long totalReviews;
    private Double averageRating;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;

    // Default constructor
    public ReviewSummaryResponse() {}

    // Constructor with all fields
    public ReviewSummaryResponse(Long productId, Long totalReviews, Double averageRating,
                                 Long fiveStarCount, Long fourStarCount, Long threeStarCount,
                                 Long twoStarCount, Long oneStarCount) {
        this.productId = productId;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
        this.fiveStarCount = fiveStarCount;
        this.fourStarCount = fourStarCount;
        this.threeStarCount = threeStarCount;
        this.twoStarCount = twoStarCount;
        this.oneStarCount = oneStarCount;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getFiveStarCount() {
        return fiveStarCount;
    }

    public void setFiveStarCount(Long fiveStarCount) {
        this.fiveStarCount = fiveStarCount;
    }

    public Long getFourStarCount() {
        return fourStarCount;
    }

    public void setFourStarCount(Long fourStarCount) {
        this.fourStarCount = fourStarCount;
    }

    public Long getThreeStarCount() {
        return threeStarCount;
    }

    public void setThreeStarCount(Long threeStarCount) {
        this.threeStarCount = threeStarCount;
    }

    public Long getTwoStarCount() {
        return twoStarCount;
    }

    public void setTwoStarCount(Long twoStarCount) {
        this.twoStarCount = twoStarCount;
    }

    public Long getOneStarCount() {
        return oneStarCount;
    }

    public void setOneStarCount(Long oneStarCount) {
        this.oneStarCount = oneStarCount;
    }

    @Override
    public String toString() {
        return "ReviewSummaryResponse{" +
                "productId=" + productId +
                ", totalReviews=" + totalReviews +
                ", averageRating=" + averageRating +
                ", fiveStarCount=" + fiveStarCount +
                ", fourStarCount=" + fourStarCount +
                ", threeStarCount=" + threeStarCount +
                ", twoStarCount=" + twoStarCount +
                ", oneStarCount=" + oneStarCount +
                '}';
    }
}