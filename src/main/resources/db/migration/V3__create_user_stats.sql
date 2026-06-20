CREATE TABLE user_stats (
    user_id BIGINT PRIMARY KEY,
    current_streak INT NOT NULL DEFAULT 0,
    max_streak INT NOT NULL DEFAULT 0,
    total_success_days INT NOT NULL DEFAULT 0,
    last_success_date DATE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_stats_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);