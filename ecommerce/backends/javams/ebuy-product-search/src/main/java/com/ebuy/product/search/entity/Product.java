package com.ebuy.product.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ebuy.product.search.config.FlexibleLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    @JsonProperty("product_id")
    private Long productId;

    private String name;
    private String description;
    private Double price;

    @JsonProperty("discounted_price")
    private Double discountedPrice;

    @JsonProperty("discount_percentage")
    private Float discountPercentage;

    @JsonProperty("has_active_discount")
    private Boolean hasActiveDiscount;

    private Integer stock;

    @JsonProperty("reorder_level")
    private Integer reorderLevel;

    @JsonProperty("is_in_stock")
    private Boolean isInStock;

    @JsonProperty("stock_status")
    private String stockStatus;

    private Category category;
    private String sku;

    @JsonProperty("image_url")
    private String imageUrl;

    private List<String> tags;
    private String brand;
    private Rating rating;

    @JsonProperty("popularity_score")
    private Float popularityScore;

    @JsonProperty("search_keywords")
    private String searchKeywords;

    @JsonProperty("created_at")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonProperty("modified_at")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime modifiedAt;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    @JsonProperty("search_boost")
    private Float searchBoost;
}