-- ============================================================================
-- matching
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS matching;

CREATE TABLE matching.matches (
    id                   UUID         PRIMARY KEY,
    ride_id              UUID         NOT NULL UNIQUE,
    kind                 VARCHAR(20)  NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    max_attempts         INTEGER      NOT NULL,
    attempt_count        INTEGER      NOT NULL DEFAULT 0,
    proposed_driver_id   UUID,
    proposal_expires_at  TIMESTAMP WITH TIME ZONE,
    accepted_driver_id   UUID,
    failure_reason       TEXT
);

CREATE TABLE matching.match_exclusions (
    id        UUID NOT NULL PRIMARY KEY,
    match_id  UUID NOT NULL REFERENCES matching.matches(id) ON DELETE CASCADE,
    driver_id UUID NOT NULL,
    UNIQUE (match_id, driver_id)
);

CREATE INDEX idx_matches_status               ON matching.matches(status);
CREATE INDEX idx_matches_proposal_expiry      ON matching.matches(proposal_expires_at) WHERE proposal_expires_at IS NOT NULL;
CREATE INDEX idx_match_exclusions_match       ON matching.match_exclusions(match_id);

CREATE TABLE matching.groupings (
    id        UUID         PRIMARY KEY,
    driver_id UUID         NOT NULL,
    status    VARCHAR(20)  NOT NULL
);

CREATE TABLE matching.grouping_members (
    id              UUID    NOT NULL PRIMARY KEY,
    grouping_id     UUID    NOT NULL REFERENCES matching.groupings(id) ON DELETE CASCADE,
    ride_request_id UUID    NOT NULL,
    ordinal         INTEGER NOT NULL,
    UNIQUE (grouping_id, ride_request_id)
);

CREATE INDEX idx_groupings_status                ON matching.groupings(status);
CREATE INDEX idx_grouping_members_request        ON matching.grouping_members(ride_request_id);
