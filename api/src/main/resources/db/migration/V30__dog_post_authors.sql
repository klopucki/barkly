ALTER TABLE dog_post ADD COLUMN author_user_id BIGINT REFERENCES app_user(id);

-- Older posts did not record an author; their dog owner is the best available attribution.
UPDATE dog_post SET author_user_id = dog.owner_user_id FROM dog WHERE dog.id = dog_post.dog_id;
ALTER TABLE dog_post ALTER COLUMN author_user_id SET NOT NULL;
