-- Create table for durable storage of pending (unseen) E2E messages
-- Messages are only stored here until delivered to the recipient
CREATE TABLE pending_messages
(
    message_id        VARCHAR(36) PRIMARY KEY COMMENT 'Message UUID from client',
    sender_id         BIGINT      NOT NULL,
    recipient_id      BIGINT      NOT NULL,
    encrypted_content TEXT        NOT NULL,
    message_type      VARCHAR(20) NOT NULL,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Constraints and Indexes
    INDEX             idx_recipient_pending (recipient_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Durable relay storage for E2E messages';