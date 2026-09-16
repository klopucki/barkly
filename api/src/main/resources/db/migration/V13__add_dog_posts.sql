CREATE TABLE dog_post (
    id BIGSERIAL PRIMARY KEY,
    dog_id BIGINT NOT NULL REFERENCES dog(id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    image_key VARCHAR(500),
    published_at TIMESTAMP NOT NULL
);

CREATE INDEX dog_post_published_at_idx ON dog_post (published_at DESC);
