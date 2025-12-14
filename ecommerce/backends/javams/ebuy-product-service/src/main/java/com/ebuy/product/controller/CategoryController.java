package com.ebuy.product.controller;


import com.ebuy.product.dto.request.CategoryCreateRequest;
import com.ebuy.product.dto.response.CategoryResponse;
import com.ebuy.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@Validated
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new product category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or category name already exists")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Creating category: {} by user: {}", request.getName(), userId);
        CategoryResponse response = categoryService.createCategory(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update an existing category", description = "Updates an existing product category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or category name already exists"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable @Min(1) Long categoryId,
            @Valid @RequestBody CategoryCreateRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Updating category ID: {} by user: {}", categoryId, userId);
        CategoryResponse response = categoryService.updateCategory(categoryId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategory(
            @Parameter(description = "Category ID") @PathVariable @Min(1) Long categoryId) {

        logger.debug("Fetching category ID: {}", categoryId);
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<?> getAllCategories(
            @Parameter(description = "Enable pagination") @RequestParam(defaultValue = "false") boolean paginated,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        if (paginated) {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            logger.debug("Fetching all categories with pagination - page: {}, size: {}", page, size);
            Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
            return ResponseEntity.ok(response);
        } else {
            logger.debug("Fetching all categories without pagination");
            List<CategoryResponse> response = categoryService.getAllCategories();
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches categories by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @Parameter(description = "Search term") @RequestParam @NotBlank String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.debug("Searching categories with term: {} - page: {}, size: {}", q, page, size);
        Page<CategoryResponse> response = categoryService.searchCategories(q, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category", description = "Soft deletes a category (only if no active products)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Category has active products"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable @Min(1) Long categoryId,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {

        logger.info("Deleting category ID: {} by user: {}", categoryId, userId);
        categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/product-count")
    @Operation(summary = "Get product count by category", description = "Gets the number of active products in a category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Map<String, Long>> getProductCountByCategory(
            @Parameter(description = "Category ID") @PathVariable @Min(1) Long categoryId) {

        logger.debug("Fetching product count for category ID: {}", categoryId);
        long productCount = categoryService.getProductCountByCategory(categoryId);
        return ResponseEntity.ok(Map.of("productCount", productCount));
    }

    @GetMapping("/exists/{name}")
    @Operation(summary = "Check if category name exists", description = "Checks if a category name is already taken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    public ResponseEntity<Map<String, Boolean>> checkCategoryNameExists(
            @Parameter(description = "Category name") @PathVariable @NotBlank String name) {

        logger.debug("Checking if category name exists: {}", name);
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
