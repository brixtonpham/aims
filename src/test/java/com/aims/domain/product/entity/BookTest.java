package com.aims.domain.product.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Book entity
 * Tests business logic, validation, and book-specific functionality
 */
@DisplayName("Book Entity Tests")
class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .productId(1L)
                .title("Clean Architecture")
                .price(50000)
                .quantity(100)
                .weight(0.5f)
                .rushOrderSupported(true)
                .imageUrl("http://example.com/book.jpg")
                .introduction("A comprehensive guide to software architecture")
                .genre("Programming")
                .pageCount(432)
                .publicationDate(LocalDate.of(2017, 9, 20))
                .authors("Robert C. Martin")
                .publishers("Prentice Hall")
                .coverType("Paperback")
                .build();
    }

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Should create book with builder pattern")
        void createBookWithBuilder_ShouldSetAllFields() {
            // Given & When - using setUp()
            
            // Then
            assertNotNull(book);
            assertEquals(1L, book.getProductId());
            assertEquals("Clean Architecture", book.getTitle());
            assertEquals(50000, book.getPrice());
            assertEquals(100, book.getQuantity());
            assertEquals("Programming", book.getGenre());
            assertEquals(432, book.getPageCount());
            assertEquals("Robert C. Martin", book.getAuthors());
            assertEquals("Prentice Hall", book.getPublishers());
            assertEquals("Paperback", book.getCoverType());
            assertEquals(LocalDate.of(2017, 9, 20), book.getPublicationDate());
        }

        @Test
        @DisplayName("Should create book with no-args constructor")
        void createBookWithNoArgsConstructor_ShouldInitializeEmpty() {
            // When
            Book emptyBook = new Book();
            
            // Then
            assertNotNull(emptyBook);
            assertNull(emptyBook.getProductId());
            assertNull(emptyBook.getTitle());
            assertNull(emptyBook.getGenre());
        }

        @Test
        @DisplayName("Should create book with minimal required fields")
        void createBookWithMinimalFields_ShouldWork() {
            // When
            Book minimalBook = Book.builder()
                    .title("Test Book")
                    .price(10000)
                    .quantity(1)
                    .build();
            
            // Then
            assertNotNull(minimalBook);
            assertEquals("Test Book", minimalBook.getTitle());
            assertEquals(10000, minimalBook.getPrice());
            assertEquals(1, minimalBook.getQuantity());
        }
    }

    @Nested
    @DisplayName("Type and Classification Tests")
    class TypeAndClassificationTests {

        @Test
        @DisplayName("Should return 'book' as product type")
        void getType_ShouldReturnBook() {
            // When
            String type = book.getType();
            
            // Then
            assertEquals("book", type);
        }

        @Test
        @DisplayName("Should identify as book type correctly")
        void bookTypeIdentification_ShouldBeConsistent() {
            // When
            String type = book.getType();
            
            // Then
            assertEquals("book", type);
            assertTrue(type.equalsIgnoreCase("BOOK"));
            assertFalse(type.equals("cd"));
            assertFalse(type.equals("dvd"));
        }
    }

    @Nested
    @DisplayName("Shipping Weight Calculation Tests")
    class ShippingWeightTests {

        @Test
        @DisplayName("Should return base weight when set")
        void getShippingWeight_WithBaseWeight_ShouldReturnBaseWeight() {
            // Given
            book.setWeight(1.2f);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            assertEquals(1.2f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should calculate weight from page count for paperback")
        void getShippingWeight_PaperbackWithPageCount_ShouldCalculateWeight() {
            // Given
            book.setWeight(null); // No base weight
            book.setCoverType("paperback");
            book.setPageCount(400);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            // Expected: 400 pages * 0.5g per page = 200g = 0.2kg
            assertEquals(0.2f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should calculate weight from page count for hardcover")
        void getShippingWeight_HardcoverWithPageCount_ShouldCalculateWeight() {
            // Given
            book.setWeight(null); // No base weight
            book.setCoverType("hardcover");
            book.setPageCount(300);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            // Expected: 300 pages * 1.0g per page = 300g = 0.3kg
            assertEquals(0.3f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should return default weight when no page count")
        void getShippingWeight_NoPageCount_ShouldReturnDefault() {
            // Given
            book.setWeight(null);
            book.setPageCount(null);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            assertEquals(0.5f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should handle zero page count")
        void getShippingWeight_ZeroPageCount_ShouldReturnDefault() {
            // Given
            book.setWeight(null);
            book.setPageCount(0);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            assertEquals(0.5f, weight, 0.001f);
        }
    }

    @Nested
    @DisplayName("Book Availability Tests")
    class BookAvailabilityTests {

        @Test
        @DisplayName("Should be available when in stock and published")
        void isBookAvailable_InStockAndPublished_ShouldReturnTrue() {
            // Given
            book.setQuantity(10);
            book.setPublicationDate(LocalDate.of(2020, 1, 1));
            
            // When
            boolean available = book.isBookAvailable();
            
            // Then
            assertTrue(available);
        }

        @Test
        @DisplayName("Should not be available when out of stock")
        void isBookAvailable_OutOfStock_ShouldReturnFalse() {
            // Given
            book.setQuantity(0);
            book.setPublicationDate(LocalDate.of(2020, 1, 1));
            
            // When
            boolean available = book.isBookAvailable();
            
            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should not be available when not yet published")
        void isBookAvailable_NotYetPublished_ShouldReturnFalse() {
            // Given
            book.setQuantity(10);
            book.setPublicationDate(LocalDate.now().plusDays(30)); // Future date
            
            // When
            boolean available = book.isBookAvailable();
            
            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should not be available when publication date is null")
        void isBookAvailable_NullPublicationDate_ShouldReturnFalse() {
            // Given
            book.setQuantity(10);
            book.setPublicationDate(null);
            
            // When
            boolean available = book.isBookAvailable();
            
            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should be available when published today")
        void isBookAvailable_PublishedToday_ShouldReturnTrue() {
            // Given
            book.setQuantity(10);
            book.setPublicationDate(LocalDate.now());
            
            // When
            boolean available = book.isBookAvailable();
            
            // Then
            assertTrue(available);
        }
    }

    @Nested
    @DisplayName("New Release Tests")
    class NewReleaseTests {

        @Test
        @DisplayName("Should be new release when published within 6 months")
        void isNewRelease_PublishedRecently_ShouldReturnTrue() {
            // Given
            book.setPublicationDate(LocalDate.now().minusMonths(3));
            
            // When
            boolean isNew = book.isNewRelease();
            
            // Then
            assertTrue(isNew);
        }

        @Test
        @DisplayName("Should not be new release when published over 6 months ago")
        void isNewRelease_PublishedLongAgo_ShouldReturnFalse() {
            // Given
            book.setPublicationDate(LocalDate.now().minusMonths(8));
            
            // When
            boolean isNew = book.isNewRelease();
            
            // Then
            assertFalse(isNew);
        }

        @Test
        @DisplayName("Should not be new release when publication date is null")
        void isNewRelease_NullPublicationDate_ShouldReturnFalse() {
            // Given
            book.setPublicationDate(null);
            
            // When
            boolean isNew = book.isNewRelease();
            
            // Then
            assertFalse(isNew);
        }

        @Test
        @DisplayName("Should be new release when published exactly 6 months ago")
        void isNewRelease_PublishedExactly6MonthsAgo_ShouldReturnFalse() {
            // Given
            book.setPublicationDate(LocalDate.now().minusMonths(6));
            
            // When
            boolean isNew = book.isNewRelease();
            
            // Then
            assertFalse(isNew);
        }

        @Test
        @DisplayName("Should be new release when published tomorrow")
        void isNewRelease_PublishedInFuture_ShouldReturnTrue() {
            // Given
            book.setPublicationDate(LocalDate.now().plusDays(1));
            
            // When
            boolean isNew = book.isNewRelease();
            
            // Then
            assertTrue(isNew);
        }
    }

    @Nested
    @DisplayName("Inherited Product Functionality Tests")
    class InheritedFunctionalityTests {

        @Test
        @DisplayName("Should inherit availability check from Product")
        void isAvailable_ShouldWorkFromParentClass() {
            // Given
            book.setQuantity(10);
            
            // When & Then
            assertTrue(book.isAvailable(5));
            assertTrue(book.isAvailable(10));
            assertFalse(book.isAvailable(15));
            assertFalse(book.isAvailable(0));
        }

        @Test
        @DisplayName("Should inherit quantity reservation from Product")
        void reserveQuantity_ShouldWorkFromParentClass() {
            // Given
            book.setQuantity(10);
            
            // When
            boolean reserved = book.reserveQuantity(3);
            
            // Then
            assertTrue(reserved);
            assertEquals(7, book.getQuantity());
        }

        @Test
        @DisplayName("Should inherit rush order support from Product")
        void supportsRushOrder_ShouldWorkFromParentClass() {
            // Given
            book.setRushOrderSupported(true);
            
            // When & Then
            assertTrue(book.supportsRushOrder());
            
            // Given
            book.setRushOrderSupported(false);
            
            // When & Then
            assertFalse(book.supportsRushOrder());
        }

        @Test
        @DisplayName("Should inherit validation from Product")
        void isValid_ShouldWorkFromParentClass() {
            // Given - valid book
            book.setTitle("Valid Book");
            book.setPrice(10000);
            book.setQuantity(5);
            
            // When & Then
            assertTrue(book.isValid());
            
            // Given - invalid book (null title)
            book.setTitle(null);
            
            // When & Then
            assertFalse(book.isValid());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null and empty string fields gracefully")
        void handleNullFields_ShouldNotThrowException() {
            // When & Then
            assertDoesNotThrow(() -> {
                Book book = Book.builder()
                        .title(null)
                        .genre(null)
                        .authors(null)
                        .publishers(null)
                        .coverType(null)
                        .build();
                
                book.getType();
                book.getShippingWeight();
                book.isBookAvailable();
                book.isNewRelease();
            });
        }

        @Test
        @DisplayName("Should handle empty string values appropriately")
        void handleEmptyStrings_ShouldWork() {
            // Given
            Book book = Book.builder()
                    .title("")
                    .genre("")
                    .authors("")
                    .publishers("")
                    .coverType("")
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> {
                assertEquals("book", book.getType());
                book.getShippingWeight();
                book.isBookAvailable();
                book.isNewRelease();
            });
        }

        @Test
        @DisplayName("Should handle negative page count")
        void handleNegativePageCount_ShouldReturnDefault() {
            // Given
            book.setWeight(null);
            book.setPageCount(-100);
            
            // When
            float weight = book.getShippingWeight();
            
            // Then
            assertEquals(0.5f, weight, 0.001f);
        }

        @Test
        @DisplayName("Should handle case-insensitive cover type")
        void handleCoverTypeCaseInsensitive_ShouldWork() {
            // Given
            book.setWeight(null);
            book.setPageCount(200);
            
            // Test different cases
            book.setCoverType("HARDCOVER");
            assertEquals(0.2f, book.getShippingWeight(), 0.001f);
            
            book.setCoverType("HardCover");
            assertEquals(0.2f, book.getShippingWeight(), 0.001f);
            
            book.setCoverType("hardcover");
            assertEquals(0.2f, book.getShippingWeight(), 0.001f);
        }
    }

    @Nested
    @DisplayName("Business Logic Integration Tests")
    class BusinessLogicIntegrationTests {

        @Test
        @DisplayName("Should combine book availability with general product availability")
        void combinedAvailability_ShouldConsiderBothConditions() {
            // Given - Book available but product not in stock
            book.setQuantity(0);
            book.setPublicationDate(LocalDate.of(2020, 1, 1));
            
            // When & Then
            assertFalse(book.isBookAvailable()); // Book-specific availability
            assertFalse(book.isAvailable(1)); // Product-level availability
            
            // Given - Book and product both available
            book.setQuantity(10);
            
            // When & Then
            assertTrue(book.isBookAvailable());
            assertTrue(book.isAvailable(5));
        }

        @Test
        @DisplayName("Should work with realistic book data")
        void realisticBookData_ShouldWorkCorrectly() {
            // Given - Real book example
            Book realBook = Book.builder()
                    .title("Design Patterns: Elements of Reusable Object-Oriented Software")
                    .price(75000)
                    .quantity(25)
                    .genre("Computer Science")
                    .pageCount(395)
                    .publicationDate(LocalDate.of(1994, 10, 31))
                    .authors("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                    .publishers("Addison-Wesley Professional")
                    .coverType("Hardcover")
                    .rushOrderSupported(true)
                    .build();
            
            // When & Then
            assertEquals("book", realBook.getType());
            assertTrue(realBook.isValid());
            assertTrue(realBook.isBookAvailable());
            assertFalse(realBook.isNewRelease()); // Published in 1994
            assertTrue(realBook.supportsRushOrder());
            assertTrue(realBook.isAvailable(20));
            assertEquals(0.395f, realBook.getShippingWeight(), 0.001f); // Hardcover calculation
        }
    }
}