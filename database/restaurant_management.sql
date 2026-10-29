-- Non-destructive bootstrap: never drop an existing restaurant database.
CREATE DATABASE IF NOT EXISTS restaurant_management;
USE restaurant_management;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS restaurant_tables (
    table_id INT PRIMARY KEY AUTO_INCREMENT,
    table_number INT UNIQUE NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS menu_categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    available BOOLEAN DEFAULT TRUE,
    description VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES menu_categories(category_id) ON DELETE RESTRICT,
    INDEX idx_menu_category (category_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    -- Customer is required for both DINE_IN and TAKEAWAY orders; takeaway has no table.
    customer_id INT NOT NULL,
    table_id INT DEFAULT NULL,
    order_type ENUM('DINE_IN', 'TAKEAWAY') NOT NULL,
    status ENUM('PENDING', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id) ON DELETE SET NULL,
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status)
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    item_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI') NOT NULL,
    status ENUM('PAID', 'PENDING', 'REFUNDED') DEFAULT 'PENDING',
    paid_at TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(30) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- SAMPLE DATA

INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', SHA2('admin123', 256), 'System Admin', 'ADMIN'),
('staff', SHA2('staff123', 256), 'Regular Staff', 'STAFF');

INSERT INTO customers (name, phone, email, address) VALUES
('Aarav Sharma', '9876543210', 'aarav@example.com', '123 MG Road, Mumbai'),
('Diya Patel', '9876543211', 'diya@example.com', '456 Brigade Road, Bangalore'),
('Rohan Gupta', '9876543212', 'rohan@example.com', '789 Park Street, Kolkata'),
('Neha Singh', '9876543213', 'neha@example.com', '101 Connaught Place, Delhi'),
('Vikram Reddy', '9876543214', 'vikram@example.com', '202 Jubilee Hills, Hyderabad');

INSERT INTO restaurant_tables (table_number, capacity, status) VALUES
(1, 2, 'AVAILABLE'),
(2, 2, 'AVAILABLE'),
(3, 4, 'AVAILABLE'),
(4, 4, 'OCCUPIED'),
(5, 4, 'RESERVED'),
(6, 6, 'AVAILABLE'),
(7, 6, 'AVAILABLE'),
(8, 8, 'AVAILABLE');

INSERT INTO menu_categories (name) VALUES
('Starters'),
('Main Course'),
('Beverages'),
('Desserts');

INSERT INTO menu_items (name, category_id, price, available, description) VALUES
('Paneer Tikka', 1, 250.00, TRUE, 'Grilled cottage cheese with spices'),
('Chicken Kebabs', 1, 350.00, TRUE, 'Juicy grilled chicken pieces'),
('Samosa', 1, 50.00, TRUE, 'Crispy pastry filled with spiced potatoes'),
('Butter Chicken', 2, 450.00, TRUE, 'Creamy tomato gravy with tender chicken'),
('Dal Makhani', 2, 300.00, TRUE, 'Slow-cooked black lentils'),
('Biryani', 2, 400.00, TRUE, 'Fragrant rice layered with spices and meat or veg'),
('Naan', 2, 40.00, TRUE, 'Traditional Indian flatbread'),
('Masala Chai', 3, 30.00, TRUE, 'Spiced Indian tea'),
('Lassi', 3, 80.00, TRUE, 'Sweet yogurt-based drink'),
('Fresh Lime Soda', 3, 60.00, TRUE, 'Refreshing lime drink'),
('Gulab Jamun', 4, 100.00, TRUE, 'Deep-fried milk solids in sugar syrup'),
('Rasmalai', 4, 120.00, TRUE, 'Cottage cheese dumplings in sweetened milk');

INSERT INTO orders (customer_id, table_id, order_type, status) VALUES
(1, 4, 'DINE_IN', 'PREPARING'),
(2, NULL, 'TAKEAWAY', 'READY'),
(3, 1, 'DINE_IN', 'COMPLETED');

INSERT INTO order_items (order_id, item_id, quantity, unit_price, item_total) VALUES
(1, 1, 2, 250.00, 500.00),
(1, 7, 4, 40.00, 160.00),
(2, 4, 1, 450.00, 450.00),
(2, 6, 2, 400.00, 800.00),
(3, 5, 1, 300.00, 300.00),
(3, 11, 2, 100.00, 200.00);

INSERT INTO payments (order_id, subtotal, tax_amount, discount, grand_total, payment_method, status) VALUES
(3, 500.00, 25.00, 0.00, 525.00, 'UPI', 'PAID'),
(2, 1250.00, 62.50, 50.00, 1262.50, 'CARD', 'PAID');

INSERT INTO inventory (item_name, quantity, unit, reorder_level) VALUES
('Basmati Rice', 50, 'kg', 20),
('Wheat Flour', 30, 'kg', 15),
('Cooking Oil', 8, 'liters', 10),
('Paneer', 5, 'kg', 5),
('Chicken', 15, 'kg', 10),
('Onions', 25, 'kg', 20),
('Tomatoes', 12, 'kg', 15),
('Salt', 10, 'kg', 2),
('Sugar', 4, 'kg', 5),
('Tea Powder', 2, 'kg', 3);
