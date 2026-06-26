ALTER TABLE weekly_reports ALTER COLUMN user_id TYPE UUID USING NULL;
ALTER TABLE weekly_reports ADD CONSTRAINT fk_weekly_reports_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;