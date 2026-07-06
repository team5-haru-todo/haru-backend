CREATE TABLE invalidated_tokens (
    token_id VARCHAR(36) PRIMARY KEY,
    expires_at TIMESTAMP NOT NULL,
    invalidated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invalidated_tokens_expires_at ON invalidated_tokens(expires_at);
