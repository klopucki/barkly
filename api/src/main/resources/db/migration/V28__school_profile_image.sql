ALTER TABLE school ADD COLUMN profile_image_id BIGINT REFERENCES school_image(id) ON DELETE SET NULL;
