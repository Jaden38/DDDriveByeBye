package com.dddrivebye.matching.application.handler;

import com.dddrivebye.matching.api.dto.MatchDto;
import com.dddrivebye.matching.api.dto.RideOfferSearchResultDto;
import com.dddrivebye.matching.application.port.RideOfferCatalogPort;
import com.dddrivebye.matching.application.port.RideOfferCatalogPort.PublishedRideOffer;
import com.dddrivebye.matching.application.query.GetMatchForRideQuery;
import com.dddrivebye.matching.application.query.SearchRideOffersQuery;
import com.dddrivebye.matching.domain.entity.Match;
import com.dddrivebye.matching.domain.repository.MatchRepository;
import com.dddrivebye.matching.domain.valueobject.RideKind;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingQueryHandlerTest {

    private static final GeoCoordinates LYON = GeoCoordinates.of(45.76, 4.83);
    private static final GeoCoordinates PARIS = GeoCoordinates.of(48.85, 2.35);
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock private MatchRepository matches;
    @Mock private RideOfferCatalogPort catalog;

    private MatchingQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MatchingQueryHandler(matches, catalog);
    }

    @Test
    void shouldReturnMatchAsDtoWhenFound() {
        UUID rideId = UUID.randomUUID();
        Match match = Match.create(rideId, RideKind.IMMEDIATE);
        match.proposeTo(UUID.randomUUID(), Instant.parse("2026-04-29T10:00:00Z"), Duration.ofSeconds(30));
        when(matches.findByRideId(rideId)).thenReturn(Optional.of(match));

        Optional<MatchDto> dto = handler.handle(new GetMatchForRideQuery(rideId));

        assertThat(dto).isPresent();
        assertThat(dto.get().rideId()).isEqualTo(rideId);
        assertThat(dto.get().status()).isEqualTo("PROPOSED");
    }

    @Test
    void shouldReturnEmptyWhenMatchAbsent() {
        UUID rideId = UUID.randomUUID();
        when(matches.findByRideId(rideId)).thenReturn(Optional.empty());

        assertThat(handler.handle(new GetMatchForRideQuery(rideId))).isEmpty();
    }

    @Test
    void shouldExcludeFullAndIncompatibleOffersAndRankByDeparture() {
        UUID jeanOffer = UUID.randomUUID();
        UUID paulOffer = UUID.randomUUID();
        UUID fullOffer = UUID.randomUUID();
        UUID incompatibleOffer = UUID.randomUUID();

        when(catalog.findOffersMatching(any(), any(), any())).thenReturn(List.of(
                offer(jeanOffer, LocalDateTime.of(DATE, LocalTime.of(8, 0)), 3,
                        "PUBLISHED", Set.of(RideOption.WHEELCHAIR_ACCESSIBLE)),
                offer(paulOffer, LocalDateTime.of(DATE, LocalTime.of(9, 30)), 1,
                        "PUBLISHED", Set.of(RideOption.WHEELCHAIR_ACCESSIBLE)),
                offer(fullOffer, LocalDateTime.of(DATE, LocalTime.of(8, 30)), 0,
                        "FULL", Set.of(RideOption.WHEELCHAIR_ACCESSIBLE)),
                offer(incompatibleOffer, LocalDateTime.of(DATE, LocalTime.of(8, 15)), 2,
                        "PUBLISHED", Set.of())));

        SearchRideOffersQuery query = new SearchRideOffersQuery(
                LYON.latitude(), LYON.longitude(),
                PARIS.latitude(), PARIS.longitude(),
                DATE, LocalTime.of(8, 30),
                Set.of(RideOption.WHEELCHAIR_ACCESSIBLE));

        List<RideOfferSearchResultDto> results = handler.handle(query);

        assertThat(results).hasSize(2);
        // 8:00 is 30 min from 8:30, 9:30 is 60 min from 8:30 — so Jean first.
        assertThat(results.get(0).rideOfferId()).isEqualTo(jeanOffer);
        assertThat(results.get(0).minutesFromPreferredTime()).isEqualTo(30);
        assertThat(results.get(1).rideOfferId()).isEqualTo(paulOffer);
        assertThat(results.get(1).minutesFromPreferredTime()).isEqualTo(60);
    }

    @Test
    void shouldFallBackToNoonWhenNoPreferredTimeProvided() {
        UUID offerId = UUID.randomUUID();
        when(catalog.findOffersMatching(any(), any(), any())).thenReturn(List.of(
                offer(offerId, LocalDateTime.of(DATE, LocalTime.NOON), 2, "PUBLISHED", Set.of())));

        SearchRideOffersQuery query = new SearchRideOffersQuery(
                LYON.latitude(), LYON.longitude(),
                PARIS.latitude(), PARIS.longitude(),
                DATE, /*preferredTime*/ null, /*requiredOptions*/ null);

        List<RideOfferSearchResultDto> results = handler.handle(query);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).minutesFromPreferredTime()).isZero();
    }

    @Test
    void shouldReturnEmptyListWhenCatalogReturnsNoOffers() {
        when(catalog.findOffersMatching(any(), any(), any())).thenReturn(List.of());

        SearchRideOffersQuery query = new SearchRideOffersQuery(
                LYON.latitude(), LYON.longitude(),
                PARIS.latitude(), PARIS.longitude(),
                DATE, LocalTime.of(8, 0), Set.of());

        assertThat(handler.handle(query)).isEmpty();
    }

    private static PublishedRideOffer offer(UUID offerId,
                                            LocalDateTime departureAt,
                                            int seats,
                                            String status,
                                            Set<RideOption> supportedOptions) {
        return new PublishedRideOffer(
                offerId, UUID.randomUUID(), LYON, PARIS, departureAt, seats, status, supportedOptions);
    }
}
