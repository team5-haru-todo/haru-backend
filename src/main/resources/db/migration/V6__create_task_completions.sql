CREATE TABLE task_completions (
    id BIGSERIAL PRIMARY KEY,
    daily_record_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    content_snapshot VARCHAR(255) NOT NULL,
    task_type_snapshot VARCHAR(20) NOT NULL,
    completion_type VARCHAR(20) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_task_completions_daily_record
        FOREIGN KEY (daily_record_id)
        REFERENCES daily_records(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_completions_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_task_completions_task_type
        CHECK (task_type_snapshot IN ('GENERAL', 'RECURRING')),

    CONSTRAINT chk_task_completions_completion_type
        CHECK (completion_type IN ('FIRST', 'ADDITIONAL')),

    CONSTRAINT uq_task_completions_daily_task
        UNIQUE (daily_record_id, task_id)
);

CREATE UNIQUE INDEX uq_task_completions_first
ON task_completions (daily_record_id)
WHERE completion_type = 'FIRST';

CREATE INDEX idx_task_completions_daily_record
ON task_completions(daily_record_id);

CREATE INDEX idx_task_completions_task
ON task_completions(task_id);