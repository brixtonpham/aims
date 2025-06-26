package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.payment.entity.PaymentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JpaPaymentTransactionRepository
 * Tests data access operations for PaymentTransaction entities
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JpaPaymentTransactionRepository Unit Tests")
class JpaPaymentTransactionRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private JpaPaymentTransactionRepository paymentTransactionRepository;

    private PaymentTransaction testTransaction;
    private PaymentTransaction existingTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = PaymentTransaction.builder()
                .orderId("ORD123")
                .transactionNo("TXN456")
                .amount(100000L)
                .bankCode("NCB")
                .responseCode("00")
                .transactionStatus(PaymentTransaction.TransactionStatus.SUCCESS)
                .payDate("20241227120000")
                .paymentMethod("VNPay")
                .gatewayTransactionId("VNPAY123456")
                .gatewayResponseMessage("Success")
                .currencyCode("VND")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        existingTransaction = PaymentTransaction.builder()
                .id(1L)
                .orderId("ORD123")
                .transactionNo("TXN456")
                .amount(100000L)
                .bankCode("NCB")
                .responseCode("00")
                .transactionStatus(PaymentTransaction.TransactionStatus.SUCCESS)
                .payDate("20241227120000")
                .paymentMethod("VNPay")
                .gatewayTransactionId("VNPAY123456")
                .gatewayResponseMessage("Success")
                .currencyCode("VND")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save new payment transaction successfully")
    void save_NewTransaction_ShouldInsertAndReturnWithId() {
        // Given
        when(jdbcTemplate.update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT LASTVAL()"), eq(Long.class))).thenReturn(1L);

        // When
        PaymentTransaction result = paymentTransactionRepository.save(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(jdbcTemplate).update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jdbcTemplate).queryForObject(eq("SELECT LASTVAL()"), eq(Long.class));
    }

    @Test
    @DisplayName("Should update existing payment transaction successfully")
    void save_ExistingTransaction_ShouldUpdateAndReturn() {
        // Given
        when(jdbcTemplate.update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        // When
        PaymentTransaction result = paymentTransactionRepository.save(existingTransaction);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(jdbcTemplate).update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should find payment transaction by ID successfully")
    void findById_ExistingId_ShouldReturnTransaction() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(1L)))
                .thenReturn(existingTransaction);

        // When
        Optional<PaymentTransaction> result = paymentTransactionRepository.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(existingTransaction.getId(), result.get().getId());
        assertEquals(existingTransaction.getOrderId(), result.get().getOrderId());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(1L));
    }

    @Test
    @DisplayName("Should return empty when payment transaction not found by ID")
    void findById_NonExistentId_ShouldReturnEmpty() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(999L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // When
        Optional<PaymentTransaction> result = paymentTransactionRepository.findById(999L);

        // Then
        assertTrue(result.isEmpty());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(999L));
    }

    @Test
    @DisplayName("Should find payment transaction by order ID successfully")
    void findByOrderId_ExistingOrderId_ShouldReturnTransaction() {
        // Given
        String orderId = "ORD123";
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(orderId)))
                .thenReturn(existingTransaction);

        // When
        Optional<PaymentTransaction> result = paymentTransactionRepository.findByOrderId(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(orderId));
    }

    @Test
    @DisplayName("Should return empty when payment transaction not found by order ID")
    void findByOrderId_NonExistentOrderId_ShouldReturnEmpty() {
        // Given
        String orderId = "NONEXISTENT";
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(orderId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // When
        Optional<PaymentTransaction> result = paymentTransactionRepository.findByOrderId(orderId);

        // Then
        assertTrue(result.isEmpty());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(orderId));
    }

    @Test
    @DisplayName("Should find payment transaction by external transaction ID successfully")
    void findByExternalTransactionId_ExistingId_ShouldReturnTransaction() {
        // Given
        String externalId = "VNPAY123456";
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(externalId)))
                .thenReturn(existingTransaction);

        // When
        Optional<PaymentTransaction> result = paymentTransactionRepository.findByExternalTransactionId(externalId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(externalId, result.get().getGatewayTransactionId());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(externalId));
    }

    @Test
    @DisplayName("Should find payment transactions by status successfully")
    void findByStatus_ExistingStatus_ShouldReturnTransactions() {
        // Given
        String status = "SUCCESS";
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq(status)))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingTransaction.getId(), result.get(0).getId());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class), eq(status));
    }

    @Test
    @DisplayName("Should find payment transactions by payment method successfully")
    void findByPaymentMethod_ExistingMethod_ShouldReturnTransactions() {
        // Given
        String paymentMethod = "VNPay";
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq(paymentMethod)))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findByPaymentMethod(paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(paymentMethod, result.get(0).getPaymentMethod());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class), eq(paymentMethod));
    }

    @Test
    @DisplayName("Should find all payment transactions successfully")
    void findAll_ShouldReturnAllTransactions() {
        // Given
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class));
    }

    @Test
    @DisplayName("Should update payment transaction successfully")
    void update_ExistingTransaction_ShouldUpdateSuccessfully() {
        // Given
        when(jdbcTemplate.update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        // When
        PaymentTransaction result = paymentTransactionRepository.update(existingTransaction);

        // Then
        assertNotNull(result);
        assertEquals(existingTransaction.getId(), result.getId());
        verify(jdbcTemplate).update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent payment transaction")
    void update_NonExistentTransaction_ShouldThrowException() {
        // Given
        when(jdbcTemplate.update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(0);

        // When & Then
        JpaPaymentTransactionRepository.PaymentTransactionNotFoundException exception = 
            assertThrows(JpaPaymentTransactionRepository.PaymentTransactionNotFoundException.class,
                () -> paymentTransactionRepository.update(existingTransaction));

        assertTrue(exception.getMessage().contains("Payment transaction not found with id: 1"));
        verify(jdbcTemplate).update(anyString(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should delete payment transaction by ID successfully")
    void deleteById_ExistingId_ShouldDeleteSuccessfully() {
        // Given
        when(jdbcTemplate.update(anyString(), eq(1L))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> paymentTransactionRepository.deleteById(1L));

        // Then
        verify(jdbcTemplate).update(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent payment transaction")
    void deleteById_NonExistentId_ShouldThrowException() {
        // Given
        when(jdbcTemplate.update(anyString(), eq(999L))).thenReturn(0);

        // When & Then
        JpaPaymentTransactionRepository.PaymentTransactionNotFoundException exception = 
            assertThrows(JpaPaymentTransactionRepository.PaymentTransactionNotFoundException.class,
                () -> paymentTransactionRepository.deleteById(999L));

        assertTrue(exception.getMessage().contains("Payment transaction not found with id: 999"));
        verify(jdbcTemplate).update(anyString(), eq(999L));
    }

    @Test
    @DisplayName("Should check if payment transaction exists by ID")
    void existsById_ExistingId_ShouldReturnTrue() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1L))).thenReturn(1);

        // When
        boolean result = paymentTransactionRepository.existsById(1L);

        // Then
        assertTrue(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(1L));
    }

    @Test
    @DisplayName("Should return false when payment transaction does not exist")
    void existsById_NonExistentId_ShouldReturnFalse() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(999L))).thenReturn(0);

        // When
        boolean result = paymentTransactionRepository.existsById(999L);

        // Then
        assertFalse(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(999L));
    }

    @Test
    @DisplayName("Should count total payment transactions")
    void count_ShouldReturnTotalCount() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(5L);

        // When
        long result = paymentTransactionRepository.count();

        // Then
        assertEquals(5L, result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }

    @Test
    @DisplayName("Should count payment transactions by status")
    void countByStatus_ExistingStatus_ShouldReturnCount() {
        // Given
        String status = "SUCCESS";
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(status))).thenReturn(3L);

        // When
        long result = paymentTransactionRepository.countByStatus(status);

        // Then
        assertEquals(3L, result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq(status));
    }

    @Test
    @DisplayName("Should find successful transactions by order ID")
    void findSuccessfulTransactionsByOrderId_ExistingOrderId_ShouldReturnTransactions() {
        // Given
        Long orderId = 123L;
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq(orderId.toString())))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findSuccessfulTransactionsByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class), eq(orderId.toString()));
    }

    @Test
    @DisplayName("Should get total amount by status")
    void getTotalAmountByStatus_ExistingStatus_ShouldReturnAmount() {
        // Given
        String status = "SUCCESS";
        when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), eq(status))).thenReturn(500000.0);

        // When
        double result = paymentTransactionRepository.getTotalAmountByStatus(status);

        // Then
        assertEquals(500000.0, result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Double.class), eq(status));
    }

    @Test
    @DisplayName("Should find refundable transactions")
    void findRefundableTransactions_ShouldReturnTransactions() {
        // Given
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findRefundableTransactions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class));
    }

    @Test
    @DisplayName("Should find failed transactions by date range")
    void findFailedTransactionsByDateRange_ValidRange_ShouldReturnTransactions() {
        // Given
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";
        List<PaymentTransaction> expectedTransactions = Arrays.asList(existingTransaction);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq(startDate), eq(endDate)))
                .thenReturn(expectedTransactions);

        // When
        List<PaymentTransaction> result = paymentTransactionRepository.findFailedTransactionsByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(BeanPropertyRowMapper.class), eq(startDate), eq(endDate));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void count_NullResult_ShouldReturnZero() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(null);

        // When
        long result = paymentTransactionRepository.count();

        // Then
        assertEquals(0L, result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }

    @Test
    @DisplayName("Should handle null values in countByStatus gracefully")
    void countByStatus_NullResult_ShouldReturnZero() {
        // Given
        String status = "SUCCESS";
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(status))).thenReturn(null);

        // When
        long result = paymentTransactionRepository.countByStatus(status);

        // Then
        assertEquals(0L, result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq(status));
    }
}