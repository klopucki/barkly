ALTER TABLE school ADD COLUMN history_image_id BIGINT REFERENCES school_image(id) ON DELETE SET NULL;
CREATE TABLE school_message (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES school(id),
    message TEXT NOT NULL,
    link TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX school_message_school_created_idx ON school_message (school_id, created_at DESC, id DESC);
