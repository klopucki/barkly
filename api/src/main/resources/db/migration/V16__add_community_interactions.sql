CREATE TABLE dog_post_reaction (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES dog_post(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    reaction_type VARCHAR(20) NOT NULL,
    CONSTRAINT dog_post_reaction_user_unique UNIQUE (post_id, user_id)
);

CREATE TABLE dog_post_comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES dog_post(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    published_at TIMESTAMP NOT NULL
);

CREATE INDEX dog_post_comment_post_published_idx ON dog_post_comment (post_id, published_at);

CREATE TABLE training_paw_reaction (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL REFERENCES training(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT training_paw_reaction_user_unique UNIQUE (training_id, user_id)
);

ALTER TABLE booking ADD COLUMN dog_id BIGINT REFERENCES dog(id) ON DELETE SET NULL;
CREATE UNIQUE INDEX booking_training_dog_active_unique ON booking (training_id, dog_id) WHERE deleted_at IS NULL AND dog_id IS NOT NULL;
