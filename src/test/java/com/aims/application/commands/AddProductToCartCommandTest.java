package com.aims.application.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AddProductToCartCommand
 * Tests command object construction, validation, and data integrity
 */
@DisplayName("AddProductToCartCommand Unit Tests")
class AddProductToCartCommandTest {

    private static final String VALID_CUSTOMER_ID = "customer123";
    private static final Long VALID_PRODUCT_ID = 1L;
    private static final Integer VALID_QUANTITY = 2;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create command with default constructor")
        void defaultConstructor_ShouldCreateCommand() {
            // When
            AddProductToCartCommand command = new AddProductToCartCommand();

            // Then
            assertNotNull(command);
            assertNull(command.getCustomerId());
            assertNull(command.getProductId());
            assertNull(command.getQuantity());
        }

        @Test
        @DisplayName("Should create command with all args constructor")
        void allArgsConstructor_ShouldCreateCommandWithAllFields() {
            // When
            AddProductToCartCommand command = new AddProductToCartCommand(
                VALID_CUSTOMER_ID, 
                VALID_PRODUCT_ID, 
                VALID_QUANTITY
            );

            // Then
            assertNotNull(command);
            assertEquals(VALID_CUSTOMER_ID, command.getCustomerId());
            assertEquals(VALID_PRODUCT_ID, command.getProductId());
            assertEquals(VALID_QUANTITY, command.getQuantity());
        }

        @Test
        @DisplayName("Should create command with builder pattern")
        void builder_ShouldCreateCommandWithAllFields() {
            // When
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            // Then
            assertNotNull(command);
            assertEquals(VALID_CUSTOMER_ID, command.getCustomerId());
            assertEquals(VALID_PRODUCT_ID, command.getProductId());
            assertEquals(VALID_QUANTITY, command.getQuantity());
        }

        @Test
        @DisplayName("Should create command with builder pattern - partial fields")
        void builder_PartialFields_ShouldCreateCommandWithSpecifiedFields() {
            // When
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .build();

            // Then
            assertNotNull(command);
            assertEquals(VALID_CUSTOMER_ID, command.getCustomerId());
            assertEquals(VALID_PRODUCT_ID, command.getProductId());
            assertNull(command.getQuantity());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        private AddProductToCartCommand command;

        @BeforeEach
        void setUp() {
            command = new AddProductToCartCommand();
        }

        @Test
        @DisplayName("Should set and get customer ID")
        void customerIdGetterSetter_ShouldWorkCorrectly() {
            // When
            command.setCustomerId(VALID_CUSTOMER_ID);

            // Then
            assertEquals(VALID_CUSTOMER_ID, command.getCustomerId());
        }

        @Test
        @DisplayName("Should set and get product ID")
        void productIdGetterSetter_ShouldWorkCorrectly() {
            // When
            command.setProductId(VALID_PRODUCT_ID);

            // Then
            assertEquals(VALID_PRODUCT_ID, command.getProductId());
        }

        @Test
        @DisplayName("Should set and get quantity")
        void quantityGetterSetter_ShouldWorkCorrectly() {
            // When
            command.setQuantity(VALID_QUANTITY);

            // Then
            assertEquals(VALID_QUANTITY, command.getQuantity());
        }

        @Test
        @DisplayName("Should handle null values")
        void settersWithNullValues_ShouldAcceptNulls() {
            // When
            command.setCustomerId(null);
            command.setProductId(null);
            command.setQuantity(null);

            // Then
            assertNull(command.getCustomerId());
            assertNull(command.getProductId());
            assertNull(command.getQuantity());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void equals_SameFieldValues_ShouldBeEqual() {
            // Given
            AddProductToCartCommand command1 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            AddProductToCartCommand command2 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            // Then
            assertEquals(command1, command2);
            assertEquals(command1.hashCode(), command2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when customer ID differs")
        void equals_DifferentCustomerId_ShouldNotBeEqual() {
            // Given
            AddProductToCartCommand command1 = AddProductToCartCommand.builder()
                .customerId("customer1")
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            AddProductToCartCommand command2 = AddProductToCartCommand.builder()
                .customerId("customer2")
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            // Then
            assertNotEquals(command1, command2);
            assertNotEquals(command1.hashCode(), command2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when product ID differs")
        void equals_DifferentProductId_ShouldNotBeEqual() {
            // Given
            AddProductToCartCommand command1 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(1L)
                .quantity(VALID_QUANTITY)
                .build();

            AddProductToCartCommand command2 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(2L)
                .quantity(VALID_QUANTITY)
                .build();

            // Then
            assertNotEquals(command1, command2);
        }

        @Test
        @DisplayName("Should not be equal when quantity differs")
        void equals_DifferentQuantity_ShouldNotBeEqual() {
            // Given
            AddProductToCartCommand command1 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(1)
                .build();

            AddProductToCartCommand command2 = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(2)
                .build();

            // Then
            assertNotEquals(command1, command2);
        }

        @Test
        @DisplayName("Should handle null comparisons")
        void equals_WithNullValues_ShouldHandleCorrectly() {
            // Given
            AddProductToCartCommand command1 = new AddProductToCartCommand();
            AddProductToCartCommand command2 = new AddProductToCartCommand();

            // Then
            assertEquals(command1, command2); // Both have null values
            assertNotEquals(command1, null); // Command should not equal null
            assertNotEquals(command1, "not a command"); // Command should not equal different type
        }

        @Test
        @DisplayName("Should be equal to itself")
        void equals_SameInstance_ShouldBeEqual() {
            // Given
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            // Then
            assertEquals(command, command);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate string representation with all fields")
        void toString_WithAllFields_ShouldContainAllFieldValues() {
            // Given
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(VALID_CUSTOMER_ID)
                .productId(VALID_PRODUCT_ID)
                .quantity(VALID_QUANTITY)
                .build();

            // When
            String toString = command.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains(VALID_CUSTOMER_ID));
            assertTrue(toString.contains(VALID_PRODUCT_ID.toString()));
            assertTrue(toString.contains(VALID_QUANTITY.toString()));
            assertTrue(toString.contains("AddProductToCartCommand"));
        }

        @Test
        @DisplayName("Should generate string representation with null fields")
        void toString_WithNullFields_ShouldHandleNulls() {
            // Given
            AddProductToCartCommand command = new AddProductToCartCommand();

            // When
            String toString = command.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("AddProductToCartCommand"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should create valid command with typical e-commerce values")
        void createCommand_TypicalEcommerceScenario_ShouldBeValid() {
            // Given
            String customerId = "user_12345";
            Long productId = 100L;
            Integer quantity = 3;

            // When
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(customerId)
                .productId(productId)
                .quantity(quantity)
                .build();

            // Then
            assertNotNull(command);
            assertEquals(customerId, command.getCustomerId());
            assertEquals(productId, command.getProductId());
            assertEquals(quantity, command.getQuantity());
        }

        @Test
        @DisplayName("Should handle edge case values")
        void createCommand_EdgeCaseValues_ShouldHandleCorrectly() {
            // Given
            String longCustomerId = "a".repeat(255); // Very long customer ID
            Long maxProductId = Long.MAX_VALUE;
            Integer maxQuantity = Integer.MAX_VALUE;

            // When
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(longCustomerId)
                .productId(maxProductId)
                .quantity(maxQuantity)
                .build();

            // Then
            assertNotNull(command);
            assertEquals(longCustomerId, command.getCustomerId());
            assertEquals(maxProductId, command.getProductId());
            assertEquals(maxQuantity, command.getQuantity());
        }

        @Test
        @DisplayName("Should create command for minimum valid values")
        void createCommand_MinimumValidValues_ShouldWork() {
            // Given
            String customerId = "1";
            Long productId = 1L;
            Integer quantity = 1;

            // When
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(customerId)
                .productId(productId)
                .quantity(quantity)
                .build();

            // Then
            assertNotNull(command);
            assertEquals(customerId, command.getCustomerId());
            assertEquals(productId, command.getProductId());
            assertEquals(quantity, command.getQuantity());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should allow modification of fields after creation")
        void modifyFields_AfterCreation_ShouldAllowChanges() {
            // Given
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId("original")
                .productId(1L)
                .quantity(1)
                .build();

            // When
            command.setCustomerId("modified");
            command.setProductId(2L);
            command.setQuantity(3);

            // Then
            assertEquals("modified", command.getCustomerId());
            assertEquals(2L, command.getProductId());
            assertEquals(3, command.getQuantity());
        }
    }
}