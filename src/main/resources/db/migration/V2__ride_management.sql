-- ============================================================================
-- ride-management
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS ride;

CREATE TABLE ride.rides (
    id               UUID             PRIMARY KEY,
    passenger_id     UUID             NOT NULL,
    driver_id        UUID,
    state            VARCHAR(50)      NOT NULL,
    pickup_lat       DOUBLE PRECISION NOT NULL,
    pickup_lon       DOUBLE PRECISION NOT NULL,
    dest_lat         DOUBLE PRECISION NOT NULL,
    dest_lon         DOUBLE PRECISION NOT NULL,
    price_amount     NUMERIC(19, 4),
    price_currency   VARCHAR(3),
    requested_seats  INTEGER          NOT NULL,
    created_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ride.ride_requests (
    id               UUID             PRIMARY KEY,
    passenger_id     UUID             NOT NULL,
    pickup_lat       DOUBLE PRECISION NOT NULL,
    pickup_lon       DOUBLE PRECISION NOT NULL,
    dest_lat         DOUBLE PRECISION NOT NULL,
    dest_lon         DOUBLE PRECISION NOT NULL,
    requested_time   TIMESTAMP        NOT NULL,
    requested_seats  INTEGER          NOT NULL,
    fulfilled        BOOLEAN          NOT NULL DEFAULT FALSE
);

CREATE TABLE ride.ride_offers (
    id               UUID             PRIMARY KEY,
    driver_id        UUID             NOT NULL,
    pickup_lat       DOUBLE PRECISION NOT NULL,
    pickup_lon       DOUBLE PRECISION NOT NULL,
    dest_lat         DOUBLE PRECISION NOT NULL,
    dest_lon         DOUBLE PRECISION NOT NULL,
    departure_time   TIMESTAMP        NOT NULL,
    total_seats      INTEGER          NOT NULL,
    available_seats  INTEGER          NOT NULL,
    price_amount     NUMERIC(19, 4),
    price_currency   VARCHAR(3),
    active           BOOLEAN          NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_rides_passenger ON ride.rides(passenger_id);
CREATE INDEX idx_rides_driver    ON ride.rides(driver_id);
CREATE INDEX idx_rides_state     ON ride.rides(state);
CREATE INDEX idx_ride_requests_fulfilled ON ride.ride_requests(fulfilled);
CREATE INDEX idx_ride_offers_active      ON ride.ride_offers(active);
