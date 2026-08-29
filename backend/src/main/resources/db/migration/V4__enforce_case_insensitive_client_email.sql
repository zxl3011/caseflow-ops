UPDATE clients
SET email = LOWER(BTRIM(email));

CREATE UNIQUE INDEX uq_clients_email_lower
    ON clients (LOWER(email));
