ALTER TABLE booking ADD COLUMN user_id BIGINT REFERENCES app_user(id);
UPDATE booking b SET user_id = d.owner_user_id FROM dog d WHERE b.dog_id = d.id;

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    training_id BIGINT NOT NULL REFERENCES training(id),
    event_key TEXT NOT NULL,
    type VARCHAR(40) NOT NULL,
    message TEXT NOT NULL,
    link TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    UNIQUE (user_id, event_key)
);
CREATE INDEX notification_user_created_idx ON notification(user_id, created_at DESC, id DESC);
CREATE INDEX notification_unread_idx ON notification(user_id) WHERE read_at IS NULL;
CREATE TABLE notification_preference (
    user_id BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
    reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE
);
