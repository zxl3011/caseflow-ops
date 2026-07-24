ALTER TABLE legal_cases
    ALTER COLUMN filing_date DROP NOT NULL,
    ADD COLUMN created_by_user_id BIGINT REFERENCES users(id);

UPDATE legal_cases AS legal_case
SET created_by_user_id = lead_assignment.user_id
FROM case_assignments AS lead_assignment
WHERE lead_assignment.legal_case_id = legal_case.id
  AND lead_assignment.assignment_role = 'LEAD_LAWYER'
  AND legal_case.created_by_user_id IS NULL;
