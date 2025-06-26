package com.aims.domain.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DeliveryInfo entity (formerly DeliveryInformation)
 * Enhanced with proper validation and business methods
 */
@Entity
@Table(name = "delivery_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;
    
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    @Column(name = "phone", nullable = false)
    private String phone;
    
    @Email(message = "Invalid email format")
    @Column(name = "email")
    private String email;
    
    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    private String address;
    
    @NotBlank(message = "Province is required")
    @Column(name = "province", nullable = false)
    private String province;
    
    @Column(name = "delivery_message")
    private String deliveryMessage;
    
    @PositiveOrZero(message = "Delivery fee must be positive or zero")
    @Column(name = "delivery_fee")
    private Integer deliveryFee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.STANDARD;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deliveryFee == null) {
            calculateDeliveryFee();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Calculate delivery fee based on delivery type and province
     */
    public void calculateDeliveryFee() {
        // Basic delivery fee calculation logic
        int baseFee = 30000; // Base fee of 30,000 VND
        
        // Rush delivery surcharge
        if (deliveryType == DeliveryType.RUSH) {
            baseFee += 20000; // Additional 20,000 VND for rush delivery
        }
        
        // Province-based fee adjustment (simplified)
        if (isRemoteProvince(province)) {
            baseFee += 15000; // Additional 15,000 VND for remote provinces
        }
        
        this.deliveryFee = baseFee;
    }
    
    /**
     * Check if province is considered remote for delivery
     */
    private boolean isRemoteProvince(String province) {
        if (province == null) return false;
        
        // List of remote provinces (simplified)
        String[] remoteProvinces = {
            "Lào Cai", "Điện Biên", "Lai Châu", "Sơn La", "Hà Giang",
            "Cao Bằng", "Bắc Kạn", "Lang Sơn", "Kon Tum", "Gia Lai",
            "Đắk Lắk", "Đắk Nông", "Lâm Đồng", "Cà Mau", "An Giang"
        };
        
        for (String remote : remoteProvinces) {
            if (province.toLowerCase().contains(remote.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate estimated delivery date
     */
    public void calculateEstimatedDeliveryDate() {
        LocalDateTime now = LocalDateTime.now();
        
        switch (deliveryType) {
            case RUSH:
                // Rush delivery: 1-2 days
                estimatedDeliveryDate = now.plusDays(1);
                break;
            case EXPRESS:
                // Express delivery: 2-3 days
                estimatedDeliveryDate = now.plusDays(2);
                break;
            case STANDARD:
            default:
                // Standard delivery: 3-5 days
                estimatedDeliveryDate = now.plusDays(3);
                if (isRemoteProvince(province)) {
                    estimatedDeliveryDate = estimatedDeliveryDate.plusDays(2);
                }
                break;
        }
    }
    
    /**
     * Mark delivery as completed
     */
    public void markAsDelivered() {
        this.actualDeliveryDate = LocalDateTime.now();
    }
    
    /**
     * Check if delivery is completed
     */
    public boolean isDelivered() {
        return actualDeliveryDate != null;
    }
    
    /**
     * Get full address as single string
     */
    public String getFullAddress() {
        return String.format("%s, %s", address, province);
    }
    
    /**
     * Get formatted delivery fee for display
     */
    public String getFormattedDeliveryFee() {
        return deliveryFee != null ? String.format("%,d VND", deliveryFee) : "Free";
    }
    
    /**
     * Check if rush delivery is enabled
     */
    public boolean isRushDelivery() {
        return deliveryType == DeliveryType.RUSH;
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               address != null && !address.trim().isEmpty() &&
               province != null && !province.trim().isEmpty() &&
               deliveryFee != null && deliveryFee >= 0;
    }
    
    /**
     * Factory method to create delivery info with validation
     */
    public static DeliveryInfo create(String name, String phone, String email, 
                                     String address, String province, 
                                     String deliveryMessage, DeliveryType deliveryType) {
        
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
            .name(name)
            .phone(phone)
            .email(email)
            .address(address)
            .province(province)
            .deliveryMessage(deliveryMessage)
            .deliveryType(deliveryType != null ? deliveryType : DeliveryType.STANDARD)
            .build();
            
        deliveryInfo.calculateDeliveryFee();
        deliveryInfo.calculateEstimatedDeliveryDate();
        
        return deliveryInfo;
    }
    
    /**
     * Delivery type enumeration
     */
    public enum DeliveryType {
        STANDARD("Standard Delivery"),
        EXPRESS("Express Delivery"),
        RUSH("Rush Delivery");
        
        private final String displayName;
        
        DeliveryType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
