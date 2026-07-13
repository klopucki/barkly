CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE school (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(260) NOT NULL UNIQUE,
    address VARCHAR(300) NOT NULL,
    krs VARCHAR(14),
    description TEXT NOT NULL DEFAULT '',
    activities TEXT NOT NULL DEFAULT '',
    pricing TEXT NOT NULL DEFAULT '',
    owner_user_id BIGINT NOT NULL REFERENCES app_user(id)
);

INSERT INTO app_user (id, email, password_hash, display_name, role)
VALUES (1, 'legacy@barkly.local', '$2a$10$GQnVMntkZi.kAQSdYpEQBuZNqYasLGzyW0hlhbFW4KIaQfCZ4O2kG', 'Legacy administrator', 'SUPER_ADMIN');

INSERT INTO school (id, name, slug, address, owner_user_id)
VALUES (1, 'Barkly', 'barkly-1', 'Adres do uzupełnienia', 1);

SELECT setval(pg_get_serial_sequence('app_user', 'id'), (SELECT MAX(id) FROM app_user));
SELECT setval(pg_get_serial_sequence('school', 'id'), (SELECT MAX(id) FROM school));

UPDATE training SET school_id = 1 WHERE school_id IS NULL OR school_id <> 1;
ALTER TABLE training ADD CONSTRAINT training_school_id_fk FOREIGN KEY (school_id) REFERENCES school(id);
