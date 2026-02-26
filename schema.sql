CREATE DATABASE IF NOT EXISTS oceanviewresortdb;
USE oceanviewresortdb;

-- 1. Staff Table (Common for all roles)
CREATE TABLE IF NOT EXISTS staff (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST') NOT NULL
);

-- 2. Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    type ENUM('Single', 'Double', 'Suite') NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('Available', 'Occupied', 'Maintenance') DEFAULT 'Available',
    description TEXT
);

-- 3. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('Confirmed', 'Cancelled', 'Completed') DEFAULT 'Confirmed',
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE
);

-- Seed Data
INSERT INTO staff (email, password, full_name, role) 
VALUES 
('admin@oceanview.com', 'admin@1234', 'Primary Admin', 'ADMIN'),
('reception@oceanview.com', 'staff@1234', 'Front Desk', 'RECEPTIONIST')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO rooms (room_number, type, price, status, description)
VALUES 
('101', 'Single', 100.00, 'Available', 'Cozy single room with sea view'),
('201', 'Double', 200.00, 'Occupied', 'Spacious double room with balcony'),
('301', 'Suite', 500.00, 'Available', 'Luxury suite with private pool')
ON DUPLICATE KEY UPDATE room_id=room_id;
