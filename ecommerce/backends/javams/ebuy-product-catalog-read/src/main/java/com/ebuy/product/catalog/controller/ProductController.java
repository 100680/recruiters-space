package com.ebuy.product.catalog.controller;

import com.ebuy.product.catalog.entity.Product;
import com.ebuy.product.catalog.entity.ProductDiscount;
import com.ebuy.product.catalog.service.ProductService;
import com.ebuy.product.catalog.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(productService.getProductById(id)));
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) Long categoryId) {
        if (name != null) {
            return productService.searchProductsByName(name);
        } else if (categoryId != null) {
            return productService.searchProductsByCategory(categoryId);
        } else {
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}/discounts")
    public List<ProductDiscount> getDiscounts(@PathVariable Long id) {
        return productService.getActiveDiscounts(id);
    }
}
