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
  created_at BIGINT NOT NULL,
  INDEX idx_users_role (current_role),
  INDEX idx_users_seller_status (seller_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  username VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  PRIMARY KEY (username, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
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
  INDEX idx_books_seller (seller_name),
  INDEX idx_books_status_created (status, created_at),
  INDEX idx_books_price (sell_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  create_time TIMESTAMP NOT NULL,
  INDEX idx_orders_buyer (buyer_name, create_time),
  INDEX idx_orders_seller (seller_name, create_time),
  CONSTRAINT fk_orders_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  score_condition INT NOT NULL,
  score_service INT NOT NULL,
  comment TEXT,
  tags TEXT,
  create_time BIGINT NOT NULL,
  INDEX idx_reviews_user (username, create_time),
  INDEX idx_reviews_order (order_id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_draft (
  username VARCHAR(64) NOT NULL,
  order_id BIGINT NOT NULL,
  score_condition INT NOT NULL,
  score_service INT NOT NULL,
  comment TEXT,
  tags TEXT,
  create_time BIGINT NOT NULL,
  PRIMARY KEY (username, order_id),
  CONSTRAINT fk_review_draft_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_review_draft_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS complaints (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  type VARCHAR(64),
  detail TEXT,
  create_time BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  INDEX idx_complaints_user (username, create_time),
  INDEX idx_complaints_order (order_id),
  CONSTRAINT fk_complaints_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_complaints_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  INDEX idx_chat_conv_time (conv_key, create_time),
  INDEX idx_chat_to_user_read (to_user, is_read),
  INDEX idx_chat_users (from_user, to_user),
  CONSTRAINT fk_chat_from_user FOREIGN KEY (from_user) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_chat_to_user FOREIGN KEY (to_user) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

INSERT IGNORE INTO books (id, book_name, author, original_price, sell_price, seller_name, condition_level, stock, status, created_at) VALUES
(1, 'Java编程思想', 'Bruce Eckel', 108, 30, '卖家1', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(2, 'Python入门到精通', '张三', 89, 25, '卖家1', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(3, '红楼梦', '曹雪芹', 45, 15, '卖家2', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(4, '经济学原理', '曼昆', 68, 20, '卖家2', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(5, '数据结构与算法', '李四', 79, 20, '卖家1', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(6, '西游记', '吴承恩', 38, 12, '卖家2', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(7, 'MySQL实战', '王五', 99, 28, '卖家1', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000),
(8, '财务管理', '赵六', 59, 18, '卖家2', '九成新', 10, 'on_sale', UNIX_TIMESTAMP() * 1000);
