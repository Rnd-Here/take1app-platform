-- Add device and app metadata columns to user_device_tokens table
ALTER TABLE user_device_tokens
ADD COLUMN device_model VARCHAR(255),
    ADD COLUMN os_version VARCHAR(255),
    ADD COLUMN app_version VARCHAR(255),
    ADD COLUMN timezone VARCHAR(255),
    ADD COLUMN language VARCHAR(50);