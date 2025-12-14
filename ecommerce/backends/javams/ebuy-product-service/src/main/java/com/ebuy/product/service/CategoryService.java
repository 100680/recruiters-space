package com.ebuy.product.service;


import com.ebuy.product.dto.request.CategoryCreateRequest;
import com.ebuy.product.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryCreateRequest request, String createdBy);

    CategoryResponse updateCategory(Long categoryId, CategoryCreateRequest request, String modifiedBy);

    CategoryResponse getCategory(Long categoryId);

    List<CategoryResponse> getAllCategories();

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    Page<CategoryResponse> searchCategories(String searchTerm, Pageable pageable);

    void deleteCategory(Long categoryId, String deletedBy);

    long getProductCountByCategory(Long categoryId);

    boolean existsByName(String name);
}
