ALTER TABLE training
    ADD COLUMN target_group_id BIGINT;

UPDATE training
SET target_group_id = selection.target_group_id
FROM (SELECT training_id, MIN(target_group_id) AS target_group_id
      FROM training_target_group
      GROUP BY training_id) selection
WHERE training.id = selection.training_id;

ALTER TABLE training
    ADD CONSTRAINT fk_training_target_group FOREIGN KEY (target_group_id) REFERENCES target_group (id);

DROP TABLE training_target_group;
