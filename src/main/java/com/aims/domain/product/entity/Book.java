package com.aims.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Book entity extending Product
 * Specific attributes and behavior for books
 */
@Entity
@DiscriminatorValue("book")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Book extends Product {
    
    @Column(name = "genre")
    private String genre;
    
    @Positive(message = "Page count must be positive")
    @Column(name = "page_count")
    private Integer pageCount;
    
    @Column(name = "publication_date")
    private LocalDate publicationDate;
    
    @Column(name = "authors")
    private String authors;
    
    @Column(name = "publishers")
    private String publishers;
    
    @Column(name = "cover_type")
    private String coverType;
    
    @Override
    public String getType() {
        return "book";
    }
    
    /**
     * Calculate shipping weight considering book-specific factors
     */
    @Override
    public float getShippingWeight() {
        float baseWeight = super.getShippingWeight();
        if (baseWeight > 0) {
            return baseWeight;
        }
        
        // Estimate weight based on page count if not provided
        if (pageCount != null && pageCount > 0) {
            // Rough estimate: paperback ~0.5g per page, hardcover ~1g per page
            float estimatedWeight = "hardcover".equalsIgnoreCase(coverType) ? 
                pageCount * 1.0f : pageCount * 0.5f;
            return estimatedWeight / 1000; // Convert grams to kg
        }
        
        return 0.5f; // Default book weight in kg
    }
    
    /**
     * Business logic for book availability
     * Books might have different availability rules
     */
    public boolean isBookAvailable() {
        return isInStock() && publicationDate != null && 
               !publicationDate.isAfter(LocalDate.now());
    }
    
    /**
     * Check if book is newly published (within last 6 months)
     */
    public boolean isNewRelease() {
        if (publicationDate == null) {
            return false;
        }
        return publicationDate.isAfter(LocalDate.now().minusMonths(6));
    }
}
