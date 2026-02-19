-- Insert admin user
INSERT INTO users (username, email, password_hash, role_id)
SELECT
    'admin',
    'admin@example.com',
    '$2a$10$zZHCJU/yeFjG4yPSMJvpR.XyyHRQoqE3m0zSiQn0cSUF.lQCzmHNq', --- (admin123)
    r.id
FROM roles r
WHERE r.name = 'ADMIN'
    ON CONFLICT (email) DO NOTHING;

-- Demo user
INSERT INTO users (username, email, password_hash, role_id)
SELECT
    'john',
    'john@example.com',
    '$2a$10$eb7KchMNZtF7T0x26R9XnOiBjZUo0L1dxBz5YUxeV9mYdJzbJc.3S', --- (user123)
    r.id
FROM roles r
WHERE r.name = 'USER'
    ON CONFLICT (email) DO NOTHING;

-- New task
INSERT INTO tasks (title, description, status, due_date, assigned_user_id)
SELECT
    'Complete Assessment',
    'Finish the Java developer assessment project',
    'NEW',
    NOW() + INTERVAL '2 days',
    u.id
FROM users u
WHERE u.email = 'john@example.com';

-- Overdue task (for scheduler testing)
INSERT INTO tasks (title, description, status, due_date, assigned_user_id)
SELECT
    'Submit Timesheet',
    'Submit last month timesheet',
    'IN_PROGRESS',
    NOW() - INTERVAL '3 days',
    u.id
FROM users u
WHERE u.email = 'john@example.com';