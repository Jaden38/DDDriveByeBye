package com.dddrivebye.ridemanagement.api;

import com.dddrivebye.ridemanagement.api.dto.RideDto;
import com.dddrivebye.ridemanagement.application.command.RequestRideCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideManagementFacade facade;

    @PostMapping
    public ResponseEntity<Void> requestRide(@Valid @RequestBody RideRequestPayload payload) {
        UUID id = facade.requestRide(new RequestRideCommand(
                payload.passengerId(),
                payload.pickupLat(),
                payload.pickupLon(),
                payload.destLat(),
                payload.destLon(),
                payload.requestedSeats()
        ));
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/{id}/propose")
    public ResponseEntity<Void> proposeDriver(@PathVariable UUID id, @RequestBody UUID driverId) {
        facade.proposeDriver(id, driverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptRide(@PathVariable UUID id) {
        facade.acceptRide(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<Void> pickUpPassenger(@PathVariable UUID id) {
        facade.pickUpPassenger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable UUID id) {
        facade.startRide(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/arrive")
    public ResponseEntity<Void> arriveAtDestination(@PathVariable UUID id) {
        facade.arriveAtDestination(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<Void> finalizeRide(@PathVariable UUID id) {
        facade.finalizeRide(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideDto> getRide(@PathVariable UUID id) {
        return facade.getRideById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record RideRequestPayload(
            @NotNull UUID passengerId,
            @NotNull Double pickupLat,
            @NotNull Double pickupLon,
            @NotNull Double destLat,
            @NotNull Double destLon,
            @Min(1) int requestedSeats
    ) {}
}
