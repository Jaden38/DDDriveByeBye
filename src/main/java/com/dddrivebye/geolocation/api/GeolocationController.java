package com.dddrivebye.geolocation.api;

import com.dddrivebye.geolocation.api.dto.DriverPositionDto;
import com.dddrivebye.geolocation.api.dto.EtaDto;
import com.dddrivebye.geolocation.api.dto.NearbyDriverDto;
import com.dddrivebye.geolocation.api.dto.RouteDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/geolocation")
public class GeolocationController {

    private final GeolocationFacade facade;

    public GeolocationController(GeolocationFacade facade) {
        this.facade = facade;
    }

    @PutMapping("/drivers/{id}/position")
    public ResponseEntity<Void> updatePosition(@PathVariable UUID id,
                                                @Valid @RequestBody PositionRequest req) {
        facade.updateDriverPosition(id, req.latitude(), req.longitude());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/drivers/{id}/position")
    public ResponseEntity<Void> removePosition(@PathVariable UUID id) {
        facade.removeDriverPosition(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/drivers/{id}/position")
    public ResponseEntity<DriverPositionDto> getPosition(@PathVariable UUID id) {
        return facade.getDriverPosition(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearbyDriverDto>> nearby(@RequestParam double latitude,
                                                         @RequestParam double longitude,
                                                         @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(facade.getDriversWithinRadius(latitude, longitude, radiusKm));
    }

    @PostMapping("/routes")
    public ResponseEntity<RouteDto> calculate(@Valid @RequestBody RouteRequest req) {
        RouteDto dto = facade.calculateRoute(
                req.originLatitude(), req.originLongitude(),
                req.destinationLatitude(), req.destinationLongitude());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/eta")
    public ResponseEntity<EtaDto> eta(@RequestParam double fromLatitude,
                                       @RequestParam double fromLongitude,
                                       @RequestParam double toLatitude,
                                       @RequestParam double toLongitude) {
        return ResponseEntity.ok(facade.getEta(fromLatitude, fromLongitude, toLatitude, toLongitude));
    }

    public record PositionRequest(double latitude, double longitude) {}

    public record RouteRequest(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude,
            @Positive Double maxRadiusKm) {}
}
