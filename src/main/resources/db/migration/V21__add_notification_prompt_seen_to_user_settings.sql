ALTER TABLE user_settings
ADD COLUMN notification_prompt_seen BOOLEAN NOT NULL DEFAULT FALSE;
