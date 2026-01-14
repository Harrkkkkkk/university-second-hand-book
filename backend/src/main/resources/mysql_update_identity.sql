
-- Create Mock University Database Table
CREATE TABLE IF NOT EXISTS university_students (
  student_id VARCHAR(32) PRIMARY KEY,
  name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Populate with some dummy data for testing
INSERT IGNORE INTO university_students (student_id, name) VALUES
('2021001', '张三'),
('2021002', '李四'),
('2021003', '王五'),
('2024001', 'TestUser');

-- Add Real-name Authentication columns to users
SET @users_has_real_name := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'real_name');
SET @users_real_name_sql := IF(@users_has_real_name = 0, 'ALTER TABLE users ADD COLUMN real_name VARCHAR(64)', 'SELECT 1');
PREPARE stmt_users_real_name FROM @users_real_name_sql;
EXECUTE stmt_users_real_name;
DEALLOCATE PREPARE stmt_users_real_name;

SET @users_has_is_verified := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'is_verified');
SET @users_is_verified_sql := IF(@users_has_is_verified = 0, 'ALTER TABLE users ADD COLUMN is_verified TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt_users_is_verified FROM @users_is_verified_sql;
EXECUTE stmt_users_is_verified;
DEALLOCATE PREPARE stmt_users_is_verified;
