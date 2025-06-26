package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.order.entity.OrderItem;
import com.aims.domain.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OrderItemRepository interface.
 * Provides data access operations for OrderItem entities (previously Orderline).
 */
@Repository
@Transactional
public class JpaOrderItemRepository implements OrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaOrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getOrderItemId() == null) {
            return insert(orderItem);
        } else {
            return update(orderItem);
        }
    }

    private OrderItem insert(OrderItem orderItem) {
        String sql = """
            INSERT INTO order_items (order_id, product_id, product_title, quantity, unit_price) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            orderItem.getOrder().getOrderId(),
            orderItem.getProductId(),
            orderItem.getProductTitle(),
            orderItem.getQuantity(),
            orderItem.getUnitPrice()
        );

        // Get the generated ID
        Long generatedId = jdbcTemplate.queryForObject(
            "SELECT LASTVAL()", Long.class);
        orderItem.setOrderItemId(generatedId);
        
        return orderItem;
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return orderItems.stream()
            .map(this::save)
            .toList();
    }

    @Override
    public Optional<OrderItem> findById(Long orderItemId) {
        try {
            String sql = "SELECT * FROM order_items WHERE order_item_id = ?";
            OrderItem orderItem = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(OrderItem.class), orderItemId);
            return Optional.of(orderItem);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY order_item_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), orderId);
    }

    @Override
    public List<OrderItem> findAll() {
        String sql = "SELECT * FROM order_items ORDER BY order_item_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class));
    }

    @Override
    public List<OrderItem> findByProductId(Long productId) {
        String sql = "SELECT * FROM order_items WHERE product_id = ? ORDER BY order_item_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), productId);
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        String sql = """
            UPDATE order_items SET 
                order_id = ?, product_id = ?, product_title = ?, quantity = ?, unit_price = ?
            WHERE order_item_id = ?
            """;
        
        int updatedRows = jdbcTemplate.update(sql,
            orderItem.getOrder().getOrderId(),
            orderItem.getProductId(),
            orderItem.getProductTitle(),
            orderItem.getQuantity(),
            orderItem.getUnitPrice(),
            orderItem.getOrderItemId()
        );

        if (updatedRows == 0) {
            throw new OrderItemNotFoundException("Order item not found with id: " + orderItem.getOrderItemId());
        }
        
        return orderItem;
    }

    @Override
    public void deleteById(Long orderItemId) {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";
        int deletedRows = jdbcTemplate.update(sql, orderItemId);
        
        if (deletedRows == 0) {
            throw new OrderItemNotFoundException("Order item not found with id: " + orderItemId);
        }
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }

    @Override
    public boolean existsById(Long orderItemId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE order_item_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, orderItemId);
        return count != null && count > 0;
    }

    @Override
    public int getTotalQuantityByProductId(Long productId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE product_id = ?";
        Integer totalQuantity = jdbcTemplate.queryForObject(sql, Integer.class, productId);
        return totalQuantity != null ? totalQuantity : 0;
    }

    @Override
    public double getTotalValueByOrderId(Long orderId) {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM order_items WHERE order_id = ?";
        Double totalValue = jdbcTemplate.queryForObject(sql, Double.class, orderId);
        return totalValue != null ? totalValue : 0.0;
    }

    @Override
    public List<OrderItem> findByQuantityGreaterThan(int quantity) {
        String sql = "SELECT * FROM order_items WHERE quantity > ? ORDER BY quantity DESC, order_item_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), quantity);
    }

    /**
     * Get order items with product details (join query)
     * @param orderId the order ID
     * @return list of order items with product information
     */
    public List<OrderItem> findByOrderIdWithProductDetails(Long orderId) {
        String sql = """
            SELECT oi.*, p.title as product_title, p.price as current_price, p.image_url
            FROM order_items oi 
            LEFT JOIN Product p ON oi.product_id = p.product_id 
            WHERE oi.order_id = ? 
            ORDER BY oi.order_item_id
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), orderId);
    }

    /**
     * Update quantity for an order item
     * @param orderItemId the order item ID
     * @param newQuantity the new quantity
     */
    public void updateQuantity(Long orderItemId, int newQuantity) {
        String sql = "UPDATE order_items SET quantity = ? WHERE order_item_id = ?";
        int updatedRows = jdbcTemplate.update(sql, newQuantity, orderItemId);
        
        if (updatedRows == 0) {
            throw new OrderItemNotFoundException("Order item not found with id: " + orderItemId);
        }
    }

    // Custom exception class
    public static class OrderItemNotFoundException extends RuntimeException {
        public OrderItemNotFoundException(String message) {
            super(message);
        }
    }
}
