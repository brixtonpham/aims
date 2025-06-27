package com.aims.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * JPA Configuration for VNPay integration
 * 
 * Note: Entity and repository scanning is now handled exclusively in PersistenceConfig.java
 * to avoid bean definition conflicts. This class is kept for potential future 
 * VNPay-specific JPA configurations.
 * 
 * VNPay entities (com.aims.vnpay.common.entity) and repositories 
 * (com.aims.vnpay.common.repository) are automatically scanned by:
 * - PersistenceConfig.java: @EnableJpaRepositories and entity manager factory configuration
 * - AimsApplication.java: @EntityScan annotations for entity discovery
 */
@Configuration
public class VNPayJpaConfig {
    // VNPay entities and repositories are now scanned by the main application
    // This avoids bean definition conflicts while keeping the config structure clean
}
