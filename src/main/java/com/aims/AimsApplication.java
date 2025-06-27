package com.aims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for AIMS (Advanced Inventory Management System)
 * Following Clean Architecture principles
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages = {
    "com.aims.infrastructure.persistence"
})
@EntityScan(basePackages = {
    "com.aims.domain",
    "com.aims.infrastructure.persistence"
})
@ComponentScan(basePackages = {
    "com.aims.presentation",
    "com.aims.application", 
    "com.aims.domain",
    "com.aims.infrastructure",
    "com.aims.shared",
    "com.aims.vnpay"
})
public class AimsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AimsApplication.class, args);
    }
}
