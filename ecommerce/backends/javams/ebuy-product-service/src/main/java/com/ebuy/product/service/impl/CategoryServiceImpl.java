package com.ebuy.product.service.impl;


import com.ebuy.product.dto.request.CategoryCreateRequest;
import com.ebuy.product.dto.response.CategoryResponse;
import com.ebuy.product.entity.Category;
import com.ebuy.product.exception.CategoryNotFoundException;
import com.ebuy.product.repository.CategoryRepository;
import com.ebuy.product.service.CategoryService;
import com.ebuy.product.util.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryCreateRequest request, String createdBy) {
        logger.info("Creating category: {}", request.getName());

        // Check if category name already exists
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
            throw new DataIntegrityViolationException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = productMapper.toEntity(request);
        category.setCreatedBy(createdBy);

        try {
            Category savedCategory = categoryRepository.save(category);
            logger.info("Category created successfully with ID: {}", savedCategory.getCategoryId());
            return productMapper.toResponse(savedCategory);
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage());
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public CategoryResponse updateCategory(Long categoryId, CategoryCreateRequest request, String modifiedBy) {
        logger.info("Updating category with ID: {}", categoryId);

        Category existingCategory = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        // Check if new name conflicts with existing categories
        if (!existingCategory.getName().equalsIgnoreCase(request.getName()) &&
                categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
            throw new DataIntegrityViolationException("Category with name '" + request.getName() + "' already exists");
        }

        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setModifiedBy(modifiedBy);

        try {
            Category updatedCategory = categoryRepository.save(existingCategory);
            logger.info("Category updated successfully with ID: {}", updatedCategory.getCategoryId());
            return productMapper.toResponse(updatedCategory);
        } catch (Exception e) {
            logger.error("Error updating category: {}", e.getMessage());
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    @Cacheable(value = "category", key = "#categoryId")
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long categoryId) {
        logger.debug("Fetching category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        CategoryResponse response = productMapper.toResponse(category);
        response.setProductCount(getProductCountByCategory(categoryId));
        return response;
    }

    @Override
    @Cacheable(value = "categories", key = "'all'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        logger.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findByIsDeletedFalseOrderByName();
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = productMapper.toResponse(category);
                    response.setProductCount(getProductCountByCategory(category.getCategoryId()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        logger.debug("Fetching all categories with pagination: {}", pageable);

        Page<Category> categories = categoryRepository.findByIsDeletedFalse(pageable);
        return categories.map(category -> {
            CategoryResponse response = productMapper.toResponse(category);
            response.setProductCount(getProductCountByCategory(category.getCategoryId()));
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String searchTerm, Pageable pageable) {
        logger.debug("Searching categories with term: {} and pagination: {}", searchTerm, pageable);

        Page<Category> categories = categoryRepository.searchByName(searchTerm, pageable);
        return categories.map(category -> {
            CategoryResponse response = productMapper.toResponse(category);
            response.setProductCount(getProductCountByCategory(category.getCategoryId()));
            return response;
        });
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public void deleteCategory(Long categoryId, String deletedBy) {
        logger.info("Soft deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        // Check if category has active products
        long productCount = getProductCountByCategory(categoryId);
        if (productCount > 0) {
            throw new DataIntegrityViolationException(
                    "Cannot delete category with " + productCount + " active products. Move products to another category first.");
        }

        category.markDeleted();
        category.setModifiedBy(deletedBy);

        try {
            categoryRepository.save(category);
            logger.info("Category soft deleted successfully with ID: {}", categoryId);
        } catch (Exception e) {
            logger.error("Error deleting category: {}", e.getMessage());
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    @Override
    @Cacheable(value = "categoryProductCount", key = "#categoryId")
    @Transactional(readOnly = true)
    public long getProductCountByCategory(Long categoryId) {
        return categoryRepository.countActiveProductsByCategory(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name);
    }
}
