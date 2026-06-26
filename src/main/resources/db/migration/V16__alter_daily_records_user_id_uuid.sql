ALTER TABLE daily_records ALTER COLUMN user_id TYPE UUID USING NULL;
ALTER TABLE daily_records ADD CONSTRAINT fk_daily_records_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;