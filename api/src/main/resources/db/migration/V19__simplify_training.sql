ALTER TABLE training
    DROP COLUMN training_type_id,
    DROP COLUMN training_level_id,
    DROP COLUMN target_group_id,
    DROP COLUMN home_visit;
DROP TABLE training_level;
DROP TABLE training_type;
DROP TABLE target_group;
ALTER TABLE training ADD COLUMN event_kind VARCHAR(30) NOT NULL DEFAULT 'TRAINING';
ALTER TABLE training ADD COLUMN price_mode VARCHAR(30) NOT NULL DEFAULT 'TO_BE_AGREED';
ALTER TABLE training ADD COLUMN price NUMERIC(10, 2);
ALTER TABLE training ADD CONSTRAINT training_event_kind CHECK (event_kind = 'TRAINING');
ALTER TABLE training ADD CONSTRAINT training_price CHECK (
    (price_mode = 'PAID' AND price IS NOT NULL AND price > 0)
    OR (price_mode IN ('FREE', 'TO_BE_AGREED') AND price IS NULL)
);
