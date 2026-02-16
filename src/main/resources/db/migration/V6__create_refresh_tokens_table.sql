CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(150) NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL
);