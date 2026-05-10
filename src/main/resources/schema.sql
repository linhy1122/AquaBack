-- AquaBackEnd Database Schema
-- Execute this SQL script to initialize the database

CREATE DATABASE IF NOT EXISTS Aqua DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE Aqua;

-- Users table (supports roles, account status control)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER/MANAGER',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    account_locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否锁定',
    account_expired TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否过期',
    credentials_expired TINYINT(1) NOT NULL DEFAULT 0 COMMENT '凭据是否过期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, email, role, enabled)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@aqua.com', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username=username;

-- Insert default test user (password: password)
INSERT INTO users (username, password, email, role, enabled)
VALUES ('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test@aqua.com', 'USER', 1)
ON DUPLICATE KEY UPDATE username=username;
