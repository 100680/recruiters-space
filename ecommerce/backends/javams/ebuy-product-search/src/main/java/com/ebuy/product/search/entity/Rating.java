package com.ebuy.product.search.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @JsonProperty("average_rating")
    private Float averageRating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    @JsonProperty("five_star")
    private Integer fiveStar;

    @JsonProperty("four_star")
    private Integer fourStar;

    @JsonProperty("three_star")
    private Integer threeStar;

    @JsonProperty("two_star")
    private Integer twoStar;

    @JsonProperty("one_star")
    private Integer oneStar;
}