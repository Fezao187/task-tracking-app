-- Index for filtering by status
CREATE INDEX idx_tasks_status ON tasks(status);

-- Index for filtering by due_date
CREATE INDEX idx_tasks_due_date ON tasks(due_date);

-- Index for filtering by assigned_user_id
CREATE INDEX idx_tasks_assigned_user
    ON tasks(assigned_user_id);