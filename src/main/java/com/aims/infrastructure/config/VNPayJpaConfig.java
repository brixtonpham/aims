package com.aims.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration for VNPay integration
 * Ensures proper entity and repository scanning for VNPay components
 */
@Configuration
@EntityScan(basePackages = {"com.aims.vnpay.common.entity"})
@EnableJpaRepositories(basePackages = {"com.aims.vnpay.common.repository"})
public class VNPayJpaConfig {
    // Configuration class to ensure VNPay entities and repositories are properly scanned
}
