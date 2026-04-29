-- ============================================================================
-- geolocation
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS geo;

CREATE TABLE geo.routes (
    id                UUID             PRIMARY KEY,
    origin_lat        DOUBLE PRECISION NOT NULL,
    origin_lon        DOUBLE PRECISION NOT NULL,
    destination_lat   DOUBLE PRECISION NOT NULL,
    destination_lon   DOUBLE PRECISION NOT NULL,
    distance_km       DOUBLE PRECISION NOT NULL,
    duration_seconds  BIGINT           NOT NULL,
    calculated_at     TIMESTAMPTZ      NOT NULL
);

-- PostGIS-backed spatial index over the origin point. The dedicated lat/lon
-- columns remain authoritative; this index supports future bbox / proximity
-- queries against persisted routes.
CREATE INDEX idx_routes_origin_geog
    ON geo.routes
    USING GIST (geography(ST_SetSRID(ST_MakePoint(origin_lon, origin_lat), 4326)));
