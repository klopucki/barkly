CREATE TABLE training
(
    id BIGSERIAL PRIMARY KEY,

    school_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,

    trainer_name VARCHAR(200) NOT NULL,

    level VARCHAR(30) NOT NULL,

    start_at TIMESTAMP NOT NULL,

    capacity INTEGER,

    description TEXT NOT NULL
);