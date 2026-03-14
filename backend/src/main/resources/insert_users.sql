-- Script INSERT dữ liệu vào bảng users

-- 1. Tạo bảng users (nếu chưa có)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Xóa dữ liệu cũ (nếu muốn)
DELETE FROM users;

-- 3. INSERT dữ liệu mẫu vào bảng users

-- Insert người dùng admin
INSERT INTO users (username, password, full_name, phone, role, active, created_at, updated_at)
VALUES ('admin', '123456', 'Quản trị viên', '0123456789', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert người dùng user thường
INSERT INTO users (username, password, full_name, phone, role, active, created_at, updated_at)
VALUES ('user1', 'password1', 'Nguyễn Văn A', '0912345678', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert người dùng user khác
INSERT INTO users (username, password, full_name, phone, role, active, created_at, updated_at)
VALUES ('user2', 'password2', 'Trần Thị B', '0987654321', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert người dùng manager
INSERT INTO users (username, password, full_name, phone, role, active, created_at, updated_at)
VALUES ('manager', 'manager123', 'Lê Văn C', '0909090909', 'MANAGER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert người dùng bị khóa
INSERT INTO users (username, password, full_name, phone, role, active, created_at, updated_at)
VALUES ('blocked_user', 'pass123', 'Phạm Văn D', '0888888888', 'USER', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Kiểm tra dữ liệu đã insert
SELECT * FROM users;

