CREATE TABLE notification_sent_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    case_name VARCHAR(255) NOT NULL,
    sent_date DATE NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (token, case_name, sent_date)
)