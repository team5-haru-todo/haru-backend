CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    kakao_id VARCHAR(255) UNIQUE,
    apple_id VARCHAR(255) UNIQUE,
    nickname VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    withdrawn_at TIMESTAMPTZ,

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'WITHDRAWN'))
);