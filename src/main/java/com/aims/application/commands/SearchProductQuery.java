package com.aims.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Query object for searching products.
 * Encapsulates search criteria and pagination parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProductQuery {
    
    /**
     * Search by product title (partial match)
     */
    private String title;
    
    /**
     * Filter by product type (book, cd, dvd)
     */
    private String type;
    
    /**
     * Minimum price filter
     */
    @PositiveOrZero(message = "Minimum price must be positive or zero")
    private Integer minPrice;
    
    /**
     * Maximum price filter
     */
    @PositiveOrZero(message = "Maximum price must be positive or zero")
    private Integer maxPrice;
    
    /**
     * Filter by availability status
     */
    private Boolean inStock;
    
    /**
     * Filter by rush order support
     */
    private Boolean rushOrderSupported;
    
    /**
     * Page number for pagination (0-based)
     */
    @Builder.Default
    @PositiveOrZero(message = "Page number must be positive or zero")
    private int page = 0;
    
    /**
     * Page size for pagination
     */
    @Builder.Default
    @PositiveOrZero(message = "Page size must be positive")
    private int size = 20;
    
    /**
     * Sort field
     */
    @Builder.Default
    private String sortBy = "title";
    
    /**
     * Sort direction (ASC or DESC)
     */
    @Builder.Default
    private String sortDirection = "ASC";
    
    /**
     * Check if any search criteria is specified
     */
    public boolean hasSearchCriteria() {
        return title != null && !title.trim().isEmpty() ||
               type != null && !type.trim().isEmpty() ||
               minPrice != null || maxPrice != null ||
               inStock != null || rushOrderSupported != null;
    }
    
    /**
     * Validate price range
     */
    public boolean isPriceRangeValid() {
        if (minPrice != null && maxPrice != null) {
            return minPrice <= maxPrice;
        }
        return true;
    }
}
