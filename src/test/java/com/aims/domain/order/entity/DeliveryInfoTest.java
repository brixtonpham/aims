package com.aims.domain.order.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeliveryInfo entity
 * Tests business logic and validation methods
 */
@DisplayName("DeliveryInfo Unit Tests")
class DeliveryInfoTest {

    private DeliveryInfo deliveryInfo;

    @BeforeEach
    void setUp() {
        deliveryInfo = DeliveryInfo.builder()
                .name("John Doe")
                .phone("0123456789")
                .email("john.doe@example.com")
                .address("123 Main Street")
                .province("Hà Nội")
                .deliveryMessage("Please call before delivery")
                .deliveryType(DeliveryInfo.DeliveryType.STANDARD)
                .build();
    }

    @Nested
    @DisplayName("Delivery Fee Calculation Tests")
    class DeliveryFeeCalculationTests {

        @Test
        @DisplayName("Should calculate standard delivery fee for normal province")
        void calculateDeliveryFee_StandardDeliveryNormalProvince_ShouldReturnBaseFee() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("Hà Nội");

            // When
            deliveryInfo.calculateDeliveryFee();

            // Then
            assertEquals(30000, deliveryInfo.getDeliveryFee());
        }

        @Test
        @DisplayName("Should calculate rush delivery fee for normal province")
        void calculateDeliveryFee_RushDeliveryNormalProvince_ShouldAddRushSurcharge() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.RUSH);
            deliveryInfo.setProvince("Hà Nội");

            // When
            deliveryInfo.calculateDeliveryFee();

            // Then
            assertEquals(50000, deliveryInfo.getDeliveryFee()); // 30000 + 20000
        }

        @Test
        @DisplayName("Should calculate standard delivery fee for remote province")
        void calculateDeliveryFee_StandardDeliveryRemoteProvince_ShouldAddRemoteSurcharge() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("Lào Cai");

            // When
            deliveryInfo.calculateDeliveryFee();

            // Then
            assertEquals(45000, deliveryInfo.getDeliveryFee()); // 30000 + 15000
        }

        @Test
        @DisplayName("Should calculate rush delivery fee for remote province")
        void calculateDeliveryFee_RushDeliveryRemoteProvince_ShouldAddBothSurcharges() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.RUSH);
            deliveryInfo.setProvince("Điện Biên");

            // When
            deliveryInfo.calculateDeliveryFee();

            // Then
            assertEquals(65000, deliveryInfo.getDeliveryFee()); // 30000 + 20000 + 15000
        }

        @Test
        @DisplayName("Should calculate express delivery fee")
        void calculateDeliveryFee_ExpressDelivery_ShouldReturnBaseFee() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.EXPRESS);
            deliveryInfo.setProvince("Hồ Chí Minh");

            // When
            deliveryInfo.calculateDeliveryFee();

            // Then
            assertEquals(30000, deliveryInfo.getDeliveryFee());
        }
    }

    @Nested
    @DisplayName("Estimated Delivery Date Tests")
    class EstimatedDeliveryDateTests {

        @Test
        @DisplayName("Should calculate standard delivery date for normal province")
        void calculateEstimatedDeliveryDate_StandardDeliveryNormal_ShouldAdd3Days() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("Hà Nội");
            LocalDateTime beforeCalculation = LocalDateTime.now();

            // When
            deliveryInfo.calculateEstimatedDeliveryDate();

            // Then
            assertNotNull(deliveryInfo.getEstimatedDeliveryDate());
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isAfter(beforeCalculation.plusDays(2)));
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isBefore(beforeCalculation.plusDays(4)));
        }

        @Test
        @DisplayName("Should calculate standard delivery date for remote province")
        void calculateEstimatedDeliveryDate_StandardDeliveryRemote_ShouldAdd5Days() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("Lào Cai");
            LocalDateTime beforeCalculation = LocalDateTime.now();

            // When
            deliveryInfo.calculateEstimatedDeliveryDate();

            // Then
            assertNotNull(deliveryInfo.getEstimatedDeliveryDate());
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isAfter(beforeCalculation.plusDays(4)));
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isBefore(beforeCalculation.plusDays(6)));
        }

        @Test
        @DisplayName("Should calculate rush delivery date")
        void calculateEstimatedDeliveryDate_RushDelivery_ShouldAdd1Day() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.RUSH);
            deliveryInfo.setProvince("Hà Nội");
            LocalDateTime beforeCalculation = LocalDateTime.now();

            // When
            deliveryInfo.calculateEstimatedDeliveryDate();

            // Then
            assertNotNull(deliveryInfo.getEstimatedDeliveryDate());
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isAfter(beforeCalculation));
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isBefore(beforeCalculation.plusDays(2)));
        }

        @Test
        @DisplayName("Should calculate express delivery date")
        void calculateEstimatedDeliveryDate_ExpressDelivery_ShouldAdd2Days() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.EXPRESS);
            deliveryInfo.setProvince("Đà Nẵng");
            LocalDateTime beforeCalculation = LocalDateTime.now();

            // When
            deliveryInfo.calculateEstimatedDeliveryDate();

            // Then
            assertNotNull(deliveryInfo.getEstimatedDeliveryDate());
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isAfter(beforeCalculation.plusDays(1)));
            assertTrue(deliveryInfo.getEstimatedDeliveryDate().isBefore(beforeCalculation.plusDays(3)));
        }
    }

    @Nested
    @DisplayName("Delivery Status Tests")
    class DeliveryStatusTests {

        @Test
        @DisplayName("Should mark delivery as completed")
        void markAsDelivered_ShouldSetActualDeliveryDate() {
            // Given
            assertNull(deliveryInfo.getActualDeliveryDate());
            LocalDateTime beforeMark = LocalDateTime.now();

            // When
            deliveryInfo.markAsDelivered();

            // Then
            assertNotNull(deliveryInfo.getActualDeliveryDate());
            assertTrue(deliveryInfo.getActualDeliveryDate().isAfter(beforeMark.minusSeconds(1)));
            assertTrue(deliveryInfo.getActualDeliveryDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        }

        @Test
        @DisplayName("Should return false when not delivered")
        void isDelivered_WhenNotDelivered_ShouldReturnFalse() {
            // Given
            deliveryInfo.setActualDeliveryDate(null);

            // When & Then
            assertFalse(deliveryInfo.isDelivered());
        }

        @Test
        @DisplayName("Should return true when delivered")
        void isDelivered_WhenDelivered_ShouldReturnTrue() {
            // Given
            deliveryInfo.setActualDeliveryDate(LocalDateTime.now());

            // When & Then
            assertTrue(deliveryInfo.isDelivered());
        }
    }

    @Nested
    @DisplayName("Address Formatting Tests")
    class AddressFormattingTests {

        @Test
        @DisplayName("Should format full address correctly")
        void getFullAddress_ShouldCombineAddressAndProvince() {
            // Given
            deliveryInfo.setAddress("123 Main Street");
            deliveryInfo.setProvince("Hà Nội");

            // When
            String fullAddress = deliveryInfo.getFullAddress();

            // Then
            assertEquals("123 Main Street, Hà Nội", fullAddress);
        }

        @Test
        @DisplayName("Should format delivery fee with currency")
        void getFormattedDeliveryFee_WithFee_ShouldFormatWithCurrency() {
            // Given
            deliveryInfo.setDeliveryFee(30000);

            // When
            String formattedFee = deliveryInfo.getFormattedDeliveryFee();

            // Then
            assertEquals("30,000 VND", formattedFee);
        }

        @Test
        @DisplayName("Should show Free when delivery fee is null")
        void getFormattedDeliveryFee_WhenNull_ShouldShowFree() {
            // Given
            deliveryInfo.setDeliveryFee(null);

            // When
            String formattedFee = deliveryInfo.getFormattedDeliveryFee();

            // Then
            assertEquals("Free", formattedFee);
        }
    }

    @Nested
    @DisplayName("Delivery Type Tests")
    class DeliveryTypeTests {

        @Test
        @DisplayName("Should return true for rush delivery")
        void isRushDelivery_WhenRushType_ShouldReturnTrue() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.RUSH);

            // When & Then
            assertTrue(deliveryInfo.isRushDelivery());
        }

        @Test
        @DisplayName("Should return false for non-rush delivery")
        void isRushDelivery_WhenNotRushType_ShouldReturnFalse() {
            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);

            // When & Then
            assertFalse(deliveryInfo.isRushDelivery());

            // Given
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.EXPRESS);

            // When & Then
            assertFalse(deliveryInfo.isRushDelivery());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should be valid with all required fields")
        void isValid_WithAllRequiredFields_ShouldReturnTrue() {
            // Given
            deliveryInfo.setName("John Doe");
            deliveryInfo.setPhone("0123456789");
            deliveryInfo.setAddress("123 Main Street");
            deliveryInfo.setProvince("Hà Nội");
            deliveryInfo.setDeliveryFee(30000);

            // When & Then
            assertTrue(deliveryInfo.isValid());
        }

        @Test
        @DisplayName("Should be invalid with null name")
        void isValid_WithNullName_ShouldReturnFalse() {
            // Given
            deliveryInfo.setName(null);
            deliveryInfo.setDeliveryFee(30000);

            // When & Then
            assertFalse(deliveryInfo.isValid());
        }

        @Test
        @DisplayName("Should be invalid with empty name")
        void isValid_WithEmptyName_ShouldReturnFalse() {
            // Given
            deliveryInfo.setName("   ");
            deliveryInfo.setDeliveryFee(30000);

            // When & Then
            assertFalse(deliveryInfo.isValid());
        }

        @Test
        @DisplayName("Should be invalid with null phone")
        void isValid_WithNullPhone_ShouldReturnFalse() {
            // Given
            deliveryInfo.setPhone(null);
            deliveryInfo.setDeliveryFee(30000);

            // When & Then
            assertFalse(deliveryInfo.isValid());
        }

        @Test
        @DisplayName("Should be invalid with negative delivery fee")
        void isValid_WithNegativeDeliveryFee_ShouldReturnFalse() {
            // Given
            deliveryInfo.setDeliveryFee(-1000);

            // When & Then
            assertFalse(deliveryInfo.isValid());
        }

        @Test
        @DisplayName("Should be valid with zero delivery fee")
        void isValid_WithZeroDeliveryFee_ShouldReturnTrue() {
            // Given
            deliveryInfo.setDeliveryFee(0);

            // When & Then
            assertTrue(deliveryInfo.isValid());
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create delivery info with all parameters")
        void create_WithAllParameters_ShouldCreateValidDeliveryInfo() {
            // When
            DeliveryInfo created = DeliveryInfo.create(
                    "Jane Smith",
                    "0987654321",
                    "jane@example.com",
                    "456 Oak Avenue",
                    "Hồ Chí Minh",
                    "Ring doorbell",
                    DeliveryInfo.DeliveryType.EXPRESS
            );

            // Then
            assertNotNull(created);
            assertEquals("Jane Smith", created.getName());
            assertEquals("0987654321", created.getPhone());
            assertEquals("jane@example.com", created.getEmail());
            assertEquals("456 Oak Avenue", created.getAddress());
            assertEquals("Hồ Chí Minh", created.getProvince());
            assertEquals("Ring doorbell", created.getDeliveryMessage());
            assertEquals(DeliveryInfo.DeliveryType.EXPRESS, created.getDeliveryType());
            assertNotNull(created.getDeliveryFee());
            assertNotNull(created.getEstimatedDeliveryDate());
        }

        @Test
        @DisplayName("Should create with default delivery type when null")
        void create_WithNullDeliveryType_ShouldUseStandardType() {
            // When
            DeliveryInfo created = DeliveryInfo.create(
                    "Jane Smith",
                    "0987654321",
                    "jane@example.com",
                    "456 Oak Avenue",
                    "Hồ Chí Minh",
                    "Ring doorbell",
                    null
            );

            // Then
            assertEquals(DeliveryInfo.DeliveryType.STANDARD, created.getDeliveryType());
        }

        @Test
        @DisplayName("Should calculate fee and delivery date automatically")
        void create_ShouldCalculateFeeAndDeliveryDate() {
            // When
            DeliveryInfo created = DeliveryInfo.create(
                    "Jane Smith",
                    "0987654321",
                    "jane@example.com",
                    "456 Oak Avenue",
                    "Lào Cai", // Remote province
                    "Ring doorbell",
                    DeliveryInfo.DeliveryType.RUSH
            );

            // Then
            assertEquals(65000, created.getDeliveryFee()); // 30000 + 20000 (rush) + 15000 (remote)
            assertNotNull(created.getEstimatedDeliveryDate());
            assertTrue(created.getEstimatedDeliveryDate().isAfter(LocalDateTime.now()));
        }
    }

    @Nested
    @DisplayName("DeliveryType Enum Tests")
    class DeliveryTypeEnumTests {

        @Test
        @DisplayName("Should have correct display names")
        void deliveryType_ShouldHaveCorrectDisplayNames() {
            assertEquals("Standard Delivery", DeliveryInfo.DeliveryType.STANDARD.getDisplayName());
            assertEquals("Express Delivery", DeliveryInfo.DeliveryType.EXPRESS.getDisplayName());
            assertEquals("Rush Delivery", DeliveryInfo.DeliveryType.RUSH.getDisplayName());
        }

        @Test
        @DisplayName("Should have all expected enum values")
        void deliveryType_ShouldHaveAllExpectedValues() {
            DeliveryInfo.DeliveryType[] types = DeliveryInfo.DeliveryType.values();
            assertEquals(3, types.length);
            
            boolean hasStandard = false, hasExpress = false, hasRush = false;
            for (DeliveryInfo.DeliveryType type : types) {
                switch (type) {
                    case STANDARD -> hasStandard = true;
                    case EXPRESS -> hasExpress = true;
                    case RUSH -> hasRush = true;
                }
            }
            
            assertTrue(hasStandard);
            assertTrue(hasExpress);
            assertTrue(hasRush);
        }
    }

    @Nested
    @DisplayName("Remote Province Detection Tests")
    class RemoteProvinceDetectionTests {

        @Test
        @DisplayName("Should detect remote provinces correctly through fee calculation")
        void remoteProvinceDetection_ThroughFeeCalculation() {
            // Test remote provinces
            String[] remoteProvinces = {
                "Lào Cai", "Điện Biên", "Lai Châu", "Sơn La", "Hà Giang",
                "Cao Bằng", "Bắc Kạn", "Lang Sơn", "Kon Tum", "Gia Lai",
                "Đắk Lắk", "Đắk Nông", "Lâm Đồng", "Cà Mau", "An Giang"
            };

            for (String province : remoteProvinces) {
                deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
                deliveryInfo.setProvince(province);
                deliveryInfo.calculateDeliveryFee();
                
                assertEquals(45000, deliveryInfo.getDeliveryFee(), 
                    "Province " + province + " should be detected as remote");
            }
        }

        @Test
        @DisplayName("Should not detect normal provinces as remote")
        void normalProvinceDetection_ThroughFeeCalculation() {
            String[] normalProvinces = {"Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng"};

            for (String province : normalProvinces) {
                deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
                deliveryInfo.setProvince(province);
                deliveryInfo.calculateDeliveryFee();
                
                assertEquals(30000, deliveryInfo.getDeliveryFee(), 
                    "Province " + province + " should not be detected as remote");
            }
        }

        @Test
        @DisplayName("Should handle case insensitive province matching")
        void remoteProvinceDetection_CaseInsensitive() {
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("lào cai"); // lowercase
            deliveryInfo.calculateDeliveryFee();
            
            assertEquals(45000, deliveryInfo.getDeliveryFee());
        }

        @Test
        @DisplayName("Should handle partial province name matching")
        void remoteProvinceDetection_PartialMatching() {
            deliveryInfo.setDeliveryType(DeliveryInfo.DeliveryType.STANDARD);
            deliveryInfo.setProvince("Thành phố Lào Cai"); // contains remote province name
            deliveryInfo.calculateDeliveryFee();
            
            assertEquals(45000, deliveryInfo.getDeliveryFee());
        }
    }
}