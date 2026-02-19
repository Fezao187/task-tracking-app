CREATE TABLE tasks
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    status           VARCHAR(50)  NOT NULL,
    due_date         TIMESTAMP,
    created_date     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_user_id BIGINT,
    CONSTRAINT fk_task_user
        FOREIGN KEY (assigned_user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);