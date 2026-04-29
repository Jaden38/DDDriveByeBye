package com.dddrivebye.matching.api;

import com.dddrivebye.matching.api.dto.MatchDto;
import com.dddrivebye.matching.api.dto.RideOfferSearchResultDto;
import com.dddrivebye.matching.application.command.RunImmediateMatchingCommand;
import com.dddrivebye.matching.application.command.RunScheduledMatchingCommand;
import com.dddrivebye.matching.application.query.SearchRideOffersQuery;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingFacade facade;

    public MatchingController(MatchingFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/immediate")
    public ResponseEntity<MatchDto> runImmediate(@Valid @RequestBody ImmediateRequest req) {
        UUID matchId = facade.runImmediateMatching(new RunImmediateMatchingCommand(
                req.rideId(),
                req.pickupLatitude(),
                req.pickupLongitude(),
                req.searchRadiusKm(),
                toOptions(req.requiredOptions()),
                req.passengerHasPet(),
                req.territoryId(),
                req.territoryRequiresVtcLicense()));
        return facade.getMatchForRide(req.rideId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(emptyMatch(matchId, req.rideId(), "IMMEDIATE")));
    }

    @PostMapping("/scheduled")
    public ResponseEntity<MatchDto> runScheduled(@Valid @RequestBody ScheduledRequest req) {
        UUID matchId = facade.runScheduledMatching(new RunScheduledMatchingCommand(
                req.rideId(),
                req.pickupLatitude(),
                req.pickupLongitude(),
                req.searchRadiusKm(),
                toOptions(req.requiredOptions()),
                req.passengerHasPet(),
                req.territoryId(),
                req.territoryRequiresVtcLicense()));
        return facade.getMatchForRide(req.rideId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(emptyMatch(matchId, req.rideId(), "SCHEDULED")));
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<Void> accept(@PathVariable UUID rideId, @RequestParam UUID driverId) {
        facade.acceptProposal(rideId, driverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/decline")
    public ResponseEntity<Void> decline(@PathVariable UUID rideId, @RequestParam UUID driverId) {
        facade.declineProposal(rideId, driverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/expire")
    public ResponseEntity<Void> expire(@PathVariable UUID rideId) {
        facade.expireProposal(rideId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID rideId) {
        facade.cancelMatch(rideId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<MatchDto> get(@PathVariable UUID rideId) {
        return facade.getMatchForRide(rideId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ride-offers/search")
    public ResponseEntity<List<RideOfferSearchResultDto>> searchOffers(
            @RequestParam double originLatitude,
            @RequestParam double originLongitude,
            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude,
            @RequestParam LocalDate date,
            @RequestParam(required = false) LocalTime preferredTime,
            @RequestParam(required = false) List<RideOption> requiredOptions) {
        Set<RideOption> required = requiredOptions == null ? Set.of() : EnumSet.copyOf(requiredOptions);
        SearchRideOffersQuery query = new SearchRideOffersQuery(
                originLatitude, originLongitude,
                destinationLatitude, destinationLongitude,
                date, preferredTime, required);
        return ResponseEntity.ok(facade.searchRideOffers(query));
    }

    private static Set<RideOption> toOptions(List<RideOption> options) {
        return options == null || options.isEmpty()
                ? Set.of()
                : EnumSet.copyOf(options);
    }

    private static MatchDto emptyMatch(UUID matchId, UUID rideId, String kind) {
        return new MatchDto(matchId, rideId, kind, "SEARCHING", null, null, null, 0, 5, null, List.of());
    }

    public record ImmediateRequest(
            @NotNull UUID rideId,
            double pickupLatitude,
            double pickupLongitude,
            @Positive double searchRadiusKm,
            List<RideOption> requiredOptions,
            boolean passengerHasPet,
            UUID territoryId,
            boolean territoryRequiresVtcLicense
    ) {
    }

    public record ScheduledRequest(
            @NotNull UUID rideId,
            double pickupLatitude,
            double pickupLongitude,
            @Positive double searchRadiusKm,
            List<RideOption> requiredOptions,
            boolean passengerHasPet,
            UUID territoryId,
            boolean territoryRequiresVtcLicense
    ) {
    }
}
