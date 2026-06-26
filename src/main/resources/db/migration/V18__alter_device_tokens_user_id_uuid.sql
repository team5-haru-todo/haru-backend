ALTER TABLE device_tokens ALTER COLUMN user_id TYPE UUID USING NULL;
ALTER TABLE device_tokens ADD CONSTRAINT fk_device_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;