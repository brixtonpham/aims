-- =====================================================
-- AIMS Database Schema - H2 Compatible Version
-- =====================================================

-- Drop existing tables if they exist (for clean reinstall)
DROP TABLE IF EXISTS payment_transactions CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS delivery_info CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS customer_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS products CASCADE;

-- =====================================================
-- V1.0.1: Core Product Tables
-- =====================================================

-- Products table with polymorphic design
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL CHECK (price > 0),
    weight DECIMAL(10,2),
    rush_order_supported BOOLEAN DEFAULT FALSE,
    image_url VARCHAR(500),
    barcode VARCHAR(50),
    import_date TIMESTAMP,
    introduction TEXT,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Book specific fields
    genre VARCHAR(100),
    page_count INTEGER,
    publication_date DATE,
    authors VARCHAR(255),
    publishers VARCHAR(255),
    cover_type VARCHAR(50),
    
    -- CD specific fields  
    artist VARCHAR(255),
    record_label VARCHAR(255),
    release_date VARCHAR(50),
    track_count INTEGER,
    
    -- DVD specific fields
    director VARCHAR(255),
    runtime_minutes INTEGER,
    studio VARCHAR(255),
    subtitle_languages VARCHAR(255),
    dubbing_languages VARCHAR(255),
    
    CONSTRAINT chk_product_type CHECK (product_type IN ('book', 'cd', 'dvd', 'PRODUCT'))
);

-- =====================================================
-- V1.0.2: Shopping Cart Tables
-- =====================================================

-- Carts table
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart items table
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uk_cart_product UNIQUE(cart_id, product_id)
);

-- =====================================================
-- V1.0.3: Order Management Tables
-- =====================================================

-- Delivery information table
CREATE TABLE delivery_info (
    delivery_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT NOT NULL,
    province VARCHAR(100) NOT NULL,
    delivery_message TEXT,
    delivery_fee INTEGER DEFAULT 0 CHECK (delivery_fee >= 0),
    delivery_type VARCHAR(20) DEFAULT 'STANDARD',
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_delivery_type CHECK (delivery_type IN ('STANDARD', 'EXPRESS', 'RUSH'))
);

-- Orders table
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    total_before_vat BIGINT DEFAULT 0 CHECK (total_before_vat >= 0),
    total_after_vat BIGINT DEFAULT 0 CHECK (total_after_vat >= 0),
    status VARCHAR(20) DEFAULT 'PENDING',
    vat_rate INTEGER DEFAULT 10 CHECK (vat_rate >= 0 AND vat_rate <= 100),
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50),
    is_rush_order BOOLEAN DEFAULT FALSE,
    delivery_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_orders_delivery FOREIGN KEY (delivery_id) REFERENCES delivery_info(delivery_id),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

-- Order items table  
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_title VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    rush_order_enabled BOOLEAN DEFAULT FALSE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price INTEGER NOT NULL CHECK (unit_price >= 0),
    total_fee BIGINT,
    delivery_time TIMESTAMP,
    instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_order_item_status CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

-- =====================================================
-- V1.0.4: Payment System Tables
-- =====================================================

-- Payment transactions table
CREATE TABLE payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    transaction_no VARCHAR(100),
    amount BIGINT NOT NULL CHECK (amount > 0),
    bank_code VARCHAR(20),
    response_code VARCHAR(10),
    transaction_status VARCHAR(20) DEFAULT 'PENDING',
    pay_date VARCHAR(20),
    payment_method VARCHAR(50),
    gateway_transaction_id VARCHAR(100),
    gateway_response_message TEXT,
    currency_code VARCHAR(3) DEFAULT 'VND',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_transaction_status CHECK (transaction_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED'))
);

-- =====================================================
-- V1.0.5: User Management Tables
-- =====================================================

-- Users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_user_role CHECK (role IN ('CUSTOMER', 'ADMIN', 'STAFF'))
);

-- Customer profiles table
CREATE TABLE customer_profiles (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    customer_code VARCHAR(50) UNIQUE,
    loyalty_points INTEGER DEFAULT 0,
    preferred_payment_method VARCHAR(50),
    shipping_address TEXT,
    billing_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_customer_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =====================================================
-- V1.0.6: Indexes for Performance
-- =====================================================

-- Product indexes
CREATE INDEX idx_products_type ON products(product_type);
CREATE INDEX idx_products_title ON products(title);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_quantity ON products(quantity);
CREATE INDEX idx_products_rush_order ON products(rush_order_supported);
CREATE INDEX idx_products_created_at ON products(created_at);

-- Cart indexes
CREATE INDEX idx_carts_customer_id ON carts(customer_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Order indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_time ON orders(order_time);
CREATE INDEX idx_orders_delivery_id ON orders(delivery_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_status ON order_items(status);

-- Payment indexes
CREATE INDEX idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(transaction_status);
CREATE INDEX idx_payment_transactions_gateway_id ON payment_transactions(gateway_transaction_id);
CREATE INDEX idx_payment_transactions_created_at ON payment_transactions(created_at);
CREATE INDEX idx_payment_transactions_payment_method ON payment_transactions(payment_method);

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_customer_profiles_user_id ON customer_profiles(user_id);
CREATE INDEX idx_customer_profiles_customer_code ON customer_profiles(customer_code);
