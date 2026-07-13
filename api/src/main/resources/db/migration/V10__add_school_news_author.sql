ALTER TABLE school_news ADD COLUMN author_user_id BIGINT;

UPDATE school_news SET author_user_id = 1 WHERE author_user_id IS NULL;

ALTER TABLE school_news ALTER COLUMN author_user_id SET NOT NULL;
ALTER TABLE school_news
    ADD CONSTRAINT school_news_author_user_id_fk
    FOREIGN KEY (author_user_id) REFERENCES app_user(id);
