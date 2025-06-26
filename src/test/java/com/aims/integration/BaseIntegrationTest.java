package com.aims.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests
 * Provides common configuration and setup for testing the complete application
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @BeforeEach
    protected void setUp() {
        // Common setup for all integration tests
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up specific test data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
    }

    /**
     * Helper method to clear test data after tests
     */
    protected void clearTestData() {
        // Implementation for clearing test data
    }
}
