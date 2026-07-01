ALTER TABLE training
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE booking
    ADD COLUMN deleted_at TIMESTAMP NULL;