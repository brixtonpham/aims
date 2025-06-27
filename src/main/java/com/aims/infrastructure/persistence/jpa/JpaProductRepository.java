package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.product.entity.Product;
import com.aims.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JPA implementation of ProductRepository interface.
 * Provides data access operations for Product entities.
 */
@Repository
@Transactional
public class JpaProductRepository implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Product save(Product product) {
        if (product.getProductId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    private Product insert(Product product) {
        String sql = """
            INSERT INTO products (product_type, title, price, weight, rush_order_supported, image_url, 
                                barcode, import_date, introduction, quantity) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getType());
            ps.setString(2, product.getTitle());
            ps.setInt(3, product.getPrice());
            ps.setObject(4, product.getWeight());
            ps.setBoolean(5, product.getRushOrderSupported());
            ps.setString(6, product.getImageUrl());
            ps.setString(7, product.getBarcode());
            ps.setTimestamp(8, product.getImportDate() != null ? Timestamp.valueOf(product.getImportDate()) : null);
            ps.setString(9, product.getIntroduction());
            ps.setInt(10, product.getQuantity());
            return ps;
        }, keyHolder);

        // Get the generated ID
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && !keys.isEmpty()) {
            Object productId = keys.get("PRODUCT_ID");
            if (productId != null) {
                product.setProductId(((Number) productId).longValue());
            }
        }
        
        return product;
    }

    @Override
    public Optional<Product> findById(Long productId) {
        try {
            String sql = "SELECT * FROM products WHERE product_id = ?";
            Product product = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Product.class), productId);
            return Optional.of(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY product_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }

    @Override
    public List<Product> findByType(String type) {
        String sql = "SELECT * FROM products WHERE LOWER(product_type) = LOWER(?) ORDER BY product_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), type);
    }

    @Override
    public List<Product> findByTitleContaining(String title) {
        String sql = "SELECT * FROM products WHERE LOWER(title) LIKE LOWER(?) ORDER BY product_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), "%" + title + "%");
    }

    @Override
    public boolean existsById(Long productId) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId);
        return count != null && count > 0;
    }

    @Override
    public boolean isAvailable(Long productId, int quantity) {
        try {
            String sql = "SELECT quantity FROM products WHERE product_id = ?";
            Integer availableQuantity = jdbcTemplate.queryForObject(sql, Integer.class, productId);
            return availableQuantity != null && availableQuantity >= quantity;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public Product update(Product product) {
        String sql = """
            UPDATE products SET 
                title = ?, price = ?, weight = ?, rush_order_supported = ?, 
                image_url = ?, barcode = ?, import_date = ?, introduction = ?, quantity = ?
            WHERE product_id = ?
            """;
        
        int updatedRows = jdbcTemplate.update(sql,
            product.getTitle(),
            product.getPrice(),
            product.getWeight(),
            product.getRushOrderSupported(),
            product.getImageUrl(),
            product.getBarcode(),
            product.getImportDate(),
            product.getIntroduction(),
            product.getQuantity(),
            product.getProductId()
        );

        if (updatedRows == 0) {
            throw new ProductNotFoundException("Product not found with id: " + product.getProductId());
        }
        
        return product;
    }

    @Override
    public void deleteById(Long productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        int deletedRows = jdbcTemplate.update(sql, productId);
        
        if (deletedRows == 0) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }
    }

    @Override
    public List<Product> findLowStockProducts(int threshold) {
        String sql = "SELECT * FROM products WHERE quantity <= ? ORDER BY quantity ASC, product_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), threshold);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM products";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Update product quantity after order placement
     * @param productId the product ID
     * @param quantity the quantity to reduce
     */
    public void reduceQuantity(Long productId, int quantity) {
        String sql = "UPDATE Product SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        int updatedRows = jdbcTemplate.update(sql, quantity, productId, quantity);
        
        if (updatedRows == 0) {
            throw new InsufficientStockException("Insufficient stock for product ID: " + productId);
        }
    }

    /**
     * Restore product quantity after order cancellation
     * @param productId the product ID
     * @param quantity the quantity to restore
     */
    public void restoreQuantity(Long productId, int quantity) {
        String sql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";
        jdbcTemplate.update(sql, quantity, productId);
    }

    /**
     * Get product weight for delivery calculation
     * @param productId the product ID
     * @return product weight
     */
    public double getProductWeight(Long productId) {
        try {
            String sql = "SELECT weight FROM Product WHERE product_id = ?";
            Float weight = jdbcTemplate.queryForObject(sql, Float.class, productId);
            return weight != null ? weight.doubleValue() : 0.0;
        } catch (EmptyResultDataAccessException e) {
            return 0.0;
        }
    }

    // Custom exception classes
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
