CREATE TABLE weekly_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    success_days INT NOT NULL DEFAULT 0,
    total_completion_count INT NOT NULL DEFAULT 0,
    recurring_completion_count INT NOT NULL DEFAULT 0,
    current_streak_snapshot INT NOT NULL DEFAULT 0,
    max_streak_snapshot INT NOT NULL DEFAULT 0,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    viewed_at TIMESTAMPTZ,

    CONSTRAINT fk_weekly_reports_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_weekly_reports_user_week
        UNIQUE (user_id, week_start_date)
);

CREATE INDEX idx_weekly_reports_user_week
ON weekly_reports(user_id, week_start_date, week_end_date);