CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    task_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_tasks_task_type
        CHECK (task_type IN ('GENERAL', 'RECURRING'))
);

CREATE INDEX idx_tasks_user_id
ON tasks(user_id);

CREATE INDEX idx_tasks_user_deleted
ON tasks(user_id, deleted_at);