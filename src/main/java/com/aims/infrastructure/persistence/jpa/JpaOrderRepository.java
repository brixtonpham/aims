package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"order_id"});
            ps.setString(1, order.getCustomerId());
            ps.setLong(2, order.getTotalBeforeVAT() != null ? order.getTotalBeforeVAT() : 0L);
            ps.setLong(3, order.getTotalAfterVAT() != null ? order.getTotalAfterVAT() : 0L);
            ps.setString(4, order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name());
            ps.setObject(5, order.getDeliveryInfo() != null ? order.getDeliveryInfo().getDeliveryId() : null);
            ps.setInt(6, order.getVatRate() != null ? order.getVatRate() : 10);
            ps.setTimestamp(7, order.getOrderTime() != null ? Timestamp.valueOf(order.getOrderTime()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, order.getPaymentMethod());
            ps.setBoolean(9, order.getIsRushOrder() != null && order.getIsRushOrder());
            return ps;
        }, keyHolder);

        // Set the generated ID
        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            order.setOrderId(generatedId.longValue());
        }
        
        return order;
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE order_id = ?";
            Order order = jdbcTemplate.queryForObject(sql, 
                new OrderRowMapper(), orderId);
            return Optional.of(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY order_id";
        return jdbcTemplate.query(sql, new OrderRowMapper());
    }

    @Override
    public List<Order> findByStatus(String status) {
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new OrderRowMapper(), status);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        // Note: Customer relationship needs to be added to Order entity
        // For now, using delivery_id as a proxy for customer identification
        String sql = "SELECT * FROM orders WHERE delivery_id = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new OrderRowMapper(), customerId);
    }

    @Override
    public List<Order> findByDeliveryId(Long deliveryId) {
        String sql = "SELECT * FROM orders WHERE delivery_id = ? ORDER BY order_id";
        return jdbcTemplate.query(sql, new OrderRowMapper(), deliveryId);
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
            order.getTotalBeforeVAT() != null ? order.getTotalBeforeVAT() : 0L,
            order.getTotalAfterVAT() != null ? order.getTotalAfterVAT() : 0L,
            order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name(),
            order.getDeliveryInfo() != null ? order.getDeliveryInfo().getDeliveryId() : null,
            order.getVatRate() != null ? order.getVatRate() : 10,
            order.getOrderTime() != null ? Timestamp.valueOf(order.getOrderTime()) : null,
            order.getPaymentMethod(),
            order.getIsRushOrder() != null && order.getIsRushOrder(),
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
        return jdbcTemplate.query(sql, new OrderRowMapper(), startDate, endDate);
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
        return jdbcTemplate.query(sql, new OrderRowMapper());
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

    @Override
    public List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new OrderRowMapper(), customerId);
    }

    // Custom exception class
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    // Custom RowMapper for Order entity
    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setOrderId(rs.getLong("order_id"));
            order.setCustomerId(rs.getString("customer_id"));
            order.setTotalBeforeVAT(rs.getLong("total_before_vat"));
            order.setTotalAfterVAT(rs.getLong("total_after_vat"));
            
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                try {
                    order.setStatus(OrderStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    order.setStatus(OrderStatus.PENDING);
                }
            }
            
            order.setVatRate(rs.getInt("vat_rate"));
            
            Timestamp orderTime = rs.getTimestamp("order_time");
            if (orderTime != null) {
                order.setOrderTime(orderTime.toLocalDateTime());
            }
            
            order.setPaymentMethod(rs.getString("payment_method"));
            order.setIsRushOrder(rs.getBoolean("is_rush_order"));
            
            // Note: DeliveryInfo would need to be loaded separately
            // For now, we just check if delivery_id is present
            rs.getLong("delivery_id"); // Just to consume the column
            
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                order.setCreatedAt(createdAt.toLocalDateTime());
            }
            
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                order.setUpdatedAt(updatedAt.toLocalDateTime());
            }
            
            return order;
        }
    }
}
