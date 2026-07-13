CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(30) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clients (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         phone VARCHAR(50),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE legal_cases (
                             id BIGSERIAL PRIMARY KEY,
                             case_number VARCHAR(50) NOT NULL UNIQUE,
                             case_type VARCHAR(50) NOT NULL,
                             status VARCHAR(30) NOT NULL,
                             filing_date DATE NOT NULL,
                             court VARCHAR(150),
                             statute_limitation_date DATE,
                             client_id BIGINT NOT NULL REFERENCES clients(id),
                             partner_id BIGINT REFERENCES users(id),
                             paralegal_id BIGINT REFERENCES users(id),
                             description TEXT,
                             search_vector TSVECTOR,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_legal_cases_search_vector
    ON legal_cases
    USING GIN (search_vector);