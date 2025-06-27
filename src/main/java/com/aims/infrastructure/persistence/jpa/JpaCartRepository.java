package com.aims.infrastructure.persistence.jpa;

import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.domain.cart.repository.CartRepository;
import com.aims.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of CartRepository interface.
 * Provides data access operations for Cart entities.
 */
@Repository
@Transactional
public class JpaCartRepository implements CartRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaCartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Cart save(Cart cart) {
        if (cart.getCartId() == null) {
            return insert(cart);
        } else {
            return update(cart);
        }
    }

    private Cart insert(Cart cart) {
        String sql = """
            INSERT INTO carts (customer_id) 
            VALUES (?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"cart_id"});
            ps.setString(1, cart.getCustomerId());
            return ps;
        }, keyHolder);

        // Get the generated ID
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new CartNotFoundException("Failed to retrieve generated cart ID");
        }
        Long generatedId = key.longValue();
        cart.setCartId(generatedId);
        
        // Save cart items if any
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            saveCartItems(cart);
        }
        
        return cart;
    }

    @Override
    public Optional<Cart> findById(Long cartId) {
        try {
            String sql = "SELECT * FROM carts WHERE cart_id = ?";
            Cart cart = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Cart.class), cartId);
            return Optional.of(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cart> findByCustomerId(Long customerId) {
        try {
            String sql = "SELECT * FROM carts WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
            Cart cart = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Cart.class), customerId.toString());
            
            // Load cart items if cart exists
            if (cart != null) {
                loadCartItems(cart);
            }
            
            return Optional.of(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private void saveCartItems(Cart cart) {
        // First, delete existing cart items
        String deleteSql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(deleteSql, cart.getCartId());
        
        // Then insert new cart items
        String insertSql = """
            INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        for (var item : cart.getCartItems()) {
            jdbcTemplate.update(insertSql,
                cart.getCartId(),
                item.getProduct().getProductId(),
                item.getQuantity(),
                java.sql.Timestamp.valueOf(item.getCreatedAt() != null ? item.getCreatedAt() : java.time.LocalDateTime.now()),
                java.sql.Timestamp.valueOf(item.getUpdatedAt() != null ? item.getUpdatedAt() : java.time.LocalDateTime.now())
            );
        }
    }

    @Override
    public Cart update(Cart cart) {
        String sql = """
            UPDATE carts SET 
                customer_id = ?
            WHERE cart_id = ?
            """;
        
        int updatedRows = jdbcTemplate.update(sql,
            cart.getCustomerId(),
            cart.getCartId()
        );

        if (updatedRows == 0) {
            throw new CartNotFoundException("Cart not found with id: " + cart.getCartId());
        }
        
        // Save cart items if any
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            saveCartItems(cart);
        }
        
        return cart;
    }

    @Override
    public void deleteById(Long cartId) {
        String sql = "DELETE FROM carts WHERE cart_id = ?";
        int deletedRows = jdbcTemplate.update(sql, cartId);
        
        if (deletedRows == 0) {
            throw new CartNotFoundException("Cart not found with id: " + cartId);
        }
    }

    @Override
    public boolean existsById(Long cartId) {
        String sql = "SELECT COUNT(*) FROM carts WHERE cart_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cartId);
        return count != null && count > 0;
    }

    @Override
    public void clearCart(Long cartId) {
        // Delete all cart items for this cart
        String deleteItemsSql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(deleteItemsSql, cartId);
    }

    @Override
    public Optional<Cart> findByIdWithItems(Long cartId) {
        // Find the cart and load its items
        Optional<Cart> cartOpt = findById(cartId);
        if (cartOpt.isPresent()) {
            loadCartItems(cartOpt.get());
        }
        return cartOpt;
    }

    @Override
    public List<Cart> findAll() {
        String sql = "SELECT * FROM carts ORDER BY cart_id";
        List<Cart> carts = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Cart.class));
        // Load cart items for each cart
        for (Cart cart : carts) {
            loadCartItems(cart);
        }
        return carts;
    }

    @Override
    public int deleteInactiveCartsOlderThan(int days) {
        String sql = """
            DELETE FROM carts 
            WHERE updated_at < (CURRENT_TIMESTAMP - INTERVAL '%d days')
            """.formatted(days);
        
        return jdbcTemplate.update(sql);
    }

    /**
     * Get cart items count
     * @param cartId the cart ID
     * @return number of items in cart
     */
    public int getCartItemsCount(Long cartId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cartId);
        return count != null ? count : 0;
    }

    private void loadCartItems(Cart cart) {
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.created_at, ci.updated_at,
                   p.product_id, p.title, p.price, p.quantity as product_quantity, p.product_type
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.product_id
            WHERE ci.cart_id = ?
            """;
        
        List<CartItem> cartItems = jdbcTemplate.query(sql, (rs, rowNum) -> {
            // Create Product
            Product product = new Product();
            product.setProductId(rs.getLong("product_id"));
            product.setTitle(rs.getString("title"));
            product.setPrice(rs.getInt("price"));
            product.setQuantity(rs.getInt("product_quantity"));
            product.setType(rs.getString("product_type"));
            
            // Create CartItem
            CartItem item = new CartItem();
            item.setCartItemId(rs.getLong("cart_item_id"));
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(rs.getInt("quantity"));
            item.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                             rs.getTimestamp("created_at").toLocalDateTime() : null);
            item.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                             rs.getTimestamp("updated_at").toLocalDateTime() : null);
            
            return item;
        }, cart.getCartId());
        
        cart.getCartItems().clear();
        cart.getCartItems().addAll(cartItems);
    }

    @Override
    public Optional<Cart> findByCartItemId(Long cartItemId) {
        try {
            String sql = """
                SELECT c.cart_id, c.customer_id, c.created_at, c.updated_at
                FROM carts c
                INNER JOIN cart_items ci ON c.cart_id = ci.cart_id
                WHERE ci.cart_item_id = ?
                """;
            
            Cart cart = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Cart.class), cartItemId);
            
            if (cart != null) {
                loadCartItems(cart);
            }
            
            return Optional.of(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Custom exception class
    public static class CartNotFoundException extends RuntimeException {
        public CartNotFoundException(String message) {
            super(message);
        }
    }
}
