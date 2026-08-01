ALTER TABLE clients
    ADD COLUMN user_id BIGINT UNIQUE REFERENCES users(id);

UPDATE clients AS client
SET user_id = app_user.id
FROM users AS app_user
WHERE LOWER(client.email) = LOWER(app_user.email)
  AND app_user.role = 'CLIENT';

CREATE TABLE case_assignments (
    id BIGSERIAL PRIMARY KEY,
    legal_case_id BIGINT NOT NULL REFERENCES legal_cases(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    assignment_role VARCHAR(30) NOT NULL,
    assigned_by_user_id BIGINT REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_case_assignments_case_user UNIQUE (legal_case_id, user_id),
    CONSTRAINT chk_case_assignments_role
        CHECK (assignment_role IN ('LEAD_LAWYER', 'ASSISTING_LAWYER', 'PARALEGAL'))
);

CREATE INDEX idx_case_assignments_user
    ON case_assignments(user_id);

INSERT INTO case_assignments (
    legal_case_id,
    user_id,
    assignment_role,
    assigned_by_user_id
)
SELECT
    id,
    partner_id,
    'LEAD_LAWYER',
    partner_id
FROM legal_cases
WHERE partner_id IS NOT NULL
ON CONFLICT (legal_case_id, user_id) DO NOTHING;

INSERT INTO case_assignments (
    legal_case_id,
    user_id,
    assignment_role,
    assigned_by_user_id
)
SELECT
    id,
    paralegal_id,
    'PARALEGAL',
    partner_id
FROM legal_cases
WHERE paralegal_id IS NOT NULL
ON CONFLICT (legal_case_id, user_id) DO NOTHING;

ALTER TABLE legal_cases
    DROP COLUMN partner_id,
    DROP COLUMN paralegal_id;
