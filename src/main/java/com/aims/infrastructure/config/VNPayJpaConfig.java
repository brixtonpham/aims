package com.aims.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * JPA Configuration for VNPay integration
 * 
 * Note: Entity and repository scanning is now handled in the main application class
 * and PersistenceConfig to avoid bean definition conflicts. This class is kept for 
 * potential future VNPay-specific JPA configurations.
 * 
 * VNPay entities (com.aims.vnpay.common.entity) and repositories 
 * (com.aims.vnpay.common.repository) are automatically scanned by:
 * - AimsApplication.java: @EnableJpaRepositories and @EntityScan annotations
 * - PersistenceConfig.java: Entity manager factory configuration
 */
@Configuration
public class VNPayJpaConfig {
    // VNPay entities and repositories are now scanned by the main application
    // This avoids bean definition conflicts while keeping the config structure clean
}
