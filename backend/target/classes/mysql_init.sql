CREATE DATABASE IF NOT EXISTS wisebookpal DEFAULT CHARACTER SET utf8mb4;
USE wisebookpal;

CREATE TABLE IF NOT EXISTS stored_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(120),
  size BIGINT NOT NULL,
  sha256 CHAR(64),
  storage_path VARCHAR(512) NOT NULL,
  uploader VARCHAR(64),
  created_at BIGINT NOT NULL,
  INDEX idx_stored_file_uploader (uploader),
  INDEX idx_stored_file_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(64) PRIMARY KEY,
  password VARCHAR(128) NOT NULL,
  current_role VARCHAR(32),
  seller_status VARCHAR(16) NOT NULL DEFAULT 'NONE',
  payment_code_file_id BIGINT,
  phone VARCHAR(32),
  email VARCHAR(128),
  gender VARCHAR(16) NOT NULL DEFAULT 'secret',
  created_at BIGINT NOT NULL,
  INDEX idx_users_role (current_role),
  INDEX idx_users_seller_status (seller_status),
  INDEX idx_users_payment_code (payment_code_file_id),
  CONSTRAINT fk_users_payment_code FOREIGN KEY (payment_code_file_id) REFERENCES stored_file(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @users_has_payment_code := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'payment_code_file_id'
);
SET @users_payment_code_sql := IF(
  @users_has_payment_code = 0,
  'ALTER TABLE users ADD COLUMN payment_code_file_id BIGINT NULL, ADD INDEX idx_users_payment_code (payment_code_file_id), ADD CONSTRAINT fk_users_payment_code FOREIGN KEY (payment_code_file_id) REFERENCES stored_file(id)',
  'SELECT 1'
);
PREPARE stmt_users_payment_code FROM @users_payment_code_sql;
EXECUTE stmt_users_payment_code;
DEALLOCATE PREPARE stmt_users_payment_code;

SET @users_has_phone := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'phone'
);
SET @users_phone_sql := IF(
  @users_has_phone = 0,
  'ALTER TABLE users ADD COLUMN phone VARCHAR(32) NULL',
  'SELECT 1'
);
PREPARE stmt_users_phone FROM @users_phone_sql;
EXECUTE stmt_users_phone;
DEALLOCATE PREPARE stmt_users_phone;

SET @users_has_email := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'email'
);
SET @users_email_sql := IF(
  @users_has_email = 0,
  'ALTER TABLE users ADD COLUMN email VARCHAR(128) NULL',
  'SELECT 1'
);
PREPARE stmt_users_email FROM @users_email_sql;
EXECUTE stmt_users_email;
DEALLOCATE PREPARE stmt_users_email;

SET @users_has_gender := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'gender'
);
SET @users_gender_sql := IF(
  @users_has_gender = 0,
  'ALTER TABLE users ADD COLUMN gender VARCHAR(16) NOT NULL DEFAULT ''secret''',
  'SELECT 1'
);
PREPARE stmt_users_gender FROM @users_gender_sql;
EXECUTE stmt_users_gender;
DEALLOCATE PREPARE stmt_users_gender;

-- Add user management columns
SET @users_has_status := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'status');
SET @users_status_sql := IF(@users_has_status = 0, 'ALTER TABLE users ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT ''normal'', ADD COLUMN credit_score INT NOT NULL DEFAULT 100, ADD COLUMN student_id VARCHAR(32), ADD COLUMN last_login_time BIGINT, ADD COLUMN last_audit_time BIGINT', 'SELECT 1');
PREPARE stmt_users_status FROM @users_status_sql;
EXECUTE stmt_users_status;
DEALLOCATE PREPARE stmt_users_status;

-- Add balance column for settlement (idempotent)
SET @users_has_balance := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'balance');
SET @users_balance_sql := IF(@users_has_balance = 0, 'ALTER TABLE users ADD COLUMN balance DOUBLE NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt_users_balance FROM @users_balance_sql;
EXECUTE stmt_users_balance;
DEALLOCATE PREPARE stmt_users_balance;

SET @users_has_last_audit_time := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'last_audit_time');
SET @users_last_audit_time_sql := IF(@users_has_last_audit_time = 0, 'ALTER TABLE users ADD COLUMN last_audit_time BIGINT', 'SELECT 1');
PREPARE stmt_users_last_audit_time FROM @users_last_audit_time_sql;
EXECUTE stmt_users_last_audit_time;
DEALLOCATE PREPARE stmt_users_last_audit_time;

-- Add blacklist columns
SET @users_has_blacklist_reason := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'blacklist_reason');
SET @users_blacklist_reason_sql := IF(@users_has_blacklist_reason = 0, 'ALTER TABLE users ADD COLUMN blacklist_reason VARCHAR(512), ADD COLUMN blacklist_time BIGINT, ADD COLUMN blacklist_operator VARCHAR(64)', 'SELECT 1');
PREPARE stmt_users_blacklist_reason FROM @users_blacklist_reason_sql;
EXECUTE stmt_users_blacklist_reason;
DEALLOCATE PREPARE stmt_users_blacklist_reason;

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator VARCHAR(64) NOT NULL,
  target_user VARCHAR(64) NOT NULL,
  action VARCHAR(64) NOT NULL,
  detail TEXT,
  create_time BIGINT NOT NULL,
  INDEX idx_logs_target (target_user, create_time),
  INDEX idx_logs_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_daily_activity (
  username VARCHAR(64) NOT NULL,
  activity_date DATE NOT NULL,
  PRIMARY KEY (username, activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  username VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  PRIMARY KEY (username, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32),
  address VARCHAR(512) NOT NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  INDEX idx_user_address_user (username, created_at),
  INDEX idx_user_address_default (username, is_default),
  CONSTRAINT fk_user_address_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_token (
  token VARCHAR(64) PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  created_at BIGINT NOT NULL,
  INDEX idx_user_token_username (username),
  CONSTRAINT fk_user_token_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_name VARCHAR(255) NOT NULL,
  author VARCHAR(255),
  original_price DOUBLE,
  sell_price DOUBLE,
  description TEXT,
  seller_name VARCHAR(64) NOT NULL,
  cover_url VARCHAR(512),
  isbn VARCHAR(64),
  publisher VARCHAR(255),
  publish_date VARCHAR(64),
  condition_level VARCHAR(32),
  stock INT NOT NULL DEFAULT 1,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL,
  seller_type VARCHAR(32),
  audit_reason VARCHAR(512),
  audit_time BIGINT,
  INDEX idx_books_seller (seller_name),
  INDEX idx_books_status_created (status, created_at),
  INDEX idx_books_price (sell_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add audit columns to books (idempotent check)
SET @books_has_audit_reason := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'audit_reason');
SET @books_audit_reason_sql := IF(@books_has_audit_reason = 0, 'ALTER TABLE books ADD COLUMN audit_reason VARCHAR(512), ADD COLUMN audit_time BIGINT', 'SELECT 1');
PREPARE stmt_books_audit FROM @books_audit_reason_sql;
EXECUTE stmt_books_audit;
DEALLOCATE PREPARE stmt_books_audit;

CREATE TABLE IF NOT EXISTS cart_item (
  username VARCHAR(64) NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (username, book_id),
  INDEX idx_cart_item_user (username),
  CONSTRAINT fk_cart_item_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS favorites (
  username VARCHAR(64) NOT NULL,
  book_id BIGINT NOT NULL,
  created_at BIGINT NOT NULL,
  PRIMARY KEY (username, book_id),
  INDEX idx_favorites_user (username),
  CONSTRAINT fk_favorites_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_favorites_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  book_name VARCHAR(255) NOT NULL,
  seller_name VARCHAR(64) NOT NULL,
  price DOUBLE NOT NULL,
  buyer_name VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  expire_at BIGINT NOT NULL,
  create_time TIMESTAMP NOT NULL,
  INDEX idx_orders_buyer (buyer_name, create_time),
  INDEX idx_orders_seller (seller_name, create_time),
  CONSTRAINT fk_orders_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100;

SET @orders_has_expire_at := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'expire_at'
);
SET @orders_expire_at_sql := IF(
  @orders_has_expire_at = 0,
  'ALTER TABLE orders ADD COLUMN expire_at BIGINT NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt_orders_expire_at FROM @orders_expire_at_sql;
EXECUTE stmt_orders_expire_at;
DEALLOCATE PREPARE stmt_orders_expire_at;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  score_condition INT NOT NULL,
  score_service INT NOT NULL,
  comment TEXT,
  tags TEXT,
  create_time BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  audit_reason VARCHAR(512),
  audit_time BIGINT,
  INDEX idx_reviews_user (username, create_time),
  INDEX idx_reviews_order (order_id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add audit columns to reviews (idempotent check)
SET @reviews_has_status := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'status');
SET @reviews_status_sql := IF(@reviews_has_status = 0, 'ALTER TABLE reviews ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT ''pending'', ADD COLUMN audit_reason VARCHAR(512), ADD COLUMN audit_time BIGINT', 'SELECT 1');
PREPARE stmt_reviews_status FROM @reviews_status_sql;
EXECUTE stmt_reviews_status;
DEALLOCATE PREPARE stmt_reviews_status;

-- Add images column to reviews (idempotent check)
SET @reviews_has_images := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'images');
SET @reviews_images_sql := IF(@reviews_has_images = 0, 'ALTER TABLE reviews ADD COLUMN images TEXT', 'SELECT 1');
PREPARE stmt_reviews_images FROM @reviews_images_sql;
EXECUTE stmt_reviews_images;
DEALLOCATE PREPARE stmt_reviews_images;

-- Funds settlement ledger
CREATE TABLE IF NOT EXISTS funds_settlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  seller_name VARCHAR(64) NOT NULL,
  amount DOUBLE NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'settled',
  settle_time BIGINT NOT NULL,
  created_time BIGINT NOT NULL,
  INDEX idx_funds_seller (seller_name, settle_time),
  INDEX idx_funds_order (order_id),
  CONSTRAINT fk_funds_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_funds_seller FOREIGN KEY (seller_name) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_draft (
  username VARCHAR(64) NOT NULL,
  order_id BIGINT NOT NULL,
  score_condition INT NOT NULL,
  score_service INT NOT NULL,
  comment TEXT,
  tags TEXT,
  images TEXT,
  create_time BIGINT NOT NULL,
  PRIMARY KEY (username, order_id),
  CONSTRAINT fk_review_draft_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_review_draft_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add images column to review_draft (idempotent check)
SET @review_draft_has_images := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'review_draft' AND COLUMN_NAME = 'images');
SET @review_draft_images_sql := IF(@review_draft_has_images = 0, 'ALTER TABLE review_draft ADD COLUMN images TEXT', 'SELECT 1');
PREPARE stmt_review_draft_images FROM @review_draft_images_sql;
EXECUTE stmt_review_draft_images;
DEALLOCATE PREPARE stmt_review_draft_images;

CREATE TABLE IF NOT EXISTS complaints (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  type VARCHAR(64),
  detail TEXT,
  create_time BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  audit_reason VARCHAR(512),
  audit_time BIGINT,
  INDEX idx_complaints_user (username, create_time),
  INDEX idx_complaints_order (order_id),
  CONSTRAINT fk_complaints_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_complaints_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add audit columns to complaints (idempotent check)
SET @complaints_has_audit_reason := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'complaints' AND COLUMN_NAME = 'audit_reason');
SET @complaints_audit_reason_sql := IF(@complaints_has_audit_reason = 0, 'ALTER TABLE complaints ADD COLUMN audit_reason VARCHAR(512), ADD COLUMN audit_time BIGINT', 'SELECT 1');
PREPARE stmt_complaints_audit FROM @complaints_audit_reason_sql;
EXECUTE stmt_complaints_audit;
DEALLOCATE PREPARE stmt_complaints_audit;

-- Add images column to complaints (idempotent check)
SET @complaints_has_images := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'complaints' AND COLUMN_NAME = 'images');
SET @complaints_images_sql := IF(@complaints_has_images = 0, 'ALTER TABLE complaints ADD COLUMN images TEXT', 'SELECT 1');
PREPARE stmt_complaints_images FROM @complaints_images_sql;
EXECUTE stmt_complaints_images;
DEALLOCATE PREPARE stmt_complaints_images;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  to_user VARCHAR(64) NOT NULL,
  type VARCHAR(64) NOT NULL,
  title VARCHAR(255),
  content TEXT,
  create_time BIGINT NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_notifications_to_user (to_user, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_read (
  notification_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  read_time BIGINT NOT NULL,
  PRIMARY KEY (notification_id, username),
  CONSTRAINT fk_notification_read_notif FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
  CONSTRAINT fk_notification_read_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conv_key VARCHAR(255) NOT NULL,
  from_user VARCHAR(64) NOT NULL,
  to_user VARCHAR(64) NOT NULL,
  book_id BIGINT,
  order_id BIGINT,
  content TEXT,
  create_time BIGINT NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  type VARCHAR(16) NOT NULL DEFAULT 'text',
  INDEX idx_chat_conv_time (conv_key, create_time),
  INDEX idx_chat_to_user_read (to_user, is_read),
  INDEX idx_chat_users (from_user, to_user),
  CONSTRAINT fk_chat_from_user FOREIGN KEY (from_user) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_chat_to_user FOREIGN KEY (to_user) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @chat_message_has_type := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'type');
SET @chat_message_type_sql := IF(@chat_message_has_type = 0, 'ALTER TABLE chat_message ADD COLUMN type VARCHAR(16) NOT NULL DEFAULT ''text''', 'SELECT 1');
PREPARE stmt_chat_message_type FROM @chat_message_type_sql;
EXECUTE stmt_chat_message_type;
DEALLOCATE PREPARE stmt_chat_message_type;

INSERT IGNORE INTO users (username, password, current_role, seller_status, created_at) VALUES
('buyer1', '123456', 'buyer', 'NONE', UNIX_TIMESTAMP() * 1000),
('seller1', '123456', 'seller', 'APPROVED', UNIX_TIMESTAMP() * 1000),
('admin1', '123456', 'admin', 'APPROVED', UNIX_TIMESTAMP() * 1000),
('user1', '123456', NULL, 'APPROVED', UNIX_TIMESTAMP() * 1000);

INSERT IGNORE INTO user_roles (username, role) VALUES
('buyer1', 'buyer'),
('seller1', 'seller'),
('admin1', 'admin'),
('user1', 'buyer'),
('user1', 'seller');



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

-- Add brute-force protection columns
SET @users_has_failed_attempts := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'failed_login_attempts');
SET @users_failed_attempts_sql := IF(@users_has_failed_attempts = 0, 'ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt_users_failed_attempts FROM @users_failed_attempts_sql;
EXECUTE stmt_users_failed_attempts;
DEALLOCATE PREPARE stmt_users_failed_attempts;

SET @users_has_lockout_time := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'lockout_end_time');
SET @users_lockout_time_sql := IF(@users_has_lockout_time = 0, 'ALTER TABLE users ADD COLUMN lockout_end_time BIGINT', 'SELECT 1');
PREPARE stmt_users_lockout_time FROM @users_lockout_time_sql;
EXECUTE stmt_users_lockout_time;
DEALLOCATE PREPARE stmt_users_lockout_time;
