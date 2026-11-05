ALTER TABLE training ADD COLUMN location_address VARCHAR(500), ADD COLUMN latitude DOUBLE PRECISION, ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE training ADD CONSTRAINT training_coordinates_valid CHECK (
    (latitude IS NULL AND longitude IS NULL) OR
    (latitude IS NOT NULL AND longitude IS NOT NULL AND latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
);
