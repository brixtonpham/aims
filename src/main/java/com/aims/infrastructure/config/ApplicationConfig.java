package com.aims.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Main application configuration class for AIMS
 * Configures component scanning, caching, async processing, and web settings
 */
@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableCaching
@ComponentScan(basePackages = {
    "com.aims.presentation.web",
    "com.aims.presentation.boundary",
    "com.aims.application.services",
    "com.aims.domain.*.service",
    "com.aims.infrastructure.persistence",
    "com.aims.infrastructure.external",
    "com.aims.shared"
})
public class ApplicationConfig implements WebMvcConfigurer {

    /**
     * Configure CORS for API endpoints
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Cache manager for application-level caching
     */
    @Bean
    @Profile("!test & !dev")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "products",
            "productsByType", 
            "cartItems",
            "deliveryFees"
        );
    }

    /**
     * Development profile cache manager with shorter TTL
     */
    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        return new ConcurrentMapCacheManager(
            "products",
            "productsByType"
        );
    }
}
