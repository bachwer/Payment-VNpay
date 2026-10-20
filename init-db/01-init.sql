-- Initialize VNPay Payment Database and Schema
CREATE DATABASE IF NOT EXISTS `payment_vnpay_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `payment_vnpay_db`;

CREATE TABLE IF NOT EXISTS `payment_transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_code` VARCHAR(255) NOT NULL UNIQUE,
    `vnp_transaction_no` VARCHAR(255) NULL,
    `amount` DECIMAL(19, 2) NULL,
    `response_code` VARCHAR(50) NULL,
    `bank_code` VARCHAR(50) NULL,
    `status` VARCHAR(50) NULL,
    `created_at` DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
