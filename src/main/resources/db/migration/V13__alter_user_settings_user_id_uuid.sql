ALTER TABLE user_settings ALTER COLUMN user_id TYPE UUID USING NULL;
ALTER TABLE user_settings ADD CONSTRAINT fk_user_settings_user
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
