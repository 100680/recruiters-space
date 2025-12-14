package com.ebuy.product.catalog.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "categories")
public class Category {
    @Id
    private Long categoryId;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant modifiedAt;
    private Instant deletedAt;
    private boolean isDeleted;
    private String createdBy;
    private String modifiedBy;
    private Long rowVersion;
}