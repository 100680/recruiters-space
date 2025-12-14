package com.ebuy.product.util;

import com.ebuy.product.dto.request.CategoryCreateRequest;
import com.ebuy.product.dto.request.ProductCreateRequest;
import com.ebuy.product.dto.request.ProductUpdateRequest;
import com.ebuy.product.dto.response.CategoryResponse;
import com.ebuy.product.dto.response.ProductResponse;
import com.ebuy.product.entity.Category;
import com.ebuy.product.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // Product mappings
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "productDiscounts", ignore = true)
    @Mapping(target = "priceHistory", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "effectivePrice", source = "price")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    @Mapping(target = "outOfStock", expression = "java(product.isOutOfStock())")
    @Mapping(target = "activeDiscount", ignore = true)
    ProductResponse toResponse(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "productDiscounts", ignore = true)
    @Mapping(target = "priceHistory", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);

    // Category mappings
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "rowVersion", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toResponse(Category category);
}