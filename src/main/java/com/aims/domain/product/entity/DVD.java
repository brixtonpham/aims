package com.aims.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DVD entity extending Product
 * Specific attributes and behavior for DVDs
 */
@Entity
@DiscriminatorValue("dvd")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class DVD extends Product {
    
    @Column(name = "genre")
    private String genre;
    
    @Column(name = "director")
    private String director;
    
    @Column(name = "studio")
    private String studio;
    
    @Column(name = "release_date")
    private String releaseDate;
    
    @Positive(message = "Runtime must be positive")
    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;
    
    @Column(name = "subtitle_languages")
    private String subtitleLanguages;
    
    @Column(name = "dubbing_languages")
    private String dubbingLanguages;
    
    @Override
    public String getType() {
        return "dvd";
    }
    
    /**
     * Calculate shipping weight for DVD
     */
    @Override
    public float getShippingWeight() {
        float baseWeight = super.getShippingWeight();
        if (baseWeight > 0) {
            return baseWeight;
        }
        
        // Standard DVD weight is approximately 0.1 kg
        return 0.1f;
    }
}
