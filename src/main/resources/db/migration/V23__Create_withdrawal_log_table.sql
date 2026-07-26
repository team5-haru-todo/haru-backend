-- 탈퇴 이벤트 1건당 1행
CREATE TABLE withdrawal_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL,
    etc_reason TEXT,
    withdrawn_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 탈퇴 시 선택한 사유. 여러 개 선택 가능하므로 withdrawal_log 1건당 N행
CREATE TABLE withdrawal_reason (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    withdrawal_log_id BIGINT NOT NULL REFERENCES withdrawal_log(id),
    reason VARCHAR(100) NOT NULL
);

CREATE INDEX idx_withdrawal_reason_reason ON withdrawal_reason (reason);
CREATE INDEX idx_withdrawal_reason_log_id ON withdrawal_reason (withdrawal_log_id);