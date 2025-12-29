-- Create table for managing FCM device tokens
-- Supports multi-device per user and soft-deletion (isActive)
CREATE TABLE user_device_tokens
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    fcm_token       VARCHAR(500) NOT NULL,
    device_id       VARCHAR(500) NOT NULL COMMENT 'Unique identifier from the mobile device',
    platform        VARCHAR(20)  NOT NULL COMMENT 'ANDROID, IOS, WEB',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Constraints
    UNIQUE KEY uk_fcm_token (fcm_token),
    UNIQUE KEY uk_user_device (user_id, device_id),
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- Indexes for efficient lookup
CREATE INDEX idx_user_tokens ON user_device_tokens (user_id, is_active);