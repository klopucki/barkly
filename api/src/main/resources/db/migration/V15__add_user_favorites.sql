CREATE TABLE user_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_favorite_type_check CHECK (target_type IN ('SCHOOL', 'TRAINING', 'ARTICLE')),
    CONSTRAINT user_favorite_unique UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX user_favorite_user_type_target_idx ON user_favorite (user_id, target_type, target_id);
