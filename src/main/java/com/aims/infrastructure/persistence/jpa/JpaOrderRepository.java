package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OrderRepository interface.
 * Provides data access operations for Order entities.
 */
@Repository
@Transactional
public class JpaOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            return insert(order);
        } else {
            return update(order);
        }
    }

    private Order insert(Order order) {
        String sql = """
            INSERT INTO orders (customer_id, total_before_vat, total_after_vat, status, 
                               delivery_id, vat_rate, order_time, payment_method, is_rush_order) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            order.getCustomerId(),
            order.getTotalBeforeVAT(),
            order.getTotalAfterVAT(),
            order.getStatus().name(),
            order.getDeliveryInfo() != null ? order.getDeliveryInfo().getDeliveryId() : null,
            order.getVatRate(),
            order.getOrderTime(),
            order.getPaymentMethod(),
            order.getIsRushOrder()
        );

        // Get the generated ID
        Long generatedId = jdbcTemplate.queryForObject(
            "SELECT LASTVAL()", Long.class);
        order.setOrderId(generatedId);
        
        return order;
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE order_id = ?";
            Order order = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Order.class), orderId);
            return Optional.of(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY order_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public List<Order> findByStatus(String status) {
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), status);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        // Note: Customer relationship needs to be added to Order entity
        // For now, using delivery_id as a proxy for customer identification
        String sql = "SELECT * FROM orders WHERE delivery_id = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), customerId);
    }

    @Override
    public List<Order> findByDeliveryId(Long deliveryId) {
        String sql = "SELECT * FROM orders WHERE delivery_id = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), deliveryId);
    }

    @Override
    public Order update(Order order) {
        String sql = """
            UPDATE orders SET 
                customer_id = ?, total_before_vat = ?, total_after_vat = ?, status = ?, 
                delivery_id = ?, vat_rate = ?, order_time = ?, payment_method = ?, is_rush_order = ?
            WHERE order_id = ?
            """;
        
        int updatedRows = jdbcTemplate.update(sql,
            order.getCustomerId(),
            order.getTotalBeforeVAT(),
            order.getTotalAfterVAT(),
            order.getStatus().name(),
            order.getDeliveryInfo() != null ? order.getDeliveryInfo().getDeliveryId() : null,
            order.getVatRate(),
            order.getOrderTime(),
            order.getPaymentMethod(),
            order.getIsRushOrder(),
            order.getOrderId()
        );

        if (updatedRows == 0) {
            throw new OrderNotFoundException("Order not found with id: " + order.getOrderId());
        }
        
        return order;
    }

    @Override
    public void deleteById(Long orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        int deletedRows = jdbcTemplate.update(sql, orderId);
        
        if (deletedRows == 0) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }
    }

    @Override
    public boolean existsById(Long orderId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Order> findByIdWithItems(Long orderId) {
        // This would require a join with OrderItem table
        // For now, return the basic order - items can be loaded separately
        return findById(orderId);
    }

    @Override
    public List<Order> findByDateRange(String startDate, String endDate) {
        String sql = """
            SELECT * FROM orders 
            WHERE order_time >= ?::date AND order_time <= ?::date 
            ORDER BY order_time DESC
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), startDate, endDate);
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status);
        return count != null ? count : 0L;
    }

    @Override
    public List<Order> findRushOrders() {
        String sql = "SELECT * FROM orders WHERE is_rush_order = true ORDER BY order_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Update order status
     * @param orderId the order ID
     * @param status the new status
     */
    public void updateStatus(Long orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        int updatedRows = jdbcTemplate.update(sql, status, orderId);
        
        if (updatedRows == 0) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }
    }

    /**
     * Get order total amount
     * @param orderId the order ID
     * @return order total amount
     */
    public Double getOrderAmount(Long orderId) {
        try {
            String sql = "SELECT total_after_vat FROM orders WHERE order_id = ?";
            return jdbcTemplate.queryForObject(sql, Double.class, orderId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Custom exception class
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }
}
