package com.dddrivebye.matching.application.handler;

import com.dddrivebye.matching.api.dto.MatchDto;
import com.dddrivebye.matching.api.dto.RideOfferSearchResultDto;
import com.dddrivebye.matching.application.port.RideOfferCatalogPort;
import com.dddrivebye.matching.application.query.GetMatchForRideQuery;
import com.dddrivebye.matching.application.query.SearchRideOffersQuery;
import com.dddrivebye.matching.domain.entity.Match;
import com.dddrivebye.matching.domain.repository.MatchRepository;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MatchingQueryHandler {

    private final MatchRepository matches;
    private final RideOfferCatalogPort rideOffers;

    public MatchingQueryHandler(MatchRepository matches, RideOfferCatalogPort rideOffers) {
        this.matches = matches;
        this.rideOffers = rideOffers;
    }

    @Transactional(readOnly = true)
    public Optional<MatchDto> handle(GetMatchForRideQuery query) {
        return matches.findByRideId(query.rideId()).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<RideOfferSearchResultDto> handle(SearchRideOffersQuery query) {
        GeoCoordinates origin = GeoCoordinates.of(query.originLatitude(), query.originLongitude());
        GeoCoordinates destination = GeoCoordinates.of(
                query.destinationLatitude(), query.destinationLongitude());
        LocalTime preferred = query.preferredTime() == null ? LocalTime.NOON : query.preferredTime();
        Set<RideOption> required = query.requiredOptions() == null ? Set.of() : query.requiredOptions();

        return rideOffers.findOffersMatching(origin, destination, query.date()).stream()
                .filter(offer -> !"FULL".equalsIgnoreCase(offer.status()))
                .filter(offer -> offer.supportedOptions().containsAll(required))
                .map(offer -> {
                    long minutesFromPreferred = Math.abs(ChronoUnit.MINUTES.between(
                            preferred, offer.departureAt().toLocalTime()));
                    return new RideOfferSearchResultDto(
                            offer.rideOfferId(),
                            offer.driverId(),
                            offer.origin().latitude(),
                            offer.origin().longitude(),
                            offer.destination().latitude(),
                            offer.destination().longitude(),
                            offer.departureAt(),
                            offer.seatsAvailable(),
                            minutesFromPreferred);
                })
                .sorted(Comparator.comparingLong(RideOfferSearchResultDto::minutesFromPreferredTime))
                .toList();
    }

    private MatchDto toDto(Match match) {
        return new MatchDto(
                match.id().value(),
                match.rideId(),
                match.kind().name(),
                match.status().name(),
                match.proposal().map(p -> p.driverId()).orElse(null),
                match.proposal().map(p -> p.expiresAt().toString()).orElse(null),
                match.acceptedDriverId().orElse(null),
                match.attemptCount(),
                match.maxAttempts(),
                match.failureReason().orElse(null),
                match.excludedDriverIds().stream().toList());
    }
}
