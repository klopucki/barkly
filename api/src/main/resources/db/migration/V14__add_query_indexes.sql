CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX school_name_trgm_idx ON school USING gin (lower(name) gin_trgm_ops);
CREATE INDEX school_address_trgm_idx ON school USING gin (lower(address) gin_trgm_ops);
CREATE INDEX school_description_trgm_idx ON school USING gin (lower(description) gin_trgm_ops);
CREATE INDEX school_activities_trgm_idx ON school USING gin (lower(activities) gin_trgm_ops);

CREATE INDEX training_title_trgm_idx ON training USING gin (lower(title) gin_trgm_ops);
CREATE INDEX training_trainer_name_trgm_idx ON training USING gin (lower(trainer_name) gin_trgm_ops);
CREATE INDEX training_active_start_at_idx ON training (start_at) WHERE deleted_at IS NULL;

CREATE INDEX school_news_title_trgm_idx ON school_news USING gin (lower(title) gin_trgm_ops);
CREATE INDEX school_news_content_trgm_idx ON school_news USING gin (lower(content) gin_trgm_ops);
CREATE INDEX school_news_active_published_at_idx ON school_news (published_at DESC) WHERE active = TRUE;
