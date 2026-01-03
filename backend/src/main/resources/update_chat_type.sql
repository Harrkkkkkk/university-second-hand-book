SET @chat_has_type := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'chat_message'
    AND COLUMN_NAME = 'type'
);
SET @chat_type_sql := IF(
  @chat_has_type = 0,
  'ALTER TABLE chat_message ADD COLUMN type VARCHAR(16) NOT NULL DEFAULT ''text''',
  'SELECT 1'
);
PREPARE stmt_chat_type FROM @chat_type_sql;
EXECUTE stmt_chat_type;
DEALLOCATE PREPARE stmt_chat_type;
