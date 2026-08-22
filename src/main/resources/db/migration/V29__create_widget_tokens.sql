CREATE TABLE widget_tokens (
    family_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_widget_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE

);
CREATE SEQUENCE widget_token_family_seq;
--조건으로 자주 조회되는 컬럼에 인덱스를 생성하여 조회 성능 향상
CREATE INDEX idx_widget_tokens_token_hash ON widget_tokens(token_hash);
CREATE INDEX idx_widget_tokens_expires_at ON widget_tokens(expires_at);
CREATE INDEX idx_widget_tokens_family_id ON widget_tokens(family_id);