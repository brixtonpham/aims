package com.aims.domain.product.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DVD entity
 * Tests DVD-specific functionality and inherited Product behavior
 */
@DisplayName("DVD Entity Tests")
class DVDTest {

    private DVD dvd;

    @BeforeEach
    void setUp() {
        dvd = DVD.builder()
            .title("The Matrix")
            .price(25000)
            .quantity(50)
            .weight(0.1f)
            .rushOrderSupported(true)
            .imageUrl("https://example.com/matrix.jpg")
            .introduction("A sci-fi action movie")
            .genre("Sci-Fi")
            .director("The Wachowskis")
            .studio("Warner Bros")
            .releaseDate("1999-03-31")
            .runtimeMinutes(136)
            .subtitleLanguages("English, Vietnamese, Chinese")
            .dubbingLanguages("English, Vietnamese")
            .build();
    }

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorBuilderTests {

        @Test
        @DisplayName("Should create DVD with builder successfully")
        void createDVD_WithBuilder_Success() {
            // Assert all fields are set correctly
            assertEquals("The Matrix", dvd.getTitle());
            assertEquals(25000, dvd.getPrice());
            assertEquals(50, dvd.getQuantity());
            assertEquals(0.1f, dvd.getWeight());
            assertTrue(dvd.getRushOrderSupported());
            assertEquals("Sci-Fi", dvd.getGenre());
            assertEquals("The Wachowskis", dvd.getDirector());
            assertEquals("Warner Bros", dvd.getStudio());
            assertEquals("1999-03-31", dvd.getReleaseDate());
            assertEquals(136, dvd.getRuntimeMinutes());
            assertEquals("English, Vietnamese, Chinese", dvd.getSubtitleLanguages());
            assertEquals("English, Vietnamese", dvd.getDubbingLanguages());
        }

        @Test
        @DisplayName("Should create DVD with no-args constructor")
        void createDVD_WithNoArgsConstructor_Success() {
            DVD emptyDVD = new DVD();
            
            assertNotNull(emptyDVD);
            assertNull(emptyDVD.getTitle());
            assertNull(emptyDVD.getGenre());
            assertNull(emptyDVD.getDirector());
        }

        @Test
        @DisplayName("Should create DVD with minimal required fields")
        void createDVD_WithMinimalFields_Success() {
            DVD minimalDVD = DVD.builder()
                .title("Test Movie")
                .price(15000)
                .quantity(10)
                .build();
            
            assertEquals("Test Movie", minimalDVD.getTitle());
            assertEquals(15000, minimalDVD.getPrice());
            assertEquals(10, minimalDVD.getQuantity());
            assertNull(minimalDVD.getGenre());
            assertNull(minimalDVD.getDirector());
        }
    }

    @Nested
    @DisplayName("DVD-Specific Method Tests")
    class DVDSpecificMethodTests {

        @Test
        @DisplayName("Should return correct type")
        void getType_ShouldReturnDVD() {
            assertEquals("dvd", dvd.getType());
        }

        @Test
        @DisplayName("Should return default shipping weight when weight not set")
        void getShippingWeight_NoWeightSet_ShouldReturnDefaultWeight() {
            DVD dvdNoWeight = DVD.builder()
                .title("Test Movie")
                .price(15000)
                .quantity(10)
                .build();
            
            assertEquals(0.1f, dvdNoWeight.getShippingWeight());
        }

        @Test
        @DisplayName("Should return actual weight when weight is set")
        void getShippingWeight_WeightSet_ShouldReturnActualWeight() {
            DVD dvdWithWeight = DVD.builder()
                .title("Test Movie")
                .price(15000)
                .quantity(10)
                .weight(0.2f)
                .build();
            
            assertEquals(0.2f, dvdWithWeight.getShippingWeight());
        }
    }

    @Nested
    @DisplayName("Inherited Product Behavior Tests")
    class InheritedBehaviorTests {

        @Test
        @DisplayName("Should validate DVD correctly")
        void isValid_ValidDVD_ShouldReturnTrue() {
            assertTrue(dvd.isValid());
        }

        @Test
        @DisplayName("Should invalidate DVD with null title")
        void isValid_NullTitle_ShouldReturnFalse() {
            dvd.setTitle(null);
            assertFalse(dvd.isValid());
        }

        @Test
        @DisplayName("Should invalidate DVD with empty title")
        void isValid_EmptyTitle_ShouldReturnFalse() {
            dvd.setTitle("");
            assertFalse(dvd.isValid());
        }

        @Test
        @DisplayName("Should invalidate DVD with null price")
        void isValid_NullPrice_ShouldReturnFalse() {
            dvd.setPrice(null);
            assertFalse(dvd.isValid());
        }

        @Test
        @DisplayName("Should invalidate DVD with negative price")
        void isValid_NegativePrice_ShouldReturnFalse() {
            dvd.setPrice(-1000);
            assertFalse(dvd.isValid());
        }

        @Test
        @DisplayName("Should invalidate DVD with negative quantity")
        void isValid_NegativeQuantity_ShouldReturnFalse() {
            dvd.setQuantity(-5);
            assertFalse(dvd.isValid());
        }

        @Test
        @DisplayName("Should check availability correctly")
        void isAvailable_SufficientStock_ShouldReturnTrue() {
            assertTrue(dvd.isAvailable(30)); // Request 30, have 50
        }

        @Test
        @DisplayName("Should check availability correctly for insufficient stock")
        void isAvailable_InsufficientStock_ShouldReturnFalse() {
            assertFalse(dvd.isAvailable(60)); // Request 60, have 50
        }

        @Test
        @DisplayName("Should check availability correctly for exact stock")
        void isAvailable_ExactStock_ShouldReturnTrue() {
            assertTrue(dvd.isAvailable(50)); // Request 50, have 50
        }

        @Test
        @DisplayName("Should check availability correctly for zero request")
        void isAvailable_ZeroRequest_ShouldReturnFalse() {
            assertFalse(dvd.isAvailable(0));
        }

        @Test
        @DisplayName("Should be in stock when quantity is positive")
        void isInStock_PositiveQuantity_ShouldReturnTrue() {
            assertTrue(dvd.isInStock());
        }

        @Test
        @DisplayName("Should not be in stock when quantity is zero")
        void isInStock_ZeroQuantity_ShouldReturnFalse() {
            dvd.setQuantity(0);
            assertFalse(dvd.isInStock());
        }

        @Test
        @DisplayName("Should support rush order when enabled")
        void supportsRushOrder_Enabled_ShouldReturnTrue() {
            assertTrue(dvd.supportsRushOrder());
        }

        @Test
        @DisplayName("Should not support rush order when disabled")
        void supportsRushOrder_Disabled_ShouldReturnFalse() {
            dvd.setRushOrderSupported(false);
            assertFalse(dvd.supportsRushOrder());
        }

        @Test
        @DisplayName("Should calculate total stock value correctly")
        void getTotalStockValue_ShouldCalculateCorrectly() {
            // 50 quantity * 25000 price = 1,250,000
            assertEquals(1250000L, dvd.getTotalStockValue());
        }

        @Test
        @DisplayName("Should return zero stock value when price is null")
        void getTotalStockValue_NullPrice_ShouldReturnZero() {
            dvd.setPrice(null);
            assertEquals(0L, dvd.getTotalStockValue());
        }

        @Test
        @DisplayName("Should return zero stock value when quantity is null")
        void getTotalStockValue_NullQuantity_ShouldReturnZero() {
            dvd.setQuantity(null);
            assertEquals(0L, dvd.getTotalStockValue());
        }
    }

    @Nested
    @DisplayName("Quantity Management Tests")
    class QuantityManagementTests {

        @Test
        @DisplayName("Should reserve quantity successfully")
        void reserveQuantity_SufficientStock_ShouldReturnTrue() {
            boolean result = dvd.reserveQuantity(20);
            
            assertTrue(result);
            assertEquals(30, dvd.getQuantity()); // 50 - 20 = 30
        }

        @Test
        @DisplayName("Should fail to reserve quantity when insufficient stock")
        void reserveQuantity_InsufficientStock_ShouldReturnFalse() {
            boolean result = dvd.reserveQuantity(60);
            
            assertFalse(result);
            assertEquals(50, dvd.getQuantity()); // Should remain unchanged
        }

        @Test
        @DisplayName("Should reserve exact available quantity")
        void reserveQuantity_ExactQuantity_ShouldReturnTrue() {
            boolean result = dvd.reserveQuantity(50);
            
            assertTrue(result);
            assertEquals(0, dvd.getQuantity());
        }

        @Test
        @DisplayName("Should release quantity correctly")
        void releaseQuantity_ShouldIncreaseQuantity() {
            dvd.reserveQuantity(20); // Now has 30
            dvd.releaseQuantity(10);
            
            assertEquals(40, dvd.getQuantity()); // 30 + 10 = 40
        }

        @Test
        @DisplayName("Should not change quantity when releasing zero")
        void releaseQuantity_Zero_ShouldNotChangeQuantity() {
            int originalQuantity = dvd.getQuantity();
            dvd.releaseQuantity(0);
            
            assertEquals(originalQuantity, dvd.getQuantity());
        }

        @Test
        @DisplayName("Should not change quantity when releasing negative amount")
        void releaseQuantity_Negative_ShouldNotChangeQuantity() {
            int originalQuantity = dvd.getQuantity();
            dvd.releaseQuantity(-5);
            
            assertEquals(originalQuantity, dvd.getQuantity());
        }
    }

    @Nested
    @DisplayName("DVD-Specific Field Validation Tests")
    class DVDFieldValidationTests {

        @Test
        @DisplayName("Should handle null genre")
        void setGenre_Null_ShouldBeAllowed() {
            assertDoesNotThrow(() -> dvd.setGenre(null));
            assertNull(dvd.getGenre());
        }

        @Test
        @DisplayName("Should handle empty genre")
        void setGenre_Empty_ShouldBeAllowed() {
            assertDoesNotThrow(() -> dvd.setGenre(""));
            assertEquals("", dvd.getGenre());
        }

        @Test
        @DisplayName("Should handle null director")
        void setDirector_Null_ShouldBeAllowed() {
            assertDoesNotThrow(() -> dvd.setDirector(null));
            assertNull(dvd.getDirector());
        }

        @Test
        @DisplayName("Should handle null runtime")
        void setRuntimeMinutes_Null_ShouldBeAllowed() {
            assertDoesNotThrow(() -> dvd.setRuntimeMinutes(null));
            assertNull(dvd.getRuntimeMinutes());
        }

        @Test
        @DisplayName("Should set positive runtime correctly")
        void setRuntimeMinutes_Positive_ShouldBeSet() {
            dvd.setRuntimeMinutes(120);
            assertEquals(120, dvd.getRuntimeMinutes());
        }

        @Test
        @DisplayName("Should handle subtitle languages")
        void setSubtitleLanguages_ShouldBeSet() {
            String subtitles = "English, French, Spanish";
            dvd.setSubtitleLanguages(subtitles);
            assertEquals(subtitles, dvd.getSubtitleLanguages());
        }

        @Test
        @DisplayName("Should handle dubbing languages")
        void setDubbingLanguages_ShouldBeSet() {
            String dubbing = "English, French";
            dvd.setDubbingLanguages(dubbing);
            assertEquals(dubbing, dvd.getDubbingLanguages());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long title")
        void setTitle_VeryLong_ShouldBeAllowed() {
            String longTitle = "A".repeat(1000);
            assertDoesNotThrow(() -> dvd.setTitle(longTitle));
            assertEquals(longTitle, dvd.getTitle());
        }

        @Test
        @DisplayName("Should handle very large price")
        void setPrice_VeryLarge_ShouldBeAllowed() {
            Integer largePrice = Integer.MAX_VALUE;
            assertDoesNotThrow(() -> dvd.setPrice(largePrice));
            assertEquals(largePrice, dvd.getPrice());
        }

        @Test
        @DisplayName("Should handle very large quantity")
        void setQuantity_VeryLarge_ShouldBeAllowed() {
            Integer largeQuantity = Integer.MAX_VALUE;
            assertDoesNotThrow(() -> dvd.setQuantity(largeQuantity));
            assertEquals(largeQuantity, dvd.getQuantity());
        }

        @Test
        @DisplayName("Should handle very large runtime")
        void setRuntimeMinutes_VeryLarge_ShouldBeAllowed() {
            Integer largeRuntime = Integer.MAX_VALUE;
            assertDoesNotThrow(() -> dvd.setRuntimeMinutes(largeRuntime));
            assertEquals(largeRuntime, dvd.getRuntimeMinutes());
        }
    }

    @Nested
    @DisplayName("Business Logic Integration Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain consistency after multiple operations")
        void multipleOperations_ShouldMaintainConsistency() {
            // Initial state
            assertTrue(dvd.isValid());
            assertTrue(dvd.isInStock());
            assertEquals(50, dvd.getQuantity());
            
            // Reserve some quantity
            assertTrue(dvd.reserveQuantity(20));
            assertEquals(30, dvd.getQuantity());
            assertTrue(dvd.isValid());
            assertTrue(dvd.isInStock());
            
            // Reserve more
            assertTrue(dvd.reserveQuantity(25));
            assertEquals(5, dvd.getQuantity());
            assertTrue(dvd.isValid());
            assertTrue(dvd.isInStock());
            
            // Try to reserve more than available
            assertFalse(dvd.reserveQuantity(10));
            assertEquals(5, dvd.getQuantity()); // Should remain unchanged
            
            // Release some quantity
            dvd.releaseQuantity(15);
            assertEquals(20, dvd.getQuantity());
            assertTrue(dvd.isValid());
            assertTrue(dvd.isInStock());
        }

        @Test
        @DisplayName("Should calculate shipping weight based on business rules")
        void shippingWeight_BusinessRules_ShouldBeCorrect() {
            // Test default DVD weight
            DVD defaultWeightDVD = new DVD();
            assertEquals(0.1f, defaultWeightDVD.getShippingWeight());
            
            // Test custom weight
            DVD customWeightDVD = DVD.builder()
                .weight(0.15f)
                .build();
            assertEquals(0.15f, customWeightDVD.getShippingWeight());
            
            // Test inherited weight logic
            assertEquals(0.1f, dvd.getShippingWeight());
        }
    }
}