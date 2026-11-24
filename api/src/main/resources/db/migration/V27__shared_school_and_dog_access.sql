CREATE TABLE school_access_invitation (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES school(id) ON DELETE CASCADE,
    invitee_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    invited_by_user_id BIGINT NOT NULL REFERENCES app_user(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT school_access_invitation_unique UNIQUE (school_id, invitee_user_id)
);

CREATE INDEX school_access_invitation_invitee_idx
    ON school_access_invitation (invitee_user_id, status);

CREATE TABLE dog_access_invitation (
    id BIGSERIAL PRIMARY KEY,
    dog_id BIGINT NOT NULL REFERENCES dog(id) ON DELETE CASCADE,
    invitee_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    invited_by_user_id BIGINT NOT NULL REFERENCES app_user(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT dog_access_invitation_unique UNIQUE (dog_id, invitee_user_id)
);

CREATE INDEX dog_access_invitation_invitee_idx
    ON dog_access_invitation (invitee_user_id, status);
