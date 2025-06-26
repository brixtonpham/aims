package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.payment.entity.PaymentTransaction;
import com.aims.domain.payment.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of PaymentTransactionRepository interface.
 * Provides data access operations for PaymentTransaction entities.
 */
@Repository
@Transactional
public class JpaPaymentTransactionRepository implements PaymentTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaPaymentTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        if (transaction.getId() == null) {
            return insert(transaction);
        } else {
            return update(transaction);
        }
    }

    private PaymentTransaction insert(PaymentTransaction transaction) {
        String sql = """
            INSERT INTO payment_transactions (order_id, transaction_no, amount, bank_code, 
                                            response_code, transaction_status, pay_date, 
                                            payment_method, gateway_transaction_id, 
                                            gateway_response_message, currency_code) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            transaction.getOrderId(),
            transaction.getTransactionNo(),
            transaction.getAmount(),
            transaction.getBankCode(),
            transaction.getResponseCode(),
            transaction.getTransactionStatus().name(),
            transaction.getPayDate(),
            transaction.getPaymentMethod(),
            transaction.getGatewayTransactionId(),
            transaction.getGatewayResponseMessage(),
            transaction.getCurrencyCode()
        );

        // Get the generated ID
        Long generatedId = jdbcTemplate.queryForObject(
            "SELECT LASTVAL()", Long.class);
        transaction.setId(generatedId);
        
        return transaction;
    }

    @Override
    public Optional<PaymentTransaction> findById(Long transactionId) {
        try {
            String sql = "SELECT * FROM payment_transactions WHERE id = ?";
            PaymentTransaction transaction = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(PaymentTransaction.class), transactionId);
            return Optional.of(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PaymentTransaction> findByExternalTransactionId(String externalTransactionId) {
        try {
            String sql = "SELECT * FROM payment_transactions WHERE gateway_transaction_id = ?";
            PaymentTransaction transaction = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(PaymentTransaction.class), externalTransactionId);
            return Optional.of(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PaymentTransaction> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ? ORDER BY transaction_date DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), orderId);
    }

    @Override
    public List<PaymentTransaction> findByStatus(String status) {
        String sql = "SELECT * FROM payment_transactions WHERE transaction_status = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), status);
    }

    @Override
    public List<PaymentTransaction> findByPaymentMethod(String paymentMethod) {
        String sql = "SELECT * FROM payment_transactions WHERE payment_method = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), paymentMethod);
    }

    @Override
    public List<PaymentTransaction> findAll() {
        String sql = "SELECT * FROM payment_transactions ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class));
    }

    @Override
    public PaymentTransaction update(PaymentTransaction transaction) {
        String sql = """
            UPDATE payment_transactions SET 
                order_id = ?, transaction_no = ?, amount = ?, bank_code = ?, 
                response_code = ?, transaction_status = ?, pay_date = ?, 
                payment_method = ?, gateway_transaction_id = ?, 
                gateway_response_message = ?, currency_code = ?
            WHERE id = ?
            """;
        
        int updatedRows = jdbcTemplate.update(sql,
            transaction.getOrderId(),
            transaction.getTransactionNo(),
            transaction.getAmount(),
            transaction.getBankCode(),
            transaction.getResponseCode(),
            transaction.getTransactionStatus().name(),
            transaction.getPayDate(),
            transaction.getPaymentMethod(),
            transaction.getGatewayTransactionId(),
            transaction.getGatewayResponseMessage(),
            transaction.getCurrencyCode(),
            transaction.getId()
        );

        if (updatedRows == 0) {
            throw new PaymentTransactionNotFoundException("Payment transaction not found with id: " + transaction.getId());
        }
        
        return transaction;
    }

    @Override
    public void deleteById(Long transactionId) {
        String sql = "DELETE FROM payment_transactions WHERE id = ?";
        int deletedRows = jdbcTemplate.update(sql, transactionId);
        
        if (deletedRows == 0) {
            throw new PaymentTransactionNotFoundException("Payment transaction not found with id: " + transactionId);
        }
    }

    @Override
    public boolean existsById(Long transactionId) {
        String sql = "SELECT COUNT(*) FROM payment_transactions WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, transactionId);
        return count != null && count > 0;
    }

    @Override
    public List<PaymentTransaction> findSuccessfulTransactionsByOrderId(Long orderId) {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ? AND transaction_status = 'SUCCESS' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), orderId.toString());
    }

    @Override
    public List<PaymentTransaction> findFailedTransactionsByDateRange(String startDate, String endDate) {
        String sql = """
            SELECT * FROM payment_transactions 
            WHERE transaction_status = 'FAILED' 
            AND created_at >= ?::date 
            AND created_at <= ?::date 
            ORDER BY created_at DESC
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), startDate, endDate);
    }

    @Override
    public double getTotalAmountByStatus(String status) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE transaction_status = ?";
        Double totalAmount = jdbcTemplate.queryForObject(sql, Double.class, status);
        return totalAmount != null ? totalAmount : 0.0;
    }

    @Override
    public List<PaymentTransaction> findRefundableTransactions() {
        String sql = """
            SELECT * FROM payment_transactions 
            WHERE transaction_status = 'SUCCESS' 
            AND created_at >= (CURRENT_TIMESTAMP - INTERVAL '30 days')
            ORDER BY created_at DESC
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class));
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM payment_transactions";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM payment_transactions WHERE transaction_status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status);
        return count != null ? count : 0L;
    }

    /**
     * Update transaction status
     * @param transactionId the transaction ID
     * @param status the new status
     * @param gatewayResponse the gateway response message
     */
    public void updateStatus(Long transactionId, String status, String gatewayResponse) {
        String sql = "UPDATE payment_transactions SET transaction_status = ?, gateway_response_message = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql, status, gatewayResponse, transactionId);
        
        if (updatedRows == 0) {
            throw new PaymentTransactionNotFoundException("Payment transaction not found with id: " + transactionId);
        }
    }

    /**
     * Find transactions by gateway
     * @param paymentGateway the payment gateway
     * @return list of transactions for the gateway
     */
    public List<PaymentTransaction> findByPaymentGateway(String paymentGateway) {
        String sql = "SELECT * FROM payment_transactions WHERE gateway_transaction_id LIKE ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class), paymentGateway + "%");
    }

    /**
     * Find recent transactions (last N days)
     * @param days number of days
     * @return list of recent transactions
     */
    public List<PaymentTransaction> findRecentTransactions(int days) {
        String sql = """
            SELECT * FROM payment_transactions 
            WHERE created_at >= (CURRENT_TIMESTAMP - INTERVAL '%d days') 
            ORDER BY created_at DESC
            """.formatted(days);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransaction.class));
    }

    @Override
    public Optional<PaymentTransaction> findByOrderId(String orderId) {
        try {
            String sql = "SELECT * FROM payment_transactions WHERE order_id = ? ORDER BY created_at DESC LIMIT 1";
            PaymentTransaction transaction = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(PaymentTransaction.class), orderId);
            return Optional.of(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Custom exception class
    public static class PaymentTransactionNotFoundException extends RuntimeException {
        public PaymentTransactionNotFoundException(String message) {
            super(message);
        }
    }
}
