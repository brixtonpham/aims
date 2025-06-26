package com.aims.domain.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentTransaction entity
 * Tests business logic and behavior of payment transactions
 */
@DisplayName("PaymentTransaction Entity Tests")
class PaymentTransactionTest {

    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        transaction = PaymentTransaction.builder()
                .orderId("12345")
                .amount(100000L)
                .bankCode("NCB")
                .paymentMethod("VNPay")
                .currencyCode("VND")
                .transactionStatus(PaymentTransaction.TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Transaction Status Tests")
    class TransactionStatusTests {

        @Test
        @DisplayName("Should return true for successful transaction")
        void isSuccessful_WhenStatusSuccessAndResponseCode00_ShouldReturnTrue() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");

            // When
            boolean result = transaction.isSuccessful();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for successful status but wrong response code")
        void isSuccessful_WhenStatusSuccessButNotResponseCode00_ShouldReturnFalse() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("01");

            // When
            boolean result = transaction.isSuccessful();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for failed transaction")
        void isFailed_WhenStatusFailed_ShouldReturnTrue() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.FAILED);

            // When
            boolean result = transaction.isFailed();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true for pending transaction")
        void isPending_WhenStatusPending_ShouldReturnTrue() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.PENDING);

            // When
            boolean result = transaction.isPending();

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Refund Tests")
    class RefundTests {

        @Test
        @DisplayName("Should allow refund for successful transaction within 30 days")
        void canBeRefunded_SuccessfulTransactionWithin30Days_ShouldReturnTrue() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");
            transaction.setCreatedAt(LocalDateTime.now().minusDays(15));

            // When
            boolean result = transaction.canBeRefunded();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should not allow refund for successful transaction after 30 days")
        void canBeRefunded_SuccessfulTransactionAfter30Days_ShouldReturnFalse() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");
            transaction.setCreatedAt(LocalDateTime.now().minusDays(31));

            // When
            boolean result = transaction.canBeRefunded();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not allow refund for failed transaction")
        void canBeRefunded_FailedTransaction_ShouldReturnFalse() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.FAILED);
            transaction.setCreatedAt(LocalDateTime.now().minusDays(5));

            // When
            boolean result = transaction.canBeRefunded();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should not allow refund when created date is null")
        void canBeRefunded_NullCreatedDate_ShouldReturnFalse() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");
            transaction.setCreatedAt(null);

            // When
            boolean result = transaction.canBeRefunded();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should successfully mark as refunded for eligible transaction")
        void markAsRefunded_EligibleTransaction_ShouldMarkAsRefunded() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");
            transaction.setCreatedAt(LocalDateTime.now().minusDays(10));

            // When
            transaction.markAsRefunded();

            // Then
            assertEquals(PaymentTransaction.TransactionStatus.REFUNDED, transaction.getTransactionStatus());
        }

        @Test
        @DisplayName("Should throw exception when marking ineligible transaction as refunded")
        void markAsRefunded_IneligibleTransaction_ShouldThrowException() {
            // Given
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.FAILED);

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> transaction.markAsRefunded()
            );

            assertEquals("Transaction cannot be refunded", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Status Update Tests")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should mark transaction as successful")
        void markAsSuccessful_ValidParameters_ShouldUpdateStatus() {
            // Given
            String transactionNo = "VNP12345";
            String payDate = "20241201120000";

            // When
            transaction.markAsSuccessful(transactionNo, payDate);

            // Then
            assertEquals(PaymentTransaction.TransactionStatus.SUCCESS, transaction.getTransactionStatus());
            assertEquals(transactionNo, transaction.getTransactionNo());
            assertEquals(payDate, transaction.getPayDate());
            assertEquals("00", transaction.getResponseCode());
        }

        @Test
        @DisplayName("Should mark transaction as failed")
        void markAsFailed_ValidParameters_ShouldUpdateStatus() {
            // Given
            String responseCode = "05";
            String errorMessage = "Insufficient funds";

            // When
            transaction.markAsFailed(responseCode, errorMessage);

            // Then
            assertEquals(PaymentTransaction.TransactionStatus.FAILED, transaction.getTransactionStatus());
            assertEquals(responseCode, transaction.getResponseCode());
            assertEquals(errorMessage, transaction.getGatewayResponseMessage());
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should format amount correctly")
        void getFormattedAmount_ValidAmount_ShouldFormatCorrectly() {
            // Given
            transaction.setAmount(100000L);
            transaction.setCurrencyCode("VND");

            // When
            String result = transaction.getFormattedAmount();

            // Then
            assertEquals("100,000 VND", result);
        }

        @Test
        @DisplayName("Should handle null amount")
        void getFormattedAmount_NullAmount_ShouldReturnDefault() {
            // Given
            transaction.setAmount(null);

            // When
            String result = transaction.getFormattedAmount();

            // Then
            assertEquals("0 VND", result);
        }

        @Test
        @DisplayName("Should identify VNPay transaction")
        void isVNPayTransaction_VNPayTransactionNo_ShouldReturnTrue() {
            // Given
            transaction.setTransactionNo("VNP12345");

            // When
            boolean result = transaction.isVNPayTransaction();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should identify non-VNPay transaction")
        void isVNPayTransaction_NonVNPayTransactionNo_ShouldReturnFalse() {
            // Given
            transaction.setTransactionNo("MOMO12345");

            // When
            boolean result = transaction.isVNPayTransaction();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle null transaction number")
        void isVNPayTransaction_NullTransactionNo_ShouldReturnFalse() {
            // Given
            transaction.setTransactionNo(null);

            // When
            boolean result = transaction.isVNPayTransaction();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should be valid when all required fields are present")
        void isValid_AllRequiredFields_ShouldReturnTrue() {
            // Given - transaction already set up in setUp()

            // When
            boolean result = transaction.isValid();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should be invalid when order ID is null")
        void isValid_NullOrderId_ShouldReturnFalse() {
            // Given
            transaction.setOrderId(null);

            // When
            boolean result = transaction.isValid();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should be invalid when order ID is empty")
        void isValid_EmptyOrderId_ShouldReturnFalse() {
            // Given
            transaction.setOrderId("");

            // When
            boolean result = transaction.isValid();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should be invalid when order ID is blank")
        void isValid_BlankOrderId_ShouldReturnFalse() {
            // Given
            transaction.setOrderId("   ");

            // When
            boolean result = transaction.isValid();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should be invalid when amount is null")
        void isValid_NullAmount_ShouldReturnFalse() {
            // Given
            transaction.setAmount(null);

            // When
            boolean result = transaction.isValid();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should be invalid when amount is zero or negative")
        void isValid_ZeroOrNegativeAmount_ShouldReturnFalse() {
            // Given & When & Then
            transaction.setAmount(0L);
            assertFalse(transaction.isValid());

            transaction.setAmount(-1000L);
            assertFalse(transaction.isValid());
        }

        @Test
        @DisplayName("Should be invalid when transaction status is null")
        void isValid_NullTransactionStatus_ShouldReturnFalse() {
            // Given
            transaction.setTransactionStatus(null);

            // When
            boolean result = transaction.isValid();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create transaction with builder")
        void builder_ValidParameters_ShouldCreateTransaction() {
            // Given & When
            PaymentTransaction newTransaction = PaymentTransaction.builder()
                    .orderId("ORDER123")
                    .amount(50000L)
                    .bankCode("TECHCOMBANK")
                    .paymentMethod("VNPay")
                    .transactionStatus(PaymentTransaction.TransactionStatus.PENDING)
                    .build();

            // Then
            assertNotNull(newTransaction);
            assertEquals("ORDER123", newTransaction.getOrderId());
            assertEquals(50000L, newTransaction.getAmount());
            assertEquals("TECHCOMBANK", newTransaction.getBankCode());
            assertEquals("VNPay", newTransaction.getPaymentMethod());
            assertEquals(PaymentTransaction.TransactionStatus.PENDING, newTransaction.getTransactionStatus());
            assertEquals("VND", newTransaction.getCurrencyCode()); // Default value
        }

        @Test
        @DisplayName("Should use default currency code")
        void builder_NoCurrencyCode_ShouldUseDefault() {
            // Given & When
            PaymentTransaction newTransaction = PaymentTransaction.builder()
                    .orderId("ORDER123")
                    .amount(50000L)
                    .build();

            // Then
            assertEquals("VND", newTransaction.getCurrencyCode());
        }
    }

    @Nested
    @DisplayName("Transaction Status Enum Tests")
    class TransactionStatusEnumTests {

        @Test
        @DisplayName("Should return correct display names")
        void transactionStatus_DisplayNames_ShouldBeCorrect() {
            // Given & When & Then
            assertEquals("Pending", PaymentTransaction.TransactionStatus.PENDING.getDisplayName());
            assertEquals("Success", PaymentTransaction.TransactionStatus.SUCCESS.getDisplayName());
            assertEquals("Failed", PaymentTransaction.TransactionStatus.FAILED.getDisplayName());
            assertEquals("Refunded", PaymentTransaction.TransactionStatus.REFUNDED.getDisplayName());
            assertEquals("Cancelled", PaymentTransaction.TransactionStatus.CANCELLED.getDisplayName());
        }
    }
}