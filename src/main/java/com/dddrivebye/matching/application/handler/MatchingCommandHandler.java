package com.dddrivebye.matching.application.handler;

import com.dddrivebye.matching.application.command.AcceptProposalCommand;
import com.dddrivebye.matching.application.command.CancelMatchCommand;
import com.dddrivebye.matching.application.command.DeclineProposalCommand;
import com.dddrivebye.matching.application.command.ExpireProposalCommand;
import com.dddrivebye.matching.application.command.RunImmediateMatchingCommand;
import com.dddrivebye.matching.application.command.RunScheduledMatchingCommand;
import com.dddrivebye.matching.application.port.GeolocationPort;
import com.dddrivebye.matching.application.port.ReputationPort;
import com.dddrivebye.matching.domain.entity.Match;
import com.dddrivebye.matching.domain.exception.MatchNotFoundException;
import com.dddrivebye.matching.domain.repository.MatchRepository;
import com.dddrivebye.matching.domain.service.DriverRanking;
import com.dddrivebye.matching.domain.valueobject.DriverCandidate;
import com.dddrivebye.matching.domain.valueobject.MatchStatus;
import com.dddrivebye.matching.domain.valueobject.ProposalWindow;
import com.dddrivebye.matching.domain.valueobject.RideKind;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.api.UserManagementFacade;
import com.dddrivebye.usermanagement.api.dto.AvailableDriverDto;
import com.dddrivebye.usermanagement.api.dto.DriverProfileDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MatchingCommandHandler {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String PROFESSIONAL = "PROFESSIONAL";

    private final MatchRepository matches;
    private final UserManagementFacade userManagement;
    private final GeolocationPort geolocation;
    private final ReputationPort reputation;
    private final DomainEventPublisher events;
    private final Clock clock;

    public MatchingCommandHandler(MatchRepository matches,
                                  UserManagementFacade userManagement,
                                  GeolocationPort geolocation,
                                  ReputationPort reputation,
                                  DomainEventPublisher events,
                                  Clock clock) {
        this.matches = matches;
        this.userManagement = userManagement;
        this.geolocation = geolocation;
        this.reputation = reputation;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public UUID handle(RunImmediateMatchingCommand command) {
        Match match = loadOrCreate(command.rideId(), RideKind.IMMEDIATE);
        runMatchingPass(match,
                GeoCoordinates.of(command.pickupLatitude(), command.pickupLongitude()),
                command.searchRadiusKm(),
                command.requiredOptions(),
                command.passengerHasPet(),
                command.territoryRequiresVtcLicense());
        persist(match);
        return match.id().value();
    }

    @Transactional
    public UUID handle(RunScheduledMatchingCommand command) {
        Match match = loadOrCreate(command.rideId(), RideKind.SCHEDULED);
        runMatchingPass(match,
                GeoCoordinates.of(command.pickupLatitude(), command.pickupLongitude()),
                command.searchRadiusKm(),
                command.requiredOptions(),
                command.passengerHasPet(),
                command.territoryRequiresVtcLicense());
        persist(match);
        return match.id().value();
    }

    @Transactional
    public void handle(AcceptProposalCommand command) {
        Match match = mustLoad(command.rideId());
        match.proposal().ifPresent(p -> {
            if (!p.driverId().equals(command.driverId())) {
                throw new MatchNotFoundException(
                        "Active proposal is for a different driver: " + p.driverId());
            }
        });
        match.accept();
        persist(match);
    }

    @Transactional
    public void handle(DeclineProposalCommand command) {
        Match match = mustLoad(command.rideId());
        match.proposal().ifPresent(p -> {
            if (!p.driverId().equals(command.driverId())) {
                throw new MatchNotFoundException(
                        "Active proposal is for a different driver: " + p.driverId());
            }
        });
        match.declineCurrentProposal();
        persist(match);
    }

    @Transactional
    public void handle(ExpireProposalCommand command) {
        Match match = mustLoad(command.rideId());
        match.expireCurrentProposal(clock.instant());
        persist(match);
    }

    @Transactional
    public void handle(CancelMatchCommand command) {
        Match match = mustLoad(command.rideId());
        match.cancel();
        persist(match);
    }

    private void runMatchingPass(Match match,
                                 GeoCoordinates pickup,
                                 double radiusKm,
                                 Set<RideOption> requiredOptions,
                                 boolean passengerHasPet,
                                 boolean territoryRequiresVtcLicense) {
        if (match.status() != MatchStatus.SEARCHING) {
            return;
        }
        List<DriverCandidate> ranked = findEligibleCandidates(
                match.kind(), pickup, radiusKm, requiredOptions, passengerHasPet,
                territoryRequiresVtcLicense, match.excludedDriverIds());

        Optional<DriverCandidate> head = ranked.stream().findFirst();
        if (head.isEmpty()) {
            match.markUnmatched("No eligible driver found within " + radiusKm + " km");
            return;
        }
        match.proposeTo(head.get().driverId(), clock.instant(), ProposalWindow.DEFAULT_RESPONSE_WINDOW);
    }

    private List<DriverCandidate> findEligibleCandidates(RideKind kind,
                                                         GeoCoordinates pickup,
                                                         double radiusKm,
                                                         Set<RideOption> requiredOptions,
                                                         boolean passengerHasPet,
                                                         boolean territoryRequiresVtcLicense,
                                                         Set<UUID> excluded) {
        String accountFilter = kind.allowsProfessionalDrivers() ? null : INDIVIDUAL;
        List<AvailableDriverDto> nearby = userManagement.getAvailableDriversNear(
                pickup.latitude(), pickup.longitude(), radiusKm, accountFilter);

        List<DriverCandidate> candidates = new ArrayList<>();
        for (AvailableDriverDto driver : nearby) {
            if (excluded.contains(driver.driverId())) {
                continue;
            }
            if (reputation.hasActiveRestriction(driver.driverId())) {
                continue;
            }
            Optional<DriverProfileDto> profileOpt = userManagement.getDriverProfile(driver.driverId());
            if (profileOpt.isEmpty()) {
                continue;
            }
            DriverProfileDto profile = profileOpt.get();
            GeoCoordinates driverPosition = GeoCoordinates.of(driver.latitude(), driver.longitude());
            double distance = geolocation.distanceKm(pickup, driverPosition);

            if (PROFESSIONAL.equals(profile.accountType())) {
                if (!kind.allowsProfessionalDrivers()) {
                    continue;
                }
                if (!isInsideZone(profile.workingZoneLabel(), pickup, distance, radiusKm)) {
                    continue;
                }
            }
            if (territoryRequiresVtcLicense && PROFESSIONAL.equals(profile.accountType())
                    && !driverHoldsVtcLicense(profile)) {
                continue;
            }
            if (!supportsRequiredOptions(profile, requiredOptions, passengerHasPet)) {
                continue;
            }
            boolean withinActivityZone = INDIVIDUAL.equals(profile.accountType())
                    && profile.activityZoneLabel() != null;
            double score = reputation.reputationScore(driver.driverId());
            candidates.add(DriverCandidate.of(
                    driver.driverId(), profile.accountType(), distance, score, withinActivityZone));
        }
        return DriverRanking.rank(candidates);
    }

    private boolean isInsideZone(String workingZoneLabel,
                                 GeoCoordinates pickup,
                                 double distanceKm,
                                 double radiusKm) {
        if (workingZoneLabel == null) {
            return false;
        }
        return distanceKm <= radiusKm;
    }

    private boolean driverHoldsVtcLicense(DriverProfileDto profile) {
        // VTC eligibility is enforced at registration time for Professional drivers
        // (see user-management). Until territorial-rule enrichment is wired to the
        // user-management facade, treat any Professional account as VTC-licensed.
        return PROFESSIONAL.equals(profile.accountType());
    }

    private boolean supportsRequiredOptions(DriverProfileDto profile,
                                            Set<RideOption> requiredOptions,
                                            boolean passengerHasPet) {
        if ((requiredOptions == null || requiredOptions.isEmpty()) && !passengerHasPet) {
            return true;
        }
        if (profile.vehicles().isEmpty()) {
            return false;
        }
        DriverProfileDto.VehicleDto vehicle = profile.vehicles().get(0);
        List<String> normalized = vehicle.options().stream()
                .map(s -> s.toUpperCase(Locale.ROOT))
                .toList();
        if (requiredOptions != null) {
            for (RideOption option : requiredOptions) {
                if (!normalized.contains(option.name())) {
                    return false;
                }
            }
        }
        if (passengerHasPet && !normalized.contains(RideOption.PETS_ALLOWED.name())) {
            return false;
        }
        return true;
    }

    private Match loadOrCreate(UUID rideId, RideKind kind) {
        return matches.findByRideId(rideId).orElseGet(() -> Match.create(rideId, kind));
    }

    private Match mustLoad(UUID rideId) {
        return matches.findByRideId(rideId)
                .orElseThrow(() -> new MatchNotFoundException("No match for ride " + rideId));
    }

    private void persist(Match match) {
        matches.save(match);
        for (BaseDomainEvent event : match.pullDomainEvents()) {
            events.publish(event);
        }
    }
}
