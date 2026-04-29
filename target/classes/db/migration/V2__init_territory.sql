CREATE SCHEMA IF NOT EXISTS territory;

CREATE TABLE territory.territories (
    id                          UUID            PRIMARY KEY,
    name                        VARCHAR(255)    NOT NULL UNIQUE,
    center_lat                  DOUBLE PRECISION NOT NULL,
    center_lon                  DOUBLE PRECISION NOT NULL,
    radius_km                   DOUBLE PRECISION NOT NULL,
    active                      BOOLEAN          NOT NULL DEFAULT TRUE,

    currency                    CHAR(3)          NOT NULL,
    per_km_rate                 NUMERIC(10, 4)   NOT NULL,
    per_min_rate                NUMERIC(10, 4)   NOT NULL,
    pickup_fee                  NUMERIC(10, 4)   NOT NULL,
    max_surge_coefficient       NUMERIC(5, 2)    NOT NULL,
    cancellation_fee            NUMERIC(10, 4)   NOT NULL,
    carpooling_grouping_enabled BOOLEAN          NOT NULL DEFAULT FALSE,
    fixed_fare                  NUMERIC(10, 4)
);

CREATE TABLE territory.territory_constraints (
    id           UUID         PRIMARY KEY,
    territory_id UUID         NOT NULL REFERENCES territory.territories(id) ON DELETE CASCADE,
    code         VARCHAR(50)  NOT NULL,
    description  TEXT         NOT NULL
);

CREATE INDEX idx_territories_active      ON territory.territories(active);
CREATE INDEX idx_constraints_territory   ON territory.territory_constraints(territory_id);
