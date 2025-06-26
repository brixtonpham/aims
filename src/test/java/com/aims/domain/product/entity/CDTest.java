package com.aims.domain.product.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CD entity
 * Tests CD-specific functionality and inherited behavior from Product
 */
@DisplayName("CD Entity Tests")
class CDTest {

    private CD cd;

    @BeforeEach
    void setUp() {
        cd = CD.builder()
            .title("Greatest Hits")
            .price(250000) // 250,000 VND
            .quantity(50)
            .weight(0.12f)
            .rushOrderSupported(true)
            .artist("Test Artist")
            .genre("Pop")
            .recordLabel("Test Records")
            .releaseDate("2023-01-15")
            .trackCount(12)
            .build();
    }

    @Nested
    @DisplayName("CD Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create CD with all fields using builder")
        void createCDWithBuilder_AllFields_Success() {
            // Act
            CD testCD = CD.builder()
                .title("Test Album")
                .price(300000)
                .quantity(100)
                .artist("Test Artist")
                .genre("Rock")
                .recordLabel("Rock Records")
                .releaseDate("2023-12-01")
                .trackCount(15)
                .build();

            // Assert
            assertNotNull(testCD);
            assertEquals("Test Album", testCD.getTitle());
            assertEquals(300000, testCD.getPrice());
            assertEquals(100, testCD.getQuantity());
            assertEquals("Test Artist", testCD.getArtist());
            assertEquals("Rock", testCD.getGenre());
            assertEquals("Rock Records", testCD.getRecordLabel());
            assertEquals("2023-12-01", testCD.getReleaseDate());
            assertEquals(15, testCD.getTrackCount());
        }

        @Test
        @DisplayName("Should create CD with minimal required fields")
        void createCDWithBuilder_MinimalFields_Success() {
            // Act
            CD minimalCD = CD.builder()
                .title("Minimal Album")
                .price(150000)
                .quantity(10)
                .build();

            // Assert
            assertNotNull(minimalCD);
            assertEquals("Minimal Album", minimalCD.getTitle());
            assertEquals(150000, minimalCD.getPrice());
            assertEquals(10, minimalCD.getQuantity());
            assertNull(minimalCD.getArtist());
            assertNull(minimalCD.getGenre());
        }

        @Test
        @DisplayName("Should create CD using no-args constructor")
        void createCDWithNoArgsConstructor_Success() {
            // Act
            CD emptyCD = new CD();

            // Assert
            assertNotNull(emptyCD);
            assertNull(emptyCD.getTitle());
            assertNull(emptyCD.getArtist());
            assertNull(emptyCD.getGenre());
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("Should return 'cd' as product type")
        void getType_ShouldReturnCD() {
            // Act
            String type = cd.getType();

            // Assert
            assertEquals("cd", type);
        }
    }

    @Nested
    @DisplayName("Shipping Weight Tests")
    class ShippingWeightTests {

        @Test
        @DisplayName("Should return base weight when weight is set")
        void getShippingWeight_WithBaseWeight_ReturnsBaseWeight() {
            // Arrange
            cd.setWeight(0.2f);

            // Act
            float weight = cd.getShippingWeight();

            // Assert
            assertEquals(0.2f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should return standard CD weight when no weight is set")
        void getShippingWeight_WithoutBaseWeight_ReturnsStandardWeight() {
            // Arrange
            cd.setWeight(null);

            // Act
            float weight = cd.getShippingWeight();

            // Assert
            assertEquals(0.15f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should return standard CD weight when weight is zero")
        void getShippingWeight_WithZeroWeight_ReturnsStandardWeight() {
            // Arrange
            cd.setWeight(0.0f);

            // Act
            float weight = cd.getShippingWeight();

            // Assert
            assertEquals(0.15f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should return base weight when weight is greater than zero")
        void getShippingWeight_WithPositiveWeight_ReturnsBaseWeight() {
            // Arrange
            cd.setWeight(0.25f);

            // Act
            float weight = cd.getShippingWeight();

            // Assert
            assertEquals(0.25f, weight, 0.001f);
        }
    }

    @Nested
    @DisplayName("Inherited Product Behavior Tests")
    class InheritedBehaviorTests {

        @Test
        @DisplayName("Should be available when sufficient quantity")
        void isAvailable_SufficientQuantity_ReturnsTrue() {
            // Act & Assert
            assertTrue(cd.isAvailable(10));
            assertTrue(cd.isAvailable(50));
            assertTrue(cd.isAvailable(1));
        }

        @Test
        @DisplayName("Should not be available when insufficient quantity")
        void isAvailable_InsufficientQuantity_ReturnsFalse() {
            // Act & Assert
            assertFalse(cd.isAvailable(51));
            assertFalse(cd.isAvailable(100));
        }

        @Test
        @DisplayName("Should not be available with zero or negative quantity request")
        void isAvailable_InvalidQuantityRequest_ReturnsFalse() {
            // Act & Assert
            assertFalse(cd.isAvailable(0));
            assertFalse(cd.isAvailable(-1));
        }

        @Test
        @DisplayName("Should be in stock when quantity is positive")
        void isInStock_PositiveQuantity_ReturnsTrue() {
            // Act & Assert
            assertTrue(cd.isInStock());
        }

        @Test
        @DisplayName("Should not be in stock when quantity is zero")
        void isInStock_ZeroQuantity_ReturnsFalse() {
            // Arrange
            cd.setQuantity(0);

            // Act & Assert
            assertFalse(cd.isInStock());
        }

        @Test
        @DisplayName("Should not be in stock when quantity is null")
        void isInStock_NullQuantity_ReturnsFalse() {
            // Arrange
            cd.setQuantity(null);

            // Act & Assert
            assertFalse(cd.isInStock());
        }

        @Test
        @DisplayName("Should support rush order when enabled")
        void supportsRushOrder_WhenEnabled_ReturnsTrue() {
            // Arrange
            cd.setRushOrderSupported(true);

            // Act & Assert
            assertTrue(cd.supportsRushOrder());
        }

        @Test
        @DisplayName("Should not support rush order when disabled")
        void supportsRushOrder_WhenDisabled_ReturnsFalse() {
            // Arrange
            cd.setRushOrderSupported(false);

            // Act & Assert
            assertFalse(cd.supportsRushOrder());
        }

        @Test
        @DisplayName("Should not support rush order when null")
        void supportsRushOrder_WhenNull_ReturnsFalse() {
            // Arrange
            cd.setRushOrderSupported(null);

            // Act & Assert
            assertFalse(cd.supportsRushOrder());
        }
    }

    @Nested
    @DisplayName("Business Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should be valid with all required fields")
        void isValid_AllRequiredFields_ReturnsTrue() {
            // Act & Assert
            assertTrue(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with null title")
        void isValid_NullTitle_ReturnsFalse() {
            // Arrange
            cd.setTitle(null);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with empty title")
        void isValid_EmptyTitle_ReturnsFalse() {
            // Arrange
            cd.setTitle("");

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with blank title")
        void isValid_BlankTitle_ReturnsFalse() {
            // Arrange
            cd.setTitle("   ");

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with null price")
        void isValid_NullPrice_ReturnsFalse() {
            // Arrange
            cd.setPrice(null);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with zero price")
        void isValid_ZeroPrice_ReturnsFalse() {
            // Arrange
            cd.setPrice(0);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with negative price")
        void isValid_NegativePrice_ReturnsFalse() {
            // Arrange
            cd.setPrice(-1000);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with null quantity")
        void isValid_NullQuantity_ReturnsFalse() {
            // Arrange
            cd.setQuantity(null);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be invalid with negative quantity")
        void isValid_NegativeQuantity_ReturnsFalse() {
            // Arrange
            cd.setQuantity(-1);

            // Act & Assert
            assertFalse(cd.isValid());
        }

        @Test
        @DisplayName("Should be valid with zero quantity")
        void isValid_ZeroQuantity_ReturnsTrue() {
            // Arrange
            cd.setQuantity(0);

            // Act & Assert
            assertTrue(cd.isValid());
        }
    }

    @Nested
    @DisplayName("CD-Specific Field Tests")
    class CDSpecificFieldTests {

        @Test
        @DisplayName("Should set and get artist correctly")
        void setAndGetArtist_ValidValue_Success() {
            // Arrange
            String artist = "New Artist";

            // Act
            cd.setArtist(artist);

            // Assert
            assertEquals(artist, cd.getArtist());
        }

        @Test
        @DisplayName("Should set and get genre correctly")
        void setAndGetGenre_ValidValue_Success() {
            // Arrange
            String genre = "Jazz";

            // Act
            cd.setGenre(genre);

            // Assert
            assertEquals(genre, cd.getGenre());
        }

        @Test
        @DisplayName("Should set and get record label correctly")
        void setAndGetRecordLabel_ValidValue_Success() {
            // Arrange
            String recordLabel = "Jazz Records Inc.";

            // Act
            cd.setRecordLabel(recordLabel);

            // Assert
            assertEquals(recordLabel, cd.getRecordLabel());
        }

        @Test
        @DisplayName("Should set and get release date correctly")
        void setAndGetReleaseDate_ValidValue_Success() {
            // Arrange
            String releaseDate = "2024-06-15";

            // Act
            cd.setReleaseDate(releaseDate);

            // Assert
            assertEquals(releaseDate, cd.getReleaseDate());
        }

        @Test
        @DisplayName("Should set and get track count correctly")
        void setAndGetTrackCount_ValidValue_Success() {
            // Arrange
            Integer trackCount = 20;

            // Act
            cd.setTrackCount(trackCount);

            // Assert
            assertEquals(trackCount, cd.getTrackCount());
        }

        @Test
        @DisplayName("Should handle null values for optional fields")
        void setOptionalFields_NullValues_Success() {
            // Act
            cd.setArtist(null);
            cd.setGenre(null);
            cd.setRecordLabel(null);
            cd.setReleaseDate(null);
            cd.setTrackCount(null);

            // Assert
            assertNull(cd.getArtist());
            assertNull(cd.getGenre());
            assertNull(cd.getRecordLabel());
            assertNull(cd.getReleaseDate());
            assertNull(cd.getTrackCount());
            // CD should still be valid as these are optional fields
            assertTrue(cd.isValid());
        }
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should reserve quantity successfully")
        void reserveQuantity_SufficientStock_Success() {
            // Arrange
            int initialQuantity = cd.getQuantity();
            int reserveAmount = 10;

            // Act
            boolean result = cd.reserveQuantity(reserveAmount);

            // Assert
            assertTrue(result);
            assertEquals(initialQuantity - reserveAmount, cd.getQuantity());
        }

        @Test
        @DisplayName("Should fail to reserve when insufficient stock")
        void reserveQuantity_InsufficientStock_Failure() {
            // Arrange
            int initialQuantity = cd.getQuantity();
            int reserveAmount = initialQuantity + 10;

            // Act
            boolean result = cd.reserveQuantity(reserveAmount);

            // Assert
            assertFalse(result);
            assertEquals(initialQuantity, cd.getQuantity()); // Quantity unchanged
        }

        @Test
        @DisplayName("Should release quantity successfully")
        void releaseQuantity_ValidAmount_Success() {
            // Arrange
            int initialQuantity = cd.getQuantity();
            int releaseAmount = 5;

            // Act
            cd.releaseQuantity(releaseAmount);

            // Assert
            assertEquals(initialQuantity + releaseAmount, cd.getQuantity());
        }

        @Test
        @DisplayName("Should handle zero release amount")
        void releaseQuantity_ZeroAmount_NoChange() {
            // Arrange
            int initialQuantity = cd.getQuantity();

            // Act
            cd.releaseQuantity(0);

            // Assert
            assertEquals(initialQuantity, cd.getQuantity());
        }

        @Test
        @DisplayName("Should ignore negative release amount")
        void releaseQuantity_NegativeAmount_NoChange() {
            // Arrange
            int initialQuantity = cd.getQuantity();

            // Act
            cd.releaseQuantity(-5);

            // Assert
            assertEquals(initialQuantity, cd.getQuantity());
        }
    }

    @Nested
    @DisplayName("Stock Value Tests")
    class StockValueTests {

        @Test
        @DisplayName("Should calculate total stock value correctly")
        void getTotalStockValue_ValidData_ReturnsCorrectValue() {
            // Arrange
            cd.setQuantity(10);
            cd.setPrice(100000); // 100,000 VND per CD

            // Act
            long totalValue = cd.getTotalStockValue();

            // Assert
            assertEquals(1000000L, totalValue); // 10 * 100,000 = 1,000,000
        }

        @Test
        @DisplayName("Should return zero when quantity is zero")
        void getTotalStockValue_ZeroQuantity_ReturnsZero() {
            // Arrange
            cd.setQuantity(0);
            cd.setPrice(100000);

            // Act
            long totalValue = cd.getTotalStockValue();

            // Assert
            assertEquals(0L, totalValue);
        }

        @Test
        @DisplayName("Should return zero when price is null")
        void getTotalStockValue_NullPrice_ReturnsZero() {
            // Arrange
            cd.setQuantity(10);
            cd.setPrice(null);

            // Act
            long totalValue = cd.getTotalStockValue();

            // Assert
            assertEquals(0L, totalValue);
        }

        @Test
        @DisplayName("Should return zero when quantity is null")
        void getTotalStockValue_NullQuantity_ReturnsZero() {
            // Arrange
            cd.setQuantity(null);
            cd.setPrice(100000);

            // Act
            long totalValue = cd.getTotalStockValue();

            // Assert
            assertEquals(0L, totalValue);
        }
    }
}