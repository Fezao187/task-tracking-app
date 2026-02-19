ALTER TABLE refresh_tokens
    ADD COLUMN expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE refresh_tokens
SET expires_at = created_at + INTERVAL '7 days';

CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);