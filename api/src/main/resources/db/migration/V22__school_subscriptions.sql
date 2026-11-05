CREATE TABLE school_subscription (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES school(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, school_id)
);
CREATE INDEX school_subscription_school_idx ON school_subscription (school_id, user_id);
