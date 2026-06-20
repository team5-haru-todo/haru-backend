CREATE TABLE daily_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_task_id BIGINT,
    record_date DATE NOT NULL,
    fire_earned BOOLEAN NOT NULL DEFAULT FALSE,
    current_task_selected_at TIMESTAMPTZ,
    first_completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_daily_records_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_daily_records_current_task
        FOREIGN KEY (current_task_id)
        REFERENCES tasks(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_daily_records_user_date
        UNIQUE (user_id, record_date)
);

CREATE INDEX idx_daily_records_user_date
ON daily_records(user_id, record_date);