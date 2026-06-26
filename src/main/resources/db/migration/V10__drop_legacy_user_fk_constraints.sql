ALTER TABLE user_settings DROP CONSTRAINT fk_user_settings_user;
ALTER TABLE user_stats DROP CONSTRAINT fk_user_stats_user;
ALTER TABLE tasks DROP CONSTRAINT fk_tasks_user;
ALTER TABLE daily_records DROP CONSTRAINT fk_daily_records_user;
ALTER TABLE weekly_reports DROP CONSTRAINT fk_weekly_reports_user;
ALTER TABLE device_tokens DROP CONSTRAINT fk_device_tokens_user;