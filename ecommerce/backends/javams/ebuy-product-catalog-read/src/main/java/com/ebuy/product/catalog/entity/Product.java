package com.ebuy.product.catalog.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id; // This maps to MongoDB's _id field

    @Indexed
    @Field("product_id")
    private Long productId; // This maps to your business product_id

    @Indexed
    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("stock")
    private Integer stock;

    @Field("reorder_level")
    private Integer reorderLevel;

    @Field("category_id")
    private Long categoryId;

    @Field("image_url")
    private String imageUrl;

    @Indexed(unique = true, sparse = true)
    @Field("sku")
    private String sku;

    @Field("created_at")
    private Instant createdAt;

    @Field("modified_at")
    private Instant modifiedAt;

    @Field("deleted_at")
    private Instant deletedAt;

    @Field("is_deleted")
    private Boolean isDeleted;

    @Field("correlation_id")
    private UUID correlationId;

    @Field("created_by")
    private String createdBy;

    @Field("modified_by")
    private String modifiedBy;

    @Field("row_version")
    private Long rowVersion;
}