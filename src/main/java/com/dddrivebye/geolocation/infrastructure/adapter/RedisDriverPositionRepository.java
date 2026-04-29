package com.dddrivebye.geolocation.infrastructure.adapter;

import com.dddrivebye.geolocation.domain.entity.RealTimePosition;
import com.dddrivebye.geolocation.domain.exception.GeolocationException;
import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis-backed driver position store.
 *
 * Layout:
 *   - GEO set     {@code geo:available-drivers}            (member = driverId, lon/lat)
 *   - String key  {@code geo:driver:{driverId}:position}   (TTL 30s, marker for liveness + capture timestamp)
 */
@Repository
public class RedisDriverPositionRepository implements DriverPositionRepository {

    private static final String GEO_SET_KEY = "geo:available-drivers";
    private static final String POSITION_KEY_PREFIX = "geo:driver:";
    private static final String POSITION_KEY_SUFFIX = ":position";
    private static final Duration POSITION_TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;

    public RedisDriverPositionRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void save(RealTimePosition position) {
        String memberId = position.driverId().value().toString();
        Point point = new Point(position.coordinates().longitude(), position.coordinates().latitude());
        geoOps().add(GEO_SET_KEY, point, memberId);
        redis.opsForValue().set(positionKey(memberId), Long.toString(position.capturedAt().toEpochMilli()), POSITION_TTL);
    }

    @Override
    public Optional<RealTimePosition> findByDriverId(DriverId driverId) {
        String memberId = driverId.value().toString();
        List<Point> points = geoOps().position(GEO_SET_KEY, memberId);
        if (points == null || points.isEmpty() || points.get(0) == null) {
            return Optional.empty();
        }
        Point p = points.get(0);
        Instant capturedAt = readCapturedAt(memberId);
        GeoCoordinates coordinates = GeoCoordinates.of(p.getY(), p.getX());
        return Optional.of(RealTimePosition.reconstitute(driverId, coordinates, capturedAt));
    }

    @Override
    public void remove(DriverId driverId) {
        String memberId = driverId.value().toString();
        redis.opsForZSet().remove(GEO_SET_KEY, memberId);
        redis.delete(positionKey(memberId));
    }

    @Override
    public List<DriverWithDistance> findWithinRadius(GeoCoordinates center, double radiusKm) {
        Circle circle = new Circle(
                new Point(center.longitude(), center.latitude()),
                new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps().radius(GEO_SET_KEY, circle, args);
        if (results == null) {
            return List.of();
        }
        List<DriverWithDistance> out = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : results.getContent()) {
            RedisGeoCommands.GeoLocation<String> loc = r.getContent();
            UUID id;
            try {
                id = UUID.fromString(loc.getName());
            } catch (IllegalArgumentException ex) {
                continue;
            }
            Point p = loc.getPoint();
            out.add(new DriverWithDistance(
                    DriverId.of(id),
                    GeoCoordinates.of(p.getY(), p.getX()),
                    r.getDistance().getValue()));
        }
        return out;
    }

    private GeoOperations<String, String> geoOps() {
        return redis.opsForGeo();
    }

    private String positionKey(String memberId) {
        return POSITION_KEY_PREFIX + memberId + POSITION_KEY_SUFFIX;
    }

    private Instant readCapturedAt(String memberId) {
        String raw = redis.opsForValue().get(positionKey(memberId));
        if (raw == null) {
            return Instant.now();
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(raw));
        } catch (NumberFormatException ex) {
            throw new GeolocationException("Corrupted position timestamp for driver " + memberId, ex);
        }
    }
}
