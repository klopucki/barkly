ALTER TABLE app_user ADD COLUMN active boolean NOT NULL DEFAULT true;
ALTER TABLE app_user ADD COLUMN security_version bigint NOT NULL DEFAULT 0;
