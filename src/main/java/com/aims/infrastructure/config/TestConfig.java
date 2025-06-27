package com.aims.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

/**
 * Test configuration for integration and unit tests
 * Provides test-specific beans and configurations
 */
@Configuration
@Profile("test")
public class TestConfig {

    /**
     * Test database configuration
     */
    @Bean
    @Primary
    public DataSource testDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
                .username("sa")
                .password("")
                .build();
    }

    /**
     * No-op cache manager for tests to avoid caching issues
     */
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new NoOpCacheManager();
    }

    /**
     * Mock email properties for testing
     */
    @Bean
    @Primary
    public NotificationConfig.EmailProperties testEmailProperties() {
        NotificationConfig.EmailProperties props = new NotificationConfig.EmailProperties();
        props.setHost("localhost");
        props.setPort(25);
        props.setUsername("test@aims.com");
        props.setPassword("testpass");
        props.setFromAddress("test@aims.com");
        props.setFromName("AIMS Test");
        return props;
    }
}
