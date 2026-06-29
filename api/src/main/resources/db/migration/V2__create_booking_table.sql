CREATE TABLE booking
(
    id          BIGSERIAL PRIMARY KEY,
    training_id BIGINT       NOT NULL,
    owner_name  VARCHAR(200) NOT NULL,
    email       VARCHAR(320) NOT NULL,
    dog_name    VARCHAR(200) NOT NULL,
    dog_age     INTEGER      NOT NULL,
    notes       TEXT,
    created_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_booking_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
);