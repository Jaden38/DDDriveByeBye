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
import com.dddrivebye.matching.domain.event.MatchFailedEvent;
import com.dddrivebye.matching.domain.event.MatchFoundEvent;
import com.dddrivebye.matching.domain.event.MatchProposalSentEvent;
import com.dddrivebye.matching.domain.exception.MatchNotFoundException;
import com.dddrivebye.matching.domain.repository.MatchRepository;
import com.dddrivebye.matching.domain.valueobject.MatchStatus;
import com.dddrivebye.matching.domain.valueobject.RideKind;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.api.UserManagementFacade;
import com.dddrivebye.usermanagement.api.dto.AvailableDriverDto;
import com.dddrivebye.usermanagement.api.dto.DriverProfileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingCommandHandlerTest {

    private static final Instant NOW = Instant.parse("2026-04-29T10:00:00Z");
    private static final double PICKUP_LAT = 48.8566;
    private static final double PICKUP_LON = 2.3522;
    private static final double SEARCH_RADIUS_KM = 5.0;

    @Mock private MatchRepository matches;
    @Mock private UserManagementFacade userManagement;
    @Mock private GeolocationPort geolocation;
    @Mock private ReputationPort reputation;
    @Mock private DomainEventPublisher events;

    private MatchingCommandHandler handler;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        handler = new MatchingCommandHandler(matches, userManagement, geolocation, reputation, events, clock);
    }

    @Test
    void shouldMarkUnmatchedWhenNoDriverNearby() {
        UUID rideId = UUID.randomUUID();
        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of());

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.UNMATCHED);
        assertThat(saved.failureReason()).get()
                .asString().contains("No eligible driver");

        BaseDomainEvent event = capturePublishedEvent(MatchFailedEvent.class);
        assertThat(event).isInstanceOf(MatchFailedEvent.class);
    }

    @Test
    void shouldProposeToNearestEligibleDriverForImmediate() {
        UUID rideId = UUID.randomUUID();
        UUID jeanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID marcId = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(jeanId, "PROFESSIONAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(aliceId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(marcId, "PROFESSIONAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(jeanId))
                .thenReturn(Optional.of(profile(jeanId, "PROFESSIONAL", "La Defense", null, List.of())));
        when(userManagement.getDriverProfile(aliceId))
                .thenReturn(Optional.of(profile(aliceId, "INDIVIDUAL", null, null, List.of())));
        // Marc is suspended — shouldn't even be looked up further.
        when(reputation.hasActiveRestriction(jeanId)).thenReturn(false);
        when(reputation.hasActiveRestriction(aliceId)).thenReturn(false);
        when(reputation.hasActiveRestriction(marcId)).thenReturn(true);
        when(reputation.reputationScore(jeanId)).thenReturn(4.8);
        when(reputation.reputationScore(aliceId)).thenReturn(4.6);
        when(geolocation.distanceKm(any(), any())).thenAnswer(inv -> {
            // Jean: 1.2km, Alice: 0.9km
            GeoCoordinates from = inv.getArgument(0);
            GeoCoordinates to = inv.getArgument(1);
            // We can't tell drivers apart from coords alone in this test —
            // map by call order using a simple counter via static field.
            return DistanceFixture.next();
        });
        DistanceFixture.reset(1.2, 0.9);

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.PROPOSED);
        assertThat(saved.proposal()).isPresent();
        assertThat(saved.proposal().get().driverId()).isEqualTo(aliceId);
        assertThat(saved.attemptCount()).isEqualTo(1);

        BaseDomainEvent event = capturePublishedEvent(MatchProposalSentEvent.class);
        MatchProposalSentEvent sent = (MatchProposalSentEvent) event;
        assertThat(sent.driverId()).isEqualTo(aliceId);
        assertThat(sent.expiresAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void shouldExcludeProfessionalWithoutWorkingZone() {
        UUID rideId = UUID.randomUUID();
        UUID jeanId = UUID.randomUUID();
        UUID paulId = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(jeanId, "PROFESSIONAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(paulId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(jeanId))
                .thenReturn(Optional.of(profile(jeanId, "PROFESSIONAL", null, null, List.of())));
        when(userManagement.getDriverProfile(paulId))
                .thenReturn(Optional.of(profile(paulId, "INDIVIDUAL", null, null, List.of())));
        when(reputation.reputationScore(any())).thenReturn(4.5);
        when(geolocation.distanceKm(any(), any())).thenReturn(1.0);

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.proposal().get().driverId()).isEqualTo(paulId);
    }

    @Test
    void shouldExcludeDriverMissingRequiredOption() {
        UUID rideId = UUID.randomUUID();
        UUID jeanId = UUID.randomUUID();
        UUID paulId = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(jeanId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(paulId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(jeanId))
                .thenReturn(Optional.of(profile(jeanId, "INDIVIDUAL", null, null, List.of("CHILD_SEAT"))));
        when(userManagement.getDriverProfile(paulId))
                .thenReturn(Optional.of(profile(paulId, "INDIVIDUAL", null, null,
                        List.of("CHILD_SEAT", "WHEELCHAIR_ACCESSIBLE"))));
        when(reputation.reputationScore(any())).thenReturn(4.5);
        when(geolocation.distanceKm(any(), any())).thenAnswer(inv -> DistanceFixture.next());
        DistanceFixture.reset(1.0, 2.0);

        handler.handle(immediateCommand(rideId,
                Set.of(RideOption.CHILD_SEAT, RideOption.WHEELCHAIR_ACCESSIBLE)));

        Match saved = captureSavedMatch();
        assertThat(saved.proposal().get().driverId()).isEqualTo(paulId);
    }

    @Test
    void shouldExcludeDriverWhoDoesNotAcceptPets() {
        UUID rideId = UUID.randomUUID();
        UUID jeanId = UUID.randomUUID();
        UUID paulId = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(jeanId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(paulId, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(jeanId))
                .thenReturn(Optional.of(profile(jeanId, "INDIVIDUAL", null, null, List.of())));
        when(userManagement.getDriverProfile(paulId))
                .thenReturn(Optional.of(profile(paulId, "INDIVIDUAL", null, null,
                        List.of("PETS_ALLOWED"))));
        when(reputation.reputationScore(any())).thenReturn(4.5);
        when(geolocation.distanceKm(any(), any())).thenAnswer(inv -> DistanceFixture.next());
        DistanceFixture.reset(1.0, 2.0);

        handler.handle(new RunImmediateMatchingCommand(
                rideId, PICKUP_LAT, PICKUP_LON, SEARCH_RADIUS_KM, Set.of(),
                /*passengerHasPet*/ true, null, false));

        Match saved = captureSavedMatch();
        assertThat(saved.proposal().get().driverId()).isEqualTo(paulId);
    }

    @Test
    void shouldPrioritizeIndividualWithinActivityZone() {
        UUID rideId = UUID.randomUUID();
        UUID alice = UUID.randomUUID();
        UUID paul = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(alice, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(paul, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(alice))
                .thenReturn(Optional.of(profile(alice, "INDIVIDUAL", null, "15th-16th", List.of())));
        when(userManagement.getDriverProfile(paul))
                .thenReturn(Optional.of(profile(paul, "INDIVIDUAL", null, null, List.of())));
        when(reputation.reputationScore(any())).thenReturn(4.5);
        when(geolocation.distanceKm(any(), any())).thenReturn(2.0);

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.proposal().get().driverId()).isEqualTo(alice);
    }

    @Test
    void shouldFilterScheduledRequestToIndividualDriversOnly() {
        UUID rideId = UUID.randomUUID();
        UUID alice = UUID.randomUUID();

        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq("INDIVIDUAL")))
                .thenReturn(List.of(new AvailableDriverDto(alice, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        when(userManagement.getDriverProfile(alice))
                .thenReturn(Optional.of(profile(alice, "INDIVIDUAL", null, null, List.of())));
        when(reputation.reputationScore(alice)).thenReturn(4.6);
        when(geolocation.distanceKm(any(), any())).thenReturn(1.5);

        handler.handle(new RunScheduledMatchingCommand(
                rideId, PICKUP_LAT, PICKUP_LON, SEARCH_RADIUS_KM, Set.of(), false, null, false));

        Match saved = captureSavedMatch();
        assertThat(saved.kind()).isEqualTo(RideKind.SCHEDULED);
        assertThat(saved.proposal().get().driverId()).isEqualTo(alice);
        verify(userManagement).getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq("INDIVIDUAL"));
    }

    @Test
    void shouldSkipMatchingWhenAlreadyPastSearchingState() {
        UUID rideId = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(UUID.randomUUID(), NOW.minusSeconds(5), java.time.Duration.ofSeconds(30));
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.PROPOSED);
        assertThat(saved.attemptCount()).isEqualTo(1);
    }

    @Test
    void shouldAcceptProposalAndPublishMatchFoundEvent() {
        UUID rideId = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(driver, NOW, java.time.Duration.ofSeconds(30));
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        handler.handle(new AcceptProposalCommand(rideId, driver));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(saved.acceptedDriverId()).contains(driver);
        capturePublishedEvent(MatchFoundEvent.class);
    }

    @Test
    void shouldRejectAcceptForDifferentDriver() {
        UUID rideId = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(driver, NOW, java.time.Duration.ofSeconds(30));
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> handler.handle(new AcceptProposalCommand(rideId, UUID.randomUUID())))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("different driver");
    }

    @Test
    void shouldDeclineProposalAndReturnToSearching() {
        UUID rideId = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(driver, NOW, java.time.Duration.ofSeconds(30));
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        handler.handle(new DeclineProposalCommand(rideId, driver));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.SEARCHING);
        assertThat(saved.excludedDriverIds()).containsExactly(driver);
    }

    @Test
    void shouldExpireProposalUsingClockInstant() {
        UUID rideId = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(driver, NOW.minusSeconds(31), java.time.Duration.ofSeconds(30));
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        handler.handle(new ExpireProposalCommand(rideId));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.SEARCHING);
        assertThat(saved.excludedDriverIds()).containsExactly(driver);
    }

    @Test
    void shouldCancelMatch() {
        UUID rideId = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));

        handler.handle(new CancelMatchCommand(rideId));

        Match saved = captureSavedMatch();
        assertThat(saved.status()).isEqualTo(MatchStatus.CANCELLED);
    }

    @Test
    void shouldFailLoadCommandsWhenNoExistingMatch() {
        UUID rideId = UUID.randomUUID();
        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new CancelMatchCommand(rideId)))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void shouldExcludeAlreadyDeclinedDriverOnRematch() {
        UUID rideId = UUID.randomUUID();
        UUID jean = UUID.randomUUID();
        UUID paul = UUID.randomUUID();
        Match existing = Match.create(rideId, RideKind.IMMEDIATE);
        existing.proposeTo(jean, NOW.minusSeconds(60), java.time.Duration.ofSeconds(30));
        existing.declineCurrentProposal();
        existing.pullDomainEvents();
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(existing));
        when(userManagement.getAvailableDriversNear(anyDouble(), anyDouble(), anyDouble(), eq(null)))
                .thenReturn(List.of(
                        new AvailableDriverDto(jean, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON),
                        new AvailableDriverDto(paul, "INDIVIDUAL", PICKUP_LAT, PICKUP_LON)));
        // Jean's profile doesn't even need to be looked up since he's excluded.
        when(userManagement.getDriverProfile(paul))
                .thenReturn(Optional.of(profile(paul, "INDIVIDUAL", null, null, List.of())));
        lenient().when(reputation.reputationScore(any())).thenReturn(4.5);
        when(geolocation.distanceKm(any(), any())).thenReturn(2.0);

        handler.handle(immediateCommand(rideId, Set.of()));

        Match saved = captureSavedMatch();
        assertThat(saved.proposal().get().driverId()).isEqualTo(paul);
        assertThat(saved.excludedDriverIds()).contains(jean);
    }

    private Match captureSavedMatch() {
        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matches).save(captor.capture());
        return captor.getValue();
    }

    private BaseDomainEvent capturePublishedEvent(Class<? extends BaseDomainEvent> expected) {
        ArgumentCaptor<BaseDomainEvent> captor = ArgumentCaptor.forClass(BaseDomainEvent.class);
        verify(events).publish(captor.capture());
        BaseDomainEvent actual = captor.getValue();
        assertThat(actual).isInstanceOf(expected);
        return actual;
    }

    private RunImmediateMatchingCommand immediateCommand(UUID rideId, Set<RideOption> options) {
        return new RunImmediateMatchingCommand(
                rideId, PICKUP_LAT, PICKUP_LON, SEARCH_RADIUS_KM,
                new HashSet<>(options), false, null, false);
    }

    private static DriverProfileDto profile(UUID id,
                                            String accountType,
                                            String workingZoneLabel,
                                            String activityZoneLabel,
                                            List<String> options) {
        DriverProfileDto.VehicleDto vehicle = new DriverProfileDto.VehicleDto(
                UUID.randomUUID(), "Renault", "Zoe", 2024, "AB-123-CD", 4, "ELECTRIC", options);
        return new DriverProfileDto(
                id, accountType, "VALIDATED", "AVAILABLE",
                workingZoneLabel, activityZoneLabel, List.of(vehicle));
    }

    /** Small helper that hands out a fixed sequence of distances per call. */
    private static final class DistanceFixture {
        private static double[] queue = new double[0];
        private static int idx = 0;

        static void reset(double... ds) {
            queue = ds;
            idx = 0;
        }

        static double next() {
            if (idx >= queue.length) {
                throw new IllegalStateException("DistanceFixture exhausted at index " + idx);
            }
            return queue[idx++];
        }
    }
}
