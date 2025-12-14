package com.ebuy.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutocompleteRequest {
    private String query;
    private int size = 10;
    private int categoryId;
}