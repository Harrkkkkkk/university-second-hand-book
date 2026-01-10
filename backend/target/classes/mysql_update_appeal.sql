ALTER TABLE users ADD COLUMN blacklist_reason VARCHAR(512), ADD COLUMN blacklist_time BIGINT, ADD COLUMN blacklist_operator VARCHAR(64);

CREATE TABLE IF NOT EXISTS blacklist_appeals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  reason TEXT NOT NULL,
  evidence TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  create_time BIGINT NOT NULL,
  audit_reason VARCHAR(512),
  audit_time BIGINT,
  auditor VARCHAR(64),
  INDEX idx_blacklist_appeals_user (username),
  INDEX idx_blacklist_appeals_status (status),
  CONSTRAINT fk_blacklist_appeals_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
