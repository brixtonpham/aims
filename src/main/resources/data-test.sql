-- Test data for VNPay E2E Integration Tests
-- This file is automatically loaded by Spring Boot for test profile

-- Insert test products
INSERT INTO products (product_id, product_type, title, price, quantity, weight, rush_order_supported, image_url, introduction, created_at, updated_at) VALUES
(1, 'book', 'Clean Architecture Test Book', 50000, 100, 0.5, true, 'http://example.com/book1.jpg', 'A comprehensive guide to clean architecture', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'PRODUCT', 'Test Product 2', 30000, 50, 0.3, false, 'http://example.com/product2.jpg', 'Second test product', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'book', 'Java Programming Essentials', 45000, 75, 0.6, true, 'http://example.com/java-book.jpg', 'Essential Java programming concepts', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'cd', 'Classical Music Collection', 25000, 30, 0.15, false, 'http://example.com/classical-cd.jpg', 'Beautiful classical music collection', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'dvd', 'Action Movie Pack', 35000, 40, 0.1, true, 'http://example.com/action-dvd.jpg', 'Exciting action movies', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test customers (if you have a customers table)
-- INSERT INTO customers (customer_id, name, email, phone, address) VALUES
-- ('TEST_CUSTOMER_001', 'Test Customer', 'test@example.com', '0123456789', 'Test Address');

-- Insert test orders
INSERT INTO orders (order_id, customer_id, total_before_vat, total_after_vat, status, vat_rate, order_time, payment_method, is_rush_order, created_at, updated_at) VALUES
(1, 'TEST_CUSTOMER_001', 72727, 80000, 'PENDING', 10, CURRENT_TIMESTAMP, 'VNPAY', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'TEST_CUSTOMER_001', 45454, 50000, 'CONFIRMED', 10, CURRENT_TIMESTAMP, 'VNPAY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test order items
INSERT INTO order_items (order_item_id, order_id, product_id, product_title, quantity, unit_price, created_at, updated_at) VALUES
(1, 1, 1, 'Clean Architecture Test Book', 1, 50000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 'Test Product 2', 1, 30000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 3, 'Java Programming Essentials', 1, 45000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test carts
INSERT INTO carts (cart_id, customer_id, created_at, updated_at) VALUES
(1, 'TEST_CUSTOMER_001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'TEST_CUSTOMER_002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test cart items
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, created_at, updated_at) VALUES
(1, 1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test delivery info
INSERT INTO delivery_info (delivery_id, name, phone, email, address, province, delivery_fee, delivery_type, created_at, updated_at) VALUES
(1, 'Test Customer', '0123456789', 'test@example.com', '123 Test Street', 'Hanoi', 30000, 'STANDARD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Rush Customer', '0987654321', 'rush@example.com', '456 Rush Avenue', 'Ho Chi Minh City', 50000, 'RUSH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert some sample payment transactions for testing queries
INSERT INTO payment_transactions (id, order_id, transaction_no, amount, bank_code, response_code, transaction_status, pay_date, payment_method, gateway_transaction_id, gateway_response_message, currency_code, created_at, updated_at) VALUES
(1, 'SAMPLE_ORDER_001', 'VNP001', 100000, 'NCB', '00', 'SUCCESS', '20250627120000', 'VNPay', 'VNP123456789', 'Transaction successful', 'VND', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'SAMPLE_ORDER_002', 'VNP002', 50000, 'VIETCOMBANK', '07', 'FAILED', '20250627130000', 'VNPay', 'VNP987654321', 'Insufficient funds', 'VND', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);