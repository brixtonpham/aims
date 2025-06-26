package com.aims.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * CD entity extending Product
 * Specific attributes and behavior for CDs
 */
@Entity
@DiscriminatorValue("cd")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CD extends Product {
    
    @Column(name = "genre")
    private String genre;
    
    @Column(name = "artist")
    private String artist;
    
    @Column(name = "record_label")
    private String recordLabel;
    
    @Column(name = "release_date")
    private String releaseDate;
    
    @Positive(message = "Track count must be positive")
    @Column(name = "track_count")
    private Integer trackCount;
    
    @Override
    public String getType() {
        return "cd";
    }
    
    /**
     * Calculate shipping weight for CD
     */
    @Override
    public float getShippingWeight() {
        float baseWeight = super.getShippingWeight();
        if (baseWeight > 0) {
            return baseWeight;
        }
        
        // Standard CD weight is approximately 0.15 kg
        return 0.15f;
    }
}
