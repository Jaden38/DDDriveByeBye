package com.dddrivebye.geolocation.application.handler;

import com.dddrivebye.geolocation.api.dto.DriverPositionDto;
import com.dddrivebye.geolocation.api.dto.EtaDto;
import com.dddrivebye.geolocation.api.dto.NearbyDriverDto;
import com.dddrivebye.geolocation.application.query.GetDriverPositionQuery;
import com.dddrivebye.geolocation.application.query.GetDriversWithinRadiusQuery;
import com.dddrivebye.geolocation.application.query.GetEtaQuery;
import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository;
import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.geolocation.domain.valueobject.Eta;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GeolocationQueryHandler {

    private final DriverPositionRepository driverPositions;
    private final RoutingService routingService;

    public GeolocationQueryHandler(DriverPositionRepository driverPositions,
                                   RoutingService routingService) {
        this.driverPositions = driverPositions;
        this.routingService = routingService;
    }

    public List<NearbyDriverDto> handle(GetDriversWithinRadiusQuery query) {
        GeoCoordinates center = GeoCoordinates.of(query.latitude(), query.longitude());
        return driverPositions.findWithinRadius(center, query.radiusKm()).stream()
                .map(d -> new NearbyDriverDto(
                        d.driverId().value(),
                        d.coordinates().latitude(),
                        d.coordinates().longitude(),
                        d.distanceKm()))
                .toList();
    }

    public EtaDto handle(GetEtaQuery query) {
        GeoCoordinates from = GeoCoordinates.of(query.fromLatitude(), query.fromLongitude());
        GeoCoordinates to = GeoCoordinates.of(query.toLatitude(), query.toLongitude());
        Eta eta = routingService.estimateEta(from, to);
        return new EtaDto(eta.toMinutes());
    }

    public Optional<DriverPositionDto> handle(GetDriverPositionQuery query) {
        return driverPositions.findByDriverId(DriverId.of(query.driverId()))
                .map(p -> new DriverPositionDto(
                        p.driverId().value(),
                        p.coordinates().latitude(),
                        p.coordinates().longitude(),
                        p.capturedAt()));
    }
}
