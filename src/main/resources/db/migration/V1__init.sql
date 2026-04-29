CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================================
-- user-management
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE users.users (
    id                      UUID         PRIMARY KEY,
    full_name               VARCHAR(255) NOT NULL,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    phone_number            VARCHAR(50)  NOT NULL,
    account_type            VARCHAR(20)  NOT NULL,
    account_status          VARCHAR(20)  NOT NULL,

    -- driver profile (null for non-drivers)
    driver_profile_status   VARCHAR(20),
    drivers_license_number  VARCHAR(100),
    drivers_license_expiry  DATE,
    vtc_license_number      VARCHAR(100),
    vtc_license_expiry      DATE,
    insurance_company       VARCHAR(255),
    insurance_policy_number VARCHAR(100),
    insurance_expiry        DATE,
    vehicle_make            VARCHAR(100),
    vehicle_model           VARCHAR(100),
    vehicle_year            INTEGER,
    vehicle_license_plate   VARCHAR(20),

    availability_status     VARCHAR(20),
    rejection_reason        TEXT,

    activity_zone_label     VARCHAR(255),
    activity_zone_lat       DOUBLE PRECISION,
    activity_zone_lon       DOUBLE PRECISION,
    activity_zone_radius_km DOUBLE PRECISION,

    working_zone_label      VARCHAR(255),
    working_zone_lat        DOUBLE PRECISION,
    working_zone_lon        DOUBLE PRECISION,
    working_zone_radius_km  DOUBLE PRECISION
);

CREATE TABLE users.vehicles (
    id            UUID         PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users.users(id) ON DELETE CASCADE,
    make          VARCHAR(100) NOT NULL,
    model         VARCHAR(100) NOT NULL,
    year_built    INTEGER      NOT NULL,
    license_plate VARCHAR(20)  NOT NULL,
    seats         INTEGER      NOT NULL,
    fuel_type     VARCHAR(20)  NOT NULL
);

CREATE TABLE users.vehicle_options (
    vehicle_id  UUID        NOT NULL REFERENCES users.vehicles(id) ON DELETE CASCADE,
    option_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (vehicle_id, option_name)
);

CREATE INDEX idx_users_email        ON users.users(email);
CREATE INDEX idx_users_availability  ON users.users(availability_status, driver_profile_status);
CREATE INDEX idx_vehicles_user       ON users.vehicles(user_id);

-- ============================================================================
-- territorial-configuration
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS territory;

CREATE TABLE territory.territories (
    id                          UUID             PRIMARY KEY,
    name                        VARCHAR(255)     NOT NULL UNIQUE,
    center_lat                  DOUBLE PRECISION NOT NULL,
    center_lon                  DOUBLE PRECISION NOT NULL,
    radius_km                   DOUBLE PRECISION NOT NULL,
    active                      BOOLEAN          NOT NULL DEFAULT TRUE,

    currency                    VARCHAR(3)       NOT NULL,
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

CREATE INDEX idx_territories_active    ON territory.territories(active);
CREATE INDEX idx_constraints_territory ON territory.territory_constraints(territory_id);
