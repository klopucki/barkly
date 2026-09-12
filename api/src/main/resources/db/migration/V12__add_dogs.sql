CREATE TABLE dog (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL REFERENCES app_user(id),
    name VARCHAR(100) NOT NULL,
    breed VARCHAR(150),
    birth_date DATE,
    sex VARCHAR(20),
    description TEXT NOT NULL DEFAULT '',
    visibility VARCHAR(20) NOT NULL DEFAULT 'MEMBERS'
);

CREATE TABLE dog_image (
    id BIGSERIAL PRIMARY KEY,
    dog_id BIGINT NOT NULL REFERENCES dog(id) ON DELETE CASCADE,
    image_key VARCHAR(100) NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'MEMBERS'
);

CREATE INDEX dog_owner_user_id_idx ON dog(owner_user_id);
CREATE INDEX dog_image_dog_id_idx ON dog_image(dog_id);
