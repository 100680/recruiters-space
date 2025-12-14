package com.ebuy.product.search.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @JsonProperty("category_id")
    private Long categoryId;

    private String name;
    private String description;

    @JsonProperty("parent_category_id")
    private Long parentCategoryId;

    @JsonProperty("is_active")
    private Boolean isActive;
}