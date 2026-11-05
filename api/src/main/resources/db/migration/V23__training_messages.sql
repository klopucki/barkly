CREATE TABLE training_message (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL REFERENCES training(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX training_message_training_created_idx ON training_message (training_id, created_at DESC, id DESC);
