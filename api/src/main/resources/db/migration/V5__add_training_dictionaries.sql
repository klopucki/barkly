CREATE TABLE training_type
(
    id     BIGSERIAL PRIMARY KEY,
    code   VARCHAR(50)  NOT NULL UNIQUE,
    name   VARCHAR(100) NOT NULL,
    active BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE training_level
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    name             VARCHAR(100) NOT NULL,
    training_type_id BIGINT,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_training_level_type FOREIGN KEY (training_type_id) REFERENCES training_type (id)
);

CREATE TABLE target_group
(
    id     BIGSERIAL PRIMARY KEY,
    code   VARCHAR(50)  NOT NULL UNIQUE,
    name   VARCHAR(100) NOT NULL,
    active BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO training_type (code, name)
VALUES ('GROUP_TRAINING', 'Group training'),
       ('SPORT_TRAINING', 'Sport training'),
       ('IGP', 'IGP'),
       ('BEHAVIORAL_TRAINING', 'Behavioral training'),
       ('CONSULTATION', 'Individual consultation');

INSERT INTO training_level (code, name, training_type_id)
VALUES ('BASIC', 'Basic', NULL),
       ('ADVANCED', 'Advanced', NULL),
       ('IGP1', 'IGP1', (SELECT id FROM training_type WHERE code = 'IGP')),
       ('IGP2', 'IGP2', (SELECT id FROM training_type WHERE code = 'IGP')),
       ('IGP3', 'IGP3', (SELECT id FROM training_type WHERE code = 'IGP'));

INSERT INTO target_group (code, name)
VALUES ('PUPPIES', 'Puppies'),
       ('SMALL_DOGS', 'Small dogs'),
       ('MEDIUM_DOGS', 'Medium dogs'),
       ('LARGE_DOGS', 'Large dogs'),
       ('ADULT_DOGS', 'Adult dogs');

ALTER TABLE training
    ADD COLUMN training_type_id BIGINT,
    ADD COLUMN training_level_id BIGINT,
    ADD COLUMN home_visit BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE training
SET training_type_id = (SELECT id
                        FROM training_type
                        WHERE code = CASE
                                         WHEN training.level = 'SPORT' THEN 'SPORT_TRAINING'
                                         ELSE 'GROUP_TRAINING'
                            END);

UPDATE training
SET training_level_id = (SELECT id FROM training_level WHERE code = training.level)
WHERE training.level IN ('BASIC', 'ADVANCED');

CREATE TABLE training_target_group
(
    training_id    BIGINT NOT NULL,
    target_group_id BIGINT NOT NULL,
    PRIMARY KEY (training_id, target_group_id),
    CONSTRAINT fk_training_target_training FOREIGN KEY (training_id) REFERENCES training (id),
    CONSTRAINT fk_training_target_group FOREIGN KEY (target_group_id) REFERENCES target_group (id)
);

INSERT INTO training_target_group (training_id, target_group_id)
SELECT training.id, target_group.id
FROM training
CROSS JOIN target_group
WHERE training.level = 'PUPPY'
  AND target_group.code = 'PUPPIES';

ALTER TABLE training
    ALTER COLUMN training_type_id SET NOT NULL,
    ADD CONSTRAINT fk_training_type FOREIGN KEY (training_type_id) REFERENCES training_type (id),
    ADD CONSTRAINT fk_training_level FOREIGN KEY (training_level_id) REFERENCES training_level (id),
    DROP COLUMN level;
