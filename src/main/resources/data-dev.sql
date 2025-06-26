-- =====================================================
-- AIMS Development Data - H2 Compatible
-- =====================================================

-- Insert sample products
INSERT INTO products (product_type, title, price, quantity, rush_order_supported, authors, genre, page_count, publishers, cover_type) VALUES
('book', 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 45000, 100, true, 'Robert C. Martin', 'Programming', 432, 'Prentice Hall', 'Paperback'),
('book', 'Design Patterns: Elements of Reusable Object-Oriented Software', 50000, 75, true, 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Programming', 395, 'Addison-Wesley', 'Hardcover'),
('book', 'The Pragmatic Programmer: Your Journey To Mastery', 42000, 120, false, 'Andy Hunt, Dave Thomas', 'Programming', 352, 'Addison-Wesley', 'Paperback'),
('book', 'Effective Java', 38000, 90, true, 'Joshua Bloch', 'Programming', 416, 'Addison-Wesley', 'Paperback'),
('book', 'Spring in Action', 55000, 60, false, 'Craig Walls', 'Programming', 520, 'Manning Publications', 'Paperback');

INSERT INTO products (product_type, title, price, quantity, rush_order_supported, artist, genre, record_label, track_count, release_date) VALUES
('cd', 'Abbey Road', 25000, 50, false, 'The Beatles', 'Rock', 'Apple Records', 17, '1969'),
('cd', 'Dark Side of the Moon', 28000, 40, false, 'Pink Floyd', 'Progressive Rock', 'Harvest Records', 10, '1973'),
('cd', 'Thriller', 22000, 65, false, 'Michael Jackson', 'Pop', 'Epic Records', 9, '1982'),
('cd', 'Back in Black', 24000, 35, false, 'AC/DC', 'Hard Rock', 'Atlantic Records', 10, '1980');

INSERT INTO products (product_type, title, price, quantity, rush_order_supported, director, genre, runtime_minutes, studio, subtitle_languages) VALUES
('dvd', 'The Shawshank Redemption', 15000, 80, false, 'Frank Darabont', 'Drama', 142, 'Castle Rock Entertainment', 'English, Vietnamese'),
('dvd', 'Inception', 18000, 60, true, 'Christopher Nolan', 'Sci-Fi', 148, 'Warner Bros', 'English, Vietnamese, Korean'),
('dvd', 'The Godfather', 20000, 45, false, 'Francis Ford Coppola', 'Crime', 175, 'Paramount Pictures', 'English, Vietnamese'),
('dvd', 'Pulp Fiction', 16000, 70, false, 'Quentin Tarantino', 'Crime', 154, 'Miramax Films', 'English, Vietnamese, French');

-- Insert sample users
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, role) VALUES
('admin', 'admin@aims.com', '$2a$10$dummy.hash.for.admin.user.password', 'System', 'Administrator', '+84900000000', 'ADMIN'),
('customer1', 'john.doe@aims.com', '$2a$10$dummy.hash.for.customer1.password', 'John', 'Doe', '+84901234567', 'CUSTOMER'),
('customer2', 'jane.smith@aims.com', '$2a$10$dummy.hash.for.customer2.password', 'Jane', 'Smith', '+84987654321', 'CUSTOMER'),
('staff1', 'staff@aims.com', '$2a$10$dummy.hash.for.staff.user.password', 'Staff', 'Member', '+84905555555', 'STAFF');

-- Insert sample customer profiles
INSERT INTO customer_profiles (user_id, customer_code, loyalty_points, preferred_payment_method, shipping_address) VALUES
(2, 'CUST001', 1500, 'VNPAY', '123 Main Street, District 1, Ho Chi Minh City'),
(3, 'CUST002', 2300, 'VNPAY', '456 Oak Avenue, Ba Dinh District, Hanoi');

-- Insert sample delivery info
INSERT INTO delivery_info (name, phone, email, address, province, delivery_type, delivery_fee) VALUES
('John Doe', '+84901234567', 'john.doe@aims.com', '123 Main Street, District 1', 'Ho Chi Minh City', 'STANDARD', 30000),
('Jane Smith', '+84987654321', 'jane.smith@aims.com', '456 Oak Avenue, Ba Dinh District', 'Hanoi', 'EXPRESS', 45000),
('Bob Johnson', '+84912345678', 'bob.johnson@aims.com', '789 Pine Road, District 3', 'Ho Chi Minh City', 'RUSH', 60000);

-- Insert sample orders
INSERT INTO orders (customer_id, total_before_vat, total_after_vat, status, payment_method, is_rush_order, delivery_id) VALUES
('CUST001', 90000, 99000, 'PENDING', 'VNPAY', false, 1),
('CUST002', 150000, 165000, 'CONFIRMED', 'VNPAY', true, 2),
('CUST001', 45000, 49500, 'DELIVERED', 'VNPAY', false, 3);

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, product_title, quantity, unit_price, total_fee) VALUES
(1, 1, 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 2, 45000, 90000),
(2, 2, 'Design Patterns: Elements of Reusable Object-Oriented Software', 2, 50000, 100000),
(2, 6, 'Abbey Road', 2, 25000, 50000),
(3, 1, 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 1, 45000, 45000);

-- Insert sample payment transactions
INSERT INTO payment_transactions (order_id, amount, transaction_status, payment_method, bank_code, response_code) VALUES
('1', 99000, 'PENDING', 'VNPAY', 'NCB', NULL),
('2', 165000, 'SUCCESS', 'VNPAY', 'VIETCOMBANK', '00'),
('3', 49500, 'SUCCESS', 'VNPAY', 'TECHCOMBANK', '00');

-- Insert sample cart data
INSERT INTO carts (customer_id) VALUES
('CUST001'),
('CUST002');

INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 3, 1),
(1, 7, 2),
(2, 4, 1),
(2, 8, 1);
