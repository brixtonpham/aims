package com.aims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Main application class for AIMS (Advanced Inventory Management System)
 * Following Clean Architecture principles
 * 
 * Note: JPA repository scanning is configured in PersistenceConfig.java
 * to avoid bean definition conflicts and maintain separation of concerns.
 */
@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = {
    "com.aims.domain",
    "com.aims.infrastructure.persistence", 
    "com.aims.vnpay.common.entity"
})
@ComponentScan(basePackages = {
    "com.aims.presentation",
    "com.aims.application", 
    "com.aims.domain",
    "com.aims.infrastructure",
    "com.aims.shared",
    "com.aims.vnpay"
}, excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = com.aims.vnpay.common.service.VNPayServiceImpl.class
))
public class AimsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AimsApplication.class, args);
    }
}
