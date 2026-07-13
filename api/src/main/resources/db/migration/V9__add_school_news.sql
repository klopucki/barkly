CREATE TABLE school_news (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES school(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    published_at TIMESTAMP NOT NULL
);
CREATE INDEX school_news_school_id_published_at_idx ON school_news (school_id, published_at DESC);
