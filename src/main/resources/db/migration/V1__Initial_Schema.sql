-- Complete database schema for TakeOne Backend
-- MySQL 8.0

-- Drop tables if exist (for fresh start)
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Firebase authentication
    uid                  VARCHAR(128) NOT NULL UNIQUE COMMENT 'Firebase UID',

    -- Username (unique, case-sensitive)
    username             VARCHAR(50) UNIQUE COMMENT 'User chosen username',
    username_hash        VARCHAR(64) UNIQUE COMMENT 'SHA-256 hash of username for fast lookup',

    -- Encrypted sensitive fields (handled by AttributeEncryptor)
    email                VARCHAR(500) COMMENT 'Encrypted email',
    mobile               VARCHAR(500) COMMENT 'Encrypted mobile number',
    first_name           VARCHAR(500) COMMENT 'Encrypted first name',
    last_name            VARCHAR(500) COMMENT 'Encrypted last name',
    dob                  VARCHAR(500) COMMENT 'Encrypted date of birth (YYYY-MM-DD)',

    -- Public profile fields
    display_name         VARCHAR(100) COMMENT 'Public display name',
    company              VARCHAR(200) COMMENT 'Company name',
    location             VARCHAR(200) COMMENT 'User location',
    profile_picture_url  VARCHAR(1000) COMMENT 'Profile picture URL',

    -- Account details
    account_type         VARCHAR(20)  NOT NULL DEFAULT 'ARTIST' COMMENT 'ARTIST, SCOUT, EMPLOYER, ADMIN',

    -- Status flags
    is_portfolio_created BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Profile setup completed',
    is_email_verified    BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Email verified by Firebase',
    is_phone_verified    BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Phone verified by Firebase',
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'Account active status',

    -- Timestamps
    last_login           DATETIME COMMENT 'Last login timestamp',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time',
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',

    -- Indexes for performance
    INDEX                idx_uid (uid),
    INDEX                idx_username (username),
    INDEX                idx_username_hash (username_hash),
    INDEX                idx_account_type (account_type),
    INDEX                idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts table';

-- Sessions table
CREATE TABLE sessions
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Foreign key to users
    user_id          BIGINT       NOT NULL COMMENT 'User ID reference',

    -- Session details
    refresh_token    VARCHAR(255) NOT NULL UNIQUE COMMENT 'Session token (Base64 URL encoded)',
    device_id        VARCHAR(500) COMMENT 'Device identifier or User-Agent',
    ip_address       VARCHAR(100) COMMENT 'Client IP address',

    -- Session validity
    expires_at       DATETIME     NOT NULL COMMENT 'Session expiration time',
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'Session active status',

    -- Timestamps
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Session creation time',
    last_accessed_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last access time',

    -- Foreign key constraint
    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Indexes for performance
    INDEX            idx_refresh_token (refresh_token),
    INDEX            idx_user_active (user_id, is_active),
    INDEX            idx_expires_at (expires_at),
    INDEX            idx_last_accessed (last_accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User sessions table';

-- Sample data for testing (optional)
-- Password: Use Firebase authentication, not stored here

-- Insert a test user
INSERT INTO users (uid,
                   username,
                   username_hash,
                   display_name,
                   account_type,
                   is_active,
                   is_email_verified,
                   is_phone_verified)
VALUES ('test_firebase_uid_123',
        'test_artist',
        SHA2('test_artist', 256),
        'Test Artist',
        'ARTIST',
        TRUE,
        TRUE,
        FALSE);

-- Verify foreign key relationship
SELECT CONSTRAINT_NAME,
       TABLE_NAME,
       COLUMN_NAME,
       REFERENCED_TABLE_NAME,
       REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME IS NOT NULL;